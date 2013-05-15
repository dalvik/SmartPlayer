package com.sky.drovik.player.media;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RelativeLayout;

import com.sky.drovik.player.R;
import com.sky.drovik.player.app.Res;

public class VideoActivity extends Activity {

	private VideoViewControl videoViewControl;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_video_view);
		RelativeLayout rootView = (RelativeLayout)findViewById(Res.id.root);
		Intent intent = getIntent();
		videoViewControl = new VideoViewControl(rootView, this, intent.getData()) {
	             
	        };
	}
}
