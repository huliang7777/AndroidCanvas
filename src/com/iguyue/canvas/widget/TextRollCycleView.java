package com.iguyue.canvas.widget;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.iguyue.canvas.R;
import com.iguyue.canvas.common.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * 文字上下循环滚动
 * @author moon
 *
 */
public class TextRollCycleView extends View 
{
	private static final int DEFAULT_NUM = 5;
	private static final float DEFAULT_PERHEIGHT = 40;
	private static final int DEFAULT_COLOR = 0xFFFFFFFF;
	private static final float DEFAULT_FONT_SIZE = 30;

	private int showNum;
	private float perHeight;
	private int color;
	private float fontSize;
	
	private Paint mPaint;
	private Rect mRect = new Rect();
	private ArrayList<String> texts = new ArrayList<String>();
	
	private float firstY;
	private float deltaY;
	private float currentY;
	private int deltaNum;
	private boolean isTouch;
	
	public TextRollCycleView( Context context )
	{
		super( context );
		showNum = DEFAULT_NUM;
		perHeight = Utils.dp2px( context, DEFAULT_PERHEIGHT );
		color = DEFAULT_COLOR;
		fontSize = Utils.sp2px( context, DEFAULT_FONT_SIZE );
		initAttrs();
	}

	
	public TextRollCycleView( Context context, AttributeSet attrs ) 
	{
		super( context, attrs );
		
		TypedArray typedArray = context.getTheme().obtainStyledAttributes( attrs, R.styleable.CustomView, 0, 0 );
		showNum = typedArray.getInteger( R.styleable.CustomView_show_num, DEFAULT_NUM );
		perHeight = typedArray.getDimension( R.styleable.CustomView_per_height, Utils.dp2px( context, DEFAULT_PERHEIGHT ) );
		color = typedArray.getColor( R.styleable.CustomView_color, DEFAULT_COLOR );
		fontSize = typedArray.getDimension( R.styleable.CustomView_text_size, Utils.sp2px( context, DEFAULT_FONT_SIZE ) );
		initAttrs();
	}
	
	private void initAttrs()
	{
		isTouch = false;
		mPaint = new Paint();
		mPaint.setAntiAlias( true );
		mPaint.setStyle( Style.FILL );
		mPaint.setColor( color );
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int width = MeasureSpec.getSize( widthMeasureSpec );
		
		int height = 0;
		int mode = MeasureSpec.getMode( heightMeasureSpec );
		int heightSize = MeasureSpec.getSize( heightMeasureSpec );
		// 测量高度
		if ( mode == MeasureSpec.EXACTLY )
		{
			height = heightSize;
		}
		else
		{
			height = (int)( showNum * perHeight + getPaddingBottom() + getPaddingTop() );
			if ( mode == MeasureSpec.AT_MOST )
			{
				height = Math.min( height, heightSize );
			}
		}
		perHeight = height / showNum;
		
		setMeasuredDimension( width, height );
	}
	
	@Override
	protected void onDraw( Canvas canvas ) 
	{
		super.onDraw( canvas );
		int centerX = getWidth() / 2;
		
		int size = showNum + 2;
		int midSize = size / 2;
		int length = texts.size();
		float scrollY = ( currentY + deltaY ) % perHeight;
		float percent = scrollY / perHeight;
		Log.d( "TextRollCycleView", ( currentY + deltaY ) + "-" + deltaNum + "-" + scrollY + "-" + percent );
		for ( int i=0; i<size; i++ )
		{
			// 根据偏移的个数计算需要显示的数据索引
			int n = ( -deltaNum + i + length ) % length;
		
			String text = texts.get( n );
			
			// 根据滑动距离进行y坐标计算
			
			float y = ( scrollY + perHeight * ( i - 1 ) + perHeight / 2 + mRect.height() / 2 );
			
			// 根据显示位置设置字体大小，透明度
			// 梯度显示,梯度为 ( 1 / ( midSize + 1 ) ) 的数值,初始值依次为1/n, 2/n, 3/n....n/n
			float textSize = 0;
			int alpha = 0;
			
			if ( i == midSize )
			{
				textSize = fontSize * ( ( midSize ) - Math.abs( i - midSize ) ) / ( 1 + midSize ) - Math.abs( percent ) * fontSize / ( 1 + midSize );
				alpha = (int)( 255 * ( ( midSize ) - Math.abs( i - midSize ) ) / ( 1 + midSize ) - Math.abs( percent ) * 255 / ( 1 + midSize ) );
			}
			else if ( ( deltaNum >= 0 && i > midSize ) || ( deltaNum < 0 && i > midSize ) )
			{
				textSize = fontSize * ( ( midSize ) - Math.abs( i - midSize ) ) / ( 1 + midSize ) - percent * fontSize / ( 1 + midSize );
				alpha = (int)( 255 * ( ( midSize ) - Math.abs( i - midSize ) ) / ( 1 + midSize ) - percent * 255 / ( 1 + midSize ) );
				Log.d( "TextRollCycleView", i + "-textSize1-" + textSize );
			}
			else
			{
				textSize = fontSize * ( ( midSize ) - Math.abs( i - midSize ) ) / ( 1 + midSize ) + percent * fontSize / ( 1 + midSize );
				alpha = (int)( 255 * ( ( midSize ) - Math.abs( i - midSize ) ) / ( 1 + midSize ) + percent * 255 / ( 1 + midSize ) );
				Log.d( "TextRollCycleView", i + "-textSize2-" + textSize );
			}

			mPaint.setTextSize( textSize );
			mPaint.setAlpha( alpha > 255 ? 255 : alpha );
			mPaint.getTextBounds( text, 0, text.length(), mRect );
			
			canvas.drawText( text, centerX - mRect.width() / 2, y, mPaint );
		}
	}
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent( MotionEvent event ) 
	{
		int action = event.getAction();
		switch ( action ) {
			case MotionEvent.ACTION_DOWN:
				firstY = event.getY();
				isTouch = true;
				return true;
	
			case MotionEvent.ACTION_MOVE:
				float y = event.getY();
				// 计算偏移量
				deltaY = y - firstY;
				// 计算偏移个数
				deltaNum = (int)( ( currentY + deltaY ) / perHeight ) % texts.size();
				
				invalidate();
				break;
				
			case MotionEvent.ACTION_UP:
				currentY += deltaY;
				deltaY = 0;
//				currentY %= texts.size() * perHeight;
				// 计算偏移个数
				deltaNum = Math.round( currentY / perHeight ) % texts.size();
				currentY = deltaNum * perHeight;
				invalidate();
				isTouch = false;
				break;
		}
		return super.onTouchEvent( event );
	}
	
	@Override
	protected void onAttachedToWindow() 
	{
		super.onAttachedToWindow();
		
		new Timer().schedule( new TimerTask() {
			
			@Override
			public void run() 
			{
				int time = 0;
				currentY = deltaNum * perHeight;
				while ( time < 1000 ) 
				{
					if ( isTouch )
					{
						try 
						{
							Thread.sleep( 2000 );
						} catch (InterruptedException e) 
						{
							e.printStackTrace();
						}
						break;
					}
					currentY += ( perHeight / ( 1000 / 50 ) );
					// 计算偏移个数
					deltaNum = (int)( currentY / perHeight ) % texts.size();
					
					currentY %= texts.size() * perHeight;
//					currentY = deltaNum * perHeight;
					postInvalidate();
					try 
					{
						Thread.sleep( 50 );
					} catch (InterruptedException e) 
					{
						e.printStackTrace();
					}
					time += 50;
				}
			}
		}, 0, 2000 );
	}


	public ArrayList<String> getTexts() 
	{
		return texts;
	}

	public void setTexts(ArrayList<String> texts) 
	{
		this.texts = texts;
	}
}
