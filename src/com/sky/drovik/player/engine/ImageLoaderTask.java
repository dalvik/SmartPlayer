package com.sky.drovik.player.engine;

import java.lang.ref.WeakReference;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.sky.drovik.player.pojo.MovieInfo;

public class ImageLoaderTask extends AsyncTask<MovieInfo, Void, Bitmap> {

	private MovieInfo imageInfo;
	
	private final WeakReference<ImageView> imageViewReference; // 防止内存溢出

	public ImageLoaderTask(ImageView imageView) {
		imageViewReference = new WeakReference<ImageView>(imageView);
	}

	@Override
	protected Bitmap doInBackground(MovieInfo... params) {
		imageInfo = params[0];
		return loadImageFile(imageInfo.thumbnailPath);
	}

	private Bitmap loadImageFile(final String filename) {
		try {
			Bitmap bmp = BitmapCache.getInstance().getBitmap(filename);
			return bmp;
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
				if (bitmap != null) {
					//int width = bitmap.getWidth(); // 获取真实宽高
					//int height = bitmap.getHeight();
					LayoutParams lp = imageView.getLayoutParams();
					//lp.height = (height * MovieList.itemWidth) / width;//调整高度
					imageView.setLayoutParams(lp);
					imageView.setImageBitmap(bitmap);
				}
			}
		}
	}
}