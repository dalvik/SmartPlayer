package com.sky.drovik.player.media;

import java.io.IOException;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.MediaController;
import android.widget.RelativeLayout;

import com.sky.drovik.player.media.VideoController.VideoPlayerControl;

@SuppressLint("NewApi")
public class FFSurfaceView extends SurfaceView implements VideoPlayerControl {

	private String TAG = "VideoView";
	// settable by the client
	private Uri mUri;
	private Map<String, String> mHeaders;
	private int mDuration;

	private Context mContext;

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

	// All the stuff we need for playing and showing a video
	private SurfaceHolder mSurfaceHolder = null;
	private MediaPlayer mMediaPlayer = null;
	private int mVideoWidth;
	private int mVideoHeight;
	private int mSurfaceWidth;
	private int mSurfaceHeight;
	private OnCompletionListener mOnCompletionListener;
	private MediaPlayer.OnPreparedListener mOnPreparedListener;
	private int mCurrentBufferPercentage;
	private OnErrorListener mOnErrorListener;
	private OnInfoListener mOnInfoListener;
	private int mSeekWhenPrepared; // recording the seek position while
									// preparing
	private boolean mCanPause;
	private boolean mCanSeekBack;
	private boolean mCanSeekForward;

	private VideoController mVideoController;
	private RelativeLayout rootView;

	public FFSurfaceView(Context context) {
		super(context);
		mContext = context;
		initVideoView();
	}

	public FFSurfaceView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		mContext = context;
		initVideoView();
	}

	public FFSurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initVideoView();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// Log.i("@@@@", "onMeasure");
		int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
		int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
		if (mVideoWidth > 0 && mVideoHeight > 0) {
			if (mVideoWidth * height > width * mVideoHeight) {
				// Log.i("@@@", "image too tall, correcting");
				height = width * mVideoHeight / mVideoWidth;
			} else if (mVideoWidth * height < width * mVideoHeight) {
				// Log.i("@@@", "image too wide, correcting");
				width = height * mVideoWidth / mVideoHeight;
			} else {
				// Log.i("@@@", "aspect ratio is correct: " +
				// width+"/"+height+"="+
				// mVideoWidth+"/"+mVideoHeight);
			}
		}
		// Log.i("@@@@@@@@@@", "setting size: " + width + 'x' + height);
		setMeasuredDimension(width, height);
	}

	public int resolveAdjustedSize(int desiredSize, int measureSpec) {
		int result = desiredSize;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		switch (specMode) {
		case MeasureSpec.UNSPECIFIED:
			/*
			 * Parent says we can be as big as we want. Just don't be larger
			 * than max size imposed on ourselves.
			 */
			result = desiredSize;
			break;

		case MeasureSpec.AT_MOST:
			/*
			 * Parent says we can be as big as we want, up to specSize. Don't be
			 * larger than specSize, and don't be larger than the max size
			 * imposed on ourselves.
			 */
			result = Math.min(desiredSize, specSize);
			break;

		case MeasureSpec.EXACTLY:
			// No choice. Do what we are told.
			result = specSize;
			break;
		}
		return result;
	}

	private void initVideoView() {
		mVideoWidth = 0;
		mVideoHeight = 0;
		getHolder().addCallback(mSHCallback);
		getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		setFocusable(true);
		setFocusableInTouchMode(true);
		requestFocus();
		mCurrentState = STATE_IDLE;
		mTargetState = STATE_IDLE;
	}

	public void setVideoPath(String path) {
		setVideoURI(Uri.parse(path));
	}

	public void setVideoURI(Uri uri) {
		setVideoURI(uri, null);
	}

	public void setVideoURI(Uri uri, Map<String, String> headers) {
		mUri = uri;
		mHeaders = headers;
		mSeekWhenPrepared = 0;
		openVideo();
		requestLayout();
		invalidate();
	}

	private void openVideo() {
		if (mUri == null || mSurfaceHolder == null) {
			// not ready for playback just yet, will try again later
			return;
		}
		// Tell the music playback service to pause
		// TODO: these constants need to be published somewhere in the
		// framework.
		Intent i = new Intent("com.android.music.musicservicecommand");
		i.putExtra("command", "pause");
		mContext.sendBroadcast(i);

		// we shouldn't clear the target state, because somebody might have
		// called start() previously
		release(false);
		try {
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setOnPreparedListener(mPreparedListener);
			mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
			mDuration = -1;
			mMediaPlayer.setOnCompletionListener(mCompletionListener);
			mMediaPlayer.setOnErrorListener(mErrorListener);
			mMediaPlayer.setOnInfoListener(mOnInfoListener);
			mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
			mCurrentBufferPercentage = 0;
			mMediaPlayer.setDataSource(mContext, mUri, mHeaders);
			mMediaPlayer.setDisplay(mSurfaceHolder);
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mMediaPlayer.setScreenOnWhilePlaying(true);
			mMediaPlayer.prepareAsync();
			// we don't set the target state here either, but preserve the
			// target state that was there before.
			mCurrentState = STATE_PREPARING;
			attachMediaController();
		} catch (IOException ex) {
			Log.w(TAG, "Unable to open content: " + mUri, ex);
			mCurrentState = STATE_ERROR;
			mTargetState = STATE_ERROR;
			mErrorListener.onError(mMediaPlayer,
					MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
			return;
		} catch (IllegalArgumentException ex) {
			Log.w(TAG, "Unable to open content: " + mUri, ex);
			mCurrentState = STATE_ERROR;
			mTargetState = STATE_ERROR;
			mErrorListener.onError(mMediaPlayer,
					MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
			return;
		}
	}

	public void stopPlayback() {
		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
			mCurrentState = STATE_IDLE;
			mTargetState = STATE_IDLE;
		}
	}

    public void setMediaController(VideoController videoController, RelativeLayout view) {
        if (videoController != null) {
        	videoController.hide();
        }
        rootView = view;
        mVideoController = videoController;
        attachMediaController();
    }


	private void attachMediaController() {
		if (mMediaPlayer != null && mVideoController != null) {
			mVideoController.setMediaPlayer(this);
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
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mVideoController.show();
                } else {
                    start();
                    mVideoController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (!mMediaPlayer.isPlaying()) {
                    start();
                    mVideoController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                    || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                if (mMediaPlayer.isPlaying()) {
                    pause();
                    mVideoController.show();
                }
                return true;
            } else {
                toggleMediaControlsVisiblity();
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private boolean isInPlaybackState() {
        return (mMediaPlayer != null &&
                mCurrentState != STATE_ERROR &&
                mCurrentState != STATE_IDLE &&
                mCurrentState != STATE_PREPARING);
    }
    
    private void toggleMediaControlsVisiblity() {
        if (mVideoController.isShowing()) {
        	mVideoController.hide();
        } else {
        	mVideoController.show();
        }
    }

	@Override
	public void start() {
		if (isInPlaybackState()) {
            mMediaPlayer.start();
            mCurrentState = STATE_PLAYING;
        }
        mTargetState = STATE_PLAYING;
	}

	@Override
	public void pause() {
        if (isInPlaybackState()) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrentState = STATE_PAUSED;
            }
        }
        mTargetState = STATE_PAUSED;
	}

	@Override
	public int getDuration() {
        if (isInPlaybackState()) {
            if (mDuration > 0) {
                return mDuration;
            }
            mDuration = mMediaPlayer.getDuration();
            return mDuration;
        }
        mDuration = -1;
        return mDuration;
	}

	@Override
	public int getCurrentPosition() {
		if (isInPlaybackState()) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
	}

	@Override
	public void seekTo(int msec) {
		if (isInPlaybackState()) {
            mMediaPlayer.seekTo(msec);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = msec;
        }
	}

	@Override
	public boolean isPlaying() {
		 return isInPlaybackState() && mMediaPlayer.isPlaying();
	}

	@Override
	public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
	}

	@Override
	public boolean canPause() {
		return mCanPause;
	}

	@Override
	public boolean canSeekBackward() {
		return mCanSeekBack;
	}

	@Override
	public boolean canSeekForward() {
		return mCanSeekForward;
	}

    public void suspend() {
        release(false);
    }

    public void resume() {
        openVideo();
    }
    
	SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
		public void surfaceChanged(SurfaceHolder holder, int format, int w,
				int h) {
			mSurfaceWidth = w;
			mSurfaceHeight = h;
			boolean isValidState = (mTargetState == STATE_PLAYING);
			boolean hasValidSize = (mVideoWidth == w && mVideoHeight == h);
			if (mMediaPlayer != null && isValidState && hasValidSize) {
				if (mSeekWhenPrepared != 0) {
					seekTo(mSeekWhenPrepared);
				}
				start();
			}
		}

		public void surfaceCreated(SurfaceHolder holder) {
			mSurfaceHolder = holder;
			openVideo();
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// after we return from this we can't use the surface any more
			mSurfaceHolder = null;
			if (mVideoController != null)
				mVideoController.hide();
			release(true);
		}
	};

	private void release(boolean cleartargetstate) {
		if (mMediaPlayer != null) {
			mMediaPlayer.reset();
			mMediaPlayer.release();
			mMediaPlayer = null;
			mCurrentState = STATE_IDLE;
			if (cleartargetstate) {
				mTargetState = STATE_IDLE;
			}
		}
	}
	
	
	
	MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener =
	        new MediaPlayer.OnVideoSizeChangedListener() {
	            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
	                mVideoWidth = mp.getVideoWidth();
	                mVideoHeight = mp.getVideoHeight();
	                if (mVideoWidth != 0 && mVideoHeight != 0) {
	                    getHolder().setFixedSize(mVideoWidth, mVideoHeight);
	                    requestLayout();
	                }
	            }
	    };

	    MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
	        public void onPrepared(MediaPlayer mp) {
	            mCurrentState = STATE_PREPARED;

	            // Get the capabilities of the player for this stream
	            /*Metadata data = mp.getMetadata(MediaPlayer.METADATA_ALL,
	                                      MediaPlayer.BYPASS_METADATA_FILTER);

	            if (data != null) {
	                mCanPause = !data.has(Metadata.PAUSE_AVAILABLE)
	                        || data.getBoolean(Metadata.PAUSE_AVAILABLE);
	                mCanSeekBack = !data.has(Metadata.SEEK_BACKWARD_AVAILABLE)
	                        || data.getBoolean(Metadata.SEEK_BACKWARD_AVAILABLE);
	                mCanSeekForward = !data.has(Metadata.SEEK_FORWARD_AVAILABLE)
	                        || data.getBoolean(Metadata.SEEK_FORWARD_AVAILABLE);
	            } else {
	                mCanPause = mCanSeekBack = mCanSeekForward = true;
	            }*/

	            if (mOnPreparedListener != null) {
	                mOnPreparedListener.onPrepared(mMediaPlayer);
	            }
	            if (mVideoController != null) {
	            	mVideoController.setEnabled(true);
	            }
	            mVideoWidth = mp.getVideoWidth();
	            mVideoHeight = mp.getVideoHeight();

	            int seekToPosition = mSeekWhenPrepared;  // mSeekWhenPrepared may be changed after seekTo() call
	            if (seekToPosition != 0) {
	                seekTo(seekToPosition);
	            }
	            if (mVideoWidth != 0 && mVideoHeight != 0) {
	                //Log.i("@@@@", "video size: " + mVideoWidth +"/"+ mVideoHeight);
	                getHolder().setFixedSize(mVideoWidth, mVideoHeight);
	                if (mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
	                    // We didn't actually change the size (it was already at the size
	                    // we need), so we won't get a "surface changed" callback, so
	                    // start the video here instead of in the callback.
	                    if (mTargetState == STATE_PLAYING) {
	                        start();
	                        if (mVideoController != null) {
	                        	mVideoController.show();
	                        }
	                    } else if (!isPlaying() &&
	                               (seekToPosition != 0 || getCurrentPosition() > 0)) {
	                       if (mVideoController != null) {
	                           // Show the media controls when we're paused into a video and make 'em stick.
	                    	   mVideoController.show(0);
	                       }
	                   }
	                }
	            } else {
	                // We don't know the video size yet, but should start anyway.
	                // The video size might be reported to us later.
	                if (mTargetState == STATE_PLAYING) {
	                    start();
	                }
	            }
	        }
	    };

	    private MediaPlayer.OnCompletionListener mCompletionListener =
	        new MediaPlayer.OnCompletionListener() {
	        public void onCompletion(MediaPlayer mp) {
	            mCurrentState = STATE_PLAYBACK_COMPLETED;
	            mTargetState = STATE_PLAYBACK_COMPLETED;
	            if (mVideoController != null) {
	            	mVideoController.hide();
	            }
	            if (mOnCompletionListener != null) {
	                mOnCompletionListener.onCompletion(mMediaPlayer);
	            }
	        }
	    };

	    private MediaPlayer.OnErrorListener mErrorListener =
	        new MediaPlayer.OnErrorListener() {
	        public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
	            Log.d(TAG, "Error: " + framework_err + "," + impl_err);
	            mCurrentState = STATE_ERROR;
	            mTargetState = STATE_ERROR;
	            if (mVideoController != null) {
	            	mVideoController.hide();
	            }

	            /* If an error handler has been supplied, use it and finish. */
	            if (mOnErrorListener != null) {
	                if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
	                    return true;
	                }
	            }

	            /* Otherwise, pop up an error dialog so the user knows that
	             * something bad has happened. Only try and pop up the dialog
	             * if we're attached to a window. When we're going away and no
	             * longer have a window, don't bother showing the user an error.
	             */
	            if (getWindowToken() != null) {
	                Resources r = mContext.getResources();
	                int messageId = 0;

	                if (framework_err == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
	                   // messageId = com.android.internal.R.string.VideoView_error_text_invalid_progressive_playback;
	                } else {
	                    //messageId = com.android.internal.R.string.VideoView_error_text_unknown;
	                }

	                new AlertDialog.Builder(mContext)
	                        .setMessage(messageId)
	                        .setPositiveButton("err",
	                                new DialogInterface.OnClickListener() {
	                                    public void onClick(DialogInterface dialog, int whichButton) {
	                                        /* If we get here, there is no onError listener, so
	                                         * at least inform them that the video is over.
	                                         */
	                                        if (mOnCompletionListener != null) {
	                                            mOnCompletionListener.onCompletion(mMediaPlayer);
	                                        }
	                                    }
	                                })
	                        .setCancelable(false)
	                        .show();
	            }
	            return true;
	        }
	    };

	    private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener =
	        new MediaPlayer.OnBufferingUpdateListener() {
	        public void onBufferingUpdate(MediaPlayer mp, int percent) {
	            mCurrentBufferPercentage = percent;
	        }
	    };
	    
	    public void setOnPreparedListener(MediaPlayer.OnPreparedListener l)
	    {
	        mOnPreparedListener = l;
	    }

	    public void setOnCompletionListener(OnCompletionListener l)
	    {
	        mOnCompletionListener = l;
	    }

	    public void setOnErrorListener(OnErrorListener l)
	    {
	        mOnErrorListener = l;
	    }

	    public void setOnInfoListener(OnInfoListener l) {
	        mOnInfoListener = l;
	    }
}
