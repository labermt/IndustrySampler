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

    // Break animation duration into delay and active time.
    // ex. 2=half animated, half delay, 3=third animated, 2 thirds delay.
    static final int BLINK_XML_OBJECT_ANIMATOR_DENOM = 2;

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

    public StackLight(Context context, AttributeSet attrs, int defStyleAttr) {
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
        @LinearLayoutCompat.OrientationMode int orientation = LinearLayout.VERTICAL;

        int blinkDuration = BLINK_DURATION_DEFAULT;

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.StackLight,
                0, 0);

        try {
            orientation = a.getInt(R.styleable.StackLight_orientation, orientation);
            blinkDuration = a.getInt(R.styleable.StackLight_blink_duration, blinkDuration);
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

    private void setStackOrientation(final @LinearLayoutCompat.OrientationMode int orientation) {
        setOrientation(orientation);
        // TODO: Can the LinearLayout Orientation be used or will this require custom storage? It is unclear if there is a way to get the current orientation from LinearLayout.
    }

    public long getBlinkDuration() {
        final long result = blinkAnimatorSet_.getDuration() * BLINK_XML_OBJECT_ANIMATOR_DENOM;
        return result;
    }

    private static void setBlinkPercentChild(final View child, final int blinkPercent) {
        if (child instanceof Segment) {
            final Segment segment = (Segment) child;
            final boolean isBlinkDurationUseParent = segment.isBlinkDurationUseParent();
            if (isBlinkDurationUseParent) {
                segment.setBlinkPercent(blinkPercent);
            }
        }
    }

    public void setBlinkDuration(final long blinkDuration) {
        long blinkDurationValue = blinkDuration;
        if (blinkDuration <=0) {
            blinkDurationValue = BLINK_DURATION_DEFAULT;
        }
        blinkDuration_ = blinkDurationValue;

        if (blinkDurationValue > 0) {
            final long blinkDurationRatio = blinkDurationValue / BLINK_XML_OBJECT_ANIMATOR_DENOM;
            blinkAnimatorSet_.setDuration(blinkDurationRatio);
            final long blinkDelayRatio = blinkDurationRatio*(BLINK_XML_OBJECT_ANIMATOR_DENOM-1);
            blinkAnimatorSet_.setStartDelay(blinkDelayRatio);
        }
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
}
