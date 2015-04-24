package com.iguyue.canvas.widget;

import com.iguyue.canvas.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff;
import android.graphics.Xfermode;
import android.util.AttributeSet;
import android.view.View;

public class WaveBallView extends View 
{
	private final float DEFUALT_RADIUS = 100.0f;
	private final int DEFAULT_COLOR = 0xAA000000;
	private final int DEFUALT_BACKGROUND_COLOR = 0x995CB85C;
	
	private float radius;
	private int color;
	private int backgroundColor;
	
	private Paint mWavePaint;
	
	public WaveBallView(Context context) 
	{
		super(context);
		radius = DEFUALT_RADIUS;
		color = DEFAULT_COLOR;
		backgroundColor = DEFUALT_BACKGROUND_COLOR;
		initData();
	}

	public WaveBallView(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		TypedArray typedArray = context.getTheme().
				obtainStyledAttributes( attrs, R.styleable.WaveBallView, 0, 0 );
		radius = typedArray.getDimension( R.styleable.WaveBallView_radius, DEFUALT_RADIUS );
		color = typedArray.getColor( R.styleable.WaveBallView_color, DEFAULT_COLOR );
		backgroundColor = typedArray.getColor( 
				R.styleable.WaveBallView_background_color, DEFUALT_BACKGROUND_COLOR );
		initData();
	}
	
	/**
	 * 初始化数据
	 */
	private void initData()
	{
		mWavePaint = new Paint();
		mWavePaint.setAntiAlias( true );
		mWavePaint.setColor( backgroundColor );
		mWavePaint.setStyle( Style.FILL );

	}
	
	/**
	 * 测量
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) 
	{
		int width = 0;
		int height = 0;
		int modeWidth = MeasureSpec.getMode( widthMeasureSpec );
		int modeHeight = MeasureSpec.getMode( heightMeasureSpec );
		int specWidthSize = MeasureSpec.getSize( widthMeasureSpec );
		int specHeightSize = MeasureSpec.getSize( heightMeasureSpec );
		
		if ( modeWidth == MeasureSpec.EXACTLY )
		{
			width = specWidthSize;
		}
		else
		{
			width = Math.round( radius * 2 ) + getPaddingLeft() + getPaddingRight();
			if ( modeWidth == MeasureSpec.AT_MOST )
			{
				width = Math.min( width, specWidthSize );
			}
		}
		
		if ( modeHeight == MeasureSpec.EXACTLY )
		{
			height = specHeightSize;
		}
		else
		{
			height = Math.round( radius * 2 ) + getPaddingBottom() + getPaddingTop();
			if ( modeHeight == MeasureSpec.AT_MOST )
			{
				height = Math.min( height, specHeightSize );
			}
		}
		int minSize = Math.min( width , height );
		radius = minSize / 2;
		
		setMeasuredDimension( width, height );
	}
	
	/**
	 * 绘制
	 */
	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) 
	{
		super.onDraw(canvas);
		int width = getWidth();
		int height = getHeight();
		int centerX = width / 2;
		int centerY = height / 2;
		
		Bitmap mBitmap = Bitmap.createBitmap( width, height, Config.ARGB_8888 );
		Canvas tempCanvas = new Canvas( mBitmap );
		tempCanvas.drawCircle( centerX, centerY, radius, mWavePaint );
		
		mWavePaint.setColor( color );
		mWavePaint.setXfermode( new PorterDuffXfermode(PorterDuff.Mode.DST_IN) );
		for ( int i=0;i<width;i++ )
		{
			tempCanvas.drawLine( i, height, i, height / 2 + (int)( Math.sin( Math.PI * (double)i * 3 / width ) * 10 ) , mWavePaint );
		}
		mWavePaint.setXfermode( null );
		mWavePaint.setColor( backgroundColor );
		
		canvas.drawBitmap( mBitmap, 0, 0, null );
	}
	

}
