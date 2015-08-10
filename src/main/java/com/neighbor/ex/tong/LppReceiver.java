package com.neighbor.ex.tong;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LppReceiver extends BroadcastReceiver {

	public static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";

	public void onReceive(Context context, Intent intent) {

		if (intent.getAction().equals(ACTION_BOOT_COMPLETED)) {
			context.startService(new Intent(context, LppService.class));
		}
	}
}
