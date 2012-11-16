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
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Video;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import com.sky.drovik.player.R;
import com.sky.drovik.player.app.Res;

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
    //private final View mProgressView;
    private final Uri mUri;
    private final ContentResolver mContentResolver;
    Handler mHandler = new Handler();

    Runnable mPlayingChecker = new Runnable() {
        public void run() {
            if (mVideoView.isPlaying()) {
                //mProgressView.setVisibility(View.GONE);
            } else {
                mHandler.postDelayed(mPlayingChecker, 250);
            }
        }
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
                // TODO Auto-generated method stub
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
            
            LayoutInflater inflate = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View newView = inflate.inflate(R.layout.bar, null);
            DisplayMetrics outDm = new DisplayMetrics();
            ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(outDm);
            FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(
                    (int)outDm.density * 355,
                    ViewGroup.LayoutParams.FILL_PARENT,
                    Gravity.CENTER_HORIZONTAL
            );
            addView(newView, frameParams);
            newView.findViewById(R.id.bar_vol_up).setOnClickListener(mClickListenerInternal);
            newView.findViewById(R.id.bar_vol_down).setOnClickListener(mClickListenerInternal);
        }        
    }
    
    public static String formatDuration(final Context context, int durationMs) {
        int duration = durationMs / 1000;
        int h = duration / 3600;
        int m = (duration - h * 3600) / 60;
        int s = duration - (h * 3600 + m * 60);
        String durationValue;
        if (h == 0) {
            durationValue = String.format(context.getString(Res.string.details_ms), m, s);
        } else {
            durationValue = String.format(context.getString(Res.string.details_hms), h, m, s);
        }
        return durationValue;
    }

    public MovieViewControl(View rootView, Context context, Uri videoUri) {
        mContentResolver = context.getContentResolver();
        mVideoView = (VideoView) rootView.findViewById(Res.id.surface_view);
        //mProgressView = rootView.findViewById(Res.id.progress_indicator);

        mUri = videoUri;

        // For streams that we expect to be slow to start up, show a
        // progress spinner until playback starts.
        String scheme = mUri.getScheme();
        if ("http".equalsIgnoreCase(scheme) || "rtsp".equalsIgnoreCase(scheme)) {
            mHandler.postDelayed(mPlayingChecker, 250);
        } else {
            //mProgressView.setVisibility(View.GONE);
        }

        mVideoView.setOnErrorListener(this);
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
            builder
                    .setMessage(String
                            .format(context.getString(Res.string.resume_playing_message), formatDuration(context, bookmark)));
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
            mVideoView.start();
        }
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

       // mVideoView.suspend();
    }

    public void onResume() {
       // mVideoView.resume();
    }

    public void onDestroy() {
        mVideoView.stopPlayback();
    }

    public boolean onError(MediaPlayer player, int arg1, int arg2) {
        mHandler.removeCallbacksAndMessages(null);
        //mProgressView.setVisibility(View.GONE);
        return false;
    }

    public void onCompletion(MediaPlayer mp) {
        onCompletion();
    }

    public void onCompletion() {
        
    }
}
