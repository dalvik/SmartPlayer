package com.sky.drovik.player.media;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils.TruncateAt;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import com.sky.drovik.player.R;
import com.sky.drovik.player.engine.ImageLoaderTask;
import com.sky.drovik.player.engine.UpdateManager;
import com.sky.drovik.player.pojo.MovieInfo;
import com.sky.drovik.views.ControlPanel;
import com.sky.drovik.views.LazyScrollView;
import com.sky.drovik.views.LazyScrollView.OnScrollListener;

public class MovieList extends Activity implements OnClickListener {

	private LazyScrollView waterFallScrollView;
	
	private LinearLayout waterFallContainer;
	
	private List<LinearLayout> waterFallItems;
	
	private List<MovieInfo> movieList = new ArrayList<MovieInfo>();
	
	private Context context;
	
	private ProgressDialog progressDialog = null;

	private int column_count = 5;// 显示列数
	
	private int page_count = column_count * 4;// 每次加载15张图片

	private int current_page = 0;
	
	public static int itemWidth;
	
	private final int update = 1;
	
	private boolean debug = true;
	
	private ControlPanel rightControlPanel = null;
	
	private View rightView = null;
	

	private ExpandableListView expandableListView = null;
	
	private String TAG = "MovieList";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_movie_list);
		context = this;
		LinearLayout layout = (LinearLayout) findViewById(R.id.container);
		DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
		itemWidth = dm.widthPixels / column_count;// 根据屏幕大小计算每列大小
		initLayout(layout);
		UpdateManager.getUpdateManager().checkAppUpdate(this, false);
	}
	
	private void initLayout(LinearLayout layout) {
		waterFallScrollView = (LazyScrollView) layout.findViewById(R.id.lazyScrollView);
		waterFallContainer = (LinearLayout) layout.findViewById(R.id.waterFallContainer);
		LayoutInflater factory = LayoutInflater.from(this);
        rightView = factory.inflate(R.layout.reight_menu, null);
        expandableListView = (ExpandableListView) rightView.findViewById(R.id.history_list);
        //expandableListView.seta
        //SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(context, groupData, expandedGroupLayout, collapsedGroupLayout, groupFrom, groupTo, childData, childLayout, childFrom, childTo)
        int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        System.out.println("w=" + w + "h=" + h);
        rightView.measure(w, h);
        int width =rightView.getMeasuredWidth(); //
        rightControlPanel = new ControlPanel(this, waterFallScrollView,  width + ControlPanel.HANDLE_WIDTH, LayoutParams.FILL_PARENT);
		layout.addView(rightControlPanel);
		rightControlPanel.fillPanelContainer(rightView);
		
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
			LinearLayout.LayoutParams itemParam = new LinearLayout.LayoutParams(itemWidth, LayoutParams.WRAP_CONTENT, 1);
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
		List<MovieInfo> videoList = media.getVideoListByPage(current_page * page_count, page_count);
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
		LinearLayout.LayoutParams ig = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		ig.gravity = Gravity.CENTER;
		imageViewItem.setLayoutParams(ig);
		TextView textView = new TextView(context);
		LinearLayout.LayoutParams itemParam = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		itemParam.gravity = Gravity.CENTER;
		itemParam.weight = 1;
		textView.setGravity(Gravity.CENTER);
		textView.setLayoutParams(itemParam);
		textView.setEllipsize(TruncateAt.END);
		textView.setShadowLayer(1.5f, 1.5f, 1.5f, 0xff000000);
		textView.setTextColor(0xffffffff);
		textView.setBackgroundResource(R.drawable.cer_shape_status_bkgnd);
		textView.setMaxWidth(itemWidth);
		textView.setSingleLine();
		textView.setText(imageInfo.title);
		waterFallItems.get(columnIndex).addView(textView);
		imageViewItem.setTag(index);
		imageViewItem.setOnClickListener(this);
		imageInfo.imageView = imageViewItem;
		if(imageInfo.thumbnailPath != null && imageInfo.magic_id != 0) {
			ImageLoaderTask imageLoaderTask = new ImageLoaderTask(this, imageViewItem);
			imageLoaderTask.execute(imageInfo);
		}
	}

	
	@Override
	public void onClick(View v) {
		Integer index = (Integer) v.getTag();
		startActivity(movieList.get(index).intent);
	}
	
}
