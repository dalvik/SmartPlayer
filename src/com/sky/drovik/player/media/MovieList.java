package com.sky.drovik.player.media;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sky.drovik.player.R;
import com.sky.drovik.player.engine.ImageLoaderTask;
import com.sky.drovik.player.pojo.MovieInfo;
import com.sky.drovik.player.views.LazyScrollView;
import com.sky.drovik.player.views.LazyScrollView.OnScrollListener;

public class MovieList extends Activity implements OnClickListener {

	private LazyScrollView waterFallScrollView;
	
	private LinearLayout waterFallContainer;
	
	private List<LinearLayout> waterFallItems;
	
	private List<MovieInfo> movieList = new ArrayList<MovieInfo>();
	
	private Context context;
	
	private ProgressDialog progressDialog = null;

	private int column_count = 3;// 显示列数
	
	private int page_count = 15;// 每次加载15张图片

	private int current_page = 0;
	
	public static int itemWidth;
	
	private final int update = 1;
	
	private boolean debug = true;
	
	private String TAG = "MovieList";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_movie_list);
		context = this;
		Display display = this.getWindowManager().getDefaultDisplay();
		itemWidth = display.getWidth() / column_count;// 根据屏幕大小计算每列大小
		initLayout();
		/*Intent intent = new Intent(this, MovieView.class);
		intent.setData(Uri.parse("/mnt/sdcard/03.avi"));
		startActivity(intent);*/
	}
	
	private void initLayout() {
		waterFallScrollView = (LazyScrollView) findViewById(R.id.waterFallScrollView);
		waterFallContainer = (LinearLayout) findViewById(R.id.waterFallContainer);
		waterFallItems = new ArrayList<LinearLayout>();
		waterFallScrollView.getView();
		waterFallScrollView.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onTop() {
				Log.d(TAG, "### onTop");
			}
			
			@Override
			public void onScroll() {
				Log.d(TAG, "### onScroll");
			}
			
			@Override
			public void onBottom() {
				Log.d(TAG, "### onBottom");
				addItemToContainer(++current_page, page_count);
			}
		});
		
		for(int i=0;i<column_count;i++) {
			LinearLayout itemLayout = new LinearLayout(this);
			LinearLayout.LayoutParams itemParam = new LinearLayout.LayoutParams(itemWidth, LayoutParams.WRAP_CONTENT);
			itemLayout.setPadding(2, 2, 2, 2);
			itemLayout.setOrientation(LinearLayout.VERTICAL);
			itemLayout.setLayoutParams(itemParam);
			waterFallItems.add(itemLayout);
			waterFallContainer.addView(itemLayout);
		}
		// 第一次加载
		loadImageFiles();
		addItemToContainer(current_page, page_count);
	}
	
	
	private void loadImageFiles() {// 分页装载视频信息
		MediaList media = new MediaList(this);
		List<MovieInfo> videoList = media.getVideoListByPage(0);
		movieList.addAll(videoList);
	}
	
	private void addItemToContainer(int pageindex, int pagecount) {
		int j = 0;
		int imageCount = movieList.size();
		for (int i = pageindex * pagecount; i < pagecount * (pageindex + 1)
				&& i < imageCount; i++) {
			j = j >= column_count ? j = 0 : j;
			addImage(movieList.get(i), i, j++);
		}
	}
	
	private void addImage(MovieInfo imageInfo, int index, int columnIndex) {
		ImageView imageViewItem = (ImageView) LayoutInflater.from(this).inflate(
				R.layout.layout_movie_list_item, null);
		waterFallItems.get(columnIndex).addView(imageViewItem);
		TextView textView = new TextView(context);
		LinearLayout.LayoutParams itemParam = new LinearLayout.LayoutParams(itemWidth, LayoutParams.WRAP_CONTENT);
		textView.setGravity(Gravity.CENTER);
		textView.setLayoutParams(itemParam);
		textView.setSingleLine();
		textView.setText(imageInfo.title);
		waterFallItems.get(columnIndex).addView(textView);
		imageViewItem.setTag(index);
		imageViewItem.setOnClickListener(this);
		System.out.println(imageInfo.toString());
		//System.out.println(Uri.parse(imageInfo.thumbnailPath));
		imageInfo.imageView = imageViewItem;
		if(imageInfo.thumbnailPath != null) {
			imageViewItem.setImageURI(Uri.parse(imageInfo.thumbnailPath));
		}
		//ImageLoaderTask imageLoaderTask = new ImageLoaderTask(imageViewItem);
		//imageLoaderTask.execute(imageInfo);

	}

	
	@Override
	public void onClick(View v) {
		Integer index = (Integer) v.getTag();
		startActivity(movieList.get(index).intent);
		/*i.setClass(this, MovieView.class);*/
		/*Intent intent = new Intent(this, MovieView.class);
		intent.setData(Uri.parse("/mnt/sdcard/03.avi"));
		startActivity(intent);*/
	}
	
}
