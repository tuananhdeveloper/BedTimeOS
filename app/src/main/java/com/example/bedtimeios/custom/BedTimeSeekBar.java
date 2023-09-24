package com.example.bedtimeios.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.bedtimeios.R;
import com.example.bedtimeios.utils.Converter;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.ShapeAppearanceModel;

/**
 * Created by Nguyen Tuan Anh on 24/06/2022.
 * FPT Software
 * tuananhprogrammer@gmail.com
 */

public class BedTimeSeekBar extends View {
    private final Context context;
    private final float circleRadius;
    private final float mBackgroundWidth;
    private final float mProgressWidth;
    private final float mIndicatorRadius;

    private final float backgroundDrawablePadding;
    private final int mBackGroundColor;
    private final int mIndicatorColor;

    private Paint mPaintBackground;
    private Paint mPaintProgress;
    private Bitmap bitmapBackground;

    private final int backgroundDrawableId;
    private final int bedDrawableId;
    private final int alarmRingDrawableId;

    private float mMaxX;
    private float mMaxY;
    private float cx;
    private float cy;
    private float mAngle; // in degrees
    private float startAngle = 0; //in degrees, 3 o'clock

    private RectF backgroundRing;
    private RectF progressRing;
    private RectF startCapRectF;
    private RectF endCapRectF;

    private final float TOTAL_MINUTES = 1440;
    private final float TOTAL_ANGLE = 360;

    private final float A_MINUTES_IN_DEGRESS = TOTAL_MINUTES/TOTAL_ANGLE; //degrees
    private final float DEGREE_PER_5_MINUTE = 5*TOTAL_ANGLE/TOTAL_MINUTES; //minutes

    private OnTimeChangeListener onTimeChangeListener;

    private static boolean enabled = true;
    private boolean isStartCapClick = false;
    private boolean isEndCapClick = false;
    private boolean isArcClick = false;

    public BedTimeSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.BedTimeSeekBar);
        this.mBackgroundWidth = attributes.getDimensionPixelSize(R.styleable.BedTimeSeekBar_background_width,80);
        this.circleRadius = attributes.getDimensionPixelSize(R.styleable.BedTimeSeekBar_circle_radius, 600/2);
        this.mIndicatorRadius = mProgressWidth = attributes.getDimensionPixelSize(R.styleable.BedTimeSeekBar_progress_width, 60);
        this.mAngle = attributes.getInt(R.styleable.BedTimeSeekBar_sweep_angle, 180);
        this.startAngle = attributes.getInt(R.styleable.BedTimeSeekBar_start_angle, 0);; //3 o'clock

        this.mBackGroundColor = attributes.getColor(R.styleable.BedTimeSeekBar_background_color, Color.BLACK);
        this.mIndicatorColor = attributes.getColor(R.styleable.BedTimeSeekBar_indicator_color, Color.RED);

        this.backgroundDrawablePadding = attributes.getDimensionPixelSize(R.styleable.BedTimeSeekBar_padding_drawable, 40);
        this.backgroundDrawableId = attributes.getResourceId(R.styleable.BedTimeSeekBar_background_drawableId, R.drawable.ic_clock_face);
        this.bedDrawableId = attributes.getResourceId(R.styleable.BedTimeSeekBar_beginCap_drawableId, R.drawable.ic_king_bed);
        this.alarmRingDrawableId = attributes.getResourceId(R.styleable.BedTimeSeekBar_endCap_drawableId, R.drawable.ic_notifications);
        attributes.recycle();

        ShapeableImageView bedImageView = new ShapeableImageView(context);
        ShapeableImageView alarmRingImageView = new ShapeableImageView(context);
        bedImageView.setImageResource(bedDrawableId);
        alarmRingImageView.setImageResource(alarmRingDrawableId);

        initBaseDimes();
//        initCapIcon(bedImageView);
        initCapIcon(alarmRingImageView);
        initPaint();
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension((int) Math.ceil(mMaxX), (int) Math.ceil(mMaxY));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paintBackGround(canvas);
        paintProgress(canvas);
        paintCapIcons(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float originX = (int) Math.ceil(mMaxX)/2f;
        float originY = (int) Math.ceil(mMaxY)/2f;
        handleTouchEvent(event, originX, originY);
        invalidate();
        return true;
    }

    @Override
    public boolean hasOnClickListeners() {
        return false;
    }

    @Override
    public boolean hasOnLongClickListeners() {
        return false;
    }

    public void setOnTimeChangeListener(OnTimeChangeListener onTimeChangeListener) {
        this.onTimeChangeListener = onTimeChangeListener;
    }

    private void initBaseDimes() {
        cx = circleRadius + mBackgroundWidth / 2f + mIndicatorRadius;
        cy = circleRadius + mBackgroundWidth / 2f + mIndicatorRadius;
        mMaxX = circleRadius * 2;
        mMaxY = circleRadius * 2;
    }

    private void initCapIcon(ShapeableImageView imageView) {
        imageView.setMinimumWidth(Converter.dpToPixels(28));
        imageView.setMinimumHeight(Converter.dpToPixels(28));
        imageView.setAdjustViewBounds(true);
//        imageView.setBackgroundColor(Color.parseColor("#"));
        imageView.setPadding(
                Converter.dpToPixels(2),
                Converter.dpToPixels(2),
                Converter.dpToPixels(2),
                Converter.dpToPixels(2)
        );
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setShapeAppearanceModel(ShapeAppearanceModel.builder()
                        .setAllCornerSizes((float) (imageView.getWidth()*0.5))
                .build());
    }

    private void initPaint() {
        float endProgressRect = 2 * circleRadius - mBackgroundWidth;
        float startBackgroundRect = mBackgroundWidth;

        progressRing = new RectF(
                startBackgroundRect,
                startBackgroundRect,
                endProgressRect,
                endProgressRect);

        backgroundRing = new RectF(
                startBackgroundRect,//left
                startBackgroundRect,//top
                endProgressRect,//right
                endProgressRect);//bottom

        mPaintBackground = new Paint();
        mPaintBackground.setAntiAlias(true);
        mPaintBackground.setStyle(Paint.Style.STROKE);
        mPaintBackground.setStrokeWidth(mBackgroundWidth);
        mPaintBackground.setColor(mBackGroundColor);
//        mPaintBackground.setShader(new BitmapShader(BitmapFactory
//                .decodeResource(getContext().getResources(), R.drawable.ic_clock_face),
//                Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

        bitmapBackground = Converter.getBitmapFromVectorDrawable(context, backgroundDrawableId);


        mPaintProgress = new Paint();
        mPaintProgress.setStyle(Paint.Style.STROKE);
        mPaintProgress.setStrokeCap(Paint.Cap.ROUND);
        mPaintProgress.setStrokeWidth(mProgressWidth);
        mPaintProgress.setAntiAlias(true);
        mPaintProgress.setColor(mIndicatorColor);
    }

    private void paintCapIcons(Canvas canvas) {
        int capSize = Converter.dpToPixels(20); //in pixels

        float Ox = (int) Math.ceil(mMaxX)/2f;
        float Oy = (int) Math.ceil(mMaxX)/2f;

        double startAngleInRadians = Converter.degreesToRadians(startAngle);
        double endAngleInRadians = Converter.degreesToRadians(startAngle + mAngle);
        float newRadius = circleRadius - mBackgroundWidth;

        //calculate
        float startCapX = Ox + (float) (newRadius*Math.cos(startAngleInRadians));
        float startCapY = Oy + (float) (newRadius*Math.sin(startAngleInRadians));
        float endCapX = Ox + (float) (newRadius*Math.cos(endAngleInRadians));
        float endCapY = Oy + (float) (newRadius*Math.sin(endAngleInRadians));

        //transform
        startCapX -= capSize/2f; startCapY -= capSize/2f; endCapX -= capSize/2f; endCapY -= capSize/2f;

        //draw startCap
        Bitmap bitmapBedDrawable = Bitmap.createScaledBitmap(
                Converter.getBitmapFromVectorDrawable(context, this.bedDrawableId), capSize, capSize, false);
        startCapRectF = new RectF(startCapX, startCapY,
                startCapX + capSize, startCapY + capSize);
        canvas.drawBitmap(bitmapBedDrawable, null, startCapRectF, null);

        //draw endCap
        Bitmap bitmapAlarmDrawable = Bitmap.createScaledBitmap(
                Converter.getBitmapFromVectorDrawable(context, this.alarmRingDrawableId),
                capSize, capSize, false);
        endCapRectF = new RectF(endCapX, endCapY,
                endCapX + capSize, endCapY + capSize);
        canvas.drawBitmap(bitmapAlarmDrawable, null, endCapRectF, null);
    }

    private void paintBackGround(@NonNull Canvas canvas) {
        canvas.drawOval(backgroundRing, mPaintBackground);
        canvas.drawBitmap(bitmapBackground, null, new RectF(
                backgroundRing.left + backgroundDrawablePadding,
                backgroundRing.top + backgroundDrawablePadding,
                backgroundRing.right - backgroundDrawablePadding,
                     backgroundRing.bottom - backgroundDrawablePadding
        ), null);
    }

    private void paintProgress(Canvas canvas) {
        canvas.drawArc(progressRing, startAngle, mAngle, false, mPaintProgress);
    }

    private void handleTouchEvent(MotionEvent event, float originX, float originY) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (!isStartCapClick) isStartCapClick = startCapRectF.contains(event.getX(), event.getY());
                isEndCapClick = endCapRectF.contains(event.getX(), event.getY());
                isArcClick = false;

                if(isStartCapClick) {
                    moveStartCap(event, originX, originY);
                }
                else if(isEndCapClick) {

                }
                else if(isArcClick) {
                    isStartCapClick = false; isEndCapClick = false;
                }
                break;
            case MotionEvent.ACTION_DOWN:
                return;
            case MotionEvent.ACTION_UP:
                isStartCapClick = false;

            default:
                break;
        }
    }

    private void moveStartCap(MotionEvent event, float originX, float originY) {
        double degreesToTransform = Converter.coordinatesToDegrees(
                originX, originY, event.getX(), event.getY());
        degreesToTransform = ((int)(degreesToTransform/DEGREE_PER_5_MINUTE))*DEGREE_PER_5_MINUTE;

        if(Math.abs(startAngle - degreesToTransform) >= DEGREE_PER_5_MINUTE) {

            mAngle = (float) (mAngle + startAngle - degreesToTransform);

            startAngle = (float) degreesToTransform;

            onTimeChangeListener.onChange((int) ((90 + startAngle)*A_MINUTES_IN_DEGRESS), 0);
        }
    }
//
//    private void changeProgress(float touchX, float touchY) {
//
//    }

    public interface OnTimeChangeListener {
        void onChange(int bedTimeMinites, float sleepTimeMinutes);
    }
}