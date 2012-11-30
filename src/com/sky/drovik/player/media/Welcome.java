package com.sky.drovik.player.media;


import com.sky.drovik.player.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class Welcome extends Activity {

	private int[] welcom_bg = {R.drawable.welcome_one, R.drawable.welcome_two};
	
	private ImageView imageView = null;
	
	private Handler handler = null;
	
	private int count = 0;
	
	private int num = welcom_bg.length;
	
	private LinearLayout layout = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_welcome);
		layout = (LinearLayout) findViewById(R.id.welcom_bg_layout);
		imageView = (ImageView) findViewById(R.id.welcom_bg);
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case 1:
					layout.setBackgroundResource(welcom_bg[count++%num]);
					if(count>30) {
						startActivity(new Intent(Welcome.this, MovieList.class));
						Welcome.this.finish();
					}else {
						handler.sendEmptyMessageDelayed(1, 100);
					}
					break;

				default:
					break;
				}
				
			}
		};
		handler.sendEmptyMessage(1);
	}
}
