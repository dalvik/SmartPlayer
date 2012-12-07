package com.sky.drovik.player.media;


import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.sky.drovik.player.R;

public class Welcome extends Activity implements OnClickListener {

	private int[] welcom_bg = {R.drawable.welcome_one, R.drawable.welcome_two};
	
	private ImageView imageView = null;
	
	
	private int count = 0;
	
	private int num = welcom_bg.length;
	
	private RelativeLayout layout = null;
	
	private int delay = 70;
	
	private boolean flag = true;
	
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				layout.setBackgroundResource(welcom_bg[count++%num]);
				if(count == 45) {
					handler.removeMessages(1);
					flag = false;
					startActivity(new Intent(Welcome.this, MovieList.class));
					Welcome.this.finish();
				}else {
					if(flag){
						handler.sendEmptyMessageDelayed(1, delay);
					}
				}
				break;

			default:
				break;
			}
			
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_welcome);
		layout = (RelativeLayout) findViewById(R.id.welcom_bg_layout);
		layout.setOnClickListener(this);
		imageView = (ImageView) findViewById(R.id.welcom_bg);
		imageView.setAnimation(AnimationUtils.loadAnimation(this, R.anim.background_rotate));
		handler.sendEmptyMessage(1);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	
	@Override
	public void onClick(View v) {
		Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(getText(R.string.welcome_bottom_tips_str).toString()));
		startActivity(intent);
	}
	
	public static Bitmap createTxtImage(String txt, int txtSize) {
		Bitmap mbmpTest = Bitmap.createBitmap(txt.length() * txtSize + 4,
				txtSize + 4, Config.ARGB_8888);
		Canvas canvasTemp = new Canvas(mbmpTest);
		Paint p = new Paint();
		p.setAntiAlias(true);
		p.setColor(Color.BLACK);
		p.setTextSize(txtSize);
		canvasTemp.drawText(txt, 2, txtSize - 2, p);
		return mbmpTest;
	}
}
