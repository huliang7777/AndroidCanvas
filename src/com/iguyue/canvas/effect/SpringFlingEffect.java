package com.iguyue.canvas.effect;

/**
 * 使用了摩擦力模型来减小速度，位置使用标准的欧拉积分来通过速度得出，速度的单位是像素/秒，所以要除以1000。
 * @author moon
 *
 */
public class SpringFlingEffect extends AbFlingEffect 
{
	/**
	 * 摩擦系数
	 */
	private float mFrictionFactor;
	
	private float mSnapFactor;
	
	
	public SpringFlingEffect( float mFrictionFactor, float mSnapFactor ) 
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
		mPosition += mVelocity * dt / 1000;
		
		// 达到最大，最小偏移后进行反向滚动
		if ( mPosition > mMaxDestPosition || mPosition < mMinDestPosition )
		{
			// 计算反向滚动速度
			mVelocity = getReverseDistance() * mSnapFactor;
		}
		
		// 逐步减少速度
		mVelocity *= mFrictionFactor;
	}

}
