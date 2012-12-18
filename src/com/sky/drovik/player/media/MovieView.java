package com.sky.drovik.player.media;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.VideoView;

import com.sky.drovik.player.R;
import com.sky.drovik.player.app.Res;

public class MovieView extends Activity {

	private VideoView videoView = null;
	
	private MovieViewControl movieViewControl;
	 
	private boolean finishOnCompletion;
	 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_movie_view);
        View rootView = findViewById(Res.id.root);
        Intent intent = getIntent();
        movieViewControl = new MovieViewControl(rootView, this, intent.getData()) {
            @Override
            public void onCompletion() {
                if (finishOnCompletion) {
                }
                MovieView.this.finish();
            }
            
            @Override
            public void onPlayError() {
            	MovieView.this.finish();
            }
        };
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.layout_video_list, menu);
        return true;
    }

    
}
