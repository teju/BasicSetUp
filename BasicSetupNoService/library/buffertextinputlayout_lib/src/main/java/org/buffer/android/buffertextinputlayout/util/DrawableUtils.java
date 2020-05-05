/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.buffer.android.buffertextinputlayout.util;

import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.*;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DrawableUtils {

    private static final String LOG_TAG = DrawableUtils.class.getSimpleName();
    private static Method setConstantStateMethod;
    private static boolean setConstantStateMethodFetched;

    private DrawableUtils() {
    }

    public static boolean setContainerConstantState(DrawableContainer drawable,
                                             Drawable.ConstantState constantState) {
        // We can use getDeclaredMethod() on v9+
        return setContainerConstantStateV9(drawable, constantState);
    }

    private static boolean setContainerConstantStateV9(DrawableContainer drawable,
                                                       Drawable.ConstantState constantState) {
        if (!setConstantStateMethodFetched) {
            try {
                setConstantStateMethod = DrawableContainer.class.getDeclaredMethod(
                        "setConstantState", DrawableContainer.DrawableContainerState.class);
                setConstantStateMethod.setAccessible(true);
            } catch (NoSuchMethodException e) {
                Log.e(LOG_TAG, "Could not fetch setConstantState()");
            }
            setConstantStateMethodFetched = true;
        }
        if (setConstantStateMethod != null) {
            try {
                setConstantStateMethod.invoke(drawable, constantState);
                return true;
            } catch (InvocationTargetException e) {
                Log.e(LOG_TAG, "Could not invoke setConstantState()");
            } catch (IllegalAccessException e) {
                Log.e(LOG_TAG, "Could not invoke setConstantState()");
            }
        }
        return false;
    }

    /**
     * Some drawable implementations have problems with mutation. This method returns false if
     * there is a known issue in the given drawable's implementation.
     */
    public static boolean canSafelyMutateDrawable(@NonNull Drawable drawable) {
        if (Build.VERSION.SDK_INT < 15 && drawable instanceof InsetDrawable) {
            return false;
        }  else if (Build.VERSION.SDK_INT < 15 && drawable instanceof GradientDrawable) {
            // GradientDrawable has a bug pre-ICS which results in mutate() resulting
            // in loss of color
            return false;
        } else if (Build.VERSION.SDK_INT < 17 && drawable instanceof LayerDrawable) {
            return false;
        }

        if (drawable instanceof DrawableContainer) {
            // If we have a DrawableContainer, let's traverse its child array
            final Drawable.ConstantState state = drawable.getConstantState();
            if (state instanceof DrawableContainer.DrawableContainerState) {
                final DrawableContainer.DrawableContainerState containerState =
                        (DrawableContainer.DrawableContainerState) state;
                for (final Drawable child : containerState.getChildren()) {
                    if (!canSafelyMutateDrawable(child)) {
                        return false;
                    }
                }
            }
        }  else if (drawable instanceof ScaleDrawable) {
            return canSafelyMutateDrawable(((ScaleDrawable) drawable).getDrawable());
        }

        return true;
    }



    /**
     * Parses tint mode.
     */
    public static PorterDuff.Mode parseTintMode(int value, PorterDuff.Mode defaultMode) {
        switch (value) {
            case 3: return PorterDuff.Mode.SRC_OVER;
            case 5: return PorterDuff.Mode.SRC_IN;
            case 9: return PorterDuff.Mode.SRC_ATOP;
            case 14: return PorterDuff.Mode.MULTIPLY;
            case 15: return PorterDuff.Mode.SCREEN;
            case 16: return PorterDuff.Mode.ADD;
            default: return defaultMode;
        }
    }

}