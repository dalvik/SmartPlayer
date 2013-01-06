package com.sky.drovik.player.media;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.youmi.android.appoffers.CheckStatusNotifier;
import net.youmi.android.appoffers.EarnedPointsNotifier;
import net.youmi.android.appoffers.EarnedPointsOrder;
import net.youmi.android.appoffers.YoumiOffersManager;
import net.youmi.android.appoffers.YoumiPointsManager;
import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
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
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mobstat.StatService;
import com.drovik.utils.ToastUtils;
import com.sky.drovik.entity.UIHelper;
import com.sky.drovik.player.AppContext;
import com.sky.drovik.player.BuildConfig;
import com.sky.drovik.player.R;
import com.sky.drovik.player.adpter.ListViewImageAdapter;
import com.sky.drovik.player.adpter.ListViewOtherImageAdapter;
import com.sky.drovik.player.adpter.ListViewSceneryImageAdapter;
import com.sky.drovik.player.app.Res;
import com.sky.drovik.player.bitmapfun.ImageCache.ImageCacheParams;
import com.sky.drovik.player.bitmapfun.ImageDetailActivity;
import com.sky.drovik.player.bitmapfun.ImageFetcher;
import com.sky.drovik.player.engine.BeautyImage;
import com.sky.drovik.player.engine.HistoryListAdpater;
import com.sky.drovik.player.engine.ImageLoaderTask;
import com.sky.drovik.player.engine.UpdateManager;
import com.sky.drovik.player.exception.StringUtils;
import com.sky.drovik.player.pojo.BaseImage;
import com.sky.drovik.player.pojo.FileUtil;
import com.sky.drovik.player.pojo.HisInfo;
import com.sky.drovik.player.pojo.MovieInfo;
import com.sky.drovik.views.ControlPanel;
import com.sky.drovik.views.LazyScrollView;
import com.sky.drovik.widget.PullToRefreshListView;
import com.sky.drovik.widget.PullToRefreshListView.OnRefreshListener;
import com.sky.drovik.widget.ScrollLayout;

public class Main extends FragmentActivity implements EarnedPointsNotifier, CheckStatusNotifier {

	private String TAG = "Main";
	
    private static final String IMAGE_CACHE_DIR = "thumbs";
    
	private boolean DEBUG = true;
	
	private ScrollLayout mScrollLayout;
	private RadioButton[] mButtons;
	private String[] mHeadTitles;
	private int mViewCount;
	private int mCurSel;

	private ImageView mHeadLogo;
	private TextView mHeadTitle;
	private ProgressBar mHeadProgress;

	//footer
	private RadioButton fbImage;
	private RadioButton fbVideo;
	private ImageView fbSetting;
	
	//beayty image contentvew
	private PullToRefreshListView beautyImageListView;
	private ListViewImageAdapter beautyImageListViewAdapter;
	private static List<BaseImage> beautyImageListViewData = new ArrayList<BaseImage>();
	private View beautyImageListViewFooter;
	private TextView beautyImageListViewFootMore;
	private ProgressBar beautyImageListViewFootProgress;
	
	//scenery image contentvew
	private PullToRefreshListView sceneryImageListView;
	private ListViewSceneryImageAdapter sceneryImageListViewAdapter;
	private static List<BaseImage> sceneryImageListViewData = new ArrayList<BaseImage>();
	private View sceneryImageListViewFooter;
	private TextView sceneryImageListViewFootMore;
	private ProgressBar sceneryImageListViewFootProgress;
		
	//other image contentvew
	private PullToRefreshListView otherImageListView;
	private ListViewOtherImageAdapter otherImageListViewAdapter;
	private static List<BaseImage> otherImageListViewData = new ArrayList<BaseImage>();
	private View otherImageListViewFooter;
	private TextView otherImageListViewFootMore;
	private ProgressBar otherImageListViewFootProgress;
		
	//handler
	private Handler beautyImageListViewHandler;
	private Handler sceneryImageListViewHandler;
	private Handler otherImageListViewHandler;
	
	
	//top buttons
	private Button frameBeautyButton;
	private Button frameSceneryButton;
	private Button frameOtherButton;
	
	private int beautyImageListSumData;
	private int sceneryImageListSumData;
	private int otherImageListSumData;
	
	private int curImageCatalog = BaseImage.CATALOG_BEAUTY;
	
	private AppContext appContext;//全局Context
	
    private ImageFetcher mImageFetcher;
    
    private int mImageThumbWidth;
    private int mImageThumbHeight;
    
    private List<BaseImage> beautyImageListTmp = null;
    private List<BaseImage> sceneryImageListTmp = null;
    private List<BaseImage> otherImageListTmp = null;
    
    //video list
    private final int MENU_DELETE = Menu.FIRST;
	
	private final int MENU_CLEAR = Menu.FIRST + 1;
	
	private final int MENU_CANCLE = Menu.FIRST + 2;
	
	private int group = 0;
	
	private int child = 0;
	
    private LazyScrollView waterFallScrollView;
	
	private LinearLayout waterFallContainer;
	
	private List<LinearLayout> waterFallItems;
	
	private List<MovieInfo> movieList = new ArrayList<MovieInfo>();
	
	private ProgressDialog progressDialog = null;

	private int column_count = 4;// 显示列数
	
	private int page_count = column_count * 4;// 每次加载15张图片

	private int current_page = 0;
	
	public static int itemWidth;
	
	private final int update = 1;
	
	private ControlPanel rightControlPanel = null;
	
	private View rightView = null;
	
	private List<Map<String,Object>> parentList=new ArrayList<Map<String,Object>>();   
	       
	private List<List<Map<String,Object>>> childList = new ArrayList<List<Map<String,Object>>>();
	
	private int[] listName = {R.string.drovik_view_history_str};
	
	private ExpandableListView expandableListView = null;
	
	private HistoryListAdpater adapter;   

	private static int photoIndex = 0;
	
	private String curPhotoName = ""; //当前相册名称
	
	
	//ym
	public static final String KEY_POINTS="BEYING";
	private static final String KEY_FILE_ORDERS="Orders";
    private boolean isDeviceInvalid;
    private String channelId = "";
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		YoumiOffersManager.init(this, "d56c188174986b81", "07603ef9797423c0");
		YoumiPointsManager.setUserID(this.getPackageName());
		setContentView(R.layout.layout_main);
		appContext = (AppContext)getApplication();
		try {
			ApplicationInfo info = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
			channelId = info.metaData.getInt("YOUMI_CHANNEL") +"";
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		mImageThumbWidth = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_width);
		mImageThumbHeight = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_height);
		mImageFetcher = new ImageFetcher(appContext, mImageThumbWidth, mImageThumbHeight);
		mImageFetcher.setLoadingImage(R.drawable.start_anima);
		ImageCacheParams cacheParams = new ImageCacheParams(appContext, IMAGE_CACHE_DIR);
		mImageFetcher.addImageCache(getSupportFragmentManager(), cacheParams);
		this.initHeadView();
		this.initFootBar();
		this.initPageScroll();
		this.initFrameButton();
        this.initFrameListView();
		UpdateManager.getUpdateManager().checkAppUpdate(this, false);
	}

	private void initPageScroll() {
		mScrollLayout = (ScrollLayout) findViewById(R.id.main_scrolllayout);
		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.main_linearlayout_footer);
    	mHeadTitles = getResources().getStringArray(R.array.head_titles);
    	mViewCount = mScrollLayout.getChildCount();
    	mButtons = new RadioButton[mViewCount];
    	if(BuildConfig.DEBUG && DEBUG) {
    		Log.d(TAG, "### mViewCount= " + mViewCount); 
    	}
    	for(int i = 0; i < mViewCount; i++)	{
    		mButtons[i] = (RadioButton) linearLayout.getChildAt(i*2);
    		mButtons[i].setTag(i);
    		mButtons[i].setChecked(false);
    		mButtons[i].setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					int pos = (Integer)(v.getTag());
					if(mCurSel == pos) {
		    			switch (pos) {
						case 0://image
							beautyImageListView.clickRefresh();
							StatService.onEvent(Main.this, "主界面", "点击图像按钮");
							break;	
						case 1://video
							StatService.onEvent(Main.this, "主界面", "点击视频按钮");
							break;
						case 2://settings
							break;
							default:
								break;
		    			}
	    			}
					setCurPoint(pos);
					mScrollLayout.snapToScreen(pos);
				}
    		});
    	}
    	//设置第一显示屏
    	mCurSel = 0;
    	mButtons[mCurSel].setEnabled(true);
    	mButtons[mCurSel].setChecked(true);
    	mScrollLayout.SetOnViewChangeListener(new ScrollLayout.OnViewChangeListener() {
			public void OnViewChange(int viewIndex) {
				setCurPoint(viewIndex);
			}
		});
	}
	
	/**
     * 设置底部栏当前焦点
     * @param index
     */
    private void setCurPoint(int index) {
    	if (index < 0 || index > mViewCount - 1 || mCurSel == index)
    		return;
    	mButtons[mCurSel].setChecked(false);
    	mButtons[index].setChecked(true);    
    	mHeadTitle.setText(mHeadTitles[index]);    	
    	mCurSel = index;
    	if(index == 0){
    		mHeadLogo.setImageResource(R.drawable.frame_logo_image);
    	} else if(index == 1){
    		mHeadLogo.setImageResource(R.drawable.frame_logo_video);
    	}
    }
    
	private void initHeadView() {
		mHeadLogo = (ImageView) findViewById(R.id.main_head_logo);
		mHeadTitle = (TextView) findViewById(R.id.main_head_title);
		mHeadProgress = (ProgressBar) findViewById(R.id.main_head_progress);
	}

	private void initFootBar() {
		fbImage = (RadioButton)findViewById(R.id.main_footbar_image);
		fbVideo = (RadioButton)findViewById(R.id.main_footbar_video);
    	fbSetting = (ImageView)findViewById(R.id.main_footbar_setting);
	}
	
    private void initFrameListView() {
    	//初始化listview控件
		this.initImageListView();
		this.initSceneryListView();
		this.initOtherListView();
		this.initVideoView();
		//加载listview数据
		this.initFrameListViewData();
    }
    
    private void initVideoView() {
    	LinearLayout layout = (LinearLayout) findViewById(R.id.container);
		DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
		itemWidth = dm.widthPixels / column_count;// 根据屏幕大小计算每列大小
		initLayout(layout);
    }
    
    private void initLayout(LinearLayout layout) {
		waterFallScrollView = (LazyScrollView) layout.findViewById(R.id.lazyScrollView);
		waterFallContainer = (LinearLayout) layout.findViewById(R.id.waterFallContainer);
		LayoutInflater factory = LayoutInflater.from(this);
        rightView = factory.inflate(R.layout.layout_reight_menu, null);
        expandableListView = (ExpandableListView) rightView.findViewById(R.id.history_list);
		parentList =getParentList(); 
		childList = getChildList();  
        adapter = new HistoryListAdpater(this, parentList, childList);   
        expandableListView.setAdapter(adapter);
        expandableListView.expandGroup(0);
        expandableListView.setGroupIndicator(null);   
        expandableListView.setDivider(null); 
        expandableListView.setOnChildClickListener(new android.widget.ExpandableListView.OnChildClickListener() {
			
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				String title = childList.get(groupPosition).get(childPosition).get("title").toString();
				String path = childList.get(groupPosition).get(childPosition).get("path").toString();
				HisInfo hisInfo = new HisInfo(System.currentTimeMillis(), title, path);
				List<HisInfo> list = FileUtil.fetchDeviceFromFile(appContext);
				FileUtil.addNewHisInfo(list, hisInfo);
				FileUtil.persistentDevice(appContext, list);
				
				Intent intent = new Intent("com.sky.drovik.action.PLAYVER_VIEW");
		        intent.setDataAndType(Uri.fromFile(new File(path)), "video/*"); 
		        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		        startActivity(intent);
				return true;
			}
		});
        
        registerForContextMenu(expandableListView);
        int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        rightView.measure(w, h);
        int width = rightView.getMeasuredWidth(); //
        rightControlPanel = new ControlPanel(this, waterFallScrollView,  width + ControlPanel.HANDLE_WIDTH, LayoutParams.FILL_PARENT);
		layout.addView(rightControlPanel);
		rightControlPanel.fillPanelContainer(rightView);
		
		waterFallItems = new ArrayList<LinearLayout>();
		waterFallScrollView.getView();
		waterFallScrollView.setOnScrollListener(new com.sky.drovik.views.LazyScrollView.OnScrollListener() {
			
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
		TextView textView = new TextView(appContext);
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
		imageViewItem.setOnClickListener(new android.view.View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Integer index = (Integer) v.getTag();
				MovieInfo info = movieList.get(index);
				HisInfo hisInfo = new HisInfo(System.currentTimeMillis(), info.title.toString(), info.path);
				List<HisInfo> list = FileUtil.fetchDeviceFromFile(appContext);
				FileUtil.addNewHisInfo(list, hisInfo);
				FileUtil.persistentDevice(appContext, list);
				startActivity(info.intent);
			}
		});
		imageInfo.imageView = imageViewItem;
		if(imageInfo.thumbnailPath != null && imageInfo.magic_id != 0) {
			ImageLoaderTask imageLoaderTask = new ImageLoaderTask(this, imageViewItem);
			imageLoaderTask.execute(imageInfo);
		}
	}
	
	private void loadImageFiles() {// 分页装载视频信息
		MediaList media = new MediaList(this);
		List<MovieInfo> videoList = media.getVideoListByPage(current_page * page_count, page_count);
		movieList.addAll(videoList);
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
	
    /**
     * 初始化主页的按钮
     */
    private void initFrameButton() {
    	frameBeautyButton = (Button)findViewById(R.id.frame_btn_beauty);
    	frameSceneryButton = (Button)findViewById(R.id.frame_btn_scenery);
    	frameOtherButton = (Button)findViewById(R.id.frame_btn_other);
    	
    	frameBeautyButton.setOnClickListener(frameNewsBtnClick(frameBeautyButton, BaseImage.CATALOG_BEAUTY));
    	frameSceneryButton.setOnClickListener(frameNewsBtnClick(frameSceneryButton, BaseImage.CATALOG_SCENERY));
    	frameOtherButton.setOnClickListener(frameNewsBtnClick(frameOtherButton, BaseImage.CATALOG_OTHER));
    	
    	frameBeautyButton.setFocusable(true);
    	frameBeautyButton.setEnabled(false);
    }
    
    private View.OnClickListener frameNewsBtnClick(final Button btn,final int catalog){
    	return new View.OnClickListener() {
			public void onClick(View v) {
				if(btn == frameBeautyButton){
					frameBeautyButton.setEnabled(false);
					StatService.onEvent(Main.this, "主界面", "美图按钮");
		    	}else{
		    		frameBeautyButton.setEnabled(true);
		    	}
		    	if(btn == frameSceneryButton){
		    		frameSceneryButton.setEnabled(false);
		    		StatService.onEvent(Main.this, "主界面", "风景按钮");
		    	}else{
		    		frameSceneryButton.setEnabled(true);
		    	}
		    	if(btn == frameOtherButton){
		    		StatService.onEvent(Main.this, "主界面", "其他按钮");
		    		frameOtherButton.setEnabled(false);
		    	}else{
		    		frameOtherButton.setEnabled(true);
		    	}
		    	curImageCatalog = catalog;
		    	if(btn == frameBeautyButton) {
		    		beautyImageListView.setVisibility(View.VISIBLE);
		    		sceneryImageListView.setVisibility(View.GONE);
		    		otherImageListView.setVisibility(View.GONE);
		    		if(beautyImageListViewData.size() == 0) {
		    			loadImageListData(curImageCatalog, 0, beautyImageListViewHandler, UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG);
		    		} else {
		    			beautyImageListViewFootMore.setText(R.string.load_more);
		    			beautyImageListViewFootProgress.setVisibility(View.GONE);
		    			loadSceneryImageListData(curImageCatalog, 0, beautyImageListViewHandler, UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG);
		    		}
		    	} else if(btn == frameSceneryButton ){
		    		beautyImageListView.setVisibility(View.GONE);
		    		otherImageListView.setVisibility(View.GONE);
		    		sceneryImageListView.setVisibility(View.VISIBLE);
		    		if(sceneryImageListViewData.size() == 0) {
		    			loadSceneryImageListData(curImageCatalog, 0, sceneryImageListViewHandler, UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG);
		    		} else {
		    			sceneryImageListViewFootMore.setText(R.string.load_more);
		    			sceneryImageListViewFootProgress.setVisibility(View.GONE);
		    			loadSceneryImageListData(curImageCatalog, 0, sceneryImageListViewHandler, UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG);
		    		}
		    	} else if(btn == frameOtherButton ){
		    		beautyImageListView.setVisibility(View.GONE);
		    		sceneryImageListView.setVisibility(View.GONE);
		    		otherImageListView.setVisibility(View.VISIBLE);
		    		if(otherImageListViewData.size() == 0) {
		    			loadOtherImageListData(curImageCatalog, 0, otherImageListViewHandler, UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG);
		    		} else {
		    			otherImageListViewFootMore.setText(R.string.load_more);
		    			otherImageListViewFootProgress.setVisibility(View.GONE);
		    			loadOtherImageListData(curImageCatalog, 0, otherImageListViewHandler, UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG);
		    		}
		    	}
			}
    	};
     }
    
    /**
     * 初始化ListView数据
     */
    private void initFrameListViewData() {
    	 //初始化Handler
        beautyImageListViewHandler = this.getListViewHandler(beautyImageListView, beautyImageListViewAdapter, beautyImageListViewFootMore, beautyImageListViewFootProgress, AppContext.PAGE_SIZE);
        sceneryImageListViewHandler = this.getListViewHandler(sceneryImageListView, sceneryImageListViewAdapter, sceneryImageListViewFootMore, sceneryImageListViewFootProgress, AppContext.PAGE_SIZE);
        otherImageListViewHandler = this.getListViewHandler(otherImageListView, otherImageListViewAdapter, otherImageListViewFootMore, otherImageListViewFootProgress, AppContext.PAGE_SIZE);
        //加载数据				
		if(beautyImageListViewData.size() == 0) {
			loadImageListData(curImageCatalog, 0, beautyImageListViewHandler, UIHelper.LISTVIEW_ACTION_INIT);
		}
    }
    
    /**
     * 获取listview的初始化Handler
     * @param lv
     * @param adapter
     * @return
     */
    private Handler getListViewHandler(final PullToRefreshListView imageListView,final BaseAdapter adapter,final TextView more,final ProgressBar progress, final int pageSize){
    	return new Handler(){
    		@Override
    		public void handleMessage(Message msg) {
    			super.handleMessage(msg);
    			if(msg.what>0) {
    				handleImageListData(msg.what, msg.obj, msg.arg2, msg.arg1);
    				if(msg.what<pageSize) {
    					imageListView.setTag(UIHelper.LISTVIEW_DATA_FULL);
    					adapter.notifyDataSetChanged();
    					more.setText(R.string.load_full);
    				}else if(msg.what == pageSize) {
    					imageListView.setTag(UIHelper.LISTVIEW_DATA_MORE);
    					adapter.notifyDataSetChanged();
    					more.setText(R.string.load_more);
    				}
    			}else if(msg.what == -1) {
    				imageListView.setTag(UIHelper.LISTVIEW_DATA_MORE);
    				more.setText(R.string.load_error);
    				
    			}
    			if(adapter.getCount() ==0) {
    				imageListView.setTag(UIHelper.LISTVIEW_DATA_EMPTY);
    				more.setText(R.string.load_empty);
    			}
    			progress.setVisibility(ProgressBar.GONE);
    			mHeadProgress.setVisibility(ProgressBar.GONE);
    			if(msg.arg1 == UIHelper.LISTVIEW_ACTION_REFRESH) {
    				imageListView.onRefreshComplete(getString(R.string.pull_to_refresh_update) + new Date().toLocaleString());
    				imageListView.setSelection(0);
    				String info = null;
    				if(curImageCatalog == BaseImage.CATALOG_BEAUTY) {
    					info = "刷新美图成功";
    				}else if(curImageCatalog == BaseImage.CATALOG_SCENERY) {
    					info = "刷新风景成功";
    				} else {
    					info = "刷新其他成功";
    				}
    				StatService.onEvent(Main.this, "主界面", info);
    			}else if(msg.arg1 == UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG) {
    				imageListView.onRefreshComplete();
    				imageListView.setSelection(0);
    			}
    		}
    	};
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void initImageListView() {
    	beautyImageListViewAdapter = new ListViewImageAdapter(this, beautyImageListViewData,  R.layout.layout_image_list_item, mImageFetcher);
    	beautyImageListViewFooter = getLayoutInflater().inflate(R.layout.layout_list_view_footer, null);
    	beautyImageListViewFootMore = (TextView)beautyImageListViewFooter.findViewById(R.id.list_view_foot_more);
    	beautyImageListViewFootProgress = (ProgressBar)beautyImageListViewFooter.findViewById(R.id.list_view_foot_progress);
    	beautyImageListView = (PullToRefreshListView)findViewById(R.id.frame_list_view_beayty_image);
    	beautyImageListView.addFooterView(beautyImageListViewFooter);
    	beautyImageListView.setAdapter(beautyImageListViewAdapter);
    	beautyImageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
          	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
          		//点击头部、底部栏无效
          		if(position == 0){
          			return;
          		}
          		if(view == beautyImageListViewFooter) {
					beautyImageListViewFootMore.setText(R.string.load_ing);
					beautyImageListViewFootProgress.setVisibility(View.VISIBLE);
					//当前pageIndex
					int pageIndex = beautyImageListSumData/AppContext.PAGE_SIZE;
					loadImageListData(curImageCatalog, pageIndex, beautyImageListViewHandler, UIHelper.LISTVIEW_ACTION_SCROLL);
          			return;
          		}
          		final Intent i = new Intent(appContext, ImageDetailActivity.class);
                i.putExtra(ImageDetailActivity.EXTRA_IMAGE, 0);
                if(position - 1>=0 && position - 1 < beautyImageListViewData.size()) {
                	photoIndex = position - 1;
                	BeautyImage beautyImage = (BeautyImage) beautyImageListViewData.get(position-1);
                	curPhotoName = beautyImage.getName();
                	if(beautyImage.getId()>0 && queryPoints(appContext)<= 0 && !beautyImage.getChannel().contains(channelId)) {
                		StatService.onEvent(Main.this, "主界面", "打开相册 " + curPhotoName+ " 注册框");
                		showErrDialog();//id==0表示审核 >0 注册  没有包含channel的话 也表示需要注册了。所以通过审核以后需要将channel值0
                		return;
                	}
                	i.putExtra(ImageDetailActivity.LIST_SIZE, beautyImage.getSrcSize());
                }else {
                	photoIndex = position - 1;
                	i.putExtra(ImageDetailActivity.LIST_SIZE, 0);
                	
                }
                i.putExtra(ImageDetailActivity.CATA_LOG, curImageCatalog);
                if (com.sky.drovik.player.bitmapfun.Utils.hasJellyBean()) {
                    // makeThumbnailScaleUpAnimation() looks kind of ugly here as the loading spinner may
                    // show plus the thumbnail image in GridView is cropped. so using
                    // makeScaleUpAnimation() instead.
                    ActivityOptions options =
                            ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getWidth(), view.getHeight());
                    startActivity(i, options.toBundle());
                } else {
                    startActivity(i);
                }
                //startActivity(i);
          		//跳转到新闻详情
          		//UIHelper.showNewsRedirect(view.getContext(), news);
          	}        	
  		});
    	beautyImageListView.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				beautyImageListView.onScrollStateChanged(view, scrollState);		
				//数据为空--不用继续下面代码了
				if(beautyImageListViewData.size() == 0) return;
				//判断是否滚动到底部
				 if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
	                  mImageFetcher.setPauseWork(true);
	             } else {
	                  mImageFetcher.setPauseWork(false);
	             }
				boolean scrollEnd = false;
				try{
					if(view.getPositionForView(beautyImageListViewFooter) == view.getLastVisiblePosition()) {
						scrollEnd = true;
					}
				} catch (Exception e) {
					scrollEnd = false;
				}
				int imageListDataState = StringUtils.toInt(beautyImageListView.getTag());
				if(scrollEnd && imageListDataState == UIHelper.LISTVIEW_DATA_MORE) {
					beautyImageListViewFootMore.setText(R.string.load_ing);
					beautyImageListViewFootProgress.setVisibility(View.VISIBLE);
					//当前pageIndex
					int pageIndex = beautyImageListSumData/AppContext.PAGE_SIZE;
					loadImageListData(curImageCatalog, pageIndex, beautyImageListViewHandler, UIHelper.LISTVIEW_ACTION_SCROLL);
				}
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				beautyImageListView.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
				if(beautyImageListViewData.size()<=0) {
					beautyImageListView.onScrollStateChanged(view, SCROLL_STATE_TOUCH_SCROLL);
				}
			}
		});
    	beautyImageListView.setOnRefreshListner(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				loadImageListData(curImageCatalog, 0, beautyImageListViewHandler, UIHelper.LISTVIEW_ACTION_REFRESH);
			}
		});
    }

    //init sencerty
	private void initSceneryListView() {
		sceneryImageListViewAdapter = new ListViewSceneryImageAdapter(this, sceneryImageListViewData, R.layout.layout_scenery_image_list_item, mImageFetcher);        
        sceneryImageListViewFooter = getLayoutInflater().inflate(R.layout.layout_list_view_footer, null);
        sceneryImageListViewFootMore = (TextView)sceneryImageListViewFooter.findViewById(R.id.list_view_foot_more);
        sceneryImageListViewFootProgress = (ProgressBar)sceneryImageListViewFooter.findViewById(R.id.list_view_foot_progress);
        sceneryImageListView = (PullToRefreshListView)findViewById(R.id.frame_list_view_secenry_image);
        sceneryImageListView.addFooterView(sceneryImageListViewFooter);//添加底部视图  必须在setAdapter前
        sceneryImageListView.setAdapter(sceneryImageListViewAdapter); 
        sceneryImageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		//点击头部、底部栏无效
        		if(position == 0){
        			return;
        		}
          		if(view == sceneryImageListViewFooter) {
					sceneryImageListViewFootMore.setText(R.string.load_ing);
					sceneryImageListViewFootProgress.setVisibility(View.VISIBLE);
					//当前pageIndex
					int pageIndex = sceneryImageListSumData/AppContext.PAGE_SIZE;
					loadSceneryImageListData(curImageCatalog, pageIndex, sceneryImageListViewHandler, UIHelper.LISTVIEW_ACTION_SCROLL);
          			return;
          		}
          		if(position-1<0 || position-1 >= sceneryImageListViewData.size()) {
          			return;
          		}
          		BaseImage sceneryBaseImage = sceneryImageListViewData.get(position-1);
          		curPhotoName = sceneryBaseImage.getName();
          		if(sceneryBaseImage.getId()>0 && queryPoints(appContext)<= 0 && !sceneryBaseImage.getChannel().contains(channelId)) {
            		showErrDialog();
            		return;
            	}
        		final Intent i = new Intent(appContext, ImageDetailActivity.class);
                i.putExtra(ImageDetailActivity.EXTRA_IMAGE, position-1);
                i.putExtra(ImageDetailActivity.LIST_SIZE, sceneryImageListViewData.size());
                i.putExtra(ImageDetailActivity.CATA_LOG, curImageCatalog);  
        		if (com.sky.drovik.player.bitmapfun.Utils.hasJellyBean()) {
                    // makeThumbnailScaleUpAnimation() looks kind of ugly here as the loading spinner may
                    // show plus the thumbnail image in GridView is cropped. so using
                    // makeScaleUpAnimation() instead.
                    ActivityOptions options =
                            ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getWidth(), view.getHeight());
                    startActivity(i, options.toBundle());
                } else {
                    startActivity(i);
                }
        		//跳转到博客详情
        		//UIHelper.showUrlRedirect(view.getContext(), blog.getUrl());
        	}        	
		});
        sceneryImageListView.setOnScrollListener(new AbsListView.OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				sceneryImageListView.onScrollStateChanged(view, scrollState);
				
				//数据为空--不用继续下面代码了
				if(sceneryImageListViewData.size() == 0) return;
				
				//判断是否滚动到底部
				boolean scrollEnd = false;
				try {
					if(view.getPositionForView(sceneryImageListViewFooter) == view.getLastVisiblePosition())
						scrollEnd = true;
				} catch (Exception e) {
					scrollEnd = false;
				}
				
				int lvDataState = StringUtils.toInt(sceneryImageListView.getTag());
				if(scrollEnd && lvDataState==UIHelper.LISTVIEW_DATA_MORE)
				{
					sceneryImageListViewFootMore.setText(R.string.load_ing);
					sceneryImageListViewFootProgress.setVisibility(View.VISIBLE);
					//当前pageIndex
					int pageIndex = sceneryImageListSumData/AppContext.PAGE_SIZE;
					loadSceneryImageListData(curImageCatalog, pageIndex, sceneryImageListViewHandler, UIHelper.LISTVIEW_ACTION_SCROLL);
				}
			}
			public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
				sceneryImageListView.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
				if(sceneryImageListViewData.size()<=0) {
					sceneryImageListView.onScrollStateChanged(view, SCROLL_STATE_TOUCH_SCROLL);
				}
			}
		});
        sceneryImageListView.setOnRefreshListner(new PullToRefreshListView.OnRefreshListener() {
            public void onRefresh() {
            	loadSceneryImageListData(curImageCatalog, 0, sceneryImageListViewHandler, UIHelper.LISTVIEW_ACTION_REFRESH);
            }
        });					
    }
	
	//init other
	private void initOtherListView() {
		otherImageListViewAdapter = new ListViewOtherImageAdapter(this, otherImageListViewData, R.layout.layout_other_image_list_item, mImageFetcher);        
		otherImageListViewFooter = getLayoutInflater().inflate(R.layout.layout_list_view_footer, null);
		otherImageListViewFootMore = (TextView)otherImageListViewFooter.findViewById(R.id.list_view_foot_more);
		otherImageListViewFootProgress = (ProgressBar)otherImageListViewFooter.findViewById(R.id.list_view_foot_progress);
		otherImageListView = (PullToRefreshListView)findViewById(R.id.frame_list_view_other_image);
		otherImageListView.addFooterView(otherImageListViewFooter);//添加底部视图  必须在setAdapter前
		otherImageListView.setAdapter(otherImageListViewAdapter); 
		otherImageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		//点击头部、底部栏无效
        		if(position == 0){
        			return;
        		}
          		if(view == otherImageListViewFooter) {
					otherImageListViewFootMore.setText(R.string.load_ing);
					otherImageListViewFootProgress.setVisibility(View.VISIBLE);
					//当前pageIndex
					int pageIndex = otherImageListSumData/AppContext.PAGE_SIZE;
					loadOtherImageListData(curImageCatalog, pageIndex, otherImageListViewHandler, UIHelper.LISTVIEW_ACTION_SCROLL);
          			return;
          		}
          		final Intent i = new Intent(appContext, ImageDetailActivity.class);
          		i.putExtra(ImageDetailActivity.EXTRA_IMAGE, 0);
          		if(position - 1>=0 && position - 1 < otherImageListViewData.size()) {
                	photoIndex = position - 1;
                	BeautyImage beautyImage = (BeautyImage) otherImageListViewData.get(position-1);
                	curPhotoName = beautyImage.getName();
                	if(beautyImage.getId()>0 && queryPoints(appContext)<= 0 && !beautyImage.getChannel().contains(channelId)) {
                		showErrDialog();
                		return;
                	}
                	i.putExtra(ImageDetailActivity.LIST_SIZE, beautyImage.getSrcSize());
                } else {
                	photoIndex = position - 1;
                	i.putExtra(ImageDetailActivity.LIST_SIZE, 0);
                }
                i.putExtra(ImageDetailActivity.CATA_LOG, curImageCatalog);  
                if (com.sky.drovik.player.bitmapfun.Utils.hasJellyBean()) {
                    // makeThumbnailScaleUpAnimation() looks kind of ugly here as the loading spinner may
                    // show plus the thumbnail image in GridView is cropped. so using
                    // makeScaleUpAnimation() instead.
                    ActivityOptions options =
                            ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getWidth(), view.getHeight());
                    startActivity(i, options.toBundle());
                } else {
                    startActivity(i);
                }
        		//跳转到博客详情
        		//UIHelper.showUrlRedirect(view.getContext(), blog.getUrl());
        	}        	
		});
		otherImageListView.setOnScrollListener(new AbsListView.OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				otherImageListView.onScrollStateChanged(view, scrollState);
				
				//数据为空--不用继续下面代码了
				if(otherImageListViewData.size() == 0) return;
				
				//判断是否滚动到底部
				boolean scrollEnd = false;
				try {
					if(view.getPositionForView(otherImageListViewFooter) == view.getLastVisiblePosition())
						scrollEnd = true;
				} catch (Exception e) {
					scrollEnd = false;
				}
				
				int lvDataState = StringUtils.toInt(otherImageListView.getTag());
				if(scrollEnd && lvDataState==UIHelper.LISTVIEW_DATA_MORE)
				{
					otherImageListViewFootMore.setText(R.string.load_ing);
					otherImageListViewFootProgress.setVisibility(View.VISIBLE);
					//当前pageIndex
					int pageIndex = otherImageListSumData/AppContext.PAGE_SIZE;
					loadOtherImageListData(curImageCatalog, pageIndex, otherImageListViewHandler, UIHelper.LISTVIEW_ACTION_SCROLL);
				}
			}
			public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
				otherImageListView.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
				if(otherImageListViewData.size()<=0) {
					otherImageListView.onScrollStateChanged(view, SCROLL_STATE_TOUCH_SCROLL);
				}
			}
		});
		otherImageListView.setOnRefreshListner(new PullToRefreshListView.OnRefreshListener() {
            public void onRefresh() {
            	loadSceneryImageListData(curImageCatalog, 0, otherImageListViewHandler, UIHelper.LISTVIEW_ACTION_REFRESH);
            }
        });					
	}
	
    public void handleImageListData(int what,Object obj,int objtype,int actiontype) {
    	switch (actiontype) {
		case UIHelper.LISTVIEW_ACTION_INIT:
		case UIHelper.LISTVIEW_ACTION_REFRESH:
		case UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG:
			switch (objtype) {
				case UIHelper.LISTVIEW_DATATYPE_BEAUTY:
					List<BaseImage> beaytyImageList = (List<BaseImage>)obj;
					beautyImageListSumData = what;
					beautyImageListViewData.clear();//先清除原有数据
					beautyImageListViewData.addAll(beaytyImageList);
					/*if(beautyImageListViewData.size()>0) {
						beautyImageListView.requestFocusFromTouch();
						beautyImageListView.setSelection(0);
					}*/
					break;
				case UIHelper.LISTVIEW_DATATYPE_SCENERY:
					List<BaseImage> sceneryImageList = (List<BaseImage>)obj;
					sceneryImageListSumData = what;
					sceneryImageListViewData.clear();//先清除原有数据
					sceneryImageListViewData.addAll(sceneryImageList);
					break;
				case UIHelper.LISTVIEW_DATATYPE_OTHER:
					List<BaseImage> otherImageList = (List<BaseImage>)obj;
					otherImageListSumData = what;
					otherImageListViewData.clear();//先清除原有数据
					otherImageListViewData.addAll(otherImageList);
					break;
			}
			break;
		case UIHelper.LISTVIEW_ACTION_SCROLL:
			switch (objtype) {
			case UIHelper.LISTVIEW_DATATYPE_BEAUTY:
				List<BaseImage> beaytyImageList = (List<BaseImage>)obj;
				beautyImageListSumData += what;
				if(beautyImageListViewData.size() > 0){
					for(BaseImage image : beaytyImageList){
						boolean b = false;
						for(BaseImage newsImage : beautyImageListViewData){
							if(image.equals(newsImage)){
								b = true;
								break;
							}
						}
						if(!b) beautyImageListViewData.add(image);
					}
				}else{
					beautyImageListViewData.addAll(beaytyImageList);
				}
				break;
			case UIHelper.LISTVIEW_DATATYPE_SCENERY:
				List<BaseImage> sceneryImageList = (List<BaseImage>)obj;
				sceneryImageListSumData += what;
				if(sceneryImageListViewData.size()>0) {
					for(BaseImage image : sceneryImageList){
						boolean b = false;
						for(BaseImage newsImage : sceneryImageListViewData){
							if(image.equals(newsImage)){
								b = true;
								break;
							}
						}
						if(!b) sceneryImageListViewData.add(image);
					}
				} else {
					sceneryImageListViewData.addAll(sceneryImageList);
				}
				break;
			case UIHelper.LISTVIEW_DATATYPE_OTHER:
				List<BaseImage> otherImageList = (List<BaseImage>)obj;
				otherImageListSumData += what;
				if(otherImageListViewData.size()>0) {
					for(BaseImage image : otherImageList){
						boolean b = false;
						for(BaseImage newsImage : otherImageListViewData){
							if(image.equals(newsImage)){
								b = true;
								break;
							}
						}
						if(!b) otherImageListViewData.add(image);
					}
				} else {
					otherImageListViewData.addAll(otherImageList);
				}
				break;
			}
    	}
    }
    
    private void loadImageListData(final int catalog,final int pageIndex, final Handler handler,final int action){ 
		mHeadProgress.setVisibility(ProgressBar.VISIBLE);		
		new Thread(){
			public void run() {				
				Message msg = new Message();
				boolean isRefresh = false;
				if(action == UIHelper.LISTVIEW_ACTION_REFRESH || action == UIHelper.LISTVIEW_ACTION_SCROLL) {
					isRefresh = true;
				}
				if(pageIndex ==0) {
					try {					
						beautyImageListTmp = appContext.getBeautyImageList(catalog, pageIndex, isRefresh);
						List<BaseImage> tmp = null;
						if(beautyImageListTmp.size()<=AppContext.PAGE_SIZE) {
							tmp = beautyImageListTmp.subList(0, beautyImageListTmp.size());
						}else {
							tmp = beautyImageListTmp.subList(0, AppContext.PAGE_SIZE);
						}
						msg.what = tmp.size();
						msg.obj = tmp;
					} catch (Exception e) {
						e.printStackTrace();
						msg.what = -1;
						msg.obj = e;
					}
				} else {
					List<BaseImage> tmp = new ArrayList<BaseImage>();
					for(int i=AppContext.PAGE_SIZE * pageIndex; i<AppContext.PAGE_SIZE * (pageIndex +1);i++) {
						if(i<beautyImageListTmp.size()) {
							tmp.add(beautyImageListTmp.get(i));
						}
					}
					msg.what = tmp.size();
					if(msg.what == 0) {
						msg.what = 1;
					}
					msg.obj = tmp;
					try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {
						e.printStackTrace();
						msg.what = -1;
						msg.obj = e;
					}
				}
				msg.arg1 = action;
				msg.arg2 = UIHelper.LISTVIEW_DATATYPE_BEAUTY;
                if(curImageCatalog == catalog) {
                	handler.sendMessage(msg);
                }
			}
		}.start();
	} 
    
    private void loadSceneryImageListData(final int catalog,final int pageIndex,final Handler handler,final int action){ 
		mHeadProgress.setVisibility(ProgressBar.VISIBLE);
		new Thread(){
			public void run() {
				Message msg = new Message();
				boolean isRefresh = false;
				if(action == UIHelper.LISTVIEW_ACTION_REFRESH || action == UIHelper.LISTVIEW_ACTION_SCROLL)
					isRefresh = true;
				if(pageIndex ==0) {
					try {					
						sceneryImageListTmp = appContext.getSceneryImageList(catalog, pageIndex, isRefresh);
						List<BaseImage> tmp = null;
						if(sceneryImageListTmp.size()<=AppContext.PAGE_SIZE) {
							tmp = sceneryImageListTmp.subList(0, sceneryImageListTmp.size());
						}else {
							tmp = sceneryImageListTmp.subList(0, AppContext.PAGE_SIZE);
						}
						msg.what = tmp.size();
						msg.obj = tmp;
					} catch (Exception e) {
						e.printStackTrace();
						msg.what = -1;
						msg.obj = e;
					}
				} else {
					List<BaseImage> tmp = new ArrayList<BaseImage>();
					for(int i=AppContext.PAGE_SIZE * pageIndex; i<AppContext.PAGE_SIZE * (pageIndex +1);i++) {
						if(i<sceneryImageListTmp.size()) {
							tmp.add(sceneryImageListTmp.get(i));
						}
					}
					msg.what = tmp.size();
					if(msg.what == 0) {
						msg.what = 1;
					}
					msg.obj = tmp;
					try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {
						e.printStackTrace();
						msg.what = -1;
						msg.obj = e;
					}
				}
				
				msg.arg1 = action;
				msg.arg2 = UIHelper.LISTVIEW_DATATYPE_SCENERY;
                if(curImageCatalog == catalog)
                	handler.sendMessage(msg);
			}
		}.start();
	} 
    
    private void loadOtherImageListData(final int catalog,final int pageIndex,final Handler handler,final int action){ 
		mHeadProgress.setVisibility(ProgressBar.VISIBLE);
		new Thread(){
			public void run() {
				Message msg = new Message();
				boolean isRefresh = false;
				if(action == UIHelper.LISTVIEW_ACTION_REFRESH || action == UIHelper.LISTVIEW_ACTION_SCROLL)
					isRefresh = true;
				if(pageIndex ==0) {
					try {					
						otherImageListTmp = appContext.getOtherImageList(catalog, pageIndex, isRefresh);
						List<BaseImage> tmp = null;
						if(otherImageListTmp.size()<=AppContext.PAGE_SIZE) {
							tmp = otherImageListTmp.subList(0, otherImageListTmp.size());
						}else {
							tmp = otherImageListTmp.subList(0, AppContext.PAGE_SIZE);
						}
						msg.what = tmp.size();
						msg.obj = tmp;
					} catch (Exception e) {
						e.printStackTrace();
						msg.what = -1;
						msg.obj = e;
					}
				} else {
					List<BaseImage> tmp = new ArrayList<BaseImage>();
					for(int i=AppContext.PAGE_SIZE * pageIndex; i<AppContext.PAGE_SIZE * (pageIndex +1);i++) {
						if(i<otherImageListTmp.size()) {
							tmp.add(otherImageListTmp.get(i));
						}
					}
					msg.what = tmp.size();
					if(msg.what == 0) {
						msg.what = 1;
					}
					msg.obj = tmp;
					try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {
						e.printStackTrace();
						msg.what = -1;
						msg.obj = e;
					}
				}
				msg.arg1 = action;
				msg.arg2 = UIHelper.LISTVIEW_DATATYPE_OTHER;
                if(curImageCatalog == catalog)
                	handler.sendMessage(msg);
			}
		}.start();
	} 
    
    @Override
    public void onResume() {
        super.onResume();
        StatService.onResume(this);
        childList = getChildList();  
		adapter.notifyDataSetChanged();
        mImageFetcher.setExitTasksEarly(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        StatService.onPause(this);
        mImageFetcher.setExitTasksEarly(true);
        mImageFetcher.flushCache();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mImageFetcher.closeCache();
    }
    
    
    @Override
	public void onEarnedPoints(Context context,
			List pointsList) {
		try {
			if (pointsList != null) {
				for (int i = 0; i < pointsList.size(); i++) {
					storePoints(context, (EarnedPointsOrder) pointsList.get(i));
					//recordOrder(context, (EarnedPointsOrder) pointsList.get(i));
				}
			} else {
				infoMsg("onPullPoints:pointsList is null");
			}
		} catch (Exception e) {
			if(BuildConfig.DEBUG && DEBUG) {
				Log.e(TAG, "### onEarnedPoints " + e.getLocalizedMessage());
			}
		}
	}
    
    /**
	 * 存储积分
	 * @param context
	 * @param order
	 */
	private void storePoints(Context context, EarnedPointsOrder order) {
		try {
			if (order != null) {
				if (order.getPoints() > 0) {
					SharedPreferences settings = context.getSharedPreferences(Main.class.getName(), Context.MODE_PRIVATE);
					int p = settings.getInt(curPhotoName, 0);
					p += order.getPoints();
					settings.edit().putInt(curPhotoName, p).commit();
					if(BuildConfig.DEBUG && DEBUG) {
						Log.e(TAG, "### store points = " + p );
					}
					/*if(p<100) {
						Toast.makeText(context, context.getString(R.string.drovik_play_regester_uncommplete_str, (100-p)), Toast.LENGTH_SHORT).show();
					}else {
						ToastUtils.showToast(context, R.string.drovik_play_regester_success_str);
					}*/
					//ToastUtils.showToast(context, R.string.drovik_play_regester_success_str);
					Toast.makeText(context, context.getString(R.string.drovik_play_earncore_success_str, curPhotoName), Toast.LENGTH_SHORT).show();
					StatService.onEvent(Main.this, "主界面", "成功注册 " + curPhotoName);
				}
			}
		} catch (Exception e) {
			if(BuildConfig.DEBUG && DEBUG) {
				Log.e(TAG, "### storePoints " + e.getLocalizedMessage());
			}
		}
	}
	
	/**
	 * 查询积分
	 * @param context
	 * @return
	 */
	public int queryPoints(Context context) {
		//Log.d(TAG, "### queryPoints = " + curPhotoName);
		SharedPreferences sp = context.getSharedPreferences(Main.class.getName(), Context.MODE_PRIVATE);
		return sp.getInt(curPhotoName, 0);
	}
	
	private void infoMsg(String msg) {
		Log.e("MyPointsManager", msg);
		StatService.onEvent(Main.this, "主界面", "注册失败 " + msg);
	}
	
	private void showErrDialog() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
        builder.setTitle(Res.string.drovik_no_authority_tips_str);
        builder.setMessage(appContext.getString(Res.string.drovik_no_authority_message_str));
    	builder.setPositiveButton(Res.string.drovik_no_authority_sure_button_str, new OnClickListener() {
    		public void onClick(DialogInterface dialog, int which) {
    			if(!isDeviceInvalid) {
    				YoumiOffersManager.showOffers(Main.this, YoumiOffersManager.TYPE_REWARD_OFFERS, Main.this);
    			} else {
    				ToastUtils.showToast(appContext, Res.string.drovik_play_invalid_device_str);
    			}
    		}
    	});
        builder.setNegativeButton(appContext.getString(Res.string.drovik_no_authority_cancle_button_str), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }
	
	@Override
	public void onCheckStatusConnectionFailed(Context arg0) {
		
	}
	
	@Override
	public void onCheckStatusResponse(Context context, boolean isAppInvalid,
			boolean isInTestMode, boolean isDeviceInvalid) {
		this.isDeviceInvalid = isDeviceInvalid;
		if(BuildConfig.DEBUG && DEBUG) {
			Log.d(TAG, "isAppInvalid = " + isAppInvalid + "  isInTestMode = " + isInTestMode + "  isDeviceInvalid = " + isDeviceInvalid);
		}
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
	
	public static String getImgagePath(int position, int catalog) {
		if(catalog == BaseImage.CATALOG_BEAUTY) {
			BeautyImage beautyImage = (BeautyImage) beautyImageListViewData.get(photoIndex);
			return beautyImage.getSrcArr()[position];
		}else if(catalog == BaseImage.CATALOG_SCENERY) {
			return sceneryImageListViewData.get(position).getSrc();
		}else {
			BeautyImage beautyImage = (BeautyImage) otherImageListViewData.get(photoIndex);
			return beautyImage.getSrcArr()[position];
		}
	}
}
