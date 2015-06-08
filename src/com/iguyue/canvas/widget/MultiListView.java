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
	private final static int INIT_COLUMN = 2;
	private int mColumn;
	private int[] mColumnBottom;
	
	
	public MultiListView( Context context ) 
	{
		super(context);
		init();
	}
	
	public MultiListView( Context context, AttributeSet attrs ) 
	{
		super(context, attrs);
		init();
	}
	
	/**
	 * 初始化数据
	 */
	private void init()
	{
		mColumn = INIT_COLUMN;
		mColumnBottom = new int[mColumn];
		for( int i=0;i<mColumn;i++ )
		{
			mColumnBottom[ i ] = 0;
		}
	}
	
	/**
	 * 从offset开始向下布局
	 * @param offset
	 */
	@Override
	protected void fillListDown( int offset )
	{
		int column = getMinColumnBottom();
		int minLastItemBottom = mColumnBottom[ column ];
		int count = mAdapter.getCount();
		int height = getHeight();
		
		while ( minLastItemBottom + offset < height 
				&& mLastItemPosition < count - 1 )
		{
			View view = mAdapter.getView( mLastItemPosition + 1, getCachedView(), this );
			addAndMeasureChild( view, -1 );
			mColumnBottom[ column ] += view.getMeasuredHeight() + ( mLastItemPosition > 0 ? Utils.dp2px( getContext(), 1 ) : 0 );
			++mLastItemPosition;
			column = getMinColumnBottom();
			minLastItemBottom = mColumnBottom[ column ];
		}
	}
	
	/**
	 * 从offset开始向上布局
	 * @param offset
	 */
	@Override
	protected void fillListUp( int offset )
	{
		if ( getChildCount() == 0)
		{
			return;
		}
		
		int minFirstItemTop = getMinColumnTop();
		
		while( minFirstItemTop + offset > 0 
			&& mFirstItemPosition > 0 )
		{
			View view = mAdapter.getView( mFirstItemPosition - 1, getCachedView(),  this );
			addAndMeasureChild( view, 0 );
			int viewHeight = (int) (view.getMeasuredHeight() + ( mFirstItemPosition > 0 ? Utils.dp2px( getContext(), 1 ) : 0 ));
			minFirstItemTop = getMinColumnTop();
			mListTopOffset -= viewHeight;
			--mFirstItemPosition;
		}
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
		child.measure( MeasureSpec.EXACTLY | width / mColumn, MeasureSpec.UNSPECIFIED );
	}

	@Override
	protected void layoutChildren() 
	{
		int top = mListTop + mListTopOffset;
//		Log.e(VIEW_LOG_TAG, "top : " + top );
		int width = getWidth();
		int childCount = getChildCount();
		
		int leftTop = top;
		int rightTop = top;
		
		for ( int i=0;i<childCount;i++ )
		{
			View child = getChildAt( i );
			int widthChild = child.getMeasuredWidth();
			int heightChild = child.getMeasuredHeight();
			
			if ( leftTop <= rightTop )
			{
				int left = ( width / 2 - widthChild ) / 2;;
				child.layout( left, leftTop, left + widthChild, leftTop + heightChild );
				leftTop += heightChild + Utils.dp2px( getContext(), 1 );
			}
			else
			{
				int left = width / 2 + ( width / 2 - widthChild ) / 2;;
				child.layout( left, rightTop, left + widthChild, rightTop + heightChild );
				rightTop += heightChild + Utils.dp2px( getContext(), 1 );
			}
		}
	}
	
	/**
	 * 获得最小列bottom
	 * @return
	 */
	private int getMinColumnBottom()
	{
		int column = 0;
		int minLastItemBottom = mColumnBottom[ 0 ];
		
		for ( int i=1;i<mColumn;i++ )
		{
			if ( minLastItemBottom > mColumnBottom[ i ] )
			{
				minLastItemBottom = mColumnBottom[ i ];
				column = i;
			}
		}
		
		return column;
	}
	
	/**
	 * 获得最小列top
	 * @return
	 */
	private int getMinColumnTop()
	{
		int minFirstItemTop = getChildAt( 0 ).getTop();
		
		for ( int i=1;i<mColumn;i++ )
		{
			View view = getChildAt( i );
			if ( view != null )
			{	
				int top = view.getTop();
				if ( minFirstItemTop > top )
				{
					minFirstItemTop = top;
				}
			}
		}
		
		return minFirstItemTop;
	}
}
