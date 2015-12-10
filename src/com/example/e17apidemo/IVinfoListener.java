package com.example.e17apidemo;

public interface IVinfoListener {

	public abstract void fireStatus(boolean fire);

	public abstract void carMileage(int mileage);

	public abstract void drivingInfoSpeed(int speed);

	public abstract void requestFail();

	public abstract void roadType(int type);
}
