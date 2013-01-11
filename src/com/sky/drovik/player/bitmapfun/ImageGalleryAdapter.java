package com.sky.drovik.player.bitmapfun;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

public class ImageGalleryAdapter extends BaseAdapter {

	private Context context;
	
	private LinearLayout.LayoutParams mImageViewLayoutParams;
	
	private ImageFetcher mImageFetcher;
	
	private String[] imageList;

	public ImageGalleryAdapter(Context context, ImageFetcher mImageFetcher, String[] imageList) {
		this.context = context;
		mImageViewLayoutParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		mImageViewLayoutParams.gravity= Gravity.CENTER;
		this.mImageFetcher = mImageFetcher;
		this.imageList = imageList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        // Now handle the main ImageView thumbnails
        ImageView imageView;
        if (convertView == null) { // if it's not recycled, instantiate and initialize
            imageView = new ImageView(context);
            imageView.setScaleType(ScaleType.MATRIX);
            imageView.setLayoutParams(mImageViewLayoutParams);
        } else { // Otherwise re-use the converted view
            imageView = (ImageView) convertView;
        }
        mImageFetcher.loadImage(imageList[position], imageView);
        return imageView;
	}

	@Override
	public int getCount() {
		return imageList.length;
	}

	@Override
	public String getItem(int position) {
		return imageList[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
}
