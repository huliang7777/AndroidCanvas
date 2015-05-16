package com.iguyue.canvas.widget;

import com.iguyue.canvas.R;
import com.iguyue.canvas.common.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * 放大镜图片
 * @author moon
 *
 */
/**
 * 正则替换
 * COLUMN_([^, & ^$]*);
 * COLUMN_$1 = "$1 ";
 * @author Administrator
 *
 */
public class MagnifierImageView extends View
{
	private final static float DEFAULT_RADIUS = 100;
	private final static float DEFAULT_SCALE = 1.5f;
	private Bitmap mBitmap;
	private Path mPath;
	private Matrix matrix;
	PaintFlagsDrawFilter filter;
	
	private float radius;
	private float centerX;
	private float centerY;
	
	public MagnifierImageView( Context context ) 
	{
		super( context );
		radius = Utils.dp2px( context, DEFAULT_RADIUS );
		initAttrs();
	}
	
	public MagnifierImageView( Context context, AttributeSet attrs ) 
	{
		super( context, attrs );
		TypedArray typedArray = context.getTheme().obtainStyledAttributes( attrs, R.styleable.CustomView,
				0, 0 );
		radius = typedArray.getDimension( R.styleable.CustomView_radius,
				 Utils.dp2px( context, DEFAULT_RADIUS ) );
		initAttrs();
	}
	
	/**
	 * 初始化
	 */
	private void initAttrs()
	{
		mBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.magnifier );
		mPath = new Path();
		mPath.addCircle( 0, 0, radius, Direction.CW );
		matrix = new Matrix();
		matrix.setScale( DEFAULT_SCALE, DEFAULT_SCALE );
		centerX = centerY = 0;
		filter = new PaintFlagsDrawFilter( 0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG );
	}

	@Override
	protected void onDraw(Canvas canvas) 
	{
		super.onDraw(canvas);
		canvas.setDrawFilter( filter );
		// 绘制底图
		canvas.drawBitmap( mBitmap, 0, 0, null );
		
		// 移动到触摸点，设为放大镜圆的中心点
		if ( centerX != 0 )
		{
			canvas.translate( centerX, centerY );
			// 根据path剪切画布成一个圆
			canvas.clipPath( mPath );
			
			// 然后移到剪切后画布的原点
			canvas.translate( -centerX * DEFAULT_SCALE, -centerY * DEFAULT_SCALE );
			// 在圆画布中绘制放大的局部图
			canvas.drawBitmap( mBitmap, matrix, null );
		}
	}
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) 
	{
		int action = event.getAction();
		switch ( action ) 
		{
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:
				centerX = event.getX();
				centerY = event.getY();
				break;
			case MotionEvent.ACTION_UP:
				centerX = 0;
				centerY = 0;
				break;
		}
		invalidate();
		return true;
	}
}
