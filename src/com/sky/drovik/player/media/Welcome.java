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
import android.widget.TextView;

import com.baidu.mobstat.StatService;
import com.sky.drovik.player.R;

public class Welcome extends Activity implements OnClickListener {

	private int[] welcom_bg = {R.drawable.welcome_one, R.drawable.welcome_two};
	
	private ImageView imageView = null;
	
	private TextView bottomInfo = null;
	
	private String showText = null;
	
	private int showTextLength = 0;
	
	private int count = 0;
	
	private int num = welcom_bg.length;
	
	private RelativeLayout layout = null;
	
	private int delay = 70;
	
	private boolean flag = true;
	
    int temp = 0;

    boolean control = true;

    private final static int FADE_IN = 0x02;
    
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
					startActivity(new Intent(Welcome.this, Main.class));
					overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);		
					Welcome.this.finish();
				}else {
					if(flag){
						handler.sendEmptyMessageDelayed(1, delay);
					}
				}
				break;

			case FADE_IN:
				if(temp<showTextLength){
                    temp++;
                    bottomInfo.setText(showText.subSequence(0, temp));
                    handler.sendEmptyMessageDelayed(FADE_IN, 100);
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
		showText = getText(R.string.welcome_bottom_tips_str).toString();
		showTextLength = showText.length();
		layout = (RelativeLayout) findViewById(R.id.welcom_bg_layout);
		layout.setOnClickListener(this);
		imageView = (ImageView) findViewById(R.id.welcome_bg);
		imageView.setAnimation(AnimationUtils.loadAnimation(this, R.anim.background_rotate));
		findViewById(R.id.welcome_top_info).setAnimation(AnimationUtils.loadAnimation(this, R.anim.welcome_toptext_rotate));
		bottomInfo = (TextView) findViewById(R.id.welcom_bottom_info);
		handler.sendEmptyMessage(1);
		//handler.sendEmptyMessageDelayed(FADE_IN, 10);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	
	@Override
	public void onClick(View v) {
		StatService.onEvent(Welcome.this, "欢迎界面", "点击欢迎界面", 1);
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
	
	@Override
	protected void onResume() {
		super.onResume();
		StatService.onResume(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPause(this);
	}
}
