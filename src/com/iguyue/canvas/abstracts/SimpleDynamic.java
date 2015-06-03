package com.iguyue.canvas.abstracts;

import android.util.Log;

/**
 * 使用了摩擦力模型来减小速度，位置使用标准的欧拉积分来通过速度得出，速度的单位是像素/秒，所以要除以1000。
 * @author moon
 *
 */
public class SimpleDynamic extends Dynamics 
{
	private float mFrictionFactor;
	
	
	public SimpleDynamic(float mFrictionFactor) 
	{
		this.mFrictionFactor = mFrictionFactor;
	}

	@Override
	protected void onUpdate(int dt) 
	{
		mPosition += mVelocity * dt / 1000;
		mVelocity *= mFrictionFactor;
		Log.e( "mVelocity", "mVelocity-" + mVelocity );
	}

}
