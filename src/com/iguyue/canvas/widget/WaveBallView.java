package com.iguyue.canvas.widget;

import java.util.Timer;
import java.util.TimerTask;

import com.iguyue.canvas.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuffXfermode;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * 波浪球View
 * @author moon
 *
 */
public class WaveBallView extends View 
{
	private final float DEFUALT_RADIUS = 100.0f;
	private final int DEFAULT_COLOR = 0xBB000000;
	private final int DEFUALT_BACKGROUND_COLOR = 0xFF309B55;
	private final float DEFUALT_PERCENT = 1.0f;
	private final int WAVE_HEIGHT = 20;
	private final int STROKE_WIDTH = 30;
	
	private float radius;
	private int color;
	private int backgroundColor;
	private int initPos;
	
	private Paint mWavePaint;
	private Paint mContentPaint;
	private Paint mRingPaint;
	
	private float percent;
	
	public WaveBallView(Context context) 
	{
		super(context);
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
		percent = typedArray.getFloat( R.styleable.WaveBallView_percent, DEFUALT_PERCENT );
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
		
		mRingPaint = new Paint();
		mRingPaint.setAntiAlias( true );
		mRingPaint.setColor( backgroundColor );
		mRingPaint.setStyle( Style.STROKE );
		mRingPaint.setStrokeWidth( STROKE_WIDTH );
		
		mContentPaint = new Paint();
		mContentPaint.setAntiAlias( true );
		mContentPaint.setColor( 0xFFFFFFFF );
		mContentPaint.setTextSize( radius / 4 );
		mContentPaint.setStyle( Style.FILL );
		
		initPos = 0;
		
//		new Thread()
//		{
//			public void run() 
//			{
//				while( true )
//				{
//					initPos += 1;
//					try 
//					{
//						Thread.sleep( 5 );
//					} 
//					catch (InterruptedException e) 
//					{
//						e.printStackTrace();
//					}
//					postInvalidate();
//				}
//			};
//		}.start();
		
		Timer senderTimer = new Timer();  
        senderTimer.schedule(
        		new TimerTask() 
        {  
            @Override  
            public void run() 
            {  
            	initPos += 1;  
            	postInvalidate();
            }  
        }, 5, 2 );
		
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
			width = Math.round( ( radius + STROKE_WIDTH ) * 2 ) + getPaddingLeft() + getPaddingRight();
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
			height = Math.round( ( radius + STROKE_WIDTH ) * 2 ) + getPaddingBottom() + getPaddingTop();
			if ( modeHeight == MeasureSpec.AT_MOST )
			{
				height = Math.min( height, specHeightSize );
			}
		}
		int minSize = Math.min( width , height );
		radius = minSize / 2 - STROKE_WIDTH;
		
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
		if ( initPos > width * 2 )
		{
			initPos = 1;
		}
	
		// 通过闭合path绘制 : PI为180°
		Path path = new Path();
		path.lineTo( 0 , height );
		for ( int i=1;i<=width;i++ )
		{
			int m = i + initPos;
			path.lineTo( i , (int)( height * ( 1- percent ) + WAVE_HEIGHT ) + Math.round( Math.sin( Math.PI * (double)m / width ) * WAVE_HEIGHT ) );
		}
		path.lineTo( width , height );
		path.lineTo( 0 , height );
		tempCanvas.drawPath( path, mWavePaint );
		
		// 通过N条Line的sina高度进行绘制
//		for ( int i=1;i<=width;i++ )
//		{
//			int m = i + initPos;
//			tempCanvas.drawLine( i, height, i, (int)( height * ( 1.0f - percent ) + WAVE_HEIGHT ) + Math.round( Math.sin( Math.PI * (double)m / width ) * WAVE_HEIGHT ) , mWavePaint );
//		}
		mWavePaint.setXfermode( null );
		mWavePaint.setColor( backgroundColor );
		
		canvas.drawBitmap( mBitmap, 0, 0, null );
		
		canvas.drawCircle( centerX, centerY, radius, mRingPaint );
		
		String content = ( percent * 100 ) + "%";
		Rect rect = new Rect();
		mContentPaint.getTextBounds( content, 0, content.length(), rect );
		canvas.drawText( content, centerX - rect.width() / 2, centerY + rect.height() / 2, mContentPaint );
	}

	public float getPercent() 
	{
		return percent;
	}

	public void setPercent(float percent) 
	{
		this.percent = percent;
	}
	
	
}
