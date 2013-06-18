package com.sky.drovik.player.engine;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.sky.drovik.player.R;
import com.sky.drovik.player.bitmapfun.ImageCache;
import com.sky.drovik.player.pojo.MovieInfo;

public class ImageLoaderTask extends AsyncTask<MovieInfo, Void, Bitmap> {

	private MovieInfo imageInfo;
	
	private final WeakReference<ImageView> imageViewReference; // ·ÀÖ¹ÄÚ´æÒç³ö

	private Context context;
	
	private ImageCache mImageCache;
	
	public ImageLoaderTask(Context context, ImageView imageView,ImageCache mImageCache) {
		this.context = context;
		this.mImageCache = mImageCache;
		imageViewReference = new WeakReference<ImageView>(imageView);
	}

	@Override
	protected Bitmap doInBackground(MovieInfo... params) {
		imageInfo = params[0];
		return loadImageFile();
	}

	private Bitmap loadImageFile() {
		try {
			return BitmapCache.getInstance().getBitmap(context,imageInfo);
		} catch (Exception e) {
			Log.e(this.getClass().getSimpleName(), "fetchDrawable failed", e);
		}
		return null;
	}

	@Override
	protected void onPostExecute(Bitmap bitmap) {
		if (isCancelled()) {
			bitmap = null;
		}
		
		if (imageViewReference != null) {
			ImageView imageView = imageViewReference.get();
			if (imageView != null) {
				
				if(bitmap != null) {
					imageView.setImageBitmap(bitmap);
					mImageCache.addBitmapToCache(imageInfo.thumbnailPath, bitmap);
				} else {
					imageView.setBackgroundResource(R.drawable.ic_launcher);
				}
			}
		}
	}
}