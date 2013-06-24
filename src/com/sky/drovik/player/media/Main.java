package com.sky.drovik.player.media;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.youmi.android.appoffers.CheckStatusNotifier;
import net.youmi.android.appoffers.EarnedPointsNotifier;
import net.youmi.android.appoffers.EarnedPointsOrder;
import net.youmi.android.appoffers.YoumiOffersManager;
import net.youmi.android.appoffers.YoumiPointsManager;
import net.youmi.push.android.YoumiPush;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
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
import com.sky.drovik.player.adpter.ListViewBeautyImageAdapter;
import com.sky.drovik.player.adpter.ListViewLocalAdapter;
import com.sky.drovik.player.adpter.ListViewLocalVideoAdapter;
import com.sky.drovik.player.adpter.ListViewOtherImageAdapter;
import com.sky.drovik.player.adpter.ListViewSceneryImageAdapter;
import com.sky.drovik.player.app.Res;
import com.sky.drovik.player.bitmapfun.FlingGalleryActivity;
import com.sky.drovik.player.bitmapfun.ImageCache.ImageCacheParams;
import com.sky.drovik.player.bitmapfun.ImageDetailActivity;
import com.sky.drovik.player.bitmapfun.ImageFetcher;
import com.sky.drovik.player.bitmapfun.ScrollyGalleryActivity;
import com.sky.drovik.player.engine.BeautyImage;
import com.sky.drovik.player.engine.UpdateManager;
import com.sky.drovik.player.exception.StringUtils;
import com.sky.drovik.player.pojo.BaseImage;
import com.sky.drovik.player.pojo.MovieInfo;
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
	private RadioButton footBarImage;
	private RadioButton footBarVideo;
	private ImageView footBarSetting;
	
	
	//local image contentvew
	private PullToRefreshListView localImageListView;
	private View localImageListViewFooter;
	private TextView localImageListViewFootMore;
	private ProgressBar localImageListViewFootProgress;
	private ListViewLocalAdapter localImageListViewAdapter;
	private static List<BaseImage> localImageListViewData = new ArrayList<BaseImage>();
	
	//beayty image contentvew
	private PullToRefreshListView beautyImageListView;
	private TextView beautyImageListViewFootMore;
	private View beautyImageListViewFooter;
	private ProgressBar beautyImageListViewFootProgress;
	private ListViewBeautyImageAdapter beautyImageListViewAdapter;
	private static List<BaseImage> beautyImageListViewData = new ArrayList<BaseImage>();
	
	//scenery image contentvew
	private PullToRefreshListView sceneryImageListView;
	private TextView sceneryImageListViewFootMore;
	private View sceneryImageListViewFooter;
	private ProgressBar sceneryImageListViewFootProgress;
	private ListViewSceneryImageAdapter sceneryImageListViewAdapter;
	private static List<BaseImage> sceneryImageListViewData = new ArrayList<BaseImage>();
		
	//other image contentvew
	private PullToRefreshListView otherImageListView;
	private TextView otherImageListViewFootMore;
	private View otherImageListViewFooter;
	private ProgressBar otherImageListViewFootProgress;
	private ListViewOtherImageAdapter otherImageListViewAdapter;
	private static List<BaseImage> otherImageListViewData = new ArrayList<BaseImage>();
	
	//video info
	private PullToRefreshListView localVideoListView;
	private TextView localVideoListViewFootMore;
	private View localVideoListViewFooter;
	private ProgressBar localVideoListViewFootProgress;
	private ListViewLocalVideoAdapter localVideoListViewAdapter;
	private static List<MovieInfo> localVideoListViewData = new ArrayList<MovieInfo>();
	
	//handler
	private Handler localImageListViewHandler;
	private Handler beautyImageListViewHandler;
	private Handler sceneryImageListViewHandler;
	private Handler otherImageListViewHandler;
	private Handler localVideoListViewHandler;
	
	//top buttons
	private Button frameLocalButton;
	private Button frameBeautyButton;
	private Button frameSceneryButton;
	private Button frameOtherButton;
	
	private int localImageListSumData;
	private int beautyImageListSumData;
	private int sceneryImageListSumData;
	private int otherImageListSumData;
	private int localVideoListSumData;
	
	private int curImageCatalog = BaseImage.CATALOG_LOCAL;
	private int curVideoCatalog = MovieInfo.CATALOG_LOCAL_VIDEO;
	
	private AppContext appContext;//全局Context
	
    private ImageFetcher mImageFetcher;
    
    private int mImageThumbWidth;
    private int mImageThumbHeight;
    
    private List<BaseImage> localImageListTmp = null;
    private List<BaseImage> beautyImageListTmp = null;
    private List<BaseImage> sceneryImageListTmp = null;
    private List<BaseImage> otherImageListTmp = null;
    
	public static int itemWidth;
	
	private final int update = 1;
	
	private static int photoIndex = 0;
	
	private String curPhotoName = ""; //当前相册名称
	
	private boolean isCheck = true;// 审核
	
	//ym
	public static final String KEY_POINTS="BEYING";
	private static final String KEY_FILE_ORDERS="Orders";
    private boolean isDeviceInvalid;
    private String channelId = "";
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//remove youmi
		/*if(isCheck) {
			YoumiOffersManager.init(this, "d56c188174986b81", "07603ef9797423c0");
			YoumiPointsManager.setUserID(this.getPackageName());
			YoumiPush.startYoumiPush(this, "d56c188174986b81", "07603ef9797423c0", false);
		}*/
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
        Intent intent = getIntent();
        if(intent != null) {
        	boolean flag = intent.getBooleanExtra("check_update", false);
        	UpdateManager.getUpdateManager().checkAppUpdate(this, flag);
        }
        if(isCheck) {
        	frameBeautyButton.setVisibility(View.INVISIBLE);
        	frameSceneryButton.setVisibility(View.INVISIBLE);
        	frameOtherButton.setVisibility(View.INVISIBLE);
        }
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
					if(mCurSel != pos) {
		    			switch (pos) {
						case 0://image
							localImageListView.clickRefresh();
							//beautyImageListView.clickRefresh();
							break;	
						case 1://video
							localVideoListView.clickRefresh();
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
		footBarImage = (RadioButton)findViewById(R.id.main_footbar_image);
		footBarVideo = (RadioButton)findViewById(R.id.main_footbar_video);
    	footBarSetting = (ImageView)findViewById(R.id.main_footbar_setting);
	}
	
    private void initFrameListView() {
    	//初始化listview控件
    	this.initLocalListView();
		this.initImageListView();
		this.initSceneryListView();
		this.initOtherListView();
		this.initLocalVideoListView();
		//加载listview数据
		this.initFrameListViewData();
    }
    
    /**
     * 初始化主页的按钮
     */
    private void initFrameButton() {
    	frameLocalButton = (Button)findViewById(R.id.frame_btn_local);
    	frameBeautyButton = (Button)findViewById(R.id.frame_btn_beauty);
    	frameSceneryButton = (Button)findViewById(R.id.frame_btn_scenery);
    	frameOtherButton = (Button)findViewById(R.id.frame_btn_other);
    	
    	frameLocalButton.setOnClickListener(frameNewsBtnClick(frameLocalButton, BaseImage.CATALOG_LOCAL));
    	frameBeautyButton.setOnClickListener(frameNewsBtnClick(frameBeautyButton, BaseImage.CATALOG_BEAUTY));
    	frameSceneryButton.setOnClickListener(frameNewsBtnClick(frameSceneryButton, BaseImage.CATALOG_SCENERY));
    	frameOtherButton.setOnClickListener(frameNewsBtnClick(frameOtherButton, BaseImage.CATALOG_OTHER));
    	
    	frameLocalButton.setFocusable(true);
    	frameLocalButton.setEnabled(false);
    }
    
    private View.OnClickListener frameNewsBtnClick(final Button btn,final int catalog){
    	return new View.OnClickListener() {
			public void onClick(View v) {
				if(btn == frameLocalButton){
					frameLocalButton.setEnabled(false);
		    	}else{
		    		frameLocalButton.setEnabled(true);
		    	}
				if(btn == frameBeautyButton){
					frameBeautyButton.setEnabled(false);
		    	}else{
		    		frameBeautyButton.setEnabled(true);
		    	}
		    	if(btn == frameSceneryButton){
		    		frameSceneryButton.setEnabled(false);
		    	}else{
		    		frameSceneryButton.setEnabled(true);
		    	}
		    	if(btn == frameOtherButton){
		    		frameOtherButton.setEnabled(false);
		    	}else{
		    		frameOtherButton.setEnabled(true);
		    	}
		    	curImageCatalog = catalog;
		    	if(btn == frameLocalButton) {
		    		localImageListView.setVisibility(View.VISIBLE);
		    		beautyImageListView.setVisibility(View.GONE);
		    		sceneryImageListView.setVisibility(View.GONE);
		    		otherImageListView.setVisibility(View.GONE);
		    		if(localImageListViewData.size() == 0) {
		    			loadLocalImageListData(curImageCatalog, 0, localImageListViewHandler, UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG);
		    		} else {
		    			localImageListViewFootMore.setText(R.string.load_more);
		    			localImageListViewFootProgress.setVisibility(View.GONE);
		    			loadLocalImageListData(curImageCatalog, 0, localImageListViewHandler, UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG);
		    		}
		    	} else if(btn == frameBeautyButton) {
		    		beautyImageListView.setVisibility(View.VISIBLE);
		    		localImageListView.setVisibility(View.GONE);
		    		sceneryImageListView.setVisibility(View.GONE);
		    		otherImageListView.setVisibility(View.GONE);
		    		if(beautyImageListViewData.size() == 0) {
		    			loadBeautyImageListData(curImageCatalog, 0, beautyImageListViewHandler, UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG);
		    		} else {
		    			beautyImageListViewFootMore.setText(R.string.load_more);
		    			beautyImageListViewFootProgress.setVisibility(View.GONE);
		    			loadBeautyImageListData(curImageCatalog, 0, beautyImageListViewHandler, UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG);
		    		}
		    	} else if(btn == frameSceneryButton ){
		    		sceneryImageListView.setVisibility(View.VISIBLE);
		    		localImageListView.setVisibility(View.GONE);
		    		beautyImageListView.setVisibility(View.GONE);
		    		otherImageListView.setVisibility(View.GONE);
		    		if(sceneryImageListViewData.size() == 0) {
		    			loadSceneryImageListData(curImageCatalog, 0, sceneryImageListViewHandler, UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG);
		    		} else {
		    			sceneryImageListViewFootMore.setText(R.string.load_ing);
		    			sceneryImageListViewFootProgress.setVisibility(View.GONE);
		    			loadSceneryImageListData(curImageCatalog, 0, sceneryImageListViewHandler, UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG);
		    		}
		    	} else if(btn == frameOtherButton ){
		    		otherImageListView.setVisibility(View.VISIBLE);
		    		localImageListView.setVisibility(View.GONE);
		    		beautyImageListView.setVisibility(View.GONE);
		    		sceneryImageListView.setVisibility(View.GONE);
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
    	localImageListViewHandler = this.getListViewHandler(localImageListView, localImageListViewAdapter, localImageListViewFootMore, localImageListViewFootProgress, AppContext.PAGE_SIZE);
        beautyImageListViewHandler = this.getListViewHandler(beautyImageListView, beautyImageListViewAdapter, beautyImageListViewFootMore, beautyImageListViewFootProgress, AppContext.PAGE_SIZE);
        sceneryImageListViewHandler = this.getListViewHandler(sceneryImageListView, sceneryImageListViewAdapter, sceneryImageListViewFootMore, sceneryImageListViewFootProgress, AppContext.PAGE_SIZE);
        otherImageListViewHandler = this.getListViewHandler(otherImageListView, otherImageListViewAdapter, otherImageListViewFootMore, otherImageListViewFootProgress, AppContext.PAGE_SIZE);
        localVideoListViewHandler = this.getListViewHandler(localVideoListView, localVideoListViewAdapter, localVideoListViewFootMore, localVideoListViewFootProgress, AppContext.PAGE_SIZE);
        //加载数据				
		if(localImageListViewData.size() == 0) {
			loadLocalImageListData(curImageCatalog, 0, localImageListViewHandler, UIHelper.LISTVIEW_ACTION_INIT);
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
    			}else if(msg.arg1 == UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG) {
    				imageListView.onRefreshComplete();
    				imageListView.setSelection(0);
    			}
    		}
    	};
    }

    //init local image listview
    private void initLocalListView() {
    	localImageListViewAdapter = new ListViewLocalAdapter(this, localImageListViewData,  R.layout.layout_local_image_list_item, mImageFetcher);
    	localImageListView = (PullToRefreshListView)findViewById(R.id.frame_list_view_local_image);
    	localImageListViewFooter = getLayoutInflater().inflate(R.layout.layout_list_view_footer, null);
    	localImageListViewFootMore = (TextView)localImageListViewFooter.findViewById(R.id.list_view_foot_more);
    	localImageListViewFootProgress = (ProgressBar)localImageListViewFooter.findViewById(R.id.list_view_foot_progress);
    	localImageListView.addFooterView(localImageListViewFooter);
    	localImageListView.setAdapter(localImageListViewAdapter);
    	localImageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
          	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
          		//点击头部、底部栏无效
          		if(position == 0){
          			return;
          		}
          		if(view == localImageListViewFooter) {
          			localImageListViewFootMore.setText(R.string.load_ing);
          			localImageListViewFootProgress.setVisibility(View.VISIBLE);
					//当前pageIndex
					int pageIndex = localImageListSumData/AppContext.PAGE_SIZE;
					loadLocalImageListData(curImageCatalog, pageIndex, localImageListViewHandler, UIHelper.LISTVIEW_ACTION_SCROLL);
          			return;
          		}
          		//Intent i = new Intent(appContext, FlingGalleryActivity.class);
          		Intent i = new Intent(appContext, ScrollyGalleryActivity.class);
          		//final Intent i = new Intent(appContext, ImageDetailActivity.class);
                i.putExtra(ImageDetailActivity.EXTRA_IMAGE, 0);
                if(position - 1>=0 && position - 1 < localImageListViewData.size()) {
                	photoIndex = position - 1;
                	BeautyImage beautyImage = (BeautyImage) localImageListViewData.get(position-1);
                	curPhotoName = beautyImage.getName();
                	//TODO
                	/*if(beautyImage.getId()>0 && queryPoints(appContext)<= 0 && !beautyImage.getChannel().contains(channelId)) {
                		showErrDialog();//id==0表示审核 >0 注册  没有包含channel的话 也表示需要注册了。所以通过审核以后需要将channel值0
                		return;
                	}*/
                	i.putExtra(ImageDetailActivity.LIST_SIZE, beautyImage.getSrcSize());
                	i.putExtra(FlingGalleryActivity.IMAGE_SRC_LIST, beautyImage.getSrcArr());//文件地址列表
                }else {
                	photoIndex = position - 1;
                	i.putExtra(ImageDetailActivity.LIST_SIZE, 0);
                	i.putExtra(FlingGalleryActivity.IMAGE_SRC_LIST, new String[0]);
                	
                }
                i.putExtra(ImageDetailActivity.CATA_LOG, curImageCatalog);
                startActivity(i);
          	}        	
  		});
    	localImageListView.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				localImageListView.onScrollStateChanged(view, scrollState);		
				//数据为空--不用继续下面代码了
				if(localImageListViewData.size() == 0) return;
				//判断是否滚动到底部
				 if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
	                  mImageFetcher.setPauseWork(true);
	             } else {
	                  mImageFetcher.setPauseWork(false);
	             }
				boolean scrollEnd = false;
				try{
					if(view.getPositionForView(localImageListViewFooter) == view.getLastVisiblePosition()) {
						scrollEnd = true;
					}
				} catch (Exception e) {
					scrollEnd = false;
				}
				int imageListDataState = StringUtils.toInt(localImageListView.getTag());
				if(scrollEnd && imageListDataState == UIHelper.LISTVIEW_DATA_MORE) {
					localImageListViewFootMore.setText(R.string.load_ing);
					localImageListViewFootProgress.setVisibility(View.VISIBLE);
					//当前pageIndex
					int pageIndex = localImageListSumData/AppContext.PAGE_SIZE;
					loadLocalImageListData(curImageCatalog, pageIndex, localImageListViewHandler, UIHelper.LISTVIEW_ACTION_SCROLL);
				}
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				localImageListView.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
				if(localImageListViewData.size()<=0) {
					localImageListView.onScrollStateChanged(view, SCROLL_STATE_TOUCH_SCROLL);
				}
			}
		});
    	localImageListView.setOnRefreshListner(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				loadLocalImageListData(curImageCatalog, 0, localImageListViewHandler, UIHelper.LISTVIEW_ACTION_REFRESH);
			}
		});
    }
    
	private void initImageListView() {
    	beautyImageListViewAdapter = new ListViewBeautyImageAdapter(this, beautyImageListViewData,  R.layout.layout_beauty_image_list_item, mImageFetcher);
    	beautyImageListView = (PullToRefreshListView)findViewById(R.id.frame_list_view_beayty_image);
    	beautyImageListViewFooter = getLayoutInflater().inflate(R.layout.layout_list_view_footer, null);
    	beautyImageListViewFootMore = (TextView)beautyImageListViewFooter.findViewById(R.id.list_view_foot_more);
    	beautyImageListViewFootProgress = (ProgressBar)beautyImageListViewFooter.findViewById(R.id.list_view_foot_progress);
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
					loadBeautyImageListData(curImageCatalog, pageIndex, beautyImageListViewHandler, UIHelper.LISTVIEW_ACTION_SCROLL);
          			return;
          		}
          		//Intent i = new Intent(appContext, FlingGalleryActivity.class);
          		Intent i = new Intent(appContext, ScrollyGalleryActivity.class);
          		//final Intent i = new Intent(appContext, ImageDetailActivity.class);
                i.putExtra(ImageDetailActivity.EXTRA_IMAGE, 0);
                if(position - 1>=0 && position - 1 < beautyImageListViewData.size()) {
                	photoIndex = position - 1;
                	BeautyImage beautyImage = (BeautyImage) beautyImageListViewData.get(position-1);
                	curPhotoName = beautyImage.getName();
                	/*if(beautyImage.getId()>0 && queryPoints(appContext)<= 0 && !beautyImage.getChannel().contains(channelId)) {
                		showErrDialog();//id==0表示审核 >0 注册  没有包含channel的话 也表示需要注册了。所以通过审核以后需要将channel值0
                		return;
                	}*/
                	i.putExtra(ImageDetailActivity.LIST_SIZE, beautyImage.getSrcSize());
                	//new add when add imageflinggallery
                	i.putExtra(FlingGalleryActivity.IMAGE_SRC_LIST, beautyImage.getSrcArr());//文件地址列表
                }else {
                	photoIndex = position - 1;
                	i.putExtra(ImageDetailActivity.LIST_SIZE, 0);
                	i.putExtra(FlingGalleryActivity.IMAGE_SRC_LIST, new String[0]);
                	
                }
                i.putExtra(ImageDetailActivity.CATA_LOG, curImageCatalog);
                /*if (com.sky.drovik.player.bitmapfun.Utils.hasJellyBean()) {
                    // makeThumbnailScaleUpAnimation() looks kind of ugly here as the loading spinner may
                    // show plus the thumbnail image in GridView is cropped. so using
                    // makeScaleUpAnimation() instead.
                    ActivityOptions options =
                            ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getWidth(), view.getHeight());
                    startActivity(i, options.toBundle());
                } else {
                    startActivity(i);
                }*/
                startActivity(i);
           		
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
					if(view.getPositionForView(beautyImageListViewFootMore) == view.getLastVisiblePosition()) {
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
					loadBeautyImageListData(curImageCatalog, pageIndex, beautyImageListViewHandler, UIHelper.LISTVIEW_ACTION_SCROLL);
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
				loadBeautyImageListData(curImageCatalog, 0, beautyImageListViewHandler, UIHelper.LISTVIEW_ACTION_REFRESH);
			}
		});
    }

    //init sencerty
	private void initSceneryListView() {
		sceneryImageListViewAdapter = new ListViewSceneryImageAdapter(this, sceneryImageListViewData, R.layout.layout_scenery_image_list_item, mImageFetcher);        
        sceneryImageListView = (PullToRefreshListView)findViewById(R.id.frame_list_view_secenry_image);
    	sceneryImageListViewFooter = getLayoutInflater().inflate(R.layout.layout_list_view_footer, null);
    	sceneryImageListViewFootMore = (TextView)sceneryImageListViewFooter.findViewById(R.id.list_view_foot_more);
    	sceneryImageListViewFootProgress = (ProgressBar)sceneryImageListViewFooter.findViewById(R.id.list_view_foot_progress);
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
          		/*if(sceneryBaseImage.getId()>0 && queryPoints(appContext)<= 0 && !sceneryBaseImage.getChannel().contains(channelId)) {
            		showErrDialog();
            		return;
            	}*/
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
		otherImageListView = (PullToRefreshListView)findViewById(R.id.frame_list_view_other_image);
    	otherImageListViewFooter = getLayoutInflater().inflate(R.layout.layout_list_view_footer, null);
    	otherImageListViewFootMore = (TextView)otherImageListViewFooter.findViewById(R.id.list_view_foot_more);
    	otherImageListViewFootProgress = (ProgressBar)otherImageListViewFooter.findViewById(R.id.list_view_foot_progress);
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
                	/*if(beautyImage.getId()>0 && queryPoints(appContext)<= 0 && !beautyImage.getChannel().contains(channelId)) {
                		showErrDialog();
                		return;
                	}*/
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
            	loadOtherImageListData(curImageCatalog, 0, otherImageListViewHandler, UIHelper.LISTVIEW_ACTION_REFRESH);
            }
        });					
	}
	
	private void initLocalVideoListView() {
		localVideoListViewAdapter = new ListViewLocalVideoAdapter(this, localVideoListViewData, R.layout.layout_local_video_list_item, mImageFetcher);
		localVideoListView = (PullToRefreshListView) findViewById(R.id.frame_list_view_local_video);
    	localVideoListViewFooter = getLayoutInflater().inflate(R.layout.layout_list_view_footer, null);
    	localVideoListViewFootMore = (TextView)localVideoListViewFooter.findViewById(R.id.list_view_foot_more);
    	localVideoListViewFootProgress = (ProgressBar)localVideoListViewFooter.findViewById(R.id.list_view_foot_progress);
		localVideoListView.addFooterView(localVideoListViewFooter);
		localVideoListView.setAdapter(localVideoListViewAdapter);
		localVideoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		//点击头部、底部栏无效
        		if(position == 0){
        			return;
        		}
          		if(view == localVideoListViewFooter) {
					localVideoListViewFootMore.setText(R.string.load_ing);
					localVideoListViewFootProgress.setVisibility(View.VISIBLE);
					//当前pageIndex
					int pageIndex = localVideoListSumData/AppContext.PAGE_SIZE;
					loadLocalVideoListData(curVideoCatalog, pageIndex, localVideoListViewHandler, UIHelper.LISTVIEW_ACTION_SCROLL);
          			return;
          		}
          		if(position - 1>=0 && position - 1 < localVideoListViewData.size()) {
          			ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
          			ConfigurationInfo configurationInfo = am.getDeviceConfigurationInfo();
          			if(configurationInfo.reqGlEsVersion <0x20000) {
          				ToastUtils.showToast(Main.this, R.string.drovik_play_ffmpeg_lower_system_version_str);
          				return;
					}
                	photoIndex = position - 1;
                	MovieInfo info = (MovieInfo) localVideoListViewData.get(position-1);
                	startActivity(info.intent);
                	//TODO
/*                	if(queryPoints(appContext)<= 0) {
                		showErrDialog();
                		return;
                	}
*/                }
        	}        	
		});
		localVideoListView.setOnScrollListener(new AbsListView.OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				localVideoListView.onScrollStateChanged(view, scrollState);
				
				//数据为空--不用继续下面代码了
				if(localVideoListViewData.size() == 0) return;
				//判断是否滚动到底部
				boolean scrollEnd = false;
				try {
					if(view.getPositionForView(localVideoListViewFooter) == view.getLastVisiblePosition())
						scrollEnd = true;
				} catch (Exception e) {
					scrollEnd = false;
				}
				
				int lvDataState = StringUtils.toInt(localVideoListView.getTag());
				if(scrollEnd && lvDataState==UIHelper.LISTVIEW_DATA_MORE)
				{
					localVideoListViewFootMore.setText(R.string.load_ing);
					localVideoListViewFootProgress.setVisibility(View.VISIBLE);
					//当前pageIndex
					int pageIndex = localVideoListSumData/AppContext.PAGE_SIZE;
					loadLocalVideoListData(curVideoCatalog, pageIndex, localVideoListViewHandler, UIHelper.LISTVIEW_ACTION_SCROLL);
				}
			}
			public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
				localVideoListView.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
				if(localVideoListViewData.size()<=0) {
					localVideoListView.onScrollStateChanged(view, SCROLL_STATE_TOUCH_SCROLL);
				}
			}
		});
		localVideoListView.setOnRefreshListner(new PullToRefreshListView.OnRefreshListener() {
            public void onRefresh() {
            	loadLocalVideoListData(curVideoCatalog, 0, localVideoListViewHandler, UIHelper.LISTVIEW_ACTION_REFRESH);
            }
        });		
	}
    public void handleImageListData(int what,Object obj,int objtype,int actiontype) {
    	switch (actiontype) {
		case UIHelper.LISTVIEW_ACTION_INIT:
		case UIHelper.LISTVIEW_ACTION_REFRESH:
		case UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG:
			switch (objtype) {
				case UIHelper.LISTVIEW_DATATYPE_LOCAL:
					List<BaseImage> localImageList = (List<BaseImage>)obj;
					localImageListSumData = what;
					localImageListViewData.clear();//先清除原有数据
					localImageListViewData.addAll(localImageList);
					break;
				case UIHelper.LISTVIEW_DATATYPE_BEAUTY:
					List<BaseImage> beaytyImageList = (List<BaseImage>)obj;
					beautyImageListSumData = what;
					beautyImageListViewData.clear();//先清除原有数据
					beautyImageListViewData.addAll(beaytyImageList);
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
				case UIHelper.LISTVIEW_DATATYPE_LOCAL_VIDEO:
					List<MovieInfo> localVideoList = (List<MovieInfo>)obj;
					localVideoListSumData = what;
					localVideoListViewData.clear();//先清除原有数据
					localVideoListViewData.addAll(localVideoList);
					break;
			}
			break;
		case UIHelper.LISTVIEW_ACTION_SCROLL:
			switch (objtype) {
			case UIHelper.LISTVIEW_DATATYPE_LOCAL:
				List<BaseImage> localImageList = (List<BaseImage>)obj;
				localImageListSumData += what;
				if(localImageListViewData.size() > 0){
					for(BaseImage image : localImageList){
						boolean b = false;
						for(BaseImage newsImage : localImageListViewData){
							if(image.equals(newsImage)){
								b = true;
								break;
							}
						}
						if(!b) localImageListViewData.add(image);
					}
				}else{
					localImageListViewData.addAll(localImageList);
				}
				break;
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
			case UIHelper.LISTVIEW_DATATYPE_LOCAL_VIDEO:
				List<MovieInfo> localVideoList = (List<MovieInfo>)obj;
				localVideoListSumData += what;
				if(localVideoListViewData.size()>0) {
					for(MovieInfo info : localVideoList){
						boolean b = false;
						for(MovieInfo newInfo : localVideoListViewData){
							if(info.equals(newInfo)){
								b = true;
								break;
							}
						}
						if(!b) localVideoListViewData.add(info);
					}
				} else {
					localVideoListViewData.addAll(localVideoList);
				}
				break;
			}
    	}
    }
    
    private void loadLocalImageListData(final int catalog,final int pageIndex, final Handler handler,final int action){ 
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
						localImageListTmp = appContext.getLocalImageList(catalog, pageIndex, isRefresh);
						List<BaseImage> tmp = null;
						if(localImageListTmp.size()<=AppContext.PAGE_SIZE) {
							tmp = localImageListTmp.subList(0, localImageListTmp.size());
						}else {
							tmp = localImageListTmp.subList(0, AppContext.PAGE_SIZE);
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
						if(i<localImageListTmp.size()) {
							tmp.add(localImageListTmp.get(i));
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
				msg.arg2 = UIHelper.LISTVIEW_DATATYPE_LOCAL;
                if(curImageCatalog == catalog) {
                	handler.sendMessage(msg);
                }
			}
		}.start();
	} 
    
    private void loadBeautyImageListData(final int catalog,final int pageIndex, final Handler handler,final int action){ 
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
    
    private void loadLocalVideoListData(final int catalog,final int pageIndex,final Handler handler,final int action) {
    	mHeadProgress.setVisibility(ProgressBar.VISIBLE);
		new Thread(){
			public void run() {
				Message msg = new Message();
				boolean isRefresh = false;
				if(action == UIHelper.LISTVIEW_ACTION_REFRESH || action == UIHelper.LISTVIEW_ACTION_SCROLL)
					isRefresh = true;
				try {					
					MediaList media = new MediaList(appContext);
					List<MovieInfo> videoList = media.getVideoListByPage(pageIndex * AppContext.PAGE_SIZE, AppContext.PAGE_SIZE);
					msg.what = videoList.size();
					msg.obj = videoList;
				} catch (Exception e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				}
				msg.arg1 = action;
				msg.arg2 = UIHelper.LISTVIEW_DATATYPE_LOCAL_VIDEO;
                if(curVideoCatalog == catalog)
                	handler.sendMessage(msg);
			}
		}.start();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        StatService.onResume(this);
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
        //TODO
        beautyImageListViewData.clear();
        sceneryImageListViewData.clear();
        otherImageListViewData.clear();
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
