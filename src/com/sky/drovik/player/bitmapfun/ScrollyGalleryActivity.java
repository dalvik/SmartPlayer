package com.sky.drovik.player.bitmapfun;

import java.io.IOException;

import android.app.WallpaperManager;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Toast;

import com.baidu.mobstat.StatService;
import com.sky.drovik.player.R;
import com.sky.drovik.player.bitmapfun.ImageWorker.OnLoadImageListener;

public class ScrollyGalleryActivity extends FragmentActivity implements OnLoadImageListener {
	
    private static final String IMAGE_CACHE_DIR = "images";
    
    public static final String EXTRA_IMAGE = "extra_image";
    
    public static final String LIST_SIZE = "list_size";
    
    public static final String IMAGE_SRC_LIST = "image_src_list";
    
    public static final String CATA_LOG = "cata_log";
    
    public int cataLog = 0;
    
	//private FlingGallery mGallery;
	
    private ImageFetcher mImageFetcher;
    
	private ImageGalleryAdapter imageAdapter;
	
	private ScrollLayout imageScrollLayout = null;
	
    private DisplayMetrics dm;
    
    @Override
    public boolean onTouchEvent(MotionEvent event)
	{
    	//return mGallery.onTouchEvent(event);
    	return imageScrollLayout.onTouchEvent(event);
    }

    @Override
	public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_scroll_image);
        imageScrollLayout = (ScrollLayout) findViewById(R.id.main_image_scroll);
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int height = displayMetrics.heightPixels;
        final int width = displayMetrics.widthPixels;
        final int longest = (height > width ? height : width) * 2/3;
        ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(this, IMAGE_CACHE_DIR);
        cacheParams.setMemCacheSizePercent(this, 0.25f); // Set memory cache to 25% of mem class

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        mImageFetcher = new ImageFetcher(this, longest);
        mImageFetcher.addImageCache(getSupportFragmentManager(), cacheParams);
        mImageFetcher.setImageFadeIn(false);
        mImageFetcher.setOnLoadImageListener(this);
        imageScrollLayout.setImageFetcher(mImageFetcher);
        if(getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
        	imageScrollLayout.setOrientation(1);
        }else {
        	imageScrollLayout.setOrientation(0);
        }
        cataLog = getIntent().getIntExtra(CATA_LOG, 0);
        String[] imageSrc = getIntent().getStringArrayExtra(IMAGE_SRC_LIST);
        final int extraCurrentItem = getIntent().getIntExtra(EXTRA_IMAGE, -1);
        imageScrollLayout.setAdapter(imageSrc, extraCurrentItem);
        imageScrollLayout.updateMetrics(displayMetrics);
        ImageView preView = (ImageView)imageScrollLayout.findViewById(R.id.preImageView);
        ImageView nextView = (ImageView)imageScrollLayout.findViewById(R.id.nextImageView);
        ImageView[] views = {preView, nextView}; 
        imageScrollLayout.initImageArr(views);
        imageScrollLayout.loadInit();
        
        /*mGallery = new FlingGallery(this, displayMetrics);
        if(getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
        	mGallery.setOrientation(1);
        }else {
        	mGallery.setOrientation(0);
        }
        mGallery.setPaddingWidth(100);
        
        imageAdapter = new ImageGalleryAdapter(ScrollyGalleryActivity.this, mImageFetcher, imageSrc);
        mGallery.setAdapter(imageAdapter, extraCurrentItem);
        mGallery.setIsGalleryCircular(true);
        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(Color.WHITE);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);

		//layoutParams.setMargins(10, 10, 10, 10);
		layoutParams.weight = 1.0f;
        layout.addView(mGallery, layoutParams);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(layout);*/
    }	
    
    @Override
	public void updateResolution(String path, int w, int h) {
    	imageScrollLayout.updateResolution(path, w, h);
	}
    
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    	final DisplayMetrics displayMetrics = new DisplayMetrics();
         getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//         final int height = displayMetrics.heightPixels;
//         final int width = displayMetrics.widthPixels;
//         final int longest = (height > width ? height : width) / 2;
//         mImageFetcher.setImageSize(longest);
         if(newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {// ˙∆¡1
        	 imageScrollLayout.setOrientation(1);
         }else {//0
        	 imageScrollLayout.setOrientation(0);
         }
         imageScrollLayout.updateMetrics(displayMetrics);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_image_detail, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.set_wallpaper:
                //NavUtils.navigateUpFromSameTask(this);
            	Bitmap b = null;//mImageFetcher.processBitmap(mGallery.getCurrentItem());
            	System.out.println("b= " + b);
            	if(b == null) {
            		return false;
            	}
				try {
					WallpaperManager.getInstance(ScrollyGalleryActivity.this).setBitmap(b);
					Toast.makeText(this, R.string.set_wall_paper_menu_toast,Toast.LENGTH_SHORT).show();
					StatService.onEvent(this, "Õº∆¨œÍ«È", "±⁄÷Ω…Ë÷√≥…π¶ ");
				} catch (IOException e) {
					Log.d("ImageDetailActivity", "### " + e.getLocalizedMessage());
					StatService.onEvent(this, "Õº∆¨œÍ«È", "±⁄÷Ω…Ë÷√ ß∞‹ ");
				}
                return true;
            case R.id.clear_cache:
                mImageFetcher.clearCache();
                StatService.onEvent(this, "Õº∆¨œÍ«È", "«Âø’Õº∆¨ª∫¥Ê ");
                Toast.makeText(this, R.string.clear_cache_complete_toast,Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
