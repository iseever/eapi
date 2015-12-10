package com.example.e17apidemo;

import java.util.List;

import com.navisoft.navi.service.DGConnection;
import com.navisoft.navi.service.NaviListener;
import com.shinco.Event.DeviceListener;
import com.shinco.Event.GetDeviceStatus;
import com.shinco.Interface.IDeviceListener.IInfoListener;
import com.shinco.Interface.IDeviceListener.IStatusListener;
import com.shinco.Service.SysConnection;
import com.shinco.Service.SysError;
import com.shinco.Service.SysListener;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class SysService extends Service {

	private static final String TAG = "SysService";

	private static IVinfoListener mIVinfoListener = null;
	private static Context mContext;
	private boolean bNaviBind = false;
	private static boolean bServiceCreate = false;
	static Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0x01:
				boolean bFire = (msg.arg1 == 0x01) ? true : false;
				if (mIVinfoListener != null) {
					Log.d(TAG, "bFire:" + bFire);
					SysService.this.mIVinfoListener.fireStatus(bFire);
				} else {
					Log.d(TAG, "mIVinfoListener is null");
				}
				break;
			case 0x02:
				int speed = msg.arg1;
				if (SysService.this.mIVinfoListener != null) {
					Log.d(TAG, "speed:" + speed);
					SysService.this.mIVinfoListener.drivingInfoSpeed(speed);
				} else {
					Log.d(TAG, "mIVinfoListener is null");
				}
				break;
			case 0x03:
				int type = msg.arg1;
				if (SysService.this.mIVinfoListener != null) {
					Log.d(TAG, "type:" + type);
					SysService.this.mIVinfoListener.roadType(type);
				} else {
					Log.d(TAG, "mIVinfoListener is null");
				}
				break;
			case 0x04:
				int mileage = msg.arg1;
				if (SysService.this.mIVinfoListener != null) {
					Log.d(TAG, "mileage:" + mileage);
					SysService.this.mIVinfoListener.carMileage(mileage);
				} else {
					Log.d(TAG, "mIVinfoListener is null");
				}
				break;
			case 0x05:
				if (SysService.this.mIVinfoListener != null) {
					SysService.this.mIVinfoListener.requestFail();
				} else {
					Log.d(TAG, "mIVinfoListener is null");
				}
				break;
			default:
				break;
			}
			super.handleMessage(msg);

		}
	};

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mContext = getBaseContext();
		registerSysInfoReceiver();
		SysConnection.getInstance(getBaseContext()).prepare(mSysListener);
		DGConnection.getInstance(getBaseContext()).prepare(mNaviListener);
	}

	public static Runnable mRequestCarMileage = new Runnable() {

		@Override
		public void run() {
			requestCarMileage();
		}
	};

	public static void MainRequestCarMileage() {
		mHandler.removeCallbacks(mRequestCarMileage);
		mHandler.postDelayed(mRequestCarMileage, 500L);
	}

	private SysListener mSysListener = new SysListener() {

		@Override
		public void onSysInited(int arg0) {
			if (arg0 == SysError.Bind_OK) {
				Log.d(TAG, "Sys Inited bind ok");
				// �������
				mHandler.removeCallbacks(mRequestCarMileage);
				mHandler.postDelayed(mRequestCarMileage, 500L);
			} else if (arg0 == SysError.Bind_Null) {
				Log.d(TAG, "Sys Inited bind null");
				SysConnection.getInstance(getBaseContext()).initService();
			}
		}
	};

	private NaviListener mNaviListener = new NaviListener() {

		@Override
		public void onSysInited(int arg0) {
			if (arg0 == SysError.Bind_OK) {
				Log.d(TAG, "Navi Inited bind ok");
				SysService.this.bNaviBind = true;
				int type = DGConnection.getInstance(getBaseContext())
						.getRoadType();
				SysService.this.mHandler.sendMessage(SysService.this.mHandler
						.obtainMessage(0x03, type, 0x0));
			} else if (arg0 == SysError.Bind_Null) {
				Log.d(TAG, "Navi Inited bind null");
				SysService.this.bNaviBind = false;
			}
		}
	};

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unRegisterSysInfoReceiver();
		SysConnection.getInstance(getBaseContext()).destroy();
		DGConnection.getInstance(getBaseContext()).destroy();
	}

	public static void setIVlistener(IVinfoListener l) {
		mIVinfoListener = l;
	}

	private DeviceListener dl = null;
	private static int mCurVehicleKeyStatus = 0x0;
	private int mLastVehicleKeyStatus = 0x0;
	private static int mCarMileage = -1;

	/**
	 * register
	 */
	public void registerSysInfoReceiver() {

		if (dl == null) {

			dl = new DeviceListener(this);
			// Կ��״̬
			// ��ʼֵ
			mCurVehicleKeyStatus = GetDeviceStatus.getVehicleKeyStatus();
			dl.onVehicleKeyStatusChange(new IStatusListener() {

				@Override
				public void onChange(int arg0) {

					/**
					 * ��Կ��״̬������(0x0:Կ�׳�ʼλ��,0x01:,0x02:,0x03:���λ��)
					 */
					mLastVehicleKeyStatus = mCurVehicleKeyStatus;
					mCurVehicleKeyStatus = arg0;

					// ���
					if (0x03 == mLastVehicleKeyStatus
							&& 0x02 == mCurVehicleKeyStatus) {
						SysService.this.mHandler
								.sendMessage(SysService.this.mHandler
										.obtainMessage(0x01, 0x01, 0x0));
						Log.i(TAG, "���");
					}

					// Ϩ��
					if ((0x02 == mLastVehicleKeyStatus && 0x00 == mCurVehicleKeyStatus)
							|| (0x02 == mLastVehicleKeyStatus && 0x01 == mCurVehicleKeyStatus)) {
						SysService.this.mHandler
								.sendMessage(SysService.this.mHandler
										.obtainMessage(0x01, 0x02, 0x0));
						Log.i(TAG, "Ϩ��");
					}
				}
			});

			// �г��ٶ� (km/h)
			dl.onDrivingInfoCome(new IInfoListener() {

				@Override
				public void onInfoCome(Object arg0) {

					DrivingInfo drivingInfo = (DrivingInfo) arg0;

					SysService.this.mHandler.sendMessage(SysService.this.mHandler
							.obtainMessage(0x02, drivingInfo.speed, 0x0));
					Log.i(TAG, "��ǰ����:" + drivingInfo.speed + "km/h");

					/**
					 * ��ȡ��ǰ��·����
					 * 
					 * ��Чֵ(-1)��δƥ�䵽��·(0)����·����(1-����Ҫ��· ,2-��Ҫ��·, 3-��Ҫ��·, 4-ʡ��
					 * ,5-���� ,6-���п���, 7-����)
					 */
					if (SysService.this.bNaviBind) {
						int type = DGConnection.getInstance(getBaseContext())
								.getRoadType();
						SysService.this.mHandler
								.sendMessage(SysService.this.mHandler
										.obtainMessage(0x03, type, 0x0));
					} else if (CheckServiceRunning(mContext,
							Common.NaviClassName)) {
						DGConnection.getInstance(getBaseContext())
								.initService();
					}

				}

			});

		}

	}

	/**
	 * unregister
	 */
	public void unRegisterSysInfoReceiver() {
		if (dl != null) {
			dl.clearListener();
			dl = null;
		}

	}

	public static int getCarMileage() {
		return mCarMileage;
	}

	public static int getCurVehicleKeyStatus() {
		return mCurVehicleKeyStatus;
	}

	/**
	 * ���������� ----�ڿ�������ʱ��������󵽵���̲�Ϊ-1���������Ϊ�Ѿ���𣻴���������ڳ����Ѿ�������������δ�����������
	 */
	public static void requestCarMileage() {
		// �������
		int count = 3;
		mCarMileage = -1;

		while (mCarMileage == -1 && count > 0) {
			mCarMileage = GetDeviceStatus.getMileage();
			if (mCarMileage == -1) {
				SysConnection.getInstance(mContext).requestCarMileage();
				try {
					Thread.sleep(500L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			count--;
		}

		// ��ȡ��ʻ�����
		if (mCarMileage != -1) {
			mCarMileage = ((mCarMileage & 0xFFFFFFFF) >> 6);
			mHandler.sendMessage(mHandler.obtainMessage(0x04, mCarMileage, 0x0));
			Log.i(TAG, "��ǰ���:" + mCarMileage + "Km");
		} else {
			mHandler.sendEmptyMessage(0x05);
		}

		// ����ʱ���������-1������Ϊ���Ѿ����
		if (mCarMileage != -1) {
			mCurVehicleKeyStatus = 0x02;
			mHandler.sendMessage(mHandler.obtainMessage(0x01, 0x01, 0x0));
			Log.i(TAG, "���");
		}
	}

	// ��鵼�������Ƿ�����
	private static boolean CheckServiceRunning(Context mContext,
			String className) {
		boolean isRunning = false;
		ActivityManager activityManager = (ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> serviceList = activityManager
				.getRunningServices(100);
		if (!(serviceList.size() > 0)) {
			return false;
		}
		for (int i = 0; i < serviceList.size(); i++) {

			if (serviceList.get(i).service.getClassName().equals(className) == true) {
				isRunning = true;
				break;
			}
		}
		return isRunning;
	}
}
