package com.iguyue.canvas.effect;

import android.util.Log;

/**
 * 滚动效果抽象类
 * @author moon
 *
 */
public abstract class AbScrollEffect 
{
	/**
	 * 最大时间
	 */
	private static final int MAX_TIMESTEP = 100;
	/**
	 * 当前位置
	 */
	protected float mPosition;
	/**
	 * 当前速度
	 */
	protected float mVelocity;
	/**
	 * 上次更新时间
	 */
	protected long mLastTime;
	
	protected float mMaxDestPosition = Float.MAX_VALUE;
	protected float mMinDestPosition = -Float.MAX_VALUE;
	
	protected float mMaxOffset;
	
	/**
	 * 设置状态
	 * @param position
	 * @param velocity
	 * @param now
	 */
	public void setState( final float position, final float velocity, final long now )
	{
		this.mPosition = position;
		this.mVelocity = velocity;
		this.mLastTime = now;
	}

	public float getPosition() 
	{
		return mPosition;
	}

	public float getVelocity() 
	{
		return mVelocity;
	}
	
	public void setVelocity(float mVelocity) {
		this.mVelocity = mVelocity;
	}

	/**
	 * 是否停止滚动
	 * @param velocityTolerance
	 * @return
	 */
	public boolean isStopScroll( final float velocityTolerance, final float positionTolerance )
	{
		final boolean standingStill = Math.abs(mVelocity) < velocityTolerance;
		final boolean withinMinLimits = mPosition + positionTolerance > mMinDestPosition;
		final boolean withinMaxLimits = mPosition - positionTolerance < mMaxDestPosition;
		return standingStill && withinMinLimits && withinMaxLimits;
	}
	
	/**
	 * 调用子类的onUpdate方法来更新位置和速度
	 * @param now
	 */
	public void update( final long now )
	{
		int dt = (int) (now - mLastTime);
		if ( dt > MAX_TIMESTEP )
		{
			dt = MAX_TIMESTEP;
		}
		onUpdate( dt );
		mLastTime = now;
	}
	
	/**
	 * 设置最大目标位置坐标
	 * @param maxDestPosition
	 */
	public void setMaxDestPosition(final float maxDestPosition) 
	{
		mMaxDestPosition = maxDestPosition;
	}
	
	/**
	 * 设置最小目标位置坐标
	 * @param maxDestPosition
	 */
	public void setMinDestPosition(final float minDestPosition) 
	{
		mMinDestPosition = minDestPosition;
	}
	
	/**
	 * 设置滑动最大，最小偏移量
	 * @param maxOffset
	 */
	public void setMaxOffset(final float maxOffset) 
	{
		this.mMaxOffset = maxOffset;
	}

	/**
	 * 获得超过最大，最小目标位置的反向距离
	 * 比最大目标位置大，比最小目标位置小
	 * @return
	 */
	protected float getReverseDistance()
	{
		float reverseDistance = 0;
		
		if ( mPosition > mMaxDestPosition ) 
		{
			reverseDistance = mMaxDestPosition - mPosition;
		} 
		else if ( mPosition < mMinDestPosition ) 
		{
			reverseDistance = mMinDestPosition - mPosition;
		}
//		Log.e( "mVelocity", "mMinDestPosition-mMaxDestPosition-reverseDistance : "
//				+ mMinDestPosition + "-" + mMaxDestPosition + "-" + reverseDistance );
		return reverseDistance;
	}
	
	/**
	 * 根据时间改变位置和速度，实现不同的滚动效果
	 * @param dt
	 */
	abstract protected void onUpdate( int dt );
}
