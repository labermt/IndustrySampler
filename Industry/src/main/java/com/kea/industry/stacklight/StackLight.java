package com.kea.industry.stacklight;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by bessermt on 5/14/17.
 */

public class StackLight extends LinearLayout {

    private static final int COLOR_RESID_DEFAULT = R.color.default_stack_light_color;
    static final int BLINK_DURATION_DEFAULT = 1000; // millisecs

    private long blinkDuration_ = BLINK_DURATION_DEFAULT;

    public StackLight(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public StackLight(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public StackLight(Context context, AttributeSet attrs, int defStyleAttr) { // TODO: Decide if this is desired.
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

/*
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public StackLight(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }
*/

    void init(final Context context, final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {

        final int stackColorResId = COLOR_RESID_DEFAULT;
        int stackColor = ContextCompat.getColor(context, stackColorResId); // TODO: Delete?

        @LinearLayoutCompat.OrientationMode int orientation = LinearLayout.VERTICAL;

        int blinkDuration = BLINK_DURATION_DEFAULT;

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.StackLight,
                0, 0);

        try {
            orientation = a.getInt(R.styleable.StackLight_orientation, orientation);
            blinkDuration = a.getInt(R.styleable.StackLight_segment_blink_duration, blinkDuration);

            // TODO: Delete: final int orientationResId = Resources.getSystem().getIdentifier("LinearLayout_orientation", "attr", "android");

        } finally {
            a.recycle();
        }

        setStackOrientation(orientation);
        blinkDuration_ = blinkDuration;
    }

    private void setStackOrientation(final @LinearLayoutCompat.OrientationMode int orientation) { // TODO: Why not just use parent method instead? Dead code?
        setOrientation(orientation);
        // TODO: Can the LinearLayout Orientation be used or will this require custom storage? It is unclear if there is a way to get the current orientation from LinearLayout.
    }

    public long getBlinkDuration() {
        final long result = blinkDuration_;
        return result;
    }

    private static void setBlinkDurationChild(final View child, final long blinkDuration) {
        if (child instanceof Segment) {
            final Segment segment = (Segment) child;
            final boolean isBlinkDurationUseParent = segment.isBlinkDurationUseParent();
            if (isBlinkDurationUseParent) {
                segment.setBlinkDuration(blinkDuration);
            }
        }
    }

    public void setBlinkDuration(final long blinkDuration) { // TODO: Check the value is positive and use default if not?
        blinkDuration_ = blinkDuration;

        int n = getChildCount();
        while (n > 0) {
            --n;
            final View child = getChildAt(n);
            setBlinkDurationChild(child, blinkDuration);
        }
        // TODO: invalidate()? blinkAnimatorSet_.cancel(),  blinkAnimatorSet_.start(),... or something else?
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        // TODO: Code here

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override // TODO: Keep or delete?
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // TODO: Test if this code is correct. I'm not sure if this is how to account for padding.
        // See: https://developer.android.com/training/custom-views/custom-drawing.html#layoutevent
        // Handle Layout Events
        // Also See: https://developer.android.com/guide/topics/ui/how-android-draws.html

/*
        final int leftPad = getPaddingLeft();
        final int topPad = getPaddingTop();
        final int rightPad = getPaddingRight();
        final int bottomPad = getPaddingBottom();

        rect_ = new Rect(leftPad, topPad, w - (leftPad + rightPad), h - (topPad + bottomPad));
*/
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        setBlinkDurationChild(child, blinkDuration_);
    }
}
