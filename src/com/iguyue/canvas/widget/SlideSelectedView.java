package com.iguyue.canvas.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * 滑动选择view
 * 仿uber
 * @author moon
 *
 */
public class SlideSelectedView extends ViewGroup 
{

	public SlideSelectedView(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom)  
	{
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) 
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	@Override
	protected void onDraw(Canvas canvas) 
	{
		super.onDraw(canvas);
		
		
	}
}
