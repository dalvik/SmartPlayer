package com.sky.drovik.views;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.sky.drovik.player.ffmpeg.JniUtils;
import com.sky.drovik.player.media.VideoController;

public class FFGLSurfaceView extends GLSurfaceView {
	// all possible internal states
	private static final int STATE_ERROR = -1;
	private static final int STATE_IDLE = 0;
	private static final int STATE_PREPARING = 1;
	private static final int STATE_PREPARED = 2;
	private static final int STATE_PLAYING = 3;
	private static final int STATE_PAUSED = 4;
	private static final int STATE_PLAYBACK_COMPLETED = 5;

	// mCurrentState is a VideoView object's current state.
	// mTargetState is the state that a method caller intends to reach.
	// For instance, regardless the VideoView object's current state,
	// calling pause() intends to bring the object to a target state
	// of STATE_PAUSED.
	private int mCurrentState = STATE_IDLE;
	private int mTargetState = STATE_IDLE;
	
	private VideoController mVideoController;
	private RelativeLayout rootView;
	
	public FFGLSurfaceView(Context context) {
		super(context);
		setUpRender();
	}
	
	public FFGLSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setUpRender();
	}


	private void setUpRender() {
		setEGLContextClientVersion(2);
		setRenderer(new MyRenderer());
		//setRenderMode(RENDERMODE_WHEN_DIRTY);
		setRenderMode(RENDERMODE_CONTINUOUSLY);
		setFocusable(true);
		setFocusableInTouchMode(true);
		requestFocus();
		mCurrentState = STATE_IDLE;
		mTargetState = STATE_IDLE;
	}


	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		JniUtils.ffmpegGLClose();
	}
	
	public void invalid() {
		//requestRender();
	}
	
    public void setMediaController(VideoController videoController, RelativeLayout view, boolean isFFmpeg) {
        if (videoController != null) {
        	videoController.hide();
        }
        rootView = view;
        mVideoController = videoController;
        mVideoController.setIfFFmpeg(isFFmpeg);
        attachMediaController();
        mCurrentState = STATE_PLAYING;
    }

	private void attachMediaController() {
		if (mVideoController != null) {
			//View anchorView = this.getParent() instanceof View ? (View)this.getParent() : this;
			if(rootView!=null){
				mVideoController.setAnchorView(rootView);
			}
			mVideoController.setEnabled(true);
		}
	}

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isInPlaybackState() && mVideoController != null) {
            toggleMediaControlsVisiblity();
        }
        return false;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        if (isInPlaybackState() && mVideoController != null) {
            toggleMediaControlsVisiblity();
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK &&
                                     keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
                                     keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
                                     keyCode != KeyEvent.KEYCODE_VOLUME_MUTE &&
                                     keyCode != KeyEvent.KEYCODE_MENU &&
                                     keyCode != KeyEvent.KEYCODE_CALL &&
                                     keyCode != KeyEvent.KEYCODE_ENDCALL;
        if (isInPlaybackState() && isKeyCodeSupported && mVideoController != null) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK ||
                    keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
            	mVideoController.show();
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                    mVideoController.hide();
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                    || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                mVideoController.show();
                return true;
            } else {
                toggleMediaControlsVisiblity();
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void toggleMediaControlsVisiblity() {
        if (mVideoController.isShowing()) {
        	mVideoController.hide();
        } else {
        	mVideoController.show();
        }
    }

    private boolean isInPlaybackState() {
        return ( mCurrentState != STATE_ERROR &&
                mCurrentState != STATE_IDLE &&
                mCurrentState != STATE_PREPARING);
    }
	
	class MyRenderer implements GLSurfaceView.Renderer {

		public MyRenderer() {
			
		}

		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig c) { 
			System.out.println("---onSurfaceCreated-----");
		}

		@Override
		public void onSurfaceChanged(GL10 gl, int w, int h) {
			JniUtils.ffmpegGLResize(w, h);
		}

		@Override
		public void onDrawFrame(GL10 gl) {
			JniUtils.ffmpegGLRender();
		}

	}
	
}
