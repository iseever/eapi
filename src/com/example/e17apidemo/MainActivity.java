package com.example.e17apidemo;

import java.util.List;

import com.navisoft.navi.service.DGConnection;
import com.shinco.Event.GetDeviceStatus;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {

	private final static String TAG = "VehicleInfo";
	private Context mContext;
	private Button mReflesh;
	private TextView mKeyStatus, mCarSpeed, mMileage, mRoutetype,
			mProductModel;

	/** 创建对象 */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mReflesh = (Button) findViewById(R.id.reflesh);
		mKeyStatus = (TextView) findViewById(R.id.txt_KeyStatus);
		mCarSpeed = (TextView) findViewById(R.id.txt_speed);
		mMileage = (TextView) findViewById(R.id.txt_mileage);
		mRoutetype = (TextView) findViewById(R.id.txt_routetype);
		mProductModel = (TextView) findViewById(R.id.txt_productModel);
		mReflesh.setOnClickListener(this);
		mContext = getBaseContext();

		/** 绑定服务 */
		SysService.setIVlistener(mIVinfoListener);
		int key = SysService.getCurVehicleKeyStatus();
		mKeyStatus.setText("" + key);
		int mileage = SysService.getCarMileage();
		if (mileage != -1) {
			mKeyStatus.setText("" + "点火");
			mMileage.setText("" + mileage + "km");
		} else {
			mMileage.setText("获取里程失败！");
		}
	}

	private IVinfoListener mIVinfoListener = new IVinfoListener() {

		@Override
		public void fireStatus(boolean fire) {
			// TODO Auto-generated method stub
			String sFire = fire ? "点火" : " 熄火";
			mKeyStatus.setText("" + sFire);
		}

		@Override
		public void carMileage(int mileage) {
			// TODO Auto-generated method stub
			mMileage.setText("" + mileage + "km");
		}

		@Override
		public void drivingInfoSpeed(int speed) {
			// TODO Auto-generated method stub
			mCarSpeed.setText("" + speed + "km/h");
		}

		@Override
		public void requestFail() {
			// TODO Auto-generated method stub
			mMileage.setText("获取里程失败！");
		}

		@Override
		public void roadType(int type) {
			// TODO Auto-generated method stub
			mRoutetype.setText("" + type);
		}
	};

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.reflesh:
			// 型号
			String model = DGConnection.getInstance(getBaseContext())
					.getProductModel();
			mProductModel.setText("" + model);
			SysService.MainRequestCarMileage();
			break;

		default:
			break;
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/**
	 * 里程和车钥匙状态获取
	 */
	private String getCarInfo() {
		/**
		 * 获取行驶里程数; -1:里程无效
		 */
		int Mileage = GetDeviceStatus.getMileage();

		// 需移位处理才是真实的里程数; 系数为：0.015625f
		Mileage = ((Mileage & 0xFFFFFFFF) >> 6);
		/**
		 * 获取车钥匙状态,车钥匙状态有四种(0x0:钥匙初始位置,0x01:,0x02:,0x03:点火位置) -1:无效
		 */
		int status = GetDeviceStatus.getVehicleKeyStatus();

		String info = "当前里程:" + Mileage + "Km," + "当前车钥匙状态:" + status;

		Log.i(TAG, info);
		return info;
	}

}
