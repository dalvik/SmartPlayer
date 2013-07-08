/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sky.drovik.player.media;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Video;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.baidu.mobstat.StatService;
import com.drovik.utils.StringUtils;
import com.drovik.utils.ToastUtils;
import com.sky.drovik.factory.DrovikRegisterFactory;
import com.sky.drovik.factory.IRegisterFoctory;
import com.sky.drovik.player.BuildConfig;
import com.sky.drovik.player.R;
import com.sky.drovik.player.app.Res;
import com.sky.drovik.player.ffmpeg.JniUtils;

public class MovieViewControl implements MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    @SuppressWarnings("unused")
    private static final String TAG = "MovieViewControl";

    private static final int HALF_MINUTE = 30 * 1000;
    private static final int TWO_MINUTES = 4 * HALF_MINUTE;

    // Copied from MediaPlaybackService in the Music Player app. Should be
    // public, but isn't.
    private static final String SERVICECMD = "com.android.music.musicservicecommand";
    private static final String CMDNAME = "command";
    private static final String CMDPAUSE = "pause";

    private final VideoView mVideoView;
    private final View mProgressView;
    
    private final Uri mUri;
    private final ContentResolver mContentResolver;
    private Context context;
    private IRegisterFoctory foctory = null;
    private boolean isDeviceInvalid;
    
    private static final boolean DEBUG = true;

    //add image video view
    private static ImageView videoImageView = null;
    
    Handler mHandler = new Handler();

    Runnable mPlayingChecker = new Runnable() {
        public void run() {
            if (mVideoView.isPlaying()) {
                mProgressView.setVisibility(View.GONE);
            } else {
                mHandler.postDelayed(mPlayingChecker, 250);
            }
        }
    };
    
    private static Handler myHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
				videoImageView.invalidate();
		};
	};
    
      static class MyMediaController extends MediaController {

      AudioManager internalAm;
      
      View.OnClickListener mClickListenerInternal = new View.OnClickListener() {
                        
            public void onClick(View v) {
                // TODO Auto-generated method stub
                switch(v.getId()) {
                    case R.id.bar_vol_up:
                        sendMessage(1);
                        break;
                    case R.id.bar_vol_down:
                        sendMessage(2);
                        break;
                        default:
                        	break;
                }
                show(3000);              
            }
        };
        
        private void sendMessage(int i) {
            Message m = Message.obtain(mHandler);
            m.arg1 = i;
            m.sendToTarget();
        }
        
        Handler mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch(msg.arg1) {
                    case 1:
                        internalAm.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_RAISE, 1);
                        break;
                    case 2:
                        internalAm.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_LOWER, 1);
                        break;
                    default:
                        break;
                }
            }
            
        };
        
        public MyMediaController(Context context) {
            super(context);
            // TODO Auto-generated constructor stub
            internalAm = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        }

        @Override
        public void setAnchorView(View view) {
            // TODO Auto-generated method stub
            super.setAnchorView(view);
            /*LayoutInflater inflate = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View newView = inflate.inflate(R.layout.bar, null);
            DisplayMetrics outDm = new DisplayMetrics();
            ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(outDm);
            FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                    ViewGroup.LayoutParams.FILL_PARENT,
                    Gravity.CENTER_HORIZONTAL
            );
            addView(newView, frameParams);
            newView.findViewById(R.id.bar_vol_up).setOnClickListener(mClickListenerInternal);
            newView.findViewById(R.id.bar_vol_down).setOnClickListener(mClickListenerInternal);*/
        }        
    }
    
    public MovieViewControl(View rootView, Context context, Uri videoUri) {
    	this.context = context;
        mContentResolver = context.getContentResolver();
        mVideoView = (VideoView) rootView.findViewById(Res.id.surface_view);
        mProgressView = rootView.findViewById(Res.id.progress_indicator);
        videoImageView = (ImageView) rootView.findViewById(Res.id.video_image_view);
        mUri = videoUri;

        // For streams that we expect to be slow to start up, show a
        // progress spinner until playback starts.
        String scheme = mUri.getScheme();
        if ("http".equalsIgnoreCase(scheme) || "rtsp".equalsIgnoreCase(scheme)) {
            mHandler.postDelayed(mPlayingChecker, 250);
        } else {
            mProgressView.setVisibility(View.GONE);
        }

        /*mVideoView.setOnErrorListener(this);
        mVideoView.setOnCompletionListener(this);
        mVideoView.setVideoURI(mUri);
        mVideoView.setMediaController(new MyMediaController(context));

        // make the video view handle keys for seeking and pausing
        mVideoView.requestFocus();

        Intent i = new Intent(SERVICECMD);
        i.putExtra(CMDNAME, CMDPAUSE);
        context.sendBroadcast(i);

        final Integer bookmark = getBookmark();
        if (bookmark != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(Res.string.resume_playing_title);
            builder.setMessage(String.format(context.getString(Res.string.resume_playing_message), StringUtils.formatDuration(context, bookmark)));
            builder.setOnCancelListener(new OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    onCompletion();
                }
            });
            builder.setPositiveButton(Res.string.resume_playing_resume, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    mVideoView.seekTo(bookmark);
                    mVideoView.start();
                }
            });
            builder.setNegativeButton(Res.string.resume_playing_restart, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    mVideoView.start();
                }
            });
            builder.show();
        } else {
            //mVideoView.start();
        }*/
        playVieoWithFFmpeg();
    }

    private static boolean uriSupportsBookmarks(Uri uri) {
        String scheme = uri.getScheme();
        String authority = uri.getAuthority();
        return ("content".equalsIgnoreCase(scheme) && MediaStore.AUTHORITY.equalsIgnoreCase(authority));
    }

    private Integer getBookmark() {
        if (!uriSupportsBookmarks(mUri)) {
            return null;
        }

        String[] projection = new String[] { Video.VideoColumns.DURATION, Video.VideoColumns.BOOKMARK };

        try {
            Cursor cursor = mContentResolver.query(mUri, projection, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        int duration = getCursorInteger(cursor, 0);
                        int bookmark = getCursorInteger(cursor, 1);
                        if ((bookmark < HALF_MINUTE) || (duration < TWO_MINUTES)
                                || (bookmark > (duration - HALF_MINUTE))) {
                            return null;
                        }
                        return Integer.valueOf(bookmark);
                    }
                } finally {
                    cursor.close();
                }
            }
        } catch (SQLiteException e) {
            // ignore
        }

        return null;
    }

    private static int getCursorInteger(Cursor cursor, int index) {
        try {
            return cursor.getInt(index);
        } catch (SQLiteException e) {
            return 0;
        } catch (NumberFormatException e) {
            return 0;
        }

    }

    private void setBookmark(int bookmark, int duration) {
        if (!uriSupportsBookmarks(mUri)) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(Video.VideoColumns.BOOKMARK, Integer.toString(bookmark));
        values.put(Video.VideoColumns.DURATION, Integer.toString(duration));
        try {
            mContentResolver.update(mUri, values, null, null);
        } catch (SecurityException ex) {
            // Ignore, can happen if we try to set the bookmark on a read-only
            // resource such as a video attached to GMail.
        } catch (SQLiteException e) {
            // ignore. can happen if the content doesn't support a bookmark
            // column.
        } catch (UnsupportedOperationException e) {
            // ignore. can happen if the external volume is already detached.
        }
    }

    public void onPause() {
        mHandler.removeCallbacksAndMessages(null);
        setBookmark(mVideoView.getCurrentPosition(), mVideoView.getDuration());
        StatService.onPause(context);
       // mVideoView.suspend();
    }

    public void onResume() {
        //mVideoView.resume();
    	StatService.onResume(context);
    }

    public void onDestroy() {
        mVideoView.stopPlayback();
    }

    public boolean onError(MediaPlayer player, int arg1, int arg2) {
        mHandler.removeCallbacksAndMessages(null);
        mProgressView.setVisibility(View.GONE);
        //ªÒ»°◊¢≤·◊¥Ã¨ Œ¥◊¢≤·Ã· æ◊¢≤· “—◊¢≤·  Ω‚¬Î
        StatService.onEvent(context, " ”∆µ≤•∑≈ ß∞‹", mUri.getPath());
        foctory = new DrovikRegisterFactory();
        showRegisterDialog(foctory.isRegister(context));
        return true;
    }

    @Override
	public void onCompletion(MediaPlayer mp) {
		StatService.onEvent(context, " ”∆µ≤•∑≈ÕÍ≥…", mUri.getPath());
        onCompletion();
    }

	public void onCompletion() {
		
	}
	
	public void onPlayError() {
		
	}

    private void showRegisterDialog(boolean status) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(Res.string.drovik_play_error_tips_str);
        builder.setMessage(status?context.getString(Res.string.drovik_play_error_developer_str):context.getString(Res.string.drovik_play_error_unsupport_format_str));
        if(status){//no register
        	builder.setPositiveButton(Res.string.drovik_play_goto_regester_str, new OnClickListener() {
        		public void onClick(DialogInterface dialog, int which) {
        			onPlayError();
        			if(!isDeviceInvalid) {
        				foctory.gotoRegister(context);
        			} else {
        				playVieoWithFFmpeg();
        			}
        		}
        	});
        	builder.setNegativeButton(status?context.getString(Res.string.drovik_play_waitting_for_update_str):context.getString(Res.string.drovik_play_cancle_regester_str), new OnClickListener() {
        		public void onClick(DialogInterface dialog, int which) {
        			onPlayError();
        		}
        	});
        	builder.show();
        }else {// register
        	playVieoWithFFmpeg();
        }
    }
    
    private void playVieoWithFFmpeg() {
    	int res = JniUtils.openVideoFile(mUri.getPath()); //"/mnt/sdcard/video.mp4"
    	if(res == JniUtils.open_file_success) {
    		System.out.println("res = " + res );
    		//int[] resulation = JniUtils.getVideoResolution(); 
    		//final Bitmap mBitmap = Bitmap.createBitmap(resulation[0],  resulation[1], Bitmap.Config.ARGB_8888);
    		final Bitmap mBitmap = Bitmap.createBitmap(512, 384, Bitmap.Config.ARGB_8888);
    		mVideoView.setVisibility(View.GONE);
    		videoImageView.setVisibility(View.VISIBLE);
    		videoImageView.setImageBitmap(mBitmap);
    		//System.out.println(resulation[0] +  " --- " + resulation[1]); 
    		//int den = resulation[2]; 
    		//int num = resulation[3]; 
    		//final int frame_rate = den/num;
    		new Thread(new Runnable() {
    			@Override 
    			public void run() { 
    				Looper.prepare();
    				int i = JniUtils.display(mBitmap);
    				System.out.println("i=" + i);
    				/*while((i===0) { 
    					try {
    						Thread.sleep(frame_rate); 
    					}
    					catch (InterruptedException e) { 
    						// TODOAuto-generated catch block 
    						e.printStackTrace();
    					}
    				}*/
    				//JniUtils.close();
    				onCompletion();
    			}
    		}).start();
    	}else {
    		switch(res) {
    		case JniUtils.open_file_fail:
    			StatService.onEvent(context, "≤•∑≈“Ï≥£", "¥ÌŒÛ¬Î£∫" + res + " " + context.getString(R.string.drovik_play_ffmpeg_open_file_fail_str));
    			if(BuildConfig.DEBUG) {
    				Log.d(TAG, "≤•∑≈“Ï≥£, ¥ÌŒÛ¬Î£∫" + res + " " + context.getString(R.string.drovik_play_ffmpeg_open_file_fail_str));
    			}
    			break;
    		case JniUtils.get_stream_info_fail:
    			//ToastUtils.showToast(context, R.string.drovik_play_ffmpeg_get_stream_info_fail_str);
    			StatService.onEvent(context, "≤•∑≈“Ï≥£", "¥ÌŒÛ¬Î£∫" + res + " " + context.getString(R.string.drovik_play_ffmpeg_get_stream_info_fail_str));
    			if(BuildConfig.DEBUG) {
    				Log.d(TAG, "≤•∑≈“Ï≥£, ¥ÌŒÛ¬Î£∫" + res + " " + context.getString(R.string.drovik_play_ffmpeg_get_stream_info_fail_str));
    			}
    			break;
    		case JniUtils.find_video_stream_fail:
    			//ToastUtils.showToast(context, R.string.drovik_play_ffmpeg_find_video_stream_fail_str);
    			StatService.onEvent(context, "≤•∑≈“Ï≥£", "¥ÌŒÛ¬Î£∫" + res + " " + context.getString(R.string.drovik_play_ffmpeg_find_video_stream_fail_str));
    			if(BuildConfig.DEBUG) {
    				Log.d(TAG, "≤•∑≈“Ï≥£, ¥ÌŒÛ¬Î£∫" + res + " " + context.getString(R.string.drovik_play_ffmpeg_find_video_stream_fail_str));
    			}
    			break;
    		case JniUtils.unsurpport_codec:
    			//ToastUtils.showToast(context, R.string.drovik_play_ffmpeg_unsurpport_codec_str);
    			StatService.onEvent(context, "≤•∑≈“Ï≥£", "¥ÌŒÛ¬Î£∫" + res + " " + context.getString(R.string.drovik_play_ffmpeg_unsurpport_codec_str));
    			if(BuildConfig.DEBUG) {
    				Log.d(TAG, "≤•∑≈“Ï≥£, ¥ÌŒÛ¬Î£∫" + res + " " + context.getString(R.string.drovik_play_ffmpeg_unsurpport_codec_str));
    			}
    			break;
    		case JniUtils.open_codec_fail:
    			//ToastUtils.showToast(context, R.string.drovik_play_ffmpeg_open_codec_fail_str);
    			StatService.onEvent(context, "≤•∑≈“Ï≥£", "¥ÌŒÛ¬Î£∫" + res + " " + context.getString(R.string.drovik_play_ffmpeg_open_codec_fail_str));
    			if(BuildConfig.DEBUG) {
    				Log.d(TAG, "≤•∑≈“Ï≥£, ¥ÌŒÛ¬Î£∫" + res + " " + context.getString(R.string.drovik_play_ffmpeg_open_codec_fail_str));
    			}
    			break;
    			default:
    				break;
    		}
    		ToastUtils.showToast(context, R.string.drovik_play_ffmpeg_open_file_fail_str);
    		onCompletion();
    	}
    }
    

	public static void refresh() {
		myHandler.sendEmptyMessage(1);
	}

}
