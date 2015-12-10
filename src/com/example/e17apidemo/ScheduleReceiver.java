package com.example.e17apidemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ScheduleReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub

		Log.i("ScheduleReceiver", "RECEIVE_BOOT_COMPLETED");
		Intent service = new Intent(arg0, SysService.class);
		service.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
		arg0.startService(service);
	}

}
