package com.sky.drovik.player.media;

import java.util.Formatter;
import java.util.Locale;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.sky.drovik.player.R;

public class VideoController extends FrameLayout{

	private final static int VOICE_DOWN = 10;
	private final static int VOICE_UP = 20;
    private VideoPlayerControl  mPlayer;
    private Context             mContext;
    private View                mRoot;
    private ProgressBar         mProgress;
    private TextView            mEndTime, mCurrentTime;
    private boolean             mShowing;
    private boolean             mDragging;
    private static final int    sDefaultTimeout = 3000;
    private static final int    FADE_OUT = 1;
    private static final int    SHOW_PROGRESS = 2;
    //private boolean             mUseFastForward;
    StringBuilder               mFormatBuilder;
    Formatter                   mFormatter;
    private ImageButton         mPauseButton;
    //private ImageButton         mFfwdButton;
   // private ImageButton         mRewButton;
    private ImageButton         mNextButton;
    private ImageButton         mPrevButton;
    
    private ImageButton			mVoiceUpButton;
    private ImageButton			mVoiceDownButton;
    private AudioManager internalAm;
    private boolean directionProgress = false;//pre
    
    private boolean isFFmpeg = false;
    
    public VideoController(Context context) {
        this(context, true);
        internalAm = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }
    
    public VideoController(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRoot = this;
        mContext = context;
        internalAm = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public void onFinishInflate() {
        if (mRoot != null)
            initControllerView(mRoot);
    }

    public VideoController(Context context, boolean useFastForward) {
        super(context);
        mContext = context;
        internalAm = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        show(sDefaultTimeout);
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        show(sDefaultTimeout);
        return true;
    }
    
    public void hide() {
        if (mShowing) {
            try {
                mHandler.removeMessages(SHOW_PROGRESS);
                if(mRoot != null) {
                	mRoot.setVisibility(View.GONE);
                }
                //mWindowManager.removeView(mDecor);
            } catch (IllegalArgumentException ex) {
                Log.w("MediaController", "already removed");
            }
            mShowing = false;
        }
    }
    
    public void show() {
        show(sDefaultTimeout);
    }
    
    public boolean isShowing() {
        return mShowing;
    }
    
    public void show(int timeout) {
        if (!mShowing) {
            setProgress();
            if (mPauseButton != null) {
                mPauseButton.requestFocus();
            }
            //mWindowManager.addView(mDecor, mDecorLayoutParams);
            if(mRoot != null) {
            	mRoot.setVisibility(View.VISIBLE);
            }
            mShowing = true;
        }
        updatePausePlay();
        
        // cause the progress bar to be updated even if mShowing
        // was already true.  This happens, for example, if we're
        // paused with the progress bar showing the user hits play.
        mHandler.sendEmptyMessage(SHOW_PROGRESS);

        Message msg = mHandler.obtainMessage(FADE_OUT);
        if (timeout != 0) {
            mHandler.removeMessages(FADE_OUT);
            mHandler.sendMessageDelayed(msg, timeout);
        }
    }
    

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int pos;
            switch (msg.what) {
                case FADE_OUT:
                    hide();
                    break;
                case SHOW_PROGRESS:
                    pos = setProgress();
                    if(isFFmpeg) {
                    	if (!mDragging && mShowing) {
                    		msg = obtainMessage(SHOW_PROGRESS);
                    		sendMessageDelayed(msg, 1000 - (pos % 1000));
                    	}
                    }else {
                    	if (!mDragging && mShowing && mPlayer.isPlaying()) {
                    		msg = obtainMessage(SHOW_PROGRESS);
                    		sendMessageDelayed(msg, 1000 - (pos % 1000));
                    	}
                    }
                    break;
                case VOICE_UP:
                	internalAm.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_RAISE, 1);
                	show();
                	break;
                case VOICE_DOWN:
                	internalAm.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_LOWER, 1);
                	show();
                	break;
                	default:
                		break;
            }
        }
    };
    
    private String stringForTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours   = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }
    
    private int setProgress() {
    	int position = 0;
    	if(isFFmpeg) {
    		
    	}else {
    		if (mPlayer == null || mDragging) {
    			return 0;
    		}
    		position = mPlayer.getCurrentPosition();
    		int duration = mPlayer.getDuration();
    		if (mProgress != null) {
    			if (duration > 0) {
    				// use long to avoid overflow
    				long pos = 1000L * position / duration;
    				mProgress.setProgress( (int) pos);
    			}
    			int percent = mPlayer.getBufferPercentage();
    			mProgress.setSecondaryProgress(percent * 10);
    		}
    		
    		if (mEndTime != null)
    			mEndTime.setText(stringForTime(duration));
    		if (mCurrentTime != null)
    			mCurrentTime.setText(stringForTime(position));
    	}

        return position;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        final boolean uniqueDown = event.getRepeatCount() == 0
                && event.getAction() == KeyEvent.ACTION_DOWN;
        if (keyCode ==  KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keyCode == KeyEvent.KEYCODE_SPACE) {
            if (uniqueDown) {
                doPauseResume();
                show(sDefaultTimeout);
                if (mPauseButton != null) {
                    mPauseButton.requestFocus();
                }
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
        	if(isFFmpeg) {
        		if (uniqueDown) {
        			mPlayer.start();
        			updatePausePlay();
        			show(sDefaultTimeout);
        		}
        	}else {
        		if (uniqueDown && !mPlayer.isPlaying()) {
        			mPlayer.start();
        			updatePausePlay();
        			show(sDefaultTimeout);
        		}
        	}
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
        	if(isFFmpeg) {
        		
        	}else {
        		if (uniqueDown && mPlayer.isPlaying()) {
        			mPlayer.pause();
        			updatePausePlay();
        			show(sDefaultTimeout);
        		}
        	}
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE
                || keyCode == KeyEvent.KEYCODE_CAMERA) {
            // don't show the controls for volume adjustment
            return super.dispatchKeyEvent(event);
        } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
            if (uniqueDown) {
                hide();
            }
            return true;
        }

        show(sDefaultTimeout);
        return super.dispatchKeyEvent(event);
    }
    
    private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
            show(3600000);

            mDragging = true;

            // By removing these pending progress messages we make sure
            // that a) we won't update the progress while the user adjusts
            // the seekbar and b) once the user is done dragging the thumb
            // we will post one of these messages to the queue again and
            // this ensures that there will be exactly one message queued up.
            mHandler.removeMessages(SHOW_PROGRESS);
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (!fromuser) {
                // We're not interested in programmatically generated changes to
                // the progress bar's position.
                return;
            }
            if(isFFmpeg) {
            	
            }else {
            	long duration = mPlayer.getDuration();
            	long newposition = (duration * progress) / 1000L;
            	mPlayer.seekTo( (int) newposition);
            	if (mCurrentTime != null)
            		mCurrentTime.setText(stringForTime( (int) newposition));
            }
        }

        public void onStopTrackingTouch(SeekBar bar) {
            mDragging = false;
            setProgress();
            updatePausePlay();
            show(sDefaultTimeout);

            // Ensure that progress is properly updated in the future,
            // the call to show() does not guarantee this because it is a
            // no-op if we are already showing.
            mHandler.sendEmptyMessage(SHOW_PROGRESS);
        }
    };
    
    private View.OnClickListener mPauseListener = new View.OnClickListener() {
        public void onClick(View v) {
            doPauseResume();
            show(sDefaultTimeout);
        }
    };
    
    private void updatePausePlay() {
        if (mRoot == null || mPauseButton == null){
        	return;
        }
        if(isFFmpeg) {
        	
        }else{
        	if (mPlayer.isPlaying()) {
        		mPauseButton.setImageResource(R.drawable.ic_media_pause);
        	} else {
        		mPauseButton.setImageResource(R.drawable.ic_media_play);
        	}
        }
    }
    
    private void doPauseResume() {
    	if(isFFmpeg) {
    		
    	}else {
    		if (mPlayer.isPlaying()) {
    			mPlayer.pause();
    		} else {
    			mPlayer.start();
    		}
    	}
        updatePausePlay();
    }

    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    
    /*private View.OnClickListener mRewListener = new View.OnClickListener() {
        public void onClick(View v) {
            int pos = mPlayer.getCurrentPosition();
            pos -= 5000; // milliseconds
            mPlayer.seekTo(pos);
            setProgress();

            show(sDefaultTimeout);
        }
    };
    
    private View.OnClickListener mFfwdListener = new View.OnClickListener() {
        public void onClick(View v) {
            int pos = mPlayer.getCurrentPosition();
            pos += 15000; // milliseconds
            mPlayer.seekTo(pos);
            setProgress();

            show(sDefaultTimeout);
        }
    };*/
    
   
    private View.OnLongClickListener mPreLongListener = new View.OnLongClickListener() {
    	@Override
    	public boolean onLongClick(View v) {
    		directionProgress = false;
    		mHandler.post(updateProgressTask);
    		return false;
    	}
    };
   
    private View.OnLongClickListener mNextLongListener = new View.OnLongClickListener() {
    	@Override
    	public boolean onLongClick(View v) {
    		directionProgress = true;
    		mHandler.post(updateProgressTask);
    		return false;
    	}
    };
   

    private View.OnClickListener mVoiceUpListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			sendMessage(VOICE_UP);
		}
	};
	
	private View.OnClickListener mVoiceDownListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			sendMessage(VOICE_DOWN);
		}
	};
	
	
    private View.OnClickListener mVideoNextListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			show(sDefaultTimeout);
		}
	};
	
	private View.OnTouchListener mVideoPreOnTouchListener = new OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if(event.getAction() == MotionEvent.ACTION_UP) {
				mHandler.removeCallbacks(updateProgressTask);
			}
			return false;
		}
	};
	
	private View.OnTouchListener mVideoNextOnTouchListener = new OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if(event.getAction() == MotionEvent.ACTION_UP) {
				mHandler.removeCallbacks(updateProgressTask);
			}
			return false;
		}
	};
	
	private View.OnClickListener mVideoPreListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			show(sDefaultTimeout);
		}
	};
	
    
    private void initControllerView(View v) {
        mPauseButton = (ImageButton) v.findViewById(R.id.pause);
        if (mPauseButton != null) {
            mPauseButton.setOnClickListener(mPauseListener);
            mPauseButton.requestFocus();
        }

       /* mFfwdButton = (ImageButton) v.findViewById(R.id.bar_vol_down);
        if (mFfwdButton != null) {
            mFfwdButton.setOnClickListener(mFfwdListener);
            if (!mFromXml) {
                //mFfwdButton.setVisibility(mUseFastForward ? View.VISIBLE : View.GONE);
            }
        }

        mRewButton = (ImageButton) v.findViewById(R.id.bar_vol_up);
        if (mRewButton != null) {
            mRewButton.setOnClickListener(mRewListener);
            if (!mFromXml) {
                //mRewButton.setVisibility(mUseFastForward ? View.VISIBLE : View.GONE);
            }
        }*/
		mVoiceDownButton = (ImageButton) v.findViewById(R.id.bar_vol_down);
        if (mVoiceDownButton != null) {
        	mVoiceDownButton.setOnClickListener(mVoiceDownListener);
        }
		
        mVoiceUpButton = (ImageButton) v.findViewById(R.id.bar_vol_up);
        if (mVoiceUpButton != null) {
        	mVoiceUpButton.setOnClickListener(mVoiceUpListener);
        }
        // By default these are hidden. They will be enabled when setPrevNextListeners() is called 
        mNextButton = (ImageButton) v.findViewById(R.id.next);
        mPrevButton = (ImageButton) v.findViewById(R.id.prev);
        mPrevButton.setOnClickListener(mVideoPreListener);
        mPrevButton.setOnLongClickListener(mPreLongListener);
        mPrevButton.setOnTouchListener(mVideoPreOnTouchListener);
        mNextButton.setOnClickListener(mVideoNextListener);
        mNextButton.setOnTouchListener(mVideoNextOnTouchListener);
        mNextButton.setOnLongClickListener(mNextLongListener);
        mProgress = (ProgressBar) v.findViewById(R.id.mediacontroller_progress);
        if (mProgress != null) {
            if (mProgress instanceof SeekBar) {
                SeekBar seeker = (SeekBar) mProgress;
                seeker.setOnSeekBarChangeListener(mSeekListener);
            }
            mProgress.setMax(1000);
        }

        mEndTime = (TextView) v.findViewById(R.id.time);
        mCurrentTime = (TextView) v.findViewById(R.id.time_current);
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

        setFocusable(true);
        setFocusableInTouchMode(true);
        mPauseButton.requestFocus();
    }

    public void setMediaPlayer(VideoPlayerControl player) {
        mPlayer = player;
        updatePausePlay();
    }
    
    public void setAnchorView(RelativeLayout view) {
        FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        removeAllViews();
        View v = makeControllerView();
        view.addView(v,frameParams);
        //addView(v, frameParams);
        show();
    }

    protected View makeControllerView() {
        LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRoot = inflate.inflate(R.layout.layout_video_controller, null);
        initControllerView(mRoot);
        return mRoot;
    }
    
    private void sendMessage(int i) {
        Message m = Message.obtain(mHandler);
        m.what = i;
        m.sendToTarget();
    }
    
    private Runnable updateProgressTask = new Runnable() {
		
		@Override
		public void run() {
			int pos = mPlayer.getCurrentPosition();
			if(directionProgress) {
				pos += 5000; // milliseconds
			}else{
				pos -= 5000; // milliseconds
			}
            mPlayer.seekTo(pos);
            setProgress();
            show(sDefaultTimeout);
			mHandler.postDelayed(updateProgressTask, 500);
		}
	};
    
    public boolean isIfFFmpeg() {
		return isFFmpeg;
	}

	public void setIfFFmpeg(boolean isFFmpeg) {
		this.isFFmpeg = isFFmpeg;
	}


	public interface VideoPlayerControl {
        void    start();
        void    pause();
        int     getDuration();
        int     getCurrentPosition();
        void    seekTo(int pos);
        boolean isPlaying();
        int     getBufferPercentage();
        boolean canPause();
        boolean canSeekBackward();
        boolean canSeekForward();
    }
}
