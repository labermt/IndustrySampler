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
import android.os.Parcel;
import android.os.Parcelable;
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

// Alternate way to implement would have been to use custom button states as follows:
// http://stackoverflow.com/questions/4336060/how-to-add-a-custom-button-state

public class Segment extends AppCompatImageButton {

/*
    private static final int[] LIGHT_OFF = {R.attr.light_off};
    private static final int[] LIGHT_ON = {R.attr.light_on};
*/

    private static final int BLINK_XML_OBJECT_ANIMATOR_DENOM = StackLight.BLINK_XML_OBJECT_ANIMATOR_DENOM;

    private static @ColorInt int COLOR_BLACK = argb(0xFF, 0x00, 0x00, 0x00);

    private static final boolean OFF_DEFAULT = false;
    private static final boolean ON_DEFAULT = false;
    private static final int BLINK_DURATION_PARENT = 0;
    private static final int BLINK_DURATION_DEFAULT = BLINK_DURATION_PARENT;
    private static final int RADIUS_DEFAULT = 0;

    private Context context_ = null;
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
            if (!canceled_ && handler_!=null) {
                handler_.post(new Runnable() {
                    @Override
                    public void run() {
                        blinkAnimatorSet_.start();
                    }
                });
            }
        }
    };

// TODO: https://stackoverflow.com/questions/3542333/how-to-prevent-custom-views-from-losing-state-across-screen-orientation-changes
// TODO: http://trickyandroid.com/saving-android-view-state-correctly/

    private static class SavedState extends BaseSavedState {
        private boolean off_ = false;
        private boolean on_ = false;

        private static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };

        private SavedState(final Parcelable superState) {
            super(superState);
        }

        private SavedState(final Parcel in) {
            super(in);
            // Use the same order as writeToParcel:
            off_ = in.readInt()!=0;
            on_ = in.readInt()!=0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            // Use the same order as SavedState:
            out.writeInt(off_ ? 1 : 0);
            out.writeInt(on_ ? 1 : 0);
        }

        private void setOff(final boolean off) {
            off_ = off;
        }

        private void setOn(final boolean on) {
            on_ = on;
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        final SavedState savedState = new SavedState(superState);
        savedState.setOff(off_);
        savedState.setOn(on_);
        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        final SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        off_= savedState.off_;
        on_= savedState.on_;
    }

    public Segment(Context context) {
        super(context, null, R.style.stackLightSegment);
        init(context, null, 0, 0);
    }

    public Segment(Context context, AttributeSet attrs) {
        super(context, attrs, R.style.stackLightSegment);
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
            blinkDuration = a.getInteger(R.styleable.StackLight_blink_duration, blinkDuration);
            radius = a.getDimensionPixelSize(R.styleable.StackLight_radius, radius);
            topLeftRadius = a.getDimensionPixelSize(R.styleable.StackLight_topLeftRadius, topLeftRadius);
            topRightRadius = a.getDimensionPixelSize(R.styleable.StackLight_topRightRadius, topRightRadius);
            bottomRightRadius = a.getDimensionPixelSize(R.styleable.StackLight_bottomRightRadius, bottomRightRadius);
            bottomLeftRadius = a.getDimensionPixelSize(R.styleable.StackLight_bottomLeftRadius, bottomLeftRadius);
            off = a.getBoolean(R.styleable.StackLight_light_off, off);
            on = a.getBoolean(R.styleable.StackLight_light_off, on);
        } finally {
            a.recycle();
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
    }

/* Currently not required, but could be helpful if custom button states are ever used.
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
            mergeDrawableStates(drawableState, LIGHT_OFF);
        }
        if (on_) {
            mergeDrawableStates(drawableState, LIGHT_ON);
        }

        return drawableState;
    }
*/

    private void updateAnimation() {
        if (blinkAnimatorSet_ != null) {
            final boolean blink = getBlink();
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
        updateAnimation();
    }

    public long getBlinkDuration() {
        final long result = blinkAnimatorSet_.getDuration() * BLINK_XML_OBJECT_ANIMATOR_DENOM;
        return result;
    }

    private static boolean isBlinkDurationUseParent(final long blinkDuration) {
        final boolean result = (blinkDuration == BLINK_DURATION_PARENT || blinkDuration <= 0);
        return result;
    }

    public void setBlinkDuration(final long blinkDuration) {
        long blinkDurationValue = blinkDuration;
        if (stackLight_ != null) {
            final boolean blinkDurationUseParent = isBlinkDurationUseParent(blinkDuration);
            if (blinkDurationUseParent) {
                blinkDurationValue = stackLight_.getBlinkDuration();
            }
        }

        if (blinkDurationValue > 0) {
            final long blinkDurationRatio = blinkDurationValue / BLINK_XML_OBJECT_ANIMATOR_DENOM;
            blinkAnimatorSet_.setDuration(blinkDurationRatio);
            final long blinkDelayRatio = blinkDurationRatio*(BLINK_XML_OBJECT_ANIMATOR_DENOM-1);
            blinkAnimatorSet_.setStartDelay(blinkDelayRatio);
        }
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

    private int getBlinkPercent() {
        int result = getBlinkPercent_();
        final boolean useParent = isBlinkDurationUseParent();
        if (stackLight_ != null && useParent) {
            result = stackLight_.getBlinkPercent();
        }
        return result;
    }

    void setBlinkPercent(final int blinkPercent) {
        setBlinkPercent_(blinkPercent);
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

        final boolean isOn = getOn();
        final boolean isBlink = getBlink();
        if (isBlink) {
            final int blinkPercentOff = 100 - getBlinkPercent();
            final int xDelta = blinkPercentOff * (right - left) / 2 / 100;
            final int yDelta = blinkPercentOff * (bottom - top) / 2 / 100;

            whiteLeft = innerQuarterLeft + xDelta;
            whiteTop = innerQuarterTop + yDelta;
            whiteRight = innerQuarterRight - xDelta;
            whiteBottom = innerQuarterBottom - yDelta;
        } else if (isOn) {
            whiteLeft = innerQuarterLeft;
            whiteTop = innerQuarterTop;
            whiteRight = innerQuarterRight;
            whiteBottom = innerQuarterBottom;
        } else { // off or undefined
            alpha = 0;
        }
        whiteRectangle_.setAlpha(alpha);
        whiteRectangle_.setBounds(whiteLeft, whiteTop, whiteRight, whiteBottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        // super.onMeasure(widthMeasureSpec, heightMeasureSpec); // TODO: ??? Keep or delete?

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

        final int leftPad = getPaddingLeft();
        final int topPad = getPaddingTop();
        final int rightPad = getPaddingRight();
        final int bottomPad = getPaddingBottom();

        rect_ = new Rect(leftPad, topPad, w - rightPad, h - bottomPad);
    }
}
