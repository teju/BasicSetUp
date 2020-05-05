package org.buffer.android.buffertextinputlayout.util;

import android.content.Context;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityEventCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.appcompat.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Checkable;

public class CheckableImageButton extends AppCompatImageButton implements Checkable {

  private static final int[] DRAWABLE_STATE_CHECKED = new int[] {android.R.attr.state_checked};

  private boolean checked;

  public CheckableImageButton(Context context) {
    this(context, null);
  }

  public CheckableImageButton(Context context, AttributeSet attrs) {
    this(context, attrs, androidx.appcompat.R.attr.imageButtonStyle);
  }

  public CheckableImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    ViewCompat.setAccessibilityDelegate(
        this,
        new AccessibilityDelegateCompat() {
          @Override
          public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {
            super.onInitializeAccessibilityEvent(host, event);
            event.setChecked(isChecked());
          }

          @Override
          public void onInitializeAccessibilityNodeInfo(
              View host, AccessibilityNodeInfoCompat info) {
            super.onInitializeAccessibilityNodeInfo(host, info);
            info.setCheckable(true);
            info.setChecked(isChecked());
          }
        });
  }

  @Override
  public void setChecked(boolean checked) {
    if (this.checked != checked) {
      this.checked = checked;
      refreshDrawableState();
      sendAccessibilityEvent(AccessibilityEventCompat.TYPE_WINDOW_CONTENT_CHANGED);
    }
  }

  @Override
  public boolean isChecked() {
    return checked;
  }

  @Override
  public void toggle() {
    setChecked(!checked);
  }

  @Override
  public int[] onCreateDrawableState(int extraSpace) {
    if (checked) {
      return mergeDrawableStates(
          super.onCreateDrawableState(extraSpace + DRAWABLE_STATE_CHECKED.length),
          DRAWABLE_STATE_CHECKED);
    } else {
      return super.onCreateDrawableState(extraSpace);
    }
  }
}