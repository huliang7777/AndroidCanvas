package com.iguyue.canvas.widget;

import java.util.Stack;

import com.iguyue.canvas.common.Utils;
import com.iguyue.canvas.effect.AbFlingEffect;
import com.iguyue.canvas.effect.EdgeBoundEffect;
import com.iguyue.canvas.effect.EdgeBoundFlingEffect;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListAdapter;

/**
 * 自定义ListView
 * @author moon
 *
 */
@SuppressLint("ClickableViewAccessibility")
public class EdgeBoundListView extends AdapterView<ListAdapter> 
{
	/**
	 * 第一次触摸的x坐标
	 */
	protected int mTouchStartX;
	/**
	 * 第一次触摸的y坐标
	 */
	protected int mTouchStartY;
	
	/**
	 * 滚动临界值
	 */
	protected int mTouchSlop;
	
	/**
	 * 第一次触摸时List顶部初始坐标
	 */
	protected int mListTopStart;
	
	/**
	 * 第一个可见item到List顶部坐标
	 */
	protected int mListTop;
	
	/**
	 * 第一个可见item位置
	 */
	protected int mFirstItemPosition;
	
	/**
	 * 最后一个可见item位置
	 */
	protected int mLastItemPosition;
	
	/**
	 * List顶部滚动的偏移量
	 */
	protected int mListTopOffset;
	
	/**
	 * 缓存views
	 */
	protected Stack<View> mCachedViews = new Stack<View>();
	
	/**
	 * 内容适配器
	 */
	protected ListAdapter mAdapter;
	
	/**
	 * 触摸状态：0-重置状态
	 */
	private static final int TOUCH_STATE_RESET = 0;
	/**
	 * 触摸状态：1-点击状态
	 */
	private static final int TOUCH_STATE_CLICK = 1;
	/**
	 * 触摸状态：2-滚动状态
	 */
	private static final int TOUCH_STATE_SCROLL = 2;
	/**
	 * 触摸状态：3-惯性滚动状态（释放后触发）
	 */
	private static final int TOUCH_STATE_FLING = 3;
	
	/**
	 * 当前触摸状态
	 */
	protected int curTouchState;
	
	/**
	 * 长按检测Runnable
	 */
	private Runnable mLongClickRunnable;
	
	/**
	 * 震动服务类
	 */
	private Vibrator mVibrator;
	private long[] vibrators;
	
	/**
	 * 惯性滚动效果类
	 */
	private AbFlingEffect mFlingEffect;
	/**
	 * 惯性滚动效果可执行类
	 */
	private Runnable mFlingEffectRunnable;
	/**
	 * 滚动速度检测类
	 */
	private VelocityTracker mVelocityTracker;
	/**
	 * 滚动速度的单位
	 */
	private static final int PIXELS_PER_SECOND = 1000;
	/**
	 * 最小滚动速度
	 */
	private static float mMinFlingVelocity;
	/**
	 * 最大滚动速度
	 */
	private static float mMaxFlingVelocity;
	
	/**
	 * 选中item背景颜色
	 */
	private static final int SELECTOR_COLOR = 0xDDEFEEEF;
	
	/**
	 * 选中item位置
	 */
	private int mSelectedPosition;
	/**
	 * 选中的区域
	 */
	private Rect mSelectorRect;
	/**
	 * 图片用于绘制选中item背景
	 */
	private Drawable mSelector;
	
	/**
	 * 顶部边界拉动效果
	 */
	private EdgeBoundEffect mTopEdgeBoundEffect;
	/**
	 * 底部边界拉动效果
	 */
	private EdgeBoundEffect mBottomEdgeBoundEffect;
	
	/**
	 * 上次触摸的y坐标
	 */
	private int mTouchLastY;
	
	
	public EdgeBoundListView(Context context) 
	{
		super(context);
		init();
	}
	
	public EdgeBoundListView(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		init();
	}
	
	/**
	 * 初始化
	 */
	private void init()
	{
		curTouchState = TOUCH_STATE_RESET;
		mVibrator = (Vibrator) getContext().getSystemService( Context.VIBRATOR_SERVICE );
		vibrators = new long[]{ 100, 400 };
		mSelectedPosition = INVALID_POSITION;
		mSelectorRect = new Rect();
		mSelector = new ColorDrawable( SELECTOR_COLOR );
		mTouchSlop = ViewConfiguration.get( getContext() ).getScaledTouchSlop();
		mMinFlingVelocity = ViewConfiguration.get( getContext() ).getScaledMinimumFlingVelocity();
		mMaxFlingVelocity = ViewConfiguration.get( getContext() ).getScaledMaximumFlingVelocity();
		
		// 初始化一个摩擦阻力的惯性滚动效果
		mFlingEffect = new EdgeBoundFlingEffect( 0.95f );
		// 惯性滚动效果可执行类，执行惯性滚动效果
		mFlingEffectRunnable = new Runnable() 
		{
			private static final float POSITION_TOLERANCE = 0.5F;
			
			@Override
			public void run() 
			{
				if ( getChildCount() > 0 )
				{
					mListTopStart = getChildAt( 0 ).getTop() - mListTopOffset;
					mTouchLastY = mListTopStart;
				}
				
				if ( curTouchState != TOUCH_STATE_SCROLL && curTouchState != TOUCH_STATE_FLING )
				{
					return;
				}
				// 根据时间更新速度和位置
				mFlingEffect.update( AnimationUtils.currentAnimationTimeMillis() );
				
				// 根据偏移量进行滚动
				int position = (int)mFlingEffect.getPosition();
				// 计算与上一次位置的增量值
				int delta = position - mTouchLastY;
				scrollList( position - mListTopStart, delta );
				mTouchLastY = position;
				
				// 如果没有达到最小速度，则一直滚动
				if ( !mFlingEffect.isStopScroll( mMinFlingVelocity, POSITION_TOLERANCE ) )
				{
					// 新的一帧进行调用
					postDelayed( this, 16 );
				}
				else
				{
					curTouchState = TOUCH_STATE_RESET;
				}
			}
		}; 
		
		mTopEdgeBoundEffect = new EdgeBoundEffect( getContext() );
		mBottomEdgeBoundEffect = new EdgeBoundEffect( getContext() );
	}

	@Override
	public ListAdapter getAdapter() 
	{
		return mAdapter;
	}

	/**
	 * 设置view适配
	 */
	@Override
	public void setAdapter(ListAdapter adapter) 
	{
		this.mAdapter = adapter;
		// 删除所有的view
		removeAllViewsInLayout();
	}

	@Override
	public View getSelectedView() 
	{
		return null;
	}

	@Override
	public void setSelection( int position ) 
	{
		
	}

	/**
	 * 布局
	 */
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) 
	{
		super.onLayout(changed, left, top, right, bottom);
		if ( null == mAdapter )
		{
			return;
		}
		
		if ( getChildCount() == 0 )
		{
			mLastItemPosition = -1;
			fillListDown( 0 );
		}
		else
		{
			int offset = mListTop + mListTopOffset - getChildAt( 0 ).getTop();
//			Log.e(VIEW_LOG_TAG, "getChildAt( 0 ).getTop() : " + getChildAt( 0 ).getTop() );
//			Log.e(VIEW_LOG_TAG, "offset : " + offset );
			// 删除不可见的view
			removeNonVisibleViews( offset );
			fillList( offset );
		}
		
		layoutChildren();
		adjustViewsUpOrDown();
	}
	
	/**
	 * 从offset开始向下布局
	 * @param offset
	 */
	protected void fillListDown( int offset )
	{
		int lastItemBottom = 0;
		int count = mAdapter.getCount();
		int height = getHeight();
		
		if ( getChildCount() != 0 )
		{
			lastItemBottom = getChildAt( getChildCount() - 1 ).getBottom();
		}
		
		while ( lastItemBottom + offset < height 
				&& mLastItemPosition < count - 1 )
		{
			View view = mAdapter.getView( mLastItemPosition + 1, getCachedView(), this );
			addAndMeasureChild( view, -1 );
			lastItemBottom += view.getMeasuredHeight() + ( mLastItemPosition + 1 != 0 ? Utils.dp2px( getContext(), 1 ) : 0 );
			++mLastItemPosition;
		}
	}
	
	/**
	 * 从offset开始向上布局
	 * @param offset
	 */
	protected void fillListUp( int offset )
	{
		if ( getChildCount() == 0)
		{
			return;
		}
		
		int firstItemTop = getChildAt( 0 ).getTop();
		while( firstItemTop + offset > 0 
			&& mFirstItemPosition > 0 )
		{
			View view = mAdapter.getView( mFirstItemPosition - 1, getCachedView(),  this );
			addAndMeasureChild( view, 0 );
			int viewHeight = (int) (view.getMeasuredHeight() + ( mFirstItemPosition - 1 != 0 ? Utils.dp2px( getContext(), 1 ) : 0 ));
			firstItemTop -= viewHeight;
			mListTopOffset -= viewHeight;
			--mFirstItemPosition;
		}
	}
	
	/**
	 * 从offset开始向下向上布局
	 * @param offset
	 */
	private void fillList( int offset )
	{
		fillListDown( offset );
		fillListUp( offset );
	}
	
	/**
	 * 删除不可见views
	 * @param offset
	 */
	protected void removeNonVisibleViews( int offset ) 
	{
		if ( getChildCount() == 0 )
		{
			return;
		}
		
		View firstView = getChildAt( 0 );
		View lastView = getChildAt( getChildCount() - 1 );

		if ( firstView != null && offset < 0 && firstView.getBottom() + offset < 0 )
		{
			removeViewInLayout( firstView );
			mCachedViews.add( firstView );
			mListTopOffset += firstView.getMeasuredHeight() + ( mFirstItemPosition != 0 ? Utils.dp2px( getContext(), 1 ) : 0 );
			++mFirstItemPosition;
			firstView = getChildAt( 0 );
		}
		if ( lastView != null && offset > 0 && lastView.getTop() + offset > getHeight() )
		{
			removeViewInLayout( lastView );
			mCachedViews.add( lastView );
			--mLastItemPosition;
			lastView = getChildAt( getChildCount() - 1 );
		}
	}
	
	/**
	 * 获取缓存view
	 * @return
	 */
	protected View getCachedView()
	{
		View view = null;
		if ( !mCachedViews.isEmpty() )
		{
			view = mCachedViews.pop();
		}
		return view;
	}
	
	/**
	 * 添加并测量child
	 * @param child
	 */
	protected void addAndMeasureChild( View child, int index )
	{
		LayoutParams params = child.getLayoutParams();
		if ( params == null )
		{
			params = new LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT );
		}
		addViewInLayout( child, index, params );
		int width = getWidth();
		child.measure( MeasureSpec.EXACTLY | width, MeasureSpec.UNSPECIFIED );
	}
	
	/**
	 * 布局child
	 */
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
	
	/**
     * 调整item的位置
     * 确保最顶部和最底部都能保持合适的距离
	 */
	private void adjustViewsUpOrDown()
	{
		View firstView = getChildAt( 0 );
		View lastView = getChildAt( getChildCount() - 1 );
		
		int offset = 0;
		// 底部超过最大目标位置坐标
		if ( mLastItemPosition == mAdapter.getCount() - 1
				&& lastView.getBottom() <= getHeight() ) 
		{
			offset = getHeight() - lastView.getBottom();
		}
        
		// 顶部超过最小目标位置坐标，进行回弹
		if ( mFirstItemPosition == 0 
				&& firstView.getTop() >= 0 )
		{
			offset = -firstView.getTop();
		}
		if ( offset != 0 )
		{
			offsetChildrenTopAndBottom( offset );
		}
	}
	
	/**
	 * 移动子view的top和bottom
	 * @param offset
	 */
	private void offsetChildrenTopAndBottom( int offset )
	{
		int childCount = getChildCount();
		
		for ( int i=0;i<childCount;i++ )
		{
			View child = getChildAt( i );
			child.layout( child.getLeft(), child.getTop() + offset, child.getRight(), child.getBottom() + offset );
		}
		
		mListTop += offset;
//		Log.e(VIEW_LOG_TAG, "offsetChildrenTopAndBottom-mListTop : " + mListTop );
//		Log.e(VIEW_LOG_TAG, "offsetChildrenTopAndBottom-childCount : " + childCount );
	}
	
	@Override
	public boolean onInterceptTouchEvent( MotionEvent event ) 
	{
		switch ( event.getAction() ) 
		{
			case MotionEvent.ACTION_MOVE:
				return startScrollIfNeeded( event );
		}
		return false;
	}
	
	/**
	 * 触摸
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) 
	{
		if ( getChildCount() == 0 )
		{
			return false;
		}
		switch ( event.getAction() ) 
		{
			case MotionEvent.ACTION_DOWN:
				onTouchDown( event );
				break;
			case MotionEvent.ACTION_MOVE:
				onTouchMove( event );
				break;
			case MotionEvent.ACTION_CANCEL:
				if ( curTouchState == TOUCH_STATE_CLICK )
				{
					removeLongClickCheck();
				}
				endTouch( 0 );
				break;
			case MotionEvent.ACTION_UP:
				float velocity = 0;
				if ( curTouchState == TOUCH_STATE_CLICK )
				{
					removeLongClickCheck();
					clickChildAt( (int)event.getX(), (int)event.getY() );
				}
				else if ( curTouchState == TOUCH_STATE_SCROLL )
				{
					mVelocityTracker.addMovement( event );
					mVelocityTracker.computeCurrentVelocity( PIXELS_PER_SECOND, mMaxFlingVelocity );
					velocity = mVelocityTracker.getYVelocity();
				}
				endTouch( velocity );
				break;
		}
		return true;
	}
	
	/**
	 * 触摸开始
	 * @param event
	 */
	private void onTouchDown( MotionEvent event )
	{
		// 记录第一次触摸的坐标位置
		mTouchStartX = (int) event.getX();
		mTouchStartY = (int) event.getY();
		
		mTouchLastY = mTouchStartY;
		// 记录第一个子view的top位置
		mListTopStart = getChildAt( 0 ).getTop() - mListTopOffset;
//		Log.e(VIEW_LOG_TAG, "mListTopStart : " + mListTopStart );
		
		if ( curTouchState == TOUCH_STATE_FLING )
		{
			curTouchState = TOUCH_STATE_SCROLL;
		}
		else
		{
			curTouchState = TOUCH_STATE_CLICK;
			// 长按检测
			startLongClickCheck();
			// 设置选中item状态
			positionSelector();
		}
		
		if ( mFlingEffectRunnable != null )
		{
			removeCallbacks( mFlingEffectRunnable );
		}
		
		if ( mVelocityTracker == null )
		{
			mVelocityTracker = VelocityTracker.obtain();
			mVelocityTracker.addMovement( event );
		}
		
		mFlingEffect.setMaxDestPosition( Float.MAX_VALUE );
		mFlingEffect.setMinDestPosition( -Float.MAX_VALUE );
	}
	
	/**
	 * 触摸移动
	 * @param event
	 */
	private void onTouchMove( MotionEvent event )
	{
		if ( curTouchState == TOUCH_STATE_CLICK )
		{
			startScrollIfNeeded( event );
		}
		
		if ( curTouchState == TOUCH_STATE_SCROLL )
		{
			mSelectorRect.setEmpty();
			invalidate();
			mVelocityTracker.addMovement( event );
			// 计算滑动的距离
			int y = (int)event.getY();
			int scrollDistance = y - mTouchStartY;
			// 计算与上一次位置的增量值
			int delta = y - mTouchLastY;
			scrollList( scrollDistance, delta );
			mTouchLastY = y;
		}
	}
	
	/**
	 * 设置选中item状态
	 */
	private void positionSelector()
	{
		mSelectedPosition = getContainChildIndex( mTouchStartX, mTouchStartY );
		if ( mSelectedPosition != INVALID_POSITION )
		{
			View sel = getChildAt( mSelectedPosition );
			mSelectorRect.set( sel.getLeft(), sel.getTop(), sel.getRight(), sel.getBottom() );
			invalidate( mSelectorRect );
		}
	}
	
	/**
	 * 根据需要是否进行滚动
	 * @param scrolledDistance
	 */
	private boolean startScrollIfNeeded( MotionEvent event )
	{
		// 计算滑动的距离
		int scrollDistance = (int)event.getY() - mTouchStartY;
		if ( Math.abs( scrollDistance ) > mTouchSlop )
		{
			curTouchState = TOUCH_STATE_SCROLL;
			removeLongClickCheck();
			return true;
		}
		
		return false;
	}
	
	/**
	 * 进行滚动
	 * @param scrolledDistance
	 * @param increamentDelta
	 */
	private void scrollList( int scrolledDistance, int increamentDelta )
	{
		// 重新计算List top的位置
		mListTop = mListTopStart + scrolledDistance;
//		Log.e(VIEW_LOG_TAG, "mListTopOffset : " + mListTopOffset );

		View firstView = getChildAt( 0 );
		View lastView = getChildAt( getChildCount() - 1 );
		int maxDistance = mListTop;
		if ( firstView != null && lastView != null )
		{
			maxDistance = firstView.getTop() - mListTopOffset - ( lastView.getBottom() - getHeight() );
		}
			
		final boolean cannotScrollDown = ( mFirstItemPosition == 0 &&
				firstView.getTop() >= 0 && scrolledDistance >= 0 );
        final boolean cannotScrollUp = ( mLastItemPosition == mAdapter.getCount() - 1 &&
        		lastView.getBottom() <= getHeight() && scrolledDistance <= 0);
        
		// 底部超过最大目标位置坐标，进行回弹
//		if ( mLastItemPosition == mAdapter.getCount() - 1
//				&& mListTop < maxDistance ) 
        if ( cannotScrollUp )
		{
        	// 进行底部约束发光特效
        	if ( mBottomEdgeBoundEffect != null )
        	{
        		mBottomEdgeBoundEffect.onPull( (float)increamentDelta / getHeight() );
        		invalidate( mBottomEdgeBoundEffect.getBounds( true ) );
        	}
        	if ( mTopEdgeBoundEffect != null && !mTopEdgeBoundEffect.isFinish() )
        	{
        		mTopEdgeBoundEffect.onRelease();
        	}
			mListTop = maxDistance;
			mTouchStartY = mTouchLastY;
			mFlingEffect.setMinDestPosition( maxDistance );
		}
        
		// 顶部超过最小目标位置坐标，进行回弹
//		if ( scrolledDistance > 0 
//				&& mListTop > 0 )
        if ( cannotScrollDown )
		{
        	// 进行顶部约束发光特效
        	if ( mTopEdgeBoundEffect != null )
        	{
        		mTopEdgeBoundEffect.onPull( (float)increamentDelta / getHeight() );
        		invalidate( mTopEdgeBoundEffect.getBounds( false ) );
        	}
        	if ( mBottomEdgeBoundEffect != null && !mBottomEdgeBoundEffect.isFinish() )
        	{
        		mBottomEdgeBoundEffect.onRelease();
        	}
			mListTop = 0;
			mTouchStartY = mTouchLastY;
			mFlingEffect.setMaxDestPosition( 0 );
		}
		
//		Log.e(VIEW_LOG_TAG, "mListTop : " + mListTop );
		// 请求重新绘制
		requestLayout();
	}
	
	/**
	 * 开始长按检测
	 */
	private void startLongClickCheck()
	{
		if( mLongClickRunnable == null )
		{
			mLongClickRunnable = new Runnable() 
			{
				@Override
				public void run() 
				{
					if ( curTouchState == TOUCH_STATE_CLICK )
					{
						int index = getContainChildIndex( mTouchStartX, mTouchStartY );
						curTouchState = TOUCH_STATE_RESET;
						longClickChildAt( index );
					}
				}
			};
		}
		if ( mLongClickRunnable != null )
		{
			postDelayed( mLongClickRunnable, ViewConfiguration.getLongPressTimeout() );
		}
	}
	
	/**
	 * 移除长按检测
	 */
	private void removeLongClickCheck()
	{
		if ( mLongClickRunnable != null )
		{
			removeCallbacks( mLongClickRunnable );
		}
	}
	
	/**
	 * 子item处理点击事件
	 * @param x
	 * @param y
	 */
	private void clickChildAt( int x, int y )
	{
		int index = getContainChildIndex( x, y );
		if ( index != INVALID_POSITION )
		{
			int position = mFirstItemPosition + index;
			long id = mAdapter.getItemId( position );
			performItemClick( getChildAt( index ), position, id );
		}
	}
	
	/**
	 * 子item处理长按事件
	 * @param index
	 */
	private void longClickChildAt( int index )
	{
		if ( index != INVALID_POSITION )
		{
			// 执行震动操作
			mVibrator.vibrate( vibrators, -1 );
			View view = getChildAt( index );
			int position = mFirstItemPosition + index;
			long id = mAdapter.getItemId( position );
			if ( getOnItemLongClickListener() != null )
			{
				getOnItemLongClickListener().onItemLongClick( this, view, position, id );
			}
		}
	}
	
	/**
	 * 根据触摸的坐标获得item的位置
	 * @param x
	 * @param y
	 */
	private int getContainChildIndex( int x, int y )
	{
		Rect rect = new Rect();
		int count = getChildCount();
		for ( int i=0;i<count;i++ )
		{
			View view = getChildAt( i );
			view.getHitRect( rect );
			if ( rect.contains( x, y ) )
			{
				return i;
			}
		}
		return INVALID_POSITION;
	}
	
	/**
	 * 触摸结束
	 */
	private void endTouch( float velocity )
	{
		mVelocityTracker.recycle();
		mVelocityTracker = null;
		
		if ( curTouchState == TOUCH_STATE_CLICK || curTouchState == TOUCH_STATE_RESET )
		{
			curTouchState = TOUCH_STATE_RESET;
			mSelectorRect.setEmpty();
			invalidate();
		}
		else
		{
			// 根据滚动速度进行惯性滚动
			if ( mFlingEffect != null )
			{
				curTouchState = TOUCH_STATE_FLING;
				mFlingEffect.setState( mListTop, velocity, AnimationUtils.currentAnimationTimeMillis() );
				post( mFlingEffectRunnable );
			}
		}
		
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) 
	{
		// 绘制选中器,绘制在item底部
		if ( !mSelectorRect.isEmpty() )
		{
			mSelector.setBounds( mSelectorRect );
			mSelector.draw( canvas );
		}
		super.dispatchDraw(canvas);
		
		// 绘制达到顶部拉动效果图
		if ( mTopEdgeBoundEffect != null && !mTopEdgeBoundEffect.isFinish() )
		{
			
            mTopEdgeBoundEffect.setSize( getWidth(), getHeight() );
            
			if( mTopEdgeBoundEffect.draw( canvas ) )
			{
				invalidate( mTopEdgeBoundEffect.getBounds( false ) );
			}
		}
		
		// 绘制达到底部拉动效果图
		if ( mBottomEdgeBoundEffect != null && !mBottomEdgeBoundEffect.isFinish() )
		{
			final int restoreCount = canvas.save();
			mBottomEdgeBoundEffect.setSize( getWidth(), getHeight() );
			canvas.translate( 0, getHeight() );
			canvas.rotate( 180, getWidth() / 2, 0 );
			if( mBottomEdgeBoundEffect.draw( canvas ) )
			{
				invalidate( mBottomEdgeBoundEffect.getBounds( true ) );
			}
			canvas.restoreToCount( restoreCount );
		}
	}
	
	@Override
	public void draw(Canvas canvas) 
	{
		super.draw(canvas);
		
		
	}
	
	@Override
	protected boolean drawChild( Canvas canvas, View child, long drawingTime ) 
	{
		return super.drawChild(canvas, child, drawingTime);
	}
}
