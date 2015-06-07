package com.iguyue.canvas.effect;

import com.iguyue.canvas.R;
import com.iguyue.canvas.common.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

/**
 * 滑动边界约束效果
 * 边界发光效果
 * @author moon
 *
 */
public class EdgeBoundEffect 
{
	/**
	 * 上下文
	 */
	private Context mContext;
	
	/**
	 * 发光图片
	 */
	private Drawable mGlowDrawable;
	/**
	 * 边界图片
	 */
	private Drawable mEdgeDrawable;
	
	/**
	 * 当前发光，边界图片scale以及alpha值
	 */
	private float mGlowScale;
	private float mGlowAlpha;
	private float mEdgeScale;
	private float mEdgeAlpha;
	
	/**
	 * 动画起始结束scale以及alpha值
	 */
	private float mGlowScaleStart;
	private float mGlowScaleFinish;
	private float mGlowAlphaStart;
	private float mGlowAlphaFinish;
	private float mEdgeScaleStart;
	private float mEdgeScaleFinish;
	private float mEdgeAlphaStart;
	private float mEdgeAlphaFinish;
	
	// 动画开始时间
	private long mStartTime;
	// 动画结束时间
	private long mDuration;
	
	/**
	 * 发光，边界图片尺寸以及特效最大高度
	 */
	private final int mGlowWidth;
	private final int mGlowHeight;
	private final int mEdgeHeight;
	private final int mMaxEffectHeight;
	
	/**
	 * 最小宽度
	 */
	private final static int MIN_WIDTH = 300;
	private final int mMinWidth;
	
	/**
	 * 绘制的宽度和高度
	 */
	private int mWidth;
	private int mHeight;
	
	private final Rect mBounds = new Rect();
	
	/**
	 * 动画执行插入器
	 */
	private final Interpolator mInterpolator;
	
	/**
	 * 状态
	 */
	private int mState;
	/**
	 * 空闲状态
	 */
	private final static int STATE_IDLE = 0; 
	/**
	 * 拉动状态
	 */
	private final static int STATE_PULL = 1;
	/**
	 * 拉动后衰弱状态
	 */
	private final static int STATE_PULL_DECAY = 2;
	/**
	 * 减弱状态
	 */
	private final static int STATE_RECEDE = 3;
	
	/**
	 * 拉动后发光图片的起始缩放
	 */
	private final static float PULL_GLOW_BEGIN_SCALE = 1.0f;
	
	/**
	 * 拉动后边界图片的起始透明度
	 */
	private final static float PULL_EDGE_BEGIN_ALPHA = 0.6f;
	/**
	 * 拉动后边界图片的起始缩放
	 */
	private final static float PULL_EDGE_BEGIN_SCALE = 0.5f;
	
	/**
	 * 最大透明度
	 */
	private final static float MAX_ALPHA = 1.0f;
	
	/**
	 * 最大边界图片缩放
	 */
	private final static float MAX_EDGE_SCALE = 1.0f;
	
	/**
	 * 最大发光图片缩放
	 */
	private final static float MAX_GLOW_SCALE = 5.0f;
	
	/**
	 * 缩放系数
	 */
	private final static float SCALE_FACTOR = 20.0f;
	/**
	 * 透明度洗漱
	 */
	private final static float ALPHA_FACTOR = 1.1f;
	
	/**
	 * 拉动动画执行时间
	 */
	private final static long PULL_TIME = 167;
	/**
	 * 拉动后衰减动画执行时间
	 */
	private final static long PULL_DECAT_TIME = 1000;
	/**
	 * 拉动以后减弱动画执行时间
	 */
	private final static long PULL_RECEDE_TIME = 1000;
	
	/**
	 * 拉动的距离
	 */
	private float mPullDistance;
	
	public EdgeBoundEffect( Context context )
	{
		mContext = context;
		Resources res = mContext.getResources();
		// 获取发光drawable
		mGlowDrawable = res.getDrawable( R.drawable.overscroll_glow );
		// 获取发光drawable
		mEdgeDrawable = res.getDrawable( R.drawable.overscroll_edge );
		
		mGlowWidth = mGlowDrawable.getIntrinsicWidth();
		mGlowHeight = mGlowDrawable.getIntrinsicHeight();
		mEdgeHeight = mEdgeDrawable.getIntrinsicHeight();
		
		mMaxEffectHeight = (int) ( Math.min( mGlowHeight * MAX_GLOW_SCALE * mGlowHeight / mGlowWidth * 0.6,
						                    mGlowHeight * MAX_GLOW_SCALE ) + 0.5 );
				
		mMinWidth = (int) Utils.dp2px( mContext, MIN_WIDTH );
		// 减速插入器
		mInterpolator = new DecelerateInterpolator();
	}
	
	/**
	 * 是否已经完成
	 */
	public boolean isFinish()
	{
		return mState == STATE_IDLE;
	}
	
	public void setSize( int width, int height )
	{
		mWidth = width;
		mHeight = height;
	}
	
	public Rect getBounds(boolean reverse) 
	{
        mBounds.set( 0, 0, mWidth, mMaxEffectHeight );
        if ( reverse )
        {
        	mBounds.offset( 0, mHeight - mMaxEffectHeight );
        }

        return mBounds;
    }
	
	/**
	 * 到了边界，继续拉动，进行边界发光效果
	 * @param delta
	 */
	public void onPull( float delta )
	{
		final long now = AnimationUtils.currentAnimationTimeMillis();
		if ( mState == STATE_PULL_DECAY && now - mStartTime < mDuration )
		{
			return;
		}
		
		if ( mState != STATE_PULL )
		{
			mGlowScale = PULL_GLOW_BEGIN_SCALE;
		}
		mState = STATE_PULL;
		
		// 记录动画开始时间和执行时间
		mStartTime = now;
		mDuration = PULL_TIME;
		
		// 总拉动的距离
		mPullDistance += delta;
		float distance = Math.abs( mPullDistance );
		
		// 通过拉动的距离进行边界图片动画起始和结束数值的计算，进行透明度和缩放的计算
		mEdgeAlpha = mEdgeAlphaStart = mEdgeAlphaFinish = 
				Math.max( PULL_EDGE_BEGIN_ALPHA, Math.min( distance, MAX_ALPHA ) );
		mEdgeScale = mEdgeScaleStart = mEdgeScaleFinish = 
				Math.max( PULL_EDGE_BEGIN_SCALE, Math.min( distance * SCALE_FACTOR, MAX_EDGE_SCALE ) );
	
		// 根据每次移动增加值进行发光图片动画起始和结束数值的计算，进行透明度和缩放的计算
		float glowDelta = Math.abs( delta );
		mGlowAlpha = mGlowAlphaStart = mGlowAlphaFinish = 
				Math.min( MAX_ALPHA, mGlowAlpha + glowDelta * ALPHA_FACTOR );
		if ( delta == 0 )
		{
			mGlowAlpha = 0;
		}
//		if ( ( delta > 0 && mPullDistance < 0 ) )
//		{
//			glowDelta = -glowDelta;
//		}
		mGlowScale = mGlowScaleStart = mGlowScaleFinish = 
				Math.min( MAX_GLOW_SCALE, mGlowScale + glowDelta * SCALE_FACTOR );
		
		Log.e( "onPull", "mPullDistance-delta: " + mPullDistance + "-" + delta );
	}
	
	/**
	 * 边界拉动以后，进行减弱释放
	 */
	public void onRelease()
	{
		mPullDistance = 0;
		
		if ( mState == STATE_PULL || mState == STATE_PULL_DECAY )
		{
			return;
		}
		
		mState = STATE_RECEDE;
		
		mStartTime = AnimationUtils.currentAnimationTimeMillis();
		mDuration = PULL_RECEDE_TIME;
		
		// 拉动以后，反向执行发光和边界图片减弱消失动画
		mEdgeAlphaStart = mEdgeAlpha;
		mEdgeScaleStart = mEdgeScale;
		mGlowAlphaStart = mGlowAlpha;
		mGlowScaleStart = mGlowScale;
		
		mEdgeAlphaFinish = 0.0f;
		mEdgeScaleFinish = 0.0f;
		mGlowAlphaFinish = 0.0f;
		mGlowScaleFinish = 0.0f;
	}
	
	public boolean draw( Canvas canvas )
	{
		update();
		// 设置当前发光图片的透明度
		mGlowDrawable.setAlpha( (int) ( Math.max( 0, Math.min( MAX_ALPHA , mGlowAlpha ) ) * 255 ) );
		
		int mGlowBottom = (int) Math.min( mGlowHeight * mGlowScale 
										* mGlowHeight / mGlowWidth * 0.6f, mMaxEffectHeight );
		
		// 如果绘制的宽度小于最小绘制宽度
		if ( mWidth < mMinWidth )
		{
			// 裁剪显示发光图片
			int mGlowLeft = ( mWidth - mMinWidth ) / 2;
			mGlowDrawable.setBounds( mGlowLeft, 0, mWidth - mGlowLeft, mGlowBottom );
		}
		else
		{
			mGlowDrawable.setBounds( 0, 0, mWidth, mGlowBottom );
		}
		
		mGlowDrawable.draw( canvas );
		
		// 设置当前边界图片的透明度
		mEdgeDrawable.setAlpha( (int) (Math.max( 0, Math.min( mEdgeAlpha, MAX_ALPHA ) ) * 255 ) );
		
		int mEdgeBottom = (int) ( mEdgeHeight * mEdgeScale );
		// 如果绘制的宽度小于最小绘制宽度
		if ( mWidth < mMinWidth )
		{
			int mEdgeLeft = ( mWidth - mMinWidth ) / 2;
			mEdgeDrawable.setBounds( mEdgeLeft, 0, mWidth - mEdgeLeft, mEdgeBottom );
		}
		else
		{
			mEdgeDrawable.setBounds( 0, 0, mWidth, mEdgeBottom );
		}
		
		mEdgeDrawable.draw( canvas );
		
		if ( mState == STATE_RECEDE && mGlowBottom == 0 && mEdgeBottom == 0 )
		{
			mState = STATE_IDLE;
		}
		
		return mState != STATE_IDLE;
	}
	
	/**
	 * 根据动画时间更新数值，以及更新当前状态
	 */
	private void update()
	{
		final long now = AnimationUtils.currentAnimationTimeMillis();
		final float t = Math.min( (float)( now - mStartTime ) / mDuration, 1.0f );
		
		final float interp = mInterpolator.getInterpolation( t );
		
		// 根据时间更新数值
		mEdgeAlpha = mEdgeAlphaStart + ( mEdgeAlphaFinish - mEdgeAlphaStart ) * interp;
		mEdgeScale = mEdgeScaleStart + ( mEdgeScaleFinish - mEdgeScaleStart ) * interp;
		mGlowAlpha = mGlowAlphaStart + ( mGlowAlphaFinish - mGlowAlphaStart ) * interp;
		mGlowScale = mGlowScaleStart + ( mGlowScaleFinish - mGlowScaleStart ) * interp;
		
//		Log.e( "update", "mGlowScale-mGlowAlpha-mEdgeScale-mEdgeAlpha: " + mGlowScale + "-" + mGlowAlpha + "-" + mEdgeScale + "-" + mEdgeAlpha );

		
		// 当前动画执行完成，进行状态改变
		if ( t >= 0.999f )
		{
			switch ( mState ) 
			{
			case STATE_PULL:
				mState = STATE_PULL_DECAY;
				
				mStartTime = AnimationUtils.currentAnimationTimeMillis();
				mDuration = PULL_DECAT_TIME;
				
				// 拉动之后，反向执行发光和边界图片衰减消失动画
				mEdgeAlphaStart = mEdgeAlpha;
				mEdgeScaleStart = mEdgeScale;
				mGlowAlphaStart = mGlowAlpha;
				mGlowScaleStart = mGlowScale;
				
				mEdgeAlphaFinish = 0.0f;
				mEdgeScaleFinish = 0.0f;
				mGlowAlphaFinish = 0.0f;
				mGlowScaleFinish = 0.0f;
				
				break;
			case STATE_PULL_DECAY:
				mState = STATE_RECEDE;
				
				// 衰减消失动画时,边缘慢慢减少超过发光
				float factor = mGlowScaleFinish != 0 ? 
						1 / ( mGlowScaleFinish * mGlowScaleFinish ) : Float.MAX_VALUE;
						
				mEdgeScale = mEdgeScaleStart + ( mEdgeScaleFinish - mEdgeScaleStart ) * interp * factor;
				break;
			case STATE_RECEDE:
				mState = STATE_IDLE;
				break;

			}
		}
	}
}
