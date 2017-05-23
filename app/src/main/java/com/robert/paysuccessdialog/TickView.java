package com.robert.paysuccessdialog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

;

/**
 * Created by Robert on 2017/3/24.
 * Android 3.0 (API level 11), 开始支持
 * 所有的View 的canvas都会使用GPU，但是硬件的加速会占有一定的RAM。
 * 在API >= 14上，默认是开启的，如果你的应用只是标准的View和Drawable，全局都打开硬件加速，是不会有任何问题的。
 * 然而，硬件加速并不支持所有的2D画图的操作，这时开着它，可能会影响到你的自定义控件或者绘画，出现异常等行为，
 * 所以关闭硬件加速 setLayerType(View.LAYER_TYPE_SOFTWARE, null);
 */

public class TickView extends View {
    private final static String TAG = "TickView";
    //打钩动画
    private ValueAnimator mTickAnimation;
    //打钩百分比
    float tickPreCent = 0;
    //打钩百分比监听
    private onTickPreCentListener mOnTickPreCentListener;
    //测量打钩
    private PathMeasure tickPathMeasure;
    //圆圈的大小,半径
    private int circleRadius;
    //圆的颜色
    private int circleColor;
    //打钩画笔宽度
    private int tickStrokeWidth;
    //打钩头坐标
    private int firStartX;
    private int firStartY;
    //打钩拐角坐标
    private int firEndX;
    private int firEndY;
    //打钩尾坐标
    private int secEndX;
    private int secEndY;
    private Paint circlePaint;
    private Paint tickPaint;

    public interface onTickPreCentListener {
        void onTickPreCent(float tickPreCent);
    }

    public void setTickPreCentUpdateListener(onTickPreCentListener onTickPreCentListener) {
        this.mOnTickPreCentListener = onTickPreCentListener;
    }

    public TickView(Context context) {
        this(context, null);
    }

    public TickView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TickView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);//关闭硬件加速
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.TickView);
        circleRadius = mTypedArray.getDimensionPixelSize(R.styleable.TickView_ctv_circleRadius, 0);
        circleColor = mTypedArray.getColor(R.styleable.TickView_ctv_circle_color, getResources().getColor(R.color.color_50be06));
        tickStrokeWidth = mTypedArray.getDimensionPixelSize(R.styleable.TickView_ctv_tick_stroke_width, DensityUtils.dp2px(context, 10));
        mTypedArray.recycle();
        initTickPathXY(context);
        initPaint();
        initTickAnimation();
    }

    private void initPaint() {
        /**
         *  设置外圆画笔属性
         */
        circlePaint = new Paint();
        circlePaint.setColor(circleColor);
        circlePaint.setAntiAlias(true);

        /**
         *  设置打钩画笔属性
         */
        tickPaint = new Paint();
        tickPaint.setColor(Color.WHITE);
        tickPaint.setStrokeWidth(tickStrokeWidth);
        tickPaint.setStyle(Paint.Style.STROKE);
        tickPaint.setStrokeCap(Paint.Cap.ROUND);//设置线帽为圆弧
        tickPaint.setStrokeJoin(Paint.Join.ROUND);//设置线段连接处的样式为圆弧
    }

    private void initTickPathXY(Context context) {
        firStartX = DensityUtils.dp2px(context, 29);
        firStartY = DensityUtils.dp2px(context, 46);

        firEndX = DensityUtils.dp2px(context, 44);
        firEndY = DensityUtils.dp2px(context, 61);

        secEndX = DensityUtils.dp2px(context, 69);
        secEndY = DensityUtils.dp2px(context, 36);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mTickAnimation != null) {
            mTickAnimation.cancel();
        }
        mOnTickPreCentListener = null;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        /**
         *   初始化打钩路径
         */
        Path tickPath = new Path();
        tickPath.moveTo(firStartX, firStartY);
        tickPath.lineTo(firEndX, firEndY);
        tickPath.lineTo(secEndX, secEndY);
        tickPathMeasure = new PathMeasure(tickPath, false);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    //打钩动画
    private void initTickAnimation() {
        mTickAnimation = ValueAnimator.ofFloat(0f, 1f);
        mTickAnimation.setDuration(600);
        mTickAnimation.setInterpolator(new LinearInterpolator());
        mTickAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                tickPreCent = (float) animation.getAnimatedValue();
                Log.d(TAG, "[onAnimationUpdate --> tickPreCent] = " + "%" + tickPreCent * 100);
                if (mOnTickPreCentListener != null) {
                    mOnTickPreCentListener.onTickPreCent(tickPreCent);
                }
                invalidate();
            }
        });
        mTickAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mTickAnimation.removeAllListeners();
                super.onAnimationEnd(animation);
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(widthSpecSize + 10, heightSpecSize + 10);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        /**
         *  画圆
         */
        if (circleRadius == 0) {//默认使用view的一半作为半径
            circleRadius = Math.min(canvas.getWidth(), canvas.getHeight()) / 2;
        }
        canvas.drawCircle(circleRadius, circleRadius, circleRadius, circlePaint);
        /**
         * 绘制打钩
         */
        Path path = new Path();
        tickPathMeasure.getSegment(0, tickPreCent * tickPathMeasure.getLength(), path, true);
        path.rLineTo(0, 0);
        canvas.drawPath(path, tickPaint);
    }

    public void start() {
        if (mTickAnimation != null) {
            //TODO 这里只是为了演示视频方便点所以才加了延时
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    mTickAnimation.start();
                }
            }, 500);
        }
    }
}
