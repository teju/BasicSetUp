package org.buffer.android.buffertextinputlayout.util;

import static androidx.appcompat.content.res.AppCompatResources.getColorStateList;
import static androidx.core.graphics.ColorUtils.compositeColors;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.ConstantState;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.util.Xml;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;
import androidx.appcompat.R;
import androidx.appcompat.graphics.drawable.AnimatedStateListDrawableCompat;
import androidx.appcompat.widget.VectorEnabledTintResources;
import androidx.collection.ArrayMap;
import androidx.collection.LongSparseArray;
import androidx.collection.LruCache;
import androidx.collection.SparseArrayCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/**
 * @hide
 */
public final class AppCompatDrawableManager {

    private interface InflateDelegate {
        Drawable createFromXmlInner(@NonNull Context context, @NonNull XmlPullParser parser,
                @NonNull AttributeSet attrs, @Nullable Resources.Theme theme);
    }

    private static final String TAG = "AppCompatDrawableManag";
    private static final boolean DEBUG = false;
    private static final PorterDuff.Mode DEFAULT_MODE = PorterDuff.Mode.SRC_IN;
    private static final String SKIP_DRAWABLE_TAG = "appcompat_skip_skip";

    private static final String PLATFORM_VD_CLAZZ = "android.graphics.drawable.VectorDrawable";

    private static AppCompatDrawableManager INSTANCE;

    /**
     * Returns the singleton instance of this class.
     */
    public static synchronized AppCompatDrawableManager get() {
        if (INSTANCE == null) {
            INSTANCE = new AppCompatDrawableManager();
            installDefaultInflateDelegates(INSTANCE);
        }
        return INSTANCE;
    }

    private static void installDefaultInflateDelegates(@NonNull AppCompatDrawableManager manager) {
        // This sdk version check will affect src:appCompat code path.
        // Although VectorDrawable exists in Android framework from Lollipop, AppCompat will use
        // (Animated)VectorDrawableCompat before Nougat to utilize bug fixes & feature backports.
        if (Build.VERSION.SDK_INT < 24) {
            manager.addDelegate("vector", new VdcInflateDelegate());
            manager.addDelegate("animated-vector", new AvdcInflateDelegate());
            manager.addDelegate("animated-selector", new AsldcInflateDelegate());
        }
    }

    private static final ColorFilterLruCache COLOR_FILTER_CACHE = new ColorFilterLruCache(6);

    /**
     * Drawables which should be tinted with the value of {@code R.attr.colorControlNormal},
     * using the default mode using a raw color filter.
     */
    private static final int[] COLORFILTER_TINT_COLOR_CONTROL_NORMAL = {
            R.drawable.abc_textfield_search_default_mtrl_alpha,
            R.drawable.abc_textfield_default_mtrl_alpha,
            R.drawable.abc_ab_share_pack_mtrl_alpha
    };

    /**
     * Drawables which should be tinted with the value of {@code R.attr.colorControlNormal}, using
     * {@link DrawableCompat}'s tinting functionality.
     */
    private static final int[] TINT_COLOR_CONTROL_NORMAL = {
            R.drawable.abc_ic_commit_search_api_mtrl_alpha,
            R.drawable.abc_seekbar_tick_mark_material,
            R.drawable.abc_ic_menu_share_mtrl_alpha,
            R.drawable.abc_ic_menu_copy_mtrl_am_alpha,
            R.drawable.abc_ic_menu_cut_mtrl_alpha,
            R.drawable.abc_ic_menu_selectall_mtrl_alpha,
            R.drawable.abc_ic_menu_paste_mtrl_am_alpha
    };

    /**
     * Drawables which should be tinted with the value of {@code R.attr.colorControlActivated},
     * using a color filter.
     */
    private static final int[] COLORFILTER_COLOR_CONTROL_ACTIVATED = {
            R.drawable.abc_textfield_activated_mtrl_alpha,
            R.drawable.abc_textfield_search_activated_mtrl_alpha,
            R.drawable.abc_cab_background_top_mtrl_alpha,
            R.drawable.abc_text_cursor_material,
            R.drawable.abc_text_select_handle_left_mtrl_dark,
            R.drawable.abc_text_select_handle_middle_mtrl_dark,
            R.drawable.abc_text_select_handle_right_mtrl_dark,
            R.drawable.abc_text_select_handle_left_mtrl_light,
            R.drawable.abc_text_select_handle_middle_mtrl_light,
            R.drawable.abc_text_select_handle_right_mtrl_light
    };

    /**
     * Drawables which should be tinted with the value of {@code android.R.attr.colorBackground},
     * using the {@link android.graphics.PorterDuff.Mode#MULTIPLY} mode and a color filter.
     */
    private static final int[] COLORFILTER_COLOR_BACKGROUND_MULTIPLY = {
            R.drawable.abc_popup_background_mtrl_mult,
            R.drawable.abc_cab_background_internal_bg,
            R.drawable.abc_menu_hardkey_panel_mtrl_mult
    };

    /**
     * Drawables which should be tinted using a state list containing values of
     * {@code R.attr.colorControlNormal} and {@code R.attr.colorControlActivated}
     */
    private static final int[] TINT_COLOR_CONTROL_STATE_LIST = {
            R.drawable.abc_tab_indicator_material,
            R.drawable.abc_textfield_search_material
    };

    /**
     * Drawables which should be tinted using a state list containing values of
     * {@code R.attr.colorControlNormal} and {@code R.attr.colorControlActivated} for the checked
     * state.
     */
    private static final int[] TINT_CHECKABLE_BUTTON_LIST = {
            R.drawable.abc_btn_check_material,
            R.drawable.abc_btn_radio_material
    };

    private WeakHashMap<Context, SparseArrayCompat<ColorStateList>> mTintLists;
    private ArrayMap<String, InflateDelegate> mDelegates;
    private SparseArrayCompat<String> mKnownDrawableIdTags;

    private final WeakHashMap<Context, LongSparseArray<WeakReference<Drawable.ConstantState>>>
            mDrawableCaches = new WeakHashMap<>(0);

    private TypedValue mTypedValue;

    private boolean mHasCheckedVectorDrawableSetup;

    public synchronized Drawable getDrawable(@NonNull Context context, @DrawableRes int resId) {
        return getDrawable(context, resId, false);
    }

    synchronized Drawable getDrawable(@NonNull Context context, @DrawableRes int resId,
            boolean failIfNotKnown) {
        checkVectorDrawableSetup(context);

        Drawable drawable = loadDrawableFromDelegates(context, resId);
        if (drawable == null) {
            drawable = createDrawableIfNeeded(context, resId);
        }
        if (drawable == null) {
            drawable = ContextCompat.getDrawable(context, resId);
        }

        if (drawable != null) {
            // Tint it if needed
            drawable = tintDrawable(context, resId, failIfNotKnown, drawable);
        }

        return drawable;
    }

    public synchronized void onConfigurationChanged(@NonNull Context context) {
        LongSparseArray<WeakReference<ConstantState>> cache = mDrawableCaches.get(context);
        if (cache != null) {
            // Crude, but we'll just clear the cache when the configuration changes
            cache.clear();
        }
    }

    private static long createCacheKey(TypedValue tv) {
        return (((long) tv.assetCookie) << 32) | tv.data;
    }

    private Drawable createDrawableIfNeeded(@NonNull Context context,
            @DrawableRes final int resId) {
        if (mTypedValue == null) {
            mTypedValue = new TypedValue();
        }
        final TypedValue tv = mTypedValue;
        context.getResources().getValue(resId, tv, true);
        final long key = createCacheKey(tv);

        Drawable dr = getCachedDrawable(context, key);
        if (dr != null) {
            // If we got a cached drawable, return it
            return dr;
        }

        // Else we need to try and create one...
        if (resId == R.drawable.abc_cab_background_top_material) {
            dr = new LayerDrawable(new Drawable[]{
                    getDrawable(context, R.drawable.abc_cab_background_internal_bg),
                    getDrawable(context, R.drawable.abc_cab_background_top_mtrl_alpha)
            });
        }

        if (dr != null) {
            dr.setChangingConfigurations(tv.changingConfigurations);
            // If we reached here then we created a new drawable, add it to the cache
            addDrawableToCache(context, key, dr);
        }

        return dr;
    }

    private Drawable tintDrawable(@NonNull Context context, @DrawableRes int resId,
            boolean failIfNotKnown, @NonNull Drawable drawable) {
        final ColorStateList tintList = getTintList(context, resId);
        if (tintList != null) {
            // First mutate the Drawable, then wrap it and set the tint list
            if (DrawableUtils.canSafelyMutateDrawable(drawable)) {
                drawable = drawable.mutate();
            }
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTintList(drawable, tintList);

            // If there is a blending mode specified for the drawable, use it
            final PorterDuff.Mode tintMode = getTintMode(resId);
            if (tintMode != null) {
                DrawableCompat.setTintMode(drawable, tintMode);
            }
        } else {
            final boolean tinted = tintDrawableUsingColorFilter(context, resId, drawable);
            if (!tinted && failIfNotKnown) {
                // If we didn't tint using a ColorFilter, and we're set to fail if we don't
                // know the id, return null
                drawable = null;
            }
        }
        return drawable;
    }

    private Drawable loadDrawableFromDelegates(@NonNull Context context, @DrawableRes int resId) {
        if (mDelegates != null && !mDelegates.isEmpty()) {
            if (mKnownDrawableIdTags != null) {
                final String cachedTagName = mKnownDrawableIdTags.get(resId);
                if (SKIP_DRAWABLE_TAG.equals(cachedTagName)
                        || (cachedTagName != null && mDelegates.get(cachedTagName) == null)) {
                    // If we don't have a delegate for the drawable tag, or we've been set to
                    // skip it, fail fast and return null
                    if (DEBUG) {
                        Log.d(TAG, "[loadDrawableFromDelegates] Skipping drawable: "
                                + context.getResources().getResourceName(resId));
                    }
                    return null;
                }
            } else {
                // Create an id cache as we'll need one later
                mKnownDrawableIdTags = new SparseArrayCompat<>();
            }

            if (mTypedValue == null) {
                mTypedValue = new TypedValue();
            }
            final TypedValue tv = mTypedValue;
            final Resources res = context.getResources();
            res.getValue(resId, tv, true);

            final long key = createCacheKey(tv);

            Drawable dr = getCachedDrawable(context, key);
            if (dr != null) {
                if (DEBUG) {
                    Log.i(TAG, "[loadDrawableFromDelegates] Returning cached drawable: " +
                            context.getResources().getResourceName(resId));
                }
                // We have a cached drawable, return it!
                return dr;
            }

            if (tv.string != null && tv.string.toString().endsWith(".xml")) {
                // If the resource is an XML file, let's try and parse it
                try {
                    @SuppressLint("ResourceType") final XmlPullParser parser = res.getXml(resId);
                    final AttributeSet attrs = Xml.asAttributeSet(parser);
                    int type;
                    while ((type = parser.next()) != XmlPullParser.START_TAG &&
                            type != XmlPullParser.END_DOCUMENT) {
                        // Empty loop
                    }
                    if (type != XmlPullParser.START_TAG) {
                        throw new XmlPullParserException("No start tag found");
                    }

                    final String tagName = parser.getName();
                    // Add the tag name to the cache
                    mKnownDrawableIdTags.append(resId, tagName);

                    // Now try and find a delegate for the tag name and inflate if found
                    final InflateDelegate delegate = mDelegates.get(tagName);
                    if (delegate != null) {
                        dr = delegate.createFromXmlInner(context, parser, attrs,
                                context.getTheme());
                    }
                    if (dr != null) {
                        // Add it to the drawable cache
                        dr.setChangingConfigurations(tv.changingConfigurations);
                        if (addDrawableToCache(context, key, dr) && DEBUG) {
                            Log.i(TAG, "[loadDrawableFromDelegates] Saved drawable to cache: " +
                                    context.getResources().getResourceName(resId));
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Exception while inflating drawable", e);
                }
            }
            if (dr == null) {
                // If we reach here then the delegate inflation of the resource failed. Mark it as
                // bad so we skip the id next time
                mKnownDrawableIdTags.append(resId, SKIP_DRAWABLE_TAG);
            }
            return dr;
        }

        return null;
    }

    private synchronized Drawable getCachedDrawable(@NonNull final Context context,
            final long key) {
        final LongSparseArray<WeakReference<ConstantState>> cache = mDrawableCaches.get(context);
        if (cache == null) {
            return null;
        }

        final WeakReference<ConstantState> wr = cache.get(key);
        if (wr != null) {
            // We have the key, and the secret
            ConstantState entry = wr.get();
            if (entry != null) {
                return entry.newDrawable(context.getResources());
            } else {
                // Our entry has been purged
                cache.delete(key);
            }
        }
        return null;
    }

    private synchronized boolean addDrawableToCache(@NonNull final Context context, final long key,
            @NonNull final Drawable drawable) {
        final ConstantState cs = drawable.getConstantState();
        if (cs != null) {
            LongSparseArray<WeakReference<ConstantState>> cache = mDrawableCaches.get(context);
            if (cache == null) {
                cache = new LongSparseArray<>();
                mDrawableCaches.put(context, cache);
            }
            cache.put(key, new WeakReference<>(cs));
            return true;
        }
        return false;
    }

    synchronized Drawable onDrawableLoadedFromResources(@NonNull Context context,
                                                        @NonNull VectorEnabledTintResources resources, @DrawableRes final int resId) {
        Drawable drawable = loadDrawableFromDelegates(context, resId);

        if (drawable != null) {
            return tintDrawable(context, resId, false, drawable);
        }
        return null;
    }

    static boolean tintDrawableUsingColorFilter(@NonNull Context context,
            @DrawableRes final int resId, @NonNull Drawable drawable) {
        PorterDuff.Mode tintMode = DEFAULT_MODE;
        boolean colorAttrSet = false;
        int colorAttr = 0;
        int alpha = -1;

        if (arrayContains(COLORFILTER_TINT_COLOR_CONTROL_NORMAL, resId)) {
            colorAttr = R.attr.colorControlNormal;
            colorAttrSet = true;
        } else if (arrayContains(COLORFILTER_COLOR_CONTROL_ACTIVATED, resId)) {
            colorAttr = R.attr.colorControlActivated;
            colorAttrSet = true;
        } else if (arrayContains(COLORFILTER_COLOR_BACKGROUND_MULTIPLY, resId)) {
            colorAttr = android.R.attr.colorBackground;
            colorAttrSet = true;
            tintMode = PorterDuff.Mode.MULTIPLY;
        } else if (resId == R.drawable.abc_list_divider_mtrl_alpha) {
            colorAttr = android.R.attr.colorForeground;
            colorAttrSet = true;
            alpha = Math.round(0.16f * 255);
        } else if (resId == R.drawable.abc_dialog_material_background) {
            colorAttr = android.R.attr.colorBackground;
            colorAttrSet = true;
        }

        if (colorAttrSet) {
            if (DrawableUtils.canSafelyMutateDrawable(drawable)) {
                drawable = drawable.mutate();
            }


            if (alpha != -1) {
                drawable.setAlpha(alpha);
            }


            return true;
        }
        return false;
    }

    private void addDelegate(@NonNull String tagName, @NonNull InflateDelegate delegate) {
        if (mDelegates == null) {
            mDelegates = new ArrayMap<>();
        }
        mDelegates.put(tagName, delegate);
    }

    private void removeDelegate(@NonNull String tagName, @NonNull InflateDelegate delegate) {
        if (mDelegates != null && mDelegates.get(tagName) == delegate) {
            mDelegates.remove(tagName);
        }
    }

    private static boolean arrayContains(int[] array, int value) {
        for (int id : array) {
            if (id == value) {
                return true;
            }
        }
        return false;
    }

    static PorterDuff.Mode getTintMode(final int resId) {
        PorterDuff.Mode mode = null;

        if (resId == R.drawable.abc_switch_thumb_material) {
            mode = PorterDuff.Mode.MULTIPLY;
        }

        return mode;
    }

    synchronized ColorStateList getTintList(@NonNull Context context, @DrawableRes int resId) {
        // Try the cache first (if it exists)
        ColorStateList tint = getTintListFromCache(context, resId);

        if (tint == null) {
            // ...if the cache did not contain a color state list, try and create one
            if (resId == R.drawable.abc_edit_text_material) {
                tint = getColorStateList(context, R.color.abc_tint_edittext);
            } else if (resId == R.drawable.abc_switch_track_mtrl_alpha) {
                tint = getColorStateList(context, R.color.abc_tint_switch_track);
            } else if (resId == R.drawable.abc_btn_borderless_material) {
                tint = createBorderlessButtonColorStateList(context);
            } else if (resId == R.drawable.abc_spinner_mtrl_am_alpha
                    || resId == R.drawable.abc_spinner_textfield_background_material) {
                tint = getColorStateList(context, R.color.abc_tint_spinner);
            } else if (arrayContains(TINT_COLOR_CONTROL_STATE_LIST, resId)) {
                tint = getColorStateList(context, R.color.abc_tint_default);
            } else if (arrayContains(TINT_CHECKABLE_BUTTON_LIST, resId)) {
                tint = getColorStateList(context, R.color.abc_tint_btn_checkable);
            } else if (resId == R.drawable.abc_seekbar_thumb_material) {
                tint = getColorStateList(context, R.color.abc_tint_seek_thumb);
            }

            if (tint != null) {
                addTintListToCache(context, resId, tint);
            }
        }
        return tint;
    }

    private ColorStateList getTintListFromCache(@NonNull Context context, @DrawableRes int resId) {
        if (mTintLists != null) {
            final SparseArrayCompat<ColorStateList> tints = mTintLists.get(context);
            return tints != null ? tints.get(resId) : null;
        }
        return null;
    }

    private void addTintListToCache(@NonNull Context context, @DrawableRes int resId,
            @NonNull ColorStateList tintList) {
        if (mTintLists == null) {
            mTintLists = new WeakHashMap<>();
        }
        SparseArrayCompat<ColorStateList> themeTints = mTintLists.get(context);
        if (themeTints == null) {
            themeTints = new SparseArrayCompat<>();
            mTintLists.put(context, themeTints);
        }
        themeTints.append(resId, tintList);
    }


    private ColorStateList createBorderlessButtonColorStateList(@NonNull Context context) {
        // We ignore the custom tint for borderless buttons
        return createButtonColorStateList(context, Color.TRANSPARENT);
    }



    private ColorStateList createButtonColorStateList(@NonNull final Context context,
            @ColorInt final int baseColor) {
        final int[][] states = new int[4][];
        final int[] colors = new int[4];
        int i = 0;

        // Default enabled state
        states[i] = ThemeUtils.EMPTY_STATE_SET;
        colors[i] = baseColor;
        i++;

        return new ColorStateList(states, colors);
    }



    private static class ColorFilterLruCache extends LruCache<Integer, PorterDuffColorFilter> {

        public ColorFilterLruCache(int maxSize) {
            super(maxSize);
        }

        PorterDuffColorFilter get(int color, PorterDuff.Mode mode) {
            return get(generateCacheKey(color, mode));
        }

        PorterDuffColorFilter put(int color, PorterDuff.Mode mode, PorterDuffColorFilter filter) {
            return put(generateCacheKey(color, mode), filter);
        }

        private static int generateCacheKey(int color, PorterDuff.Mode mode) {
            int hashCode = 1;
            hashCode = 31 * hashCode + color;
            hashCode = 31 * hashCode + mode.hashCode();
            return hashCode;
        }
    }



    private static PorterDuffColorFilter createTintFilter(ColorStateList tint,
            PorterDuff.Mode tintMode, final int[] state) {
        if (tint == null || tintMode == null) {
            return null;
        }
        final int color = tint.getColorForState(state, Color.TRANSPARENT);
        return getPorterDuffColorFilter(color, tintMode);
    }

    public static synchronized PorterDuffColorFilter getPorterDuffColorFilter(
            int color, PorterDuff.Mode mode) {
        // First, let's see if the cache already contains the color filter
        PorterDuffColorFilter filter = COLOR_FILTER_CACHE.get(color, mode);

        if (filter == null) {
            // Cache miss, so create a color filter and add it to the cache
            filter = new PorterDuffColorFilter(color, mode);
            COLOR_FILTER_CACHE.put(color, mode, filter);
        }

        return filter;
    }

    private static void setPorterDuffColorFilter(Drawable d, int color, PorterDuff.Mode mode) {
        if (DrawableUtils.canSafelyMutateDrawable(d)) {
            d = d.mutate();
        }
        d.setColorFilter(getPorterDuffColorFilter(color, mode == null ? DEFAULT_MODE : mode));
    }

    private void checkVectorDrawableSetup(@NonNull Context context) {
        if (mHasCheckedVectorDrawableSetup) {
            // We've already checked so return now...
            return;
        }
        // Here we will check that a known Vector drawable resource inside AppCompat can be
        // correctly decoded
        mHasCheckedVectorDrawableSetup = true;
        final Drawable d = getDrawable(context, R.drawable.abc_vector_test);
        if (d == null || !isVectorDrawable(d)) {
            mHasCheckedVectorDrawableSetup = false;
            throw new IllegalStateException("This app has been built with an incorrect "
                    + "configuration. Please configure your build for VectorDrawableCompat.");
        }
    }

    private static boolean isVectorDrawable(@NonNull Drawable d) {
        return d instanceof VectorDrawableCompat
                || PLATFORM_VD_CLAZZ.equals(d.getClass().getName());
    }

    private static class VdcInflateDelegate implements InflateDelegate {
        VdcInflateDelegate() {
        }

        @Override
        public Drawable createFromXmlInner(@NonNull Context context, @NonNull XmlPullParser parser,
                @NonNull AttributeSet attrs, @Nullable Resources.Theme theme) {
            try {
                return VectorDrawableCompat
                        .createFromXmlInner(context.getResources(), parser, attrs, theme);
            } catch (Exception e) {
                Log.e("VdcInflateDelegate", "Exception while inflating <vector>", e);
                return null;
            }
        }
    }

    private static class AvdcInflateDelegate implements InflateDelegate {
        AvdcInflateDelegate() {
        }

        @Override
        public Drawable createFromXmlInner(@NonNull Context context, @NonNull XmlPullParser parser,
                @NonNull AttributeSet attrs, @Nullable Resources.Theme theme) {
            try {
                return AnimatedVectorDrawableCompat
                        .createFromXmlInner(context, context.getResources(), parser, attrs, theme);
            } catch (Exception e) {
                Log.e("AvdcInflateDelegate", "Exception while inflating <animated-vector>", e);
                return null;
            }
        }
    }

    @RequiresApi(11)
    static class AsldcInflateDelegate implements InflateDelegate {
        @Override
        public Drawable createFromXmlInner(@NonNull Context context, @NonNull XmlPullParser parser,
                @NonNull AttributeSet attrs, @Nullable Resources.Theme theme) {
            try {
                return AnimatedStateListDrawableCompat
                        .createFromXmlInner(context, context.getResources(), parser, attrs, theme);
            } catch (Exception e) {
                Log.e("AsldcInflateDelegate", "Exception while inflating <animated-selector>", e);
                return null;
            }
        }
    }
}