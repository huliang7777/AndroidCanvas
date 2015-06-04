package com.iguyue.canvas.effect;

import android.util.Log;

/**
 * 使用了摩擦力模型来减小速度，位置使用标准的欧拉积分来通过速度得出，速度的单位是像素/秒，所以要除以1000。
 * @author moon
 *
 */
public class FrictionScrollEffect extends AbScrollEffect 
{
	/**
	 * 摩擦系数
	 */
	private float mFrictionFactor;
	
	private float mSnapFactor;
	
	
	public FrictionScrollEffect( float mFrictionFactor, float mSnapFactor ) 
	{
		this.mFrictionFactor = mFrictionFactor;
		this.mSnapFactor = mSnapFactor;
	}

	/**
	 * 速度每次乘以一个摩擦系数,随着时间一点点减少
	 */
	@Override
	protected void onUpdate(int dt) 
	{
		// 计算是否进行反向滚动操作
		mVelocity += getReverseDistance() * mSnapFactor;
		
		mPosition += mVelocity * dt / 1000;
		
		// 设置滚动最大，最小偏移位置
		if ( mPosition > mMaxDestPosition + mMaxOffset )
		{
			mPosition = mMaxDestPosition + mMaxOffset;
		}
		else if ( mPosition < mMinDestPosition - mMaxOffset )
		{
			mPosition = mMinDestPosition - mMaxOffset;
		}
		
		// 逐步减少速度
		mVelocity *= mFrictionFactor;
		
//		Log.e( "mVelocity", "mVelocity : " + mVelocity );
	}

}
