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

import java.util.Map;

import net.youmi.android.appoffers.CheckStatusNotifier;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.baidu.mobstat.StatService;
import com.sky.drovik.factory.DrovikRegisterFactory;
import com.sky.drovik.factory.IRegisterFoctory;
import com.sky.drovik.player.BuildConfig;
import com.sky.drovik.player.R;
import com.sky.drovik.player.app.Res;
import com.sky.drovik.player.ffmpeg.JniUtils;
import com.sky.drovik.views.FFGLSurfaceView;
import com.sky.drovik.views.FFSurfaceView;

public class VideoViewControl implements MediaPlayer.OnErrorListener,
		MediaPlayer.OnCompletionListener, CheckStatusNotifier {

	private static final String TAG = "MovieViewControl";
	private Map<String, String> mHeaders;
	private int mCurrentState = STATE_IDLE;
	private int mTargetState = STATE_IDLE;
	private static final int STATE_ERROR = -1;
	private static final int STATE_IDLE = 0;
	private static final int STATE_PREPARING = 1;
	private static final int STATE_PREPARED = 2;
	private static final int STATE_PLAYING = 3;
	private static final int STATE_PAUSED = 4;
	private static final int STATE_PLAYBACK_COMPLETED = 5;

	private static final int HALF_MINUTE = 30 * 1000;
	private static final int TWO_MINUTES = 4 * HALF_MINUTE;

	// Copied from MediaPlaybackService in the Music Player app. Should be
	// public, but isn't.
	private static final String SERVICECMD = "com.android.music.musicservicecommand";
	private static final String CMDNAME = "command";
	private static final String CMDPAUSE = "pause";

	private final FFSurfaceView mVideoSurfaceView;
	private static FFGLSurfaceView mFfglSurfaceView;
	
	private final View mProgressView;

	private RelativeLayout rootView;

	private final Uri mUri;
	private Context context;
	private IRegisterFoctory foctory = null;
	private boolean isDeviceInvalid;

	private static final boolean DEBUG = true;

	Handler mHandler = new Handler();

	public VideoViewControl(RelativeLayout rootView, Context context,
			Uri videoUri) {
		this.context = context;
		this.rootView = rootView;
		mVideoSurfaceView = (FFSurfaceView) rootView
				.findViewById(Res.id.surface_view);
		mFfglSurfaceView = (FFGLSurfaceView) rootView.findViewById(R.id.glsurface_video_view);
		mProgressView = rootView.findViewById(Res.id.progress_indicator);
		mUri = videoUri;
	/*	mVideoSurfaceView.setOnErrorListener(this);
		mVideoSurfaceView.setOnCompletionListener(this);
		mVideoSurfaceView.setMediaController(new VideoController(context), rootView, false);
		mVideoSurfaceView.setVideoURI(mUri);
		mVideoSurfaceView.requestFocus();

		Intent i = new Intent(SERVICECMD);
		i.putExtra(CMDNAME, CMDPAUSE);
		context.sendBroadcast(i);
		mVideoSurfaceView.start();
*/

/**/
		playVieoWithFFmpeg();
	}

	public void onPause() {
		mHandler.removeCallbacksAndMessages(null);
		StatService.onPause(context);
	}

	public void onResume() {
		StatService.onResume(context);
	}

	public boolean onError(MediaPlayer player, int arg1, int arg2) {
		mHandler.removeCallbacksAndMessages(null);
		mProgressView.setVisibility(View.GONE);
		// ªÒ»°◊¢≤·◊¥Ã¨ Œ¥◊¢≤·Ã· æ◊¢≤· “—◊¢≤· Ω‚¬Î
		StatService.onEvent(context, " ”∆µ≤•∑≈ ß∞‹", mUri.getPath());
		foctory = new DrovikRegisterFactory();
		showRegisterDialog(foctory.isRegister(context));
		//onCompletion();
		return true;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		StatService.onEvent(context, " ”∆µ≤•∑≈ÕÍ≥…", mUri.getPath());
		onCompletion();
	}

	public void onCompletion() {
		System.out.println("onCompletion");
	}

	public void onPlayError() {
		System.out.println("onPlayError");
	}

	private void showRegisterDialog(boolean status) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(Res.string.drovik_play_error_tips_str);
		builder.setMessage(status ? context
				.getString(Res.string.drovik_play_error_developer_str)
				: context
						.getString(Res.string.drovik_play_error_unsupport_format_str));
		if (status) {// no register
			builder.setPositiveButton(Res.string.drovik_play_goto_regester_str,
					new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							onPlayError();
							if (!isDeviceInvalid) {
								foctory.gotoRegister(context);
							} else {
								playVieoWithFFmpeg();
							}
						}
					});
			builder.setNegativeButton(
					status ? context
							.getString(Res.string.drovik_play_waitting_for_update_str)
							: context
									.getString(Res.string.drovik_play_cancle_regester_str),
					new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							onPlayError();
						}
					});
			builder.show();
		} else {// register
			playVieoWithFFmpeg();
		}
	}

	private void playVieoWithFFmpeg() {
		mFfglSurfaceView.setMediaController(new VideoController(context), rootView, true);
		int state = mFfglSurfaceView.setVideoURI(mUri);
		if(state != -1) {
			mVideoSurfaceView.setVisibility(View.GONE);
			mFfglSurfaceView.setVisibility(View.VISIBLE);
			rootView.requestLayout();
			rootView.invalidate();
			mFfglSurfaceView.requestFocus();
			mFfglSurfaceView.setUpRender();
			mFfglSurfaceView.start();
		}
		///int[] resulation = JniUtils.openVideoFile(mUri.getPath()); // "/mnt/sdcard/video.mp4"
		/*int res = 0;
		if (res == JniUtils.open_file_success) {
			System.out.println("res = " + res);
			System.out.println(resulation[0] + " --- " + resulation[1]);
			int den = resulation[2];
			int num = resulation[3];
			//final int frame_rate = den / num;
			JniUtils.decodeMedia();
			new Thread(new Runnable() {
				@Override
				public void run() {
					Looper.prepare();
					int i = JniUtils.display();
					onCompletion();
				}
			}).start();
		} else {
			switch (res) {
			case JniUtils.open_file_fail:
				StatService
						.onEvent(
								context,
								"≤•∑≈“Ï≥£",
								"¥ÌŒÛ¬Î£∫"
										+ res
										+ " "
										+ context
												.getString(R.string.drovik_play_ffmpeg_open_file_fail_str));
				if (BuildConfig.DEBUG) {
					Log.d(TAG,
							"≤•∑≈“Ï≥£, ¥ÌŒÛ¬Î£∫"
									+ res
									+ " "
									+ context
											.getString(R.string.drovik_play_ffmpeg_open_file_fail_str));
				}
				break;
			case JniUtils.get_stream_info_fail:
				// ToastUtils.showToast(context,
				// R.string.drovik_play_ffmpeg_get_stream_info_fail_str);
				StatService
						.onEvent(
								context,
								"≤•∑≈“Ï≥£",
								"¥ÌŒÛ¬Î£∫"
										+ res
										+ " "
										+ context
												.getString(R.string.drovik_play_ffmpeg_get_stream_info_fail_str));
				if (BuildConfig.DEBUG) {
					Log.d(TAG,
							"≤•∑≈“Ï≥£, ¥ÌŒÛ¬Î£∫"
									+ res
									+ " "
									+ context
											.getString(R.string.drovik_play_ffmpeg_get_stream_info_fail_str));
				}
				break;
			case JniUtils.find_video_stream_fail:
				// ToastUtils.showToast(context,
				// R.string.drovik_play_ffmpeg_find_video_stream_fail_str);
				StatService
						.onEvent(
								context,
								"≤•∑≈“Ï≥£",
								"¥ÌŒÛ¬Î£∫"
										+ res
										+ " "
										+ context
												.getString(R.string.drovik_play_ffmpeg_find_video_stream_fail_str));
				if (BuildConfig.DEBUG) {
					Log.d(TAG,
							"≤•∑≈“Ï≥£, ¥ÌŒÛ¬Î£∫"
									+ res
									+ " "
									+ context
											.getString(R.string.drovik_play_ffmpeg_find_video_stream_fail_str));
				}
				break;
			case JniUtils.unsurpport_codec:
				// ToastUtils.showToast(context,
				// R.string.drovik_play_ffmpeg_unsurpport_codec_str);
				StatService
						.onEvent(
								context,
								"≤•∑≈“Ï≥£",
								"¥ÌŒÛ¬Î£∫"
										+ res
										+ " "
										+ context
												.getString(R.string.drovik_play_ffmpeg_unsurpport_codec_str));
				if (BuildConfig.DEBUG) {
					Log.d(TAG,
							"≤•∑≈“Ï≥£, ¥ÌŒÛ¬Î£∫"
									+ res
									+ " "
									+ context
											.getString(R.string.drovik_play_ffmpeg_unsurpport_codec_str));
				}
				break;
			case JniUtils.open_codec_fail:
				// ToastUtils.showToast(context,
				// R.string.drovik_play_ffmpeg_open_codec_fail_str);
				StatService
						.onEvent(
								context,
								"≤•∑≈“Ï≥£",
								"¥ÌŒÛ¬Î£∫"
										+ res
										+ " "
										+ context
												.getString(R.string.drovik_play_ffmpeg_open_codec_fail_str));
				if (BuildConfig.DEBUG) {
					Log.d(TAG,
							"≤•∑≈“Ï≥£, ¥ÌŒÛ¬Î£∫"
									+ res
									+ " "
									+ context
											.getString(R.string.drovik_play_ffmpeg_open_codec_fail_str));
				}
				break;
			default:
				break;
			}
			ToastUtils.showToast(context,
					R.string.drovik_play_ffmpeg_open_file_fail_str);
			onCompletion();*/
	//	}
	}

	@Override
	public void onCheckStatusConnectionFailed(Context arg0) {

	}

	@Override
	public void onCheckStatusResponse(Context context, boolean isAppInvalid,
			boolean isInTestMode, boolean isDeviceInvalid) {
		this.isDeviceInvalid = isDeviceInvalid;
		if (BuildConfig.DEBUG && DEBUG) {
			Log.d(TAG, "isAppInvalid = " + isAppInvalid + "  isInTestMode = "
					+ isInTestMode + "  isDeviceInvalid = " + isDeviceInvalid);
		}
	}
	
	//public static void refresh() {
		//mFfglSurfaceView.invalid();
	//}

}
