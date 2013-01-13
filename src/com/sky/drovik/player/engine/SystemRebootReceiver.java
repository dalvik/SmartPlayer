package com.sky.drovik.player.engine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.sky.drovik.player.service.SmartPlayerService;

public class SystemRebootReceiver extends BroadcastReceiver {

	private String REBOOT_ACTION = "android.intent.action.BOOT_COMPLETED";
	
	private final int reboot_action_msg = 1;
	
	private Handler myHandler = new Handler() {
	
		@Override
		public void handleMessage(Message message) {
			System.out.println(message);
			switch(message.what) {
			case reboot_action_msg:
					break;
			}
		}
	};
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent != null && REBOOT_ACTION.equals(intent.getAction())) {
			//myHandler.sendEmptyMessage(reboot_action_msg);
			System.out.println("reboot");
			Intent i = new Intent(context, SmartPlayerService.class);
			context.startService(i);
		}
	}

}
