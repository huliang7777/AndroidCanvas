package com.iguyue.canvas.widget;

import com.iguyue.canvas.R;
import com.iguyue.canvas.common.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * 旋转圆圈View
 * 
 * @author moon
 *
 */
public class RotateCircleView extends View {
	private final static int DASH_NUM = 400;
	private final static float DEFUALT_RADIUS = 100.0f;
	private final static int DEFAULT_COLOR = 0xEE309B55;
	private final static float DEFUALT_PERCENT = 1.0f;
	private final static int STROKE_WIDTH = 30;
	private final static int MORE_RING_RADIUS = 10;

	private float radius;
	private int color;
	private float percent;
	private String title;
	private String content;

	private Paint mTitlePaint;
	private Paint mContentPaint;
	private Paint mCirclePaint;
	private Paint mRingPaint;

	private Camera mCamera;
	private Matrix mMatrixCanvas;

	private Shader sweepGradient;
	private float rotateX;
	private float firstY;
	private int contentInitY;
	private int contentCurrY;

	public RotateCircleView(Context context) {
		super(context);
		radius = DEFUALT_RADIUS;
		color = DEFAULT_COLOR;
		percent = DEFUALT_PERCENT;
		initAttrs();
	}

	public RotateCircleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray typedArray = context.getTheme().obtainStyledAttributes(
				attrs, R.styleable.CustomView, 0, 0);
		radius = typedArray.getDimension(R.styleable.CustomView_radius,
				DEFUALT_RADIUS);
		color = typedArray
				.getColor(R.styleable.CustomView_color, DEFAULT_COLOR);
		percent = typedArray.getFloat(R.styleable.CustomView_percent,
				DEFUALT_PERCENT);
		initAttrs();
	}

	private void initAttrs() {
		mCamera = new Camera();
		mMatrixCanvas = new Matrix();
		title = "Circle";
		content = Math.round(percent * 100) + "%";
		mContentPaint = new Paint();
		mContentPaint.setAntiAlias(true);
		mContentPaint.setStyle(Style.FILL);
		mContentPaint.setColor(color);
		mContentPaint.setTextSize(Utils.sp2px(getContext(), 40));

		mTitlePaint = new Paint();
		mTitlePaint.setAntiAlias(true);
		mTitlePaint.setStyle(Style.FILL);
		mTitlePaint.setColor(color);
		mTitlePaint.setTextSize(Utils.sp2px(getContext(), 20));

		mCirclePaint = new Paint();
		mCirclePaint.setAntiAlias(true);
		mCirclePaint.setStyle(Style.STROKE);
		mCirclePaint.setStrokeWidth(STROKE_WIDTH);
		mCirclePaint.setColor(color);
		// 设置为虚线环(绘制400个虚线)
		double intervals = 2 * Math.PI
				* Utils.dp2px(getContext(), radius + STROKE_WIDTH);
		intervals = intervals / DASH_NUM;
		mCirclePaint.setPathEffect(new DashPathEffect(new float[] {
				Utils.dp2px(getContext(), 1),
				(float) intervals - Utils.dp2px(getContext(), 1) }, 0));
		// 外环
		mRingPaint = new Paint();
		mRingPaint.setAntiAlias(true);
		mRingPaint.setStyle(Style.STROKE);
		mRingPaint.setColor(color);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		Rect rect = new Rect();
		mContentPaint.getTextBounds(content, 0, content.length(), rect);
		contentCurrY = contentInitY = h / 2 + rect.height() / 2;
		sweepGradient = new SweepGradient(w / 2, h / 2, new int[] { Color.RED,
				Color.RED, color, color, color }, new float[] { 0, 30.0f / 360,
				32.0f / 360, 320.0f / 360, 360 });
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = measureWidth(widthMeasureSpec);
		int height = measurHeight(heightMeasureSpec);

		radius = Math.min(width, height) / 2 - STROKE_WIDTH - MORE_RING_RADIUS;
		setMeasuredDimension((int) (width * 1.2f), (int) (height * 1.2f));
	}

	private int measureWidth(int widthMeasureSpec) {
		int width = 0;
		int mode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);

		if (mode == MeasureSpec.EXACTLY) {
			width = widthSize;
		} else {
			width = Math.round((radius + STROKE_WIDTH + MORE_RING_RADIUS) * 2)
					+ getPaddingLeft() + getPaddingRight();
			if (mode == MeasureSpec.AT_MOST) {
				width = Math.min(width, widthSize);
			}
		}
		return width;
	}

	private int measurHeight(int heightMeasureSpec) {
		int height = 0;
		int mode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		if (mode == MeasureSpec.EXACTLY) {
			height = heightSize;
		} else {
			height = Math.round((radius + STROKE_WIDTH + MORE_RING_RADIUS) * 2)
					+ getPaddingTop() + getPaddingBottom();
			if (mode == MeasureSpec.AT_MOST) {
				height = Math.min(height, heightSize);
			}
		}
		return height;
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int width = getWidth();
		int height = getHeight();
		int centerX = width / 2;
		int centerY = height / 2;
		Rect rect = new Rect();

		canvas.save();
		// 旋转画布
		rotateCanvas(canvas, centerX, centerY);

		Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas bitmapCanvas = new Canvas(bitmap);
		// draw 标题
		// 估算文字大小
		mTitlePaint.getTextBounds(title, 0, title.length(), rect);
		bitmapCanvas.drawText(title, centerX - rect.width() / 2, height * 0.12f, mTitlePaint);

		// draw 外环
		// 计算标题所占的角度
		float degree = rect.width()
				/ (float) (2 * Math.PI * (radius + STROKE_WIDTH + MORE_RING_RADIUS))
				* 360 + 10;
		bitmapCanvas.drawArc(new RectF(centerX - radius - STROKE_WIDTH
				- MORE_RING_RADIUS, centerY - radius - STROKE_WIDTH
				- MORE_RING_RADIUS, centerX + radius + STROKE_WIDTH
				+ MORE_RING_RADIUS, centerY + radius + STROKE_WIDTH
				+ MORE_RING_RADIUS), 270f + degree / 2, 360f - degree, false,
				mRingPaint);

		// draw虚线环
		// 颜色过渡旋转，根据百分比旋转
		Matrix matrix = new Matrix();
		matrix.setRotate(percent * 360 + 90, centerX, centerY);
		sweepGradient.setLocalMatrix(matrix);
		mCirclePaint.setShader(sweepGradient);

		bitmapCanvas.drawArc(new RectF(centerX - radius, centerY - radius,
				centerX + radius, centerY + radius), 0,
				360.0f - 360.0f / DASH_NUM, false, mCirclePaint);

		canvas.drawBitmap(bitmap, 0, 0, null);
		canvas.restore();

		canvas.save();
		// draw 内容
		// 估算文字大小
		mContentPaint.getTextBounds(content, 0, content.length(), rect);
		canvas.drawText(content, centerX - rect.width() / 2, contentCurrY,
				mContentPaint);

		canvas.restore();
	}

	/**
	 * 旋转画布
	 * 
	 * @param canvas
	 */
	private void rotateCanvas(Canvas canvas, float centerX, float centerY) {
		mMatrixCanvas.reset();

		mCamera.save();
		mCamera.rotateX(rotateX);
		mCamera.getMatrix(mMatrixCanvas);
		mCamera.restore();

		// 以图片的中心点为旋转中心,如果不加这两句，就是以（0,0）点为旋转中心
		/**
		 * preTranslate是指在rotate前,平移,postTranslate是指在setScale后平移
		 * 注意他们参数是平移的距离,而不是平移目的地的坐标!
		 * 由于旋转是以(0,0)为中心的,所以为了把界面的中心与(0,0)对齐,就要preTranslate(-centerX,
		 * -centerY), rotate完成后,调用postTranslate(centerX, centerY),
		 * 再把图片移回来,这样看到的动画效果就是activity的界面图片从中心不停的旋转
		 */
		mMatrixCanvas.preTranslate(-centerX, -centerY);
		mMatrixCanvas.postTranslate(centerX, centerY);

		canvas.concat(mMatrixCanvas);
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getActionMasked();
		switch (action) {
		case MotionEvent.ACTION_DOWN: {
			firstY = event.getX();
			return true;
		}
		case MotionEvent.ACTION_MOVE: {
			float y = event.getX();
			rotateX = Math.abs(y - firstY) / 10;
			rotateX = rotateX > 90 ? 90 : rotateX;
			mRingPaint.setAlpha((int) (255 * (90 - rotateX) / 90));
			mTitlePaint.setAlpha((int) (255 * (90 - rotateX) / 90));
			mCirclePaint.setAlpha((int) (255 * (90 - rotateX) / 90));
			contentCurrY = contentInitY - (int) (40 * rotateX / 90);
			percent = rotateX / 90;
			content = Math.round(percent * 100) + "%";
			postInvalidate();
			return true;
		}
		case MotionEvent.ACTION_UP: {
			rotateX = 0;
			mRingPaint.setAlpha(255);
			mTitlePaint.setAlpha(255);
			mCirclePaint.setAlpha(255);
			contentCurrY = contentInitY;
			percent = 0;
			content = Math.round(percent * 100) + "%";
			postInvalidate();
			return true;
		}
		}
		return super.onTouchEvent(event);
	}
}
