package com.sky.drovik.player.engine;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import com.sky.drovik.player.media.MovieList;
import com.sky.drovik.player.pojo.MovieInfo;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore.Images.Thumbnails;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

public class ImageLoaderTask extends AsyncTask<MovieInfo, Void, Bitmap> {

	private MovieInfo imageInfo;
	
	private final WeakReference<ImageView> imageViewReference; // 防止内存溢出

	public ImageLoaderTask(ImageView imageView) {
		imageViewReference = new WeakReference<ImageView>(imageView);
	}

	@Override
	protected Bitmap doInBackground(MovieInfo... params) {
		imageInfo = params[0];
		return loadImageFile(imageInfo.path);
	}

	private Bitmap loadImageFile(final String filename) {
		InputStream is = null;
		try {
			Bitmap bmp = BitmapCache.getInstance().getBitmap(filename);
			return bmp;
		} catch (Exception e) {
			Log.e(this.getClass().getSimpleName(), "fetchDrawable failed", e);
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
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
					int width = bitmap.getWidth(); // 获取真实宽高
					int height = bitmap.getHeight();
					LayoutParams lp = imageView.getLayoutParams();
					lp.height = (height * MovieList.itemWidth) / width;//调整高度
					imageView.setLayoutParams(lp);
					imageView.setImageBitmap(bitmap);
				}
			}
		}
	}
}