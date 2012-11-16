package com.sky.drovik.player.media;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class TestActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = new Intent(this, MovieView.class);
		intent.setData(Uri.parse("/mnt/sdcard/03.avi"));
		startActivity(intent);
	}
}
