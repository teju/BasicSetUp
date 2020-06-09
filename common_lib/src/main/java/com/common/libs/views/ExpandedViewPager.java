package com.common.libs.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.viewpager.widget.ViewPager;

public class ExpandedViewPager extends ViewPager {

    private View mCurrentView;

    public ExpandedViewPager(Context context) {
        super(context);
    }

    public ExpandedViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int oriheightMeasureSpec = heightMeasureSpec;

        try {
            if (mCurrentView == null) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                return;
            }

            int height = 0;
            mCurrentView.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            int h = mCurrentView.getMeasuredHeight();
            if (h > height) height = h;
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } catch (Exception e) {
            try {
                getAdapter().notifyDataSetChanged();
                super.onMeasure(widthMeasureSpec, oriheightMeasureSpec);
            } catch (Exception ex) {}
        }

    }

    public void measureCurrentView(View currentView) {
        mCurrentView = currentView;
        requestLayout();
    }
}
