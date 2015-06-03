package com.iguyue.canvas.abstracts;

public abstract class Dynamics 
{
	private static final int MAX_TIMESTEP = 50;
	protected float mPosition;
	protected float mVelocity;
	protected long mLastTime;
	
	public void setState( final float position, final float velocity, final long now )
	{
		this.mPosition = position;
		this.mVelocity = velocity;
		this.mLastTime = now;
	}

	public float getmPosition() 
	{
		return mPosition;
	}

	public float getmVelocity() 
	{
		return mVelocity;
	}
	
	public boolean isAtRest( final float velocityTolerance )
	{
		return Math.abs( mVelocity ) < velocityTolerance;
	}
	
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
	
	abstract protected void onUpdate( int dt );
}
