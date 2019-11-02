package com.meow.fancontrol;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.AttrRes;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;

public final class DialView extends View {
    // some static constants
    private static final int RADIUS_OFFSET_LABEL = 30, RADIUS_OFFSET_INDICATOR = -35;

    // and some private variables for custom view
    private float radius = 0;
    private FanSpeed fanSpeed = FanSpeed.OFF;

    private int fanSpeedLowColor = 0, fanSpeedMediumColor = 0, fanSpeedHighColor = 0;

    // The position object, which will be used when drawing
    private final PointF pointF = new PointF(0, 0);

    private final Paint paint;

    public DialView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // initialize the paint object
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.FILL);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(55);
        final Typeface t = Typeface.create("", Typeface.BOLD);
        p.setTypeface(t);
        paint = p;

        // also, this property can be clicked
        setClickable(true);

        // set a bunch of colors
        withStyledAttribute(getContext(), attrs, R.styleable.DialView, array -> {
            fanSpeedLowColor = array.getColor(R.styleable.DialView_fanColor1, 0);
            fanSpeedMediumColor = array.getColor(R.styleable.DialView_fanColor2, 0);
            fanSpeedHighColor = array.getColor(R.styleable.DialView_fanColor3, 0);
        });
    }

    public DialView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DialView(Context context) {
        this(context, null);
    }

    private enum FanSpeed {
        OFF(R.string.fan_off),
        ONE(R.string.fan_one),
        TWO(R.string.fan_two),
        THREE(R.string.fan_three);

        final int code;

        FanSpeed(final int code) {
            this.code = code;
        }

        FanSpeed next() {
            final FanSpeed rtn;
            switch (this) {
                case OFF:
                    rtn = ONE;
                    break;
                case ONE:
                    rtn = TWO;
                    break;
                case TWO:
                    rtn = THREE;
                    break;
                default:
                    rtn = OFF;
                    break;
            }
            return rtn;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        radius = Math.min(getWidth(), getHeight()) * 0.8f / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // change color depending on FanSpeed
        switch (fanSpeed) {
            case OFF:
                paint.setColor(Color.GRAY);
                break;
            case ONE:
                paint.setColor(fanSpeedLowColor);
                break;
            case TWO:
                paint.setColor(fanSpeedMediumColor);
                break;
            default:
                paint.setColor(fanSpeedHighColor);
        }

        // draw a circle for the dial
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, radius, paint);
        // and a smaller circle
        final double markerRadius = radius + RADIUS_OFFSET_INDICATOR;
        computeFanSpeedPosition(pointF, fanSpeed, markerRadius);
        paint.setColor(Color.BLACK);
        canvas.drawCircle(pointF.x, pointF.y, radius / 12, paint);

        // draw fan speed labels
        final double labelRadius = radius + RADIUS_OFFSET_LABEL;
        for (FanSpeed f : FanSpeed.values()) {
            computeFanSpeedPosition(pointF, f, labelRadius);
            final String label = getResources().getString(f.code);
            canvas.drawText(label, pointF.x, pointF.y, paint);
        }
    }

    @Override
    public boolean performClick() {
        final boolean superClick = super.performClick();
        if (!superClick) {
            // idk
            fanSpeed = fanSpeed.next();
            final String label = getResources().getString(fanSpeed.code);
            setContentDescription(label);

            // invalidate the entire UI and return
            invalidate();
        }
        return true;
    }

    private void computeFanSpeedPosition(
            final PointF pointF, final FanSpeed position, final double radius
    ) {
        final float startAngle = (float) (Math.PI * 9 / 8),
                angle = (float) (startAngle + position.ordinal() * Math.PI / 4);
        pointF.x = (float) (radius * Math.cos(angle) + getWidth() / 2);
        pointF.y = (float) (radius * Math.sin(angle) + getHeight() / 2);
    }

    private void withStyledAttribute(
            final Context context, final AttributeSet set, final int[] styleableAttrs,
            final Consumer<TypedArray> consumer
    ) {
        final TypedArray array = context.obtainStyledAttributes(set, styleableAttrs);
        consumer.accept(array);
        array.recycle();
    }
}
