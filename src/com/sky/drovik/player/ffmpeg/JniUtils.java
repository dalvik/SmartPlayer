package com.sky.drovik.player.ffmpeg;

import android.graphics.Bitmap;

public class JniUtils {

	public final static int open_file_fail = -1;
	public final static int open_file_success = 0;
	public final static int	get_stream_info_fail = -2;
	public final static int	find_video_stream_fail = -3;
	public final static int unsurpport_codec = -4;
	public final static int open_codec_fail = - 5;
	public final static int bitmap_getinfo_error = -6;
	public final static int bitmap_lockpixels_error = -7;
	public final static int initialize_conversion_error = -8;
	public final static int decode_next_frame = 0;
	public final static int stream_read_over = -1;
	
	private JniUtils() {
	}

	// FFmpeg
	//open video file
	/*
		int Java_com_sky_drovik_player_ffmpeg_JniUtils_openVideoFile(JNIEnv * env, jobject this,jstring name) {

	 */
	public static native int openVideoFile(String fileName);
	
	public static native int[] getVideoResolution();
	
	public static native int drawFrame(Bitmap bitmap);
	
	public static native int close();
	
}
