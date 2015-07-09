package com.iguyue.canvas.widget;

import com.iguyue.canvas.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * 滑动选择view
 * 仿uber
 * @author moon
 *
 */
@SuppressLint("ClickableViewAccessibility")
public class SlideSelectedView extends ViewGroup 
{
	private Rect mDrawableRect;
	private Drawable mDrawable;
	
	private View child;
	private int childWidth;
	private int spaceWidth;
	
	private int curPos;
	private int childX;
	
	private float mLastX;
	
	public SlideSelectedView(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		
		TypedArray typedArray = context.getTheme().obtainStyledAttributes(
				attrs, R.styleable.CustomView, 0, 0);

		mDrawable = typedArray.getDrawable(R.styleable.CustomView_custom_src);
		setWillNotDraw( false );
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom)  
	{
		int count = getChildCount();
		int width = getWidth();
		int height = getHeight();
		
		int startX = childX;
		child = getChildAt( curPos );
		child.setVisibility( View.VISIBLE );
		
		if ( mDrawable != null && mDrawableRect == null )
		{
			childWidth = child.getMeasuredWidth();
			
			spaceWidth = ( width - childWidth ) / ( count + 1 );
			
			mDrawableRect = new Rect();
			int drawableHeight = mDrawable.getIntrinsicHeight();
			int t = ( height - drawableHeight ) / 2;
			mDrawableRect.left = childWidth / 2;
			mDrawableRect.top = t;
			mDrawableRect.right = width - childWidth / 2;
			mDrawableRect.bottom = t + drawableHeight;
		}

		child.layout( startX, top, startX + childWidth, bottom );
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) 
	{
		int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
		int measuredHeight = 0;
		
		measureChildren(widthMeasureSpec, heightMeasureSpec);
		
		int count = getChildCount();
		for(int i= 0;i<count;i++)
		{
			View child = getChildAt( i );
			
			int childHeight = child.getMeasuredHeight();
			if ( childHeight > measuredHeight )
			{
				measuredHeight = childHeight;
			}
		}
		
		setMeasuredDimension(measuredWidth, measuredHeight);
	}
	
	@Override
	protected void onDraw(Canvas canvas) 
	{
		super.onDraw(canvas);
		
		if ( mDrawable != null )
		{
			mDrawable.setBounds( mDrawableRect );
			mDrawable.draw( canvas );
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) 
	{
		int width = getWidth();
		int action = event.getAction();
		switch ( action ) {
			case MotionEvent.ACTION_DOWN:
				mLastX = event.getX();
				return true;
	
			case MotionEvent.ACTION_MOVE:
				float y = event.getX();
				// 计算偏移量
				int deltaX = (int) (y - mLastX);
				
				child.setPressed( true );
				
				childX += deltaX;
				
				if ( childX < 0 )
				{
					childX = 0;
				}
				if ( childX > width - childWidth )
				{
					childX = width - childWidth;
				}
				mLastX = y;
				
				int maxDistance = ( curPos * 2 + 1 ) * spaceWidth;
				int minDistance = ( curPos * 2 - 1 ) * spaceWidth;
				if ( childX > maxDistance && deltaX > 0 )
				{
					curPos++;
					child.setVisibility( View.GONE );
				}
				else if ( childX < minDistance && deltaX < 0 )
				{
					curPos--;
					child.setVisibility( View.GONE );
				}
				
				requestLayout();
				break;
				
			case MotionEvent.ACTION_UP:
				child.setPressed( false );
				int curDistance = curPos * 2 * spaceWidth;
				if ( childX != curDistance )
				{
					childX = curDistance;
				}
				requestLayout();
				break;
		}
		return super.onTouchEvent( event );
	}
}
