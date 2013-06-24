package com.sky.drovik.player.media;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.os.Build;
import android.os.Bundle;
import android.widget.RelativeLayout;

import com.sky.drovik.player.R;
import com.sky.drovik.player.app.Res;
import com.sky.drovik.views.FFGLSurfaceView;

public class VideoActivity extends Activity {

	private VideoViewControl videoViewControl;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_video_view);
		ActivityManager am =(ActivityManager) getSystemService(Context.ACTIVITY_SERVICE); 
		ConfigurationInfo info = am.getDeviceConfigurationInfo();     
		//final boolean supportsEs2 = info.reqGlEsVersion >= 0x20000;// || Build.FINGERPRINT.startsWith("generic");//.FINGERPRINT.startsWith(°∞generic°±);
		//System.out.println("supportsEs2 = " + supportsEs2);
		//ºŸ»Á «opengles 1.1 info.reqGlEsVersion= 0x00010001(65537)
		//System.out.println(info.reqGlEsVersion + " " + info.toString());
		RelativeLayout rootView = (RelativeLayout) findViewById(Res.id.root);
		Intent intent = getIntent();
		videoViewControl = new VideoViewControl(rootView, this,
				intent.getData()) {
			@Override
			public void onCompletion() {
				super.onCompletion();
				VideoActivity.this.finish();
			}

			@Override
			public void onPlayError() {
				super.onPlayError();
				VideoActivity.this.finish();
			}
		};
	}
	
	public void callBackRefresh(int msg) {
		/*VideoViewControl.refresh();
		if(msg == 2) {
			System.out.println("video play over!");
			videoViewControl.onCompletion();
			VideoActivity.this.finish();
		}*/
	}
}
