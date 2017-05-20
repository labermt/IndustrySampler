package com.kea.industry.stacklight;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import static android.graphics.Color.argb;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Created by bessermt on 4/6/17.
 */

// TODO: http://stackoverflow.com/questions/4336060/how-to-add-a-custom-button-state

public class Segment extends AppCompatImageView { // TODO: Change AppCompatImageView to AppCompatImageButton but without unwanted gaps.

    private static final int[] STATE_OFF = {R.attr.segment_state_off};
    private static final int[] STATE_ON = {R.attr.segment_state_on};

    private static final int BLINK_XML_OBJECT_ANIMATOR_COUNT = 2; // Currently 2 blink.xml objectAnimators.

    private static @ColorInt int COLOR_BLACK = argb(0xFF, 0x00, 0x00, 0x00);

    private static final boolean OFF_DEFAULT = false;
    private static final boolean ON_DEFAULT = false;
    private static final int BLINK_DURATION_PARENT = 0;
    private static final int BLINK_DURATION_DEFAULT = BLINK_DURATION_PARENT;
    private static final int RADIUS_DEFAULT = 0;

    private Context context_ = null;
    private final Handler handler_ = new Handler(Looper.getMainLooper());
    private StackLight stackLight_ = null;

    private @ColorInt int color_ = COLOR_BLACK;
    private boolean off_ = OFF_DEFAULT;
    private boolean on_ = ON_DEFAULT;
    private boolean blinkDurationUseParent_ = false;

    private float radius_ = RADIUS_DEFAULT;
    private float[] radii_ = new float[] {
            RADIUS_DEFAULT, RADIUS_DEFAULT, // (topLeftRadius.x, topLeftRadius.y)
            RADIUS_DEFAULT, RADIUS_DEFAULT, // (topRightRadius.x, topRightRadius.y)
            RADIUS_DEFAULT, RADIUS_DEFAULT, // (bottomRightRadius.x, bottomRightRadius.y)
            RADIUS_DEFAULT, RADIUS_DEFAULT  // (bottomLeftRadius.x, bottomLeftRadius.y)
    };

    private Rect rect_ = null;

    private GradientDrawable whiteRectangle_ = new GradientDrawable();
    private GradientDrawable blackRectangle_ = new GradientDrawable();
    private GradientDrawable colorRectangle_ = new GradientDrawable();

    private AnimatorSet blinkAnimatorSet_ = null;
    private int blinkPercent_ = 0;

    private final AnimatorListenerAdapter animatorListenerAdapter_ = new AnimatorListenerAdapter() {
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
            if (!canceled_) {
                handler_.post(new Runnable() {
                    @Override
                    public void run() {
                        blinkAnimatorSet_.start();
                    }
                });
            }
        }
    };

    public Segment(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public Segment(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public Segment(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

/*
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Segment(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }
*/

    void init(final Context context, final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {

        @ColorInt int color = COLOR_BLACK;

        boolean off = false;
        boolean on = false;

        int blinkDuration = BLINK_DURATION_PARENT;

        int radius = RADIUS_DEFAULT;
        int topLeftRadius = RADIUS_DEFAULT;
        int topRightRadius = RADIUS_DEFAULT;
        int bottomRightRadius = RADIUS_DEFAULT;
        int bottomLeftRadius = RADIUS_DEFAULT;

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.StackLight,
                0, 0);

        try {
            color = a.getColor(R.styleable.StackLight_segment_color, color);
            blinkDuration = a.getInteger(R.styleable.StackLight_segment_blink_duration, blinkDuration);
            radius = a.getDimensionPixelSize(R.styleable.StackLight_radius, radius);
            topLeftRadius = a.getDimensionPixelSize(R.styleable.StackLight_topLeftRadius, topLeftRadius);
            topRightRadius = a.getDimensionPixelSize(R.styleable.StackLight_topRightRadius, topRightRadius);
            bottomRightRadius = a.getDimensionPixelSize(R.styleable.StackLight_bottomRightRadius, bottomRightRadius);
            bottomLeftRadius = a.getDimensionPixelSize(R.styleable.StackLight_bottomLeftRadius, bottomLeftRadius);
        } finally {
            a.recycle();
        }

        // TODO: Add try block, or maybe move up to above try block.
        if (attrs != null) {
            int i = attrs.getAttributeCount();
            while (i > 0) {
                --i;
                final int name = attrs.getAttributeNameResource(i);
                if (name == R.attr.segment_state_off) {
                    off = attrs.getAttributeBooleanValue(i, off);
                } else if (name == R.attr.segment_state_on) {
                    on = attrs.getAttributeBooleanValue(i, on);
                }
            }
        }

        final float[] radii = new float[] {
                topLeftRadius, topLeftRadius,
                topRightRadius, topRightRadius,
                bottomLeftRadius, bottomLeftRadius,
                bottomRightRadius, bottomRightRadius
        };

        context_ = context;

        // TODO: try/catch
        blinkAnimatorSet_ = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.blink);

        final boolean blinkDurationUseParent = isBlinkDurationUseParent(blinkDuration);
        setBlinkDurationUseParent(blinkDurationUseParent);
        if (blinkDurationUseParent) {
            blinkDuration = StackLight.BLINK_DURATION_DEFAULT;
        }
        setBlinkDuration(blinkDuration);
        blinkAnimatorSet_.addListener(animatorListenerAdapter_);
        blinkAnimatorSet_.setTarget(this);

        whiteRectangle_.setShape(GradientDrawable.RECTANGLE);
        whiteRectangle_.setColor(Color.WHITE);

        blackRectangle_.setShape(GradientDrawable.RECTANGLE);
        blackRectangle_.setColor(Color.BLACK);

        colorRectangle_.setShape(GradientDrawable.RECTANGLE);
        setColor(color);

        setOnOff(on);
        setBlink(on && off);

        setCornerRadius(radius);
        setCornerRadii(radii);
        // TODO: Call mutate or not? See setCornerRadius and setCornerRadii

        final Drawable[] layers = new Drawable[] {colorRectangle_, blackRectangle_, whiteRectangle_};

        final LayerDrawable segmentDrawable = new LayerDrawable(layers);

        setBackground(segmentDrawable);

        refreshDrawableState();

        // TODO: Mutate or not todo mutate?
        // whiteRectangle_.mutate();
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {

        if (off_) {
            ++extraSpace;
        }
        if (on_) {
            ++extraSpace;
        }

        final int[] drawableState = super.onCreateDrawableState(extraSpace);

        if (off_) {
            mergeDrawableStates(drawableState, STATE_OFF);
        }
        if (on_) {
            mergeDrawableStates(drawableState, STATE_ON);
        }

        return drawableState;
    }

    private void updateAnimation() {
        // TODO: ??? Or would a custom button state work
        // http://stackoverflow.com/questions/4336060/how-to-add-a-custom-button-state
        // https://developer.android.com/guide/topics/graphics/drawable-animation.html
        final boolean blink = getBlink();
        if (blinkAnimatorSet_ != null) {
            if (blink) {
                blinkAnimatorSet_.start();
            } else {
                blinkAnimatorSet_.end();
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);

        final ViewGroup.LayoutParams groupLayoutParams = getLayoutParams();
        if (groupLayoutParams != null) {
            LinearLayout.LayoutParams layoutParams = null;
            int weight = 0;
            int width = groupLayoutParams.width;
            int height = groupLayoutParams.height;
            final @LinearLayoutCompat.OrientationMode int orientation = stackLight_.getOrientation();
            switch (orientation) {
                case LinearLayout.HORIZONTAL:
                    if (width == WRAP_CONTENT) {
                        width = 0;
                        weight = 1;
                        layoutParams = new LinearLayout.LayoutParams(width, height, weight);
                    }
                    break;

                case LinearLayout.VERTICAL:
                    if (height == WRAP_CONTENT) {
                        height = 0;
                        weight = 1;
                        layoutParams = new LinearLayout.LayoutParams(width, height, weight);
                    }
                    break;

                default:
                    break;
            }
            if (layoutParams != null) {
                setLayoutParams(layoutParams);
            }
        }
        updateAnimation();
    }

    public @ColorInt int getColor() {
        final @ColorInt int result = color_;
        return result;
    }

    public void setColor(final @ColorInt int color) {
        color_ = color;
        colorRectangle_.setColor(color);
        // call mutate()?
        invalidate();
    }

    public void setColorResId(final @ColorRes int colorResId) {
        final @ColorInt int color = ContextCompat.getColor(context_, colorResId);
        setColor(color);
    }

    public boolean getOff() {
        final boolean result = !on_;
        return result;
    }

    public boolean getOn() {
        final boolean result = !off_ && on_;
        return result;
    }

    public void setOnOff(final boolean on) {
        off_ = !on;
        on_ = on;
        refreshDrawableState();
        invalidate();
    }

    public boolean getBlink() {
        final boolean result = off_ && on_;
        return result;
    }

    public void setBlink(final boolean blink) {
        if (blink) {
            off_ = true;
            on_ = true;
        } else if (off_ == true && on_ == true) {
            on_ = false;
        }
        refreshDrawableState(); // TODO: Do the refreshDrawableState() calls do anything?
        // invalidate(); // TODO: Does it help to invalidate()?
        updateAnimation();
    }

    public long getBlinkDuration() {
        final long result = blinkAnimatorSet_.getDuration() * BLINK_XML_OBJECT_ANIMATOR_COUNT;
        return result;
    }

    private static boolean isBlinkDurationUseParent(final long blinkDuration) {
        final boolean result = (blinkDuration == BLINK_DURATION_PARENT || blinkDuration <= 0);
        return result;
    }

    public void setBlinkDuration(final long blinkDuration) {
        long blinkDurationValue = blinkDuration;
        if (stackLight_ != null && isBlinkDurationUseParent(blinkDuration)) {
            blinkDurationValue = stackLight_.getBlinkDuration();
        }

        blinkAnimatorSet_.setDuration(blinkDurationValue / BLINK_XML_OBJECT_ANIMATOR_COUNT);
        // TODO: invalidate()? blinkAnimatorSet_.cancel(),  blinkAnimatorSet_.start(),... or something else?
    }

    public boolean isBlinkDurationUseParent() {
        final boolean result = blinkDurationUseParent_;
        return result;
    }

    public void setBlinkDurationUseParent(final boolean blinkDurationUseParent) {
        blinkDurationUseParent_ = blinkDurationUseParent;
        if (blinkDurationUseParent) {
            setBlinkDuration(BLINK_DURATION_PARENT);
        }
    }

    public float getCornerRadius() {
        // final float result = colorRectangle_.getCornerRadius(); // Only available in API 24 or greater.
        final float result = radius_;
        return result;
    }

    public void setCornerRadius(final float radius) {
        radius_ = radius;
        colorRectangle_.setCornerRadius(radius);
        invalidate();
    }

    public float[] getCornerRadii() {
        // final float[] result = colorRectangle_.getCornerRadii(); // Only available in API 24 or greater.
        final float[] result = radii_;
        return result;
    }

    public void setCornerRadii(final float[] radii) {
        radii_ = radii;
        colorRectangle_.setCornerRadii(radii);
    }

    // See res/animator/blink.xml android:propertyName="blinkPercent_"
    private int getBlinkPercent_() {
        return blinkPercent_;
    }

    private void setBlinkPercent_(int blinkPercent) {
        blinkPercent_ = blinkPercent;
        invalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        stackLight_ = (StackLight) getParent();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final int left = rect_.left;
        final int top = rect_.top;
        final int right = rect_.right;
        final int bottom = rect_.bottom;

        colorRectangle_.setBounds(left, top, right, bottom);

        final int xQuarter = (right - left) / 4;
        final int yQuarter = (bottom - top) / 4;

        final int innerQuarterLeft = left + xQuarter;
        final int innerQuarterTop = top + yQuarter;
        final int innerQuarterRight = right - xQuarter;
        final int innerQuarterBottom = bottom - yQuarter;

        blackRectangle_.setBounds(innerQuarterLeft, innerQuarterTop, innerQuarterRight, innerQuarterBottom);

        int alpha = 255;
        int whiteLeft = 0;
        int whiteTop = 0;
        int whiteRight = 0;
        int whiteBottom = 0;
        if (off_ && on_) { // blink
            final int percentOff = 100 - blinkPercent_;
            final int xDelta = percentOff * (right - left) / 2 / 100;
            final int yDelta = percentOff * (bottom - top) / 2 / 100;

            whiteLeft = innerQuarterLeft + xDelta;
            whiteTop = innerQuarterTop + yDelta;
            whiteRight = innerQuarterRight - xDelta;
            whiteBottom = innerQuarterBottom - yDelta;
        } else if (on_) { // on
            whiteLeft = innerQuarterLeft;
            whiteTop = innerQuarterTop;
            whiteRight = innerQuarterRight;
            whiteBottom = innerQuarterBottom;
        } else { // off or undefined
            alpha = 0;
        }
        whiteRectangle_.setAlpha(alpha);
        whiteRectangle_.setBounds(whiteLeft, whiteTop, whiteRight, whiteBottom);

        // TODO: read https://developer.android.com/guide/topics/graphics/2d-graphics.html
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec); // TODO: ??? Keep or delete?

        // TODO: Are all these calculations for sibling count required now that the parent is
        // responsible for the layout?

        int parentAvailableWidth = stackLight_.getWidth();
        int parentAvailableHeight = stackLight_.getHeight();

        final int siblingCount = stackLight_.getChildCount(); // TODO: Is this required? Perhaps just getting the width and height makes more sense.

        final @LinearLayoutCompat.OrientationMode int orientation = stackLight_.getOrientation();

        if (orientation == LinearLayout.HORIZONTAL) {
            parentAvailableWidth = parentAvailableWidth/siblingCount;
        } else {
            parentAvailableHeight = parentAvailableHeight/siblingCount;
        }

        int width = MeasureSpec.getSize(widthMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        switch (widthMode) {
            case MeasureSpec.EXACTLY:
                break;

            case MeasureSpec.AT_MOST:
                width = Math.min(width, parentAvailableWidth);
                break;

            case MeasureSpec.UNSPECIFIED:
                width = parentAvailableWidth;
                break;

            default:
                break;
        }

        int height = MeasureSpec.getSize(heightMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        switch (heightMode) {
            case MeasureSpec.EXACTLY:
                break;

            case MeasureSpec.AT_MOST:
                height = Math.min(height, parentAvailableHeight);
                break;

            case MeasureSpec.UNSPECIFIED:
                height = parentAvailableHeight;
                break;

            default:
                break;
        }

        setMeasuredDimension(width, height);
    }

    @Override // TODO: Keep or delete?
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // TODO: Test if this code is correct. I'm not sure if this is how to account for padding.
        // See: https://developer.android.com/training/custom-views/custom-drawing.html#layoutevent
        // Handle Layout Events
        // Also See: https://developer.android.com/guide/topics/ui/how-android-draws.html

        /// TODO: ???? This seems wrong... do some research on what is correct.
        final int leftPad = getPaddingLeft(); //???// TODO: Maybe the pad values should be 0?
        final int topPad = getPaddingTop();
        final int rightPad = getPaddingRight();
        final int bottomPad = getPaddingBottom();

        rect_ = new Rect(leftPad, topPad, w - rightPad, h - bottomPad);
    }
}
