package com.iguyue.canvas.widget;

import com.iguyue.canvas.common.Utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * 多项ListView
 * @author moon
 *
 */
public class MultiListView extends EdgeBoundListView 
{

	public MultiListView( Context context ) 
	{
		super(context);
	}
	
	public MultiListView( Context context, AttributeSet attrs ) 
	{
		super(context, attrs);
	}
	
	/**
	 * 添加并测量child
	 * @param child
	 */
	@Override
	protected void addAndMeasureChild( View child, int index )
	{
		LayoutParams params = child.getLayoutParams();
		if ( params == null )
		{
			params = new LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT );
		}
		addViewInLayout( child, index, params );
		int width = getWidth();
		child.measure( MeasureSpec.EXACTLY | width / 2, MeasureSpec.UNSPECIFIED );
	}

	@Override
	protected void layoutChildren() 
	{
		int top = mListTop + mListTopOffset;
//		Log.e(VIEW_LOG_TAG, "top : " + top );
		int width = getWidth();
		int childCount = getChildCount();
		
		for ( int i=0;i<childCount;i++ )
		{
			View child = getChildAt( i );
			int widthChild = child.getMeasuredWidth();
			int heightChild = child.getMeasuredHeight();
			int left = ( width - widthChild ) / 2;
			child.layout( left, top, left + widthChild, top + heightChild );
			top += heightChild + Utils.dp2px( getContext(), 1 );
		}
	}
}
