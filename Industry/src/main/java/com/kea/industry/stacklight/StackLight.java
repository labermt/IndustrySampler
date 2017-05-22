package com.kea.industry.stacklight;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Created by bessermt on 5/14/17.
 */

public class StackLight extends LinearLayout {

    private static final int COLOR_RESID_DEFAULT = R.color.default_stack_light_color;
    static final int BLINK_DURATION_DEFAULT = 1000; // millisecs
    static final int BLINK_XML_OBJECT_ANIMATOR_SPLIT = 2; // Half duration, half startOffset.

    private long blinkDuration_ = BLINK_DURATION_DEFAULT;

    private AnimatorSet blinkAnimatorSet_ = null;
    private int blinkPercent_ = 0;

    private final AnimatorListenerAdapter animatorListenerAdapter_ = new AnimatorListenerAdapter() {
        private final Handler handler_ = new Handler(Looper.getMainLooper());
        private boolean canceled_;

        @Override
        public void onAnimationStart(Animator animation) {
            canceled_ = false;
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            canceled_ = true;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (handler_!=null && !canceled_) {
                handler_.post(new Runnable() {
                    @Override
                    public void run() {
                        //  TODO: DELETE? blinkAnimatorSet_.setStartDelay(???);
                        blinkAnimatorSet_.start();
                    }
                });
            }
        }
    };

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

        // TODO: try/catch
        blinkAnimatorSet_ = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.blink);

        setBlinkDuration(blinkDuration);
        blinkAnimatorSet_.addListener(animatorListenerAdapter_);
        blinkAnimatorSet_.setTarget(this);
    }

    private void setStackOrientation(final @LinearLayoutCompat.OrientationMode int orientation) { // TODO: Why not just use parent method instead? Dead code?
        setOrientation(orientation);
        // TODO: Can the LinearLayout Orientation be used or will this require custom storage? It is unclear if there is a way to get the current orientation from LinearLayout.
    }

    public long getBlinkDuration() {
        final long result = blinkAnimatorSet_.getDuration() * BLINK_XML_OBJECT_ANIMATOR_SPLIT;
        return result;
    }

/* TODO: Delete
    private static void setBlinkDurationChild(final View child, final long blinkDuration) {
        if (child instanceof Segment) {
            final Segment segment = (Segment) child;
            final boolean isBlinkDurationUseParent = segment.isBlinkDurationUseParent();
            if (isBlinkDurationUseParent) {
                segment.setBlinkDuration(blinkDuration);
            }
        }
    }
*/

    private static void setBlinkPercentChild(final View child, final int blinkPercent) {
        if (child instanceof Segment) {
            final Segment segment = (Segment) child;
            final boolean isBlinkDurationUseParent = segment.isBlinkDurationUseParent();
            if (isBlinkDurationUseParent) {
                segment.setBlinkPercent(blinkPercent);
            }
        }
    }

    public void setBlinkDuration(final long blinkDuration) { // TODO: Check the value is positive and use default if not?
        long blinkDurationValue = blinkDuration;
        if (blinkDuration <=0) {
            blinkDurationValue = BLINK_DURATION_DEFAULT;
        }
        blinkDuration_ = blinkDurationValue;

/* TODO: Delete
        int n = getChildCount();
        while (n > 0) {
            --n;
            final View child = getChildAt(n);
            setBlinkDurationChild(child, blinkDurationValue);
        }
*/

        if (blinkDurationValue > 0) {
            final long blinkDurationHalf = blinkDurationValue / BLINK_XML_OBJECT_ANIMATOR_SPLIT;
            blinkAnimatorSet_.setDuration(blinkDurationHalf);
            blinkAnimatorSet_.setStartDelay(blinkDurationHalf);
        }

        // TODO: invalidate()? blinkAnimatorSet_.cancel(),  blinkAnimatorSet_.start(),... or something else?
    }

    // See res/animator/blink.xml android:propertyName="blinkPercent_"
    private int getBlinkPercent_() {
        return blinkPercent_;
    }

    private void setBlinkPercent_(int blinkPercent) {
        blinkPercent_ = blinkPercent;

        int n = getChildCount();
        while (n > 0) {
            --n;
            final View child = getChildAt(n);
            setBlinkPercentChild(child, blinkPercent);
        }
    }

    int getBlinkPercent() {
        final int result = getBlinkPercent_(); // TODO: Unclear on threading here. How is blinkPercent_ atomic?
        return result;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);

        blinkAnimatorSet_.start();
    }

// TODO: Delete empty callbacks:
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec); // TODO: ??? Keep or delete?

        // setMeasuredDimension(width, height); // TODO: Keep or delete.

        // super.onMeasure(widthMeasureSpec, heightMeasureSpec); // TODO: Or this at the end?
    }

    // TODO: Delete all empty callbacks:
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

/*
    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        setBlinkDurationChild(child, blinkDuration_);
    }
*/
}
