package com.sky.drovik.player.ffmpeg;


public class JniUtils {

	static {
		try {
			System.loadLibrary("ffmpeg");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
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

	public static native int[] openVideoFile(String fileName);
	
	public static native int[] getVideoResolution();

	public static native int decodeMedia();//start decode av thread
	
	public static native int display();//display video view
	
	public static native boolean canPlay();
	
	public static native boolean isPlaying();
	
	public static native int setPlay();
	
	public static native int setStop();
	
	public static native boolean canPause();
	
	public static native boolean isPause();
	
	public static native int setPause();
	
	public static native boolean canSeekForward();
	
	public static native boolean canSeekBackward();
	
	public static native int seekTo(int msec);
	
	public static native int getDuration();
	
	public static native int getCurrentPosition();
	
	public static native int close();
	
	//native gl 
	public static native void ffmpegGLResize(int w, int h);
	
	public static native void ffmpegGLRender();
	
	public static native void ffmpegGLClose();
	
	
}
