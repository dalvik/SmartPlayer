package com.sky.drovik.player.media;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils.TruncateAt;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sky.drovik.player.BuildConfig;
import com.sky.drovik.player.R;
import com.sky.drovik.player.engine.HistoryListAdpater;
import com.sky.drovik.player.engine.ImageLoaderTask;
import com.sky.drovik.player.pojo.FileUtil;
import com.sky.drovik.player.pojo.HisInfo;
import com.sky.drovik.player.pojo.MovieInfo;
import com.sky.drovik.views.ControlPanel;
import com.sky.drovik.views.LazyScrollView;
import com.sky.drovik.views.LazyScrollView.OnScrollListener;

public class MovieList extends Activity implements OnClickListener, OnChildClickListener, OnCreateContextMenuListener {

	private final int MENU_DELETE = Menu.FIRST;
	
	private final int MENU_CLEAR = Menu.FIRST + 1;
	
	private final int MENU_CANCLE = Menu.FIRST + 2;
	
	private int group = 0;
	
	private int child = 0;
	
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
	
	private List<Map<String,Object>> parentList=new ArrayList<Map<String,Object>>();   
	       
	private List<List<Map<String,Object>>> childList = new ArrayList<List<Map<String,Object>>>();
	
	private int[] listName = {R.string.drovik_view_history_str};
	
	private ExpandableListView expandableListView = null;
	
	private HistoryListAdpater adapter;   
	
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
		//UpdateManager.getUpdateManager().checkAppUpdate(this, false);
		initLayout(layout);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
        childList = getChildList();  
		adapter.notifyDataSetChanged();
	}
	
	private void initLayout(LinearLayout layout) {
		waterFallScrollView = (LazyScrollView) layout.findViewById(R.id.lazyScrollView);
		waterFallContainer = (LinearLayout) layout.findViewById(R.id.waterFallContainer);
		LayoutInflater factory = LayoutInflater.from(this);
        rightView = factory.inflate(R.layout.reight_menu, null);
        expandableListView = (ExpandableListView) rightView.findViewById(R.id.history_list);
		parentList =getParentList(); 
		childList = getChildList();  
        adapter = new HistoryListAdpater(this, parentList, childList);   
        expandableListView.setAdapter(adapter);
        expandableListView.expandGroup(0);
        expandableListView.setGroupIndicator(null);   
        expandableListView.setDivider(null); 
        expandableListView.setOnChildClickListener(this);
        registerForContextMenu(expandableListView);
        int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        rightView.measure(w, h);
        int width = rightView.getMeasuredWidth(); //
        System.out.println("getMeasuredWidth=" + width);
        rightControlPanel = new ControlPanel(this, waterFallScrollView,  3 + width + ControlPanel.HANDLE_WIDTH, LayoutParams.FILL_PARENT);
		layout.addView(rightControlPanel);
		rightControlPanel.fillPanelContainer(rightView);
		
		waterFallItems = new ArrayList<LinearLayout>();
		waterFallScrollView.getView();
		waterFallScrollView.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onTop() {
				if(BuildConfig.DEBUG) {
					Log.d(TAG, "### onTop");
				}
			}
			
			@Override
			public void onScroll() {
				if(BuildConfig.DEBUG) {
					Log.d(TAG, "### onScroll");
				}
			}
			
			@Override
			public void onBottom() {
				if(BuildConfig.DEBUG) {
					Log.d(TAG, "### onBottom");
				}
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
	
	public List<Map<String,Object>> getParentList(){   
		        List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();   
		        for(int i=0;i<listName.length;i++){   
		             Map<String, Object> curGroupMap = new HashMap<String, Object>();   
		             list.add(curGroupMap);   
		             curGroupMap.put("list", getText(listName[i]));   
		       }   
		        return list;   
		   }   
	
	public List<List<Map<String,Object>>> getChildList(){   
		childList.clear();
        List<HisInfo> list = FileUtil.fetchDeviceFromFile(this);
         for (int i = 0; i < listName.length; i++) {   
             List<Map<String, Object>> children = new ArrayList<Map<String, Object>>(); 
             for(HisInfo h:list) {
            	 Map<String, Object> curChildMap = new HashMap<String, Object>();   
            	 children.add(curChildMap);   
            	 curChildMap.put("title", h.getName());
            	 curChildMap.put("path", h.getPath());
             }
             childList.add(children);   
        }   
        return childList;   
           
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
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		String title = childList.get(groupPosition).get(childPosition).get("title").toString();
		String path = childList.get(groupPosition).get(childPosition).get("path").toString();
		HisInfo hisInfo = new HisInfo(System.currentTimeMillis(), title, path);
		List<HisInfo> list = FileUtil.fetchDeviceFromFile(this);
		FileUtil.addNewHisInfo(list, hisInfo);
		FileUtil.persistentDevice(this, list);
		
		Intent intent = new Intent("com.sky.drovik.action.PLAYVER_VIEW");
        intent.setDataAndType(Uri.fromFile(new File(path)), "video/*"); 
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
		return true;
	}
	
	
	
	@Override
	public void onClick(View v) {
		Integer index = (Integer) v.getTag();
		MovieInfo info = movieList.get(index);
		HisInfo hisInfo = new HisInfo(System.currentTimeMillis(), info.title.toString(), info.path);
		List<HisInfo> list = FileUtil.fetchDeviceFromFile(this);
		FileUtil.addNewHisInfo(list, hisInfo);
		FileUtil.persistentDevice(this, list);
		startActivity(info.intent);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
		int type = ExpandableListView.getPackedPositionType(info.packedPosition);
		group = ExpandableListView.getPackedPositionGroup(info.packedPosition);
		child = ExpandableListView.getPackedPositionChild(info.packedPosition);
		if(type == 0) {
			menu.setHeaderTitle(parentList.get(group).get("list").toString());
			menu.add(0, MENU_CLEAR, 2, getString(R.string.drovik_context_menu_clear_str));
			menu.add(0, MENU_CANCLE, 3, getString(R.string.drovik_context_menu_cancle_str));
		}else {
			menu.setHeaderTitle(childList.get(group).get(child).get("title").toString());
			menu.add(0, MENU_DELETE, 1, getString(R.string.drovik_context_menu_delete_str));
			menu.add(0, MENU_CANCLE, 3, getString(R.string.drovik_context_menu_cancle_str));
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case MENU_DELETE:
			List<HisInfo> list1 = FileUtil.fetchDeviceFromFile(this);
			FileUtil.removeHisInfo(list1, childList.get(group).get(child).get("path").toString());
			FileUtil.persistentDevice(this, list1);
			childList = getChildList();
			adapter.notifyDataSetChanged();
			break;
		case MENU_CLEAR:
			List<HisInfo> list = FileUtil.fetchDeviceFromFile(this);
			list.clear();
			FileUtil.persistentDevice(this, list);
			childList = getChildList();
			adapter.notifyDataSetChanged();
			break;
			default:
				break;
		}
		return super.onContextItemSelected(item);
	}
}
