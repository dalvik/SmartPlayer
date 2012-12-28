package com.sky.drovik.player.media;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import com.sky.drovik.entity.Image;
import com.sky.drovik.entity.UIHelper;
import com.sky.drovik.player.AppContext;
import com.sky.drovik.player.BuildConfig;
import com.sky.drovik.player.R;
import com.sky.drovik.player.adpter.ListViewImageAdapter;
import com.sky.drovik.player.adpter.ListViewSceneryImageAdapter;
import com.sky.drovik.player.exception.AppException;
import com.sky.drovik.player.exception.StringUtils;
import com.sky.drovik.player.pojo.BaseImage;
import com.sky.drovik.utils.ImageCache.ImageCacheParams;
import com.sky.drovik.utils.ImageFetcher;
import com.sky.drovik.widget.PullToRefreshListView;
import com.sky.drovik.widget.PullToRefreshListView.OnRefreshListener;
import com.sky.drovik.widget.ScrollLayout;

public class Main extends Activity {

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
	private List<BaseImage> beautyImageListViewData = new ArrayList<BaseImage>();
	private View imageListViewFooter;
	private TextView imageListViewFootMore;
	private ProgressBar imageListViewFootProgress;
	
	//scenery image contentvew
	private PullToRefreshListView sceneryImageListView;
	private ListViewSceneryImageAdapter sceneryImageListViewAdapter;
	private List<BaseImage> sceneryImageListViewData = new ArrayList<BaseImage>();
	private View sceneryimageListViewFooter;
	private TextView sceneryImageListViewFootMore;
	private ProgressBar sceneryImageListViewFootProgress;
		
		
	//handler
	private Handler beaytyImageListViewHandler;
	private Handler sceneryImageListViewHandler;
	
	
	//top buttons
	private Button frameBeautyButton;
	private Button frameSceneryButton;
	private Button frameOtherButton;
	
	private int beautyImageListSumData;
	private int sceneryImageListSumData;
	
	private int curImageCatalog = Image.CATALOG_ALL;
	
	private AppContext appContext;//全局Context
	
    private ImageFetcher mImageFetcher;
    
    private int mImageThumbSize;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_main);
		appContext = (AppContext)getApplication();
		mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
		mImageFetcher = new ImageFetcher(appContext, mImageThumbSize);
		mImageFetcher.setLoadingImage(R.drawable.empty_photo);
		ImageCacheParams cacheParams = new ImageCacheParams(appContext, IMAGE_CACHE_DIR);
		mImageFetcher.addImageCache(appContext, cacheParams);
		this.initHeadView();
		this.initFootBar();
		this.initPageScroll();
		this.initFrameButton();
        this.initFrameListView();
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
							break;	
						case 1://video
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
    		mHeadLogo.setImageResource(R.drawable.frame_logo_news);
    	} else if(index == 1){
    		mHeadLogo.setImageResource(R.drawable.frame_icon_post);
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
		//加载listview数据
		this.initFrameListViewData();
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
    	
    	
    }
    
    private View.OnClickListener frameNewsBtnClick(final Button btn,final int catalog){
    	return new View.OnClickListener() {
			public void onClick(View v) {
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
		    	if(btn == frameBeautyButton) {
		    		beautyImageListView.setVisibility(View.VISIBLE);
		    		sceneryImageListView.setVisibility(View.GONE);
		    		loadImageListData(curImageCatalog, 0, sceneryImageListViewHandler, UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG);
		    	} else {
		    		beautyImageListView.setVisibility(View.GONE);
		    		sceneryImageListView.setVisibility(View.VISIBLE);
		    		if(sceneryImageListViewData.size() == 0) {
		    			loadSceneryImageListData(curImageCatalog, 0, sceneryImageListViewHandler, UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG);
		    		} else {
		    			sceneryImageListViewFootMore.setText(R.string.load_more);
		    			sceneryImageListViewFootProgress.setVisibility(View.GONE);
		    			loadSceneryImageListData(curImageCatalog, 0, sceneryImageListViewHandler, UIHelper.LISTVIEW_ACTION_CHANGE_CATALOG);
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
        beaytyImageListViewHandler = this.getListViewHandler(beautyImageListView, beautyImageListViewAdapter, imageListViewFootMore, imageListViewFootProgress, AppContext.PAGE_SIZE);
        sceneryImageListViewHandler = this.getListViewHandler(sceneryImageListView, sceneryImageListViewAdapter, sceneryImageListViewFootMore, sceneryImageListViewFootProgress, AppContext.PAGE_SIZE);
        //加载数据				
		if(beautyImageListViewData.size() == 0) {
			loadImageListData(curImageCatalog, 0, beaytyImageListViewHandler, UIHelper.LISTVIEW_ACTION_INIT);
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

    private void initImageListView() {
    	beautyImageListViewAdapter = new ListViewImageAdapter(this, beautyImageListViewData,  R.layout.layout_image_list_item, mImageFetcher);
    	imageListViewFooter = getLayoutInflater().inflate(R.layout.layout_list_view_footer, null);
    	imageListViewFootMore = (TextView)imageListViewFooter.findViewById(R.id.list_view_foot_more);
    	imageListViewFootProgress = (ProgressBar)imageListViewFooter.findViewById(R.id.list_view_foot_progress);
    	beautyImageListView = (PullToRefreshListView)findViewById(R.id.frame_list_view_beayty_image);
    	beautyImageListView.addFooterView(imageListViewFooter);
    	beautyImageListView.setAdapter(beautyImageListViewAdapter);
    	beautyImageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
          	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
          		//点击头部、底部栏无效
          		if(position == 0 || view == imageListViewFooter) return;
          		
          		Image image = null;        		
          		//判断是否是TextView
          		if(view instanceof TextView){
          			image = (Image)view.getTag();
          		}else{
          			TextView tv = (TextView)view.findViewById(R.id.image_list_item_name);
          			image = (Image)tv.getTag();
          		}
          		if(image == null) return;
          		//TODO
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
					if(view.getPositionForView(imageListViewFooter) == view.getLastVisiblePosition()) {
						scrollEnd = true;
					}
				} catch (Exception e) {
					scrollEnd = false;
				}
				int imageListDataState = StringUtils.toInt(beautyImageListView.getTag());
				if(scrollEnd && imageListDataState == 1) {
					
				}
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				beautyImageListView.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
			}
		});
    	beautyImageListView.setOnRefreshListner(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				loadImageListData(curImageCatalog, 0, beaytyImageListViewHandler, UIHelper.LISTVIEW_ACTION_REFRESH);
			}
		});
    }

    //init sencerty
	private void initSceneryListView() {
		sceneryImageListViewAdapter = new ListViewSceneryImageAdapter(this, sceneryImageListViewData, R.layout.layout_scenery_image_list_item, mImageFetcher);        
        sceneryimageListViewFooter = getLayoutInflater().inflate(R.layout.layout_list_view_footer, null);
        sceneryImageListViewFootMore = (TextView)sceneryimageListViewFooter.findViewById(R.id.list_view_foot_more);
        sceneryImageListViewFootProgress = (ProgressBar)sceneryimageListViewFooter.findViewById(R.id.list_view_foot_progress);
        sceneryImageListView = (PullToRefreshListView)findViewById(R.id.frame_list_view_secenry_image);
        sceneryImageListView.addFooterView(sceneryimageListViewFooter);//添加底部视图  必须在setAdapter前
        sceneryImageListView.setAdapter(sceneryImageListViewAdapter); 
        sceneryImageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		//点击头部、底部栏无效
        		if(position == 0 || view == sceneryImageListView) return;
        		
        		/*Blog blog = null;        		
        		//判断是否是TextView
        		if(view instanceof TextView){
        			blog = (Blog)view.getTag();
        		}else{
        			TextView tv = (TextView)view.findViewById(R.id.blog_listitem_title);
        			blog = (Blog)tv.getTag();
        		}
        		if(blog == null) return;*/
        		//TODO
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
					if(view.getPositionForView(sceneryimageListViewFooter) == view.getLastVisiblePosition())
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
			}
		});
        sceneryImageListView.setOnRefreshListner(new PullToRefreshListView.OnRefreshListener() {
            public void onRefresh() {
            	loadSceneryImageListData(curImageCatalog, 0, sceneryImageListViewHandler, UIHelper.LISTVIEW_ACTION_REFRESH);
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
					break;
				case UIHelper.LISTVIEW_DATATYPE_SCENERY:
					List<BaseImage> sceneryImageList = (List<BaseImage>)obj;
					sceneryImageListSumData = what;
					sceneryImageListViewData.clear();//先清除原有数据
					sceneryImageListViewData.addAll(sceneryImageList);
					/*BlogList blist = (BlogList)obj;
					notice = blist.getNotice();
					lvBlogSumData = what;
					lvBlogData.clear();//先清除原有数据
					lvBlogData.addAll(blist.getBloglist());*/
					break;
				case UIHelper.LISTVIEW_DATATYPE_POST:
					/*PostList plist = (PostList)obj;
					notice = plist.getNotice();
					lvQuestionSumData = what;
					lvQuestionData.clear();//先清除原有数据
					lvQuestionData.addAll(plist.getPostlist());*/
					break;
				case UIHelper.LISTVIEW_DATATYPE_TWEET:
					/*TweetList tlist = (TweetList)obj;
					notice = tlist.getNotice();
					lvTweetSumData = what;
					lvTweetData.clear();//先清除原有数据
					lvTweetData.addAll(tlist.getTweetlist());*/
					break;
				case UIHelper.LISTVIEW_DATATYPE_ACTIVE:
					/*ActiveList alist = (ActiveList)obj;
					notice = alist.getNotice();
					lvActiveSumData = what;
					lvActiveData.clear();//先清除原有数据
					lvActiveData.addAll(alist.getActivelist());*/
					break;
				case UIHelper.LISTVIEW_DATATYPE_MESSAGE:
					/*MessageList mlist = (MessageList)obj;
					notice = mlist.getNotice();
					lvMsgSumData = what;
					lvMsgData.clear();//先清除原有数据
					lvMsgData.addAll(mlist.getMessagelist());*/
					break;
			}
			break;
		case UIHelper.LISTVIEW_ACTION_SCROLL:
			switch (objtype) {
			case UIHelper.LISTVIEW_DATATYPE_BEAUTY:
				List<BaseImage> beaytyImageList = (List<BaseImage>)obj;
				//notice = list.getNotice();
				beautyImageListSumData += what;
				if(beautyImageListViewData.size() > 0){
					for(BaseImage image : beaytyImageList){
						boolean b = false;
						/*for(News news2 : lvNewsData){
							if(news1.getId() == news2.getId()){
								b = true;
								break;
							}
						}
						if(!b) imageListViewData.add(image);*/
					}
				}else{
					beautyImageListViewData.addAll(beaytyImageList);
				}
				break;
			case UIHelper.LISTVIEW_DATATYPE_SCENERY:
				List<BaseImage> sceneryImageList = (List<BaseImage>)obj;
				//notice = list.getNotice();
				sceneryImageListSumData += what;
				if(sceneryImageListViewData.size()>0) {
					
				} else {
					sceneryImageListViewData.addAll(sceneryImageList);
				}
				/*BlogList blist = (BlogList)obj;
				notice = blist.getNotice();
				lvBlogSumData += what;
				if(lvBlogData.size() > 0){
					for(Blog blog1 : blist.getBloglist()){
						boolean b = false;
						for(Blog blog2 : lvBlogData){
							if(blog1.getId() == blog2.getId()){
								b = true;
								break;
							}
						}
						if(!b) lvBlogData.add(blog1);
					}
				}else{
					lvBlogData.addAll(blist.getBloglist());
				}*/
				break;
			case UIHelper.LISTVIEW_DATATYPE_POST:
				/*PostList plist = (PostList)obj;
				notice = plist.getNotice();
				lvQuestionSumData += what;
				if(lvQuestionData.size() > 0){
					for(Post post1 : plist.getPostlist()){
						boolean b = false;
						for(Post post2 : lvQuestionData){
							if(post1.getId() == post2.getId()){
								b = true;
								break;
							}
						}
						if(!b) lvQuestionData.add(post1);
					}
				}else{
					lvQuestionData.addAll(plist.getPostlist());
				}*/
				break;
			case UIHelper.LISTVIEW_DATATYPE_TWEET:
				/*TweetList tlist = (TweetList)obj;
				notice = tlist.getNotice();
				lvTweetSumData += what;
				if(lvTweetData.size() > 0){
					for(Tweet tweet1 : tlist.getTweetlist()){
						boolean b = false;
						for(Tweet tweet2 : lvTweetData){
							if(tweet1.getId() == tweet2.getId()){
								b = true;
								break;
							}
						}
						if(!b) lvTweetData.add(tweet1);
					}
				}else{
					lvTweetData.addAll(tlist.getTweetlist());
				}*/
				break;
			case UIHelper.LISTVIEW_DATATYPE_ACTIVE:
				/*ActiveList alist = (ActiveList)obj;
				notice = alist.getNotice();
				lvActiveSumData += what;
				if(lvActiveData.size() > 0){
					for(Active active1 : alist.getActivelist()){
						boolean b = false;
						for(Active active2 : lvActiveData){
							if(active1.getId() == active2.getId()){
								b = true;
								break;
							}
						}
						if(!b) lvActiveData.add(active1);
					}
				}else{
					lvActiveData.addAll(alist.getActivelist());
				}*/
				break;
			case UIHelper.LISTVIEW_DATATYPE_MESSAGE:
				/*MessageList mlist = (MessageList)obj;
				notice = mlist.getNotice();
				lvMsgSumData += what;
				if(lvMsgData.size() > 0){
					for(Messages msg1 : mlist.getMessagelist()){
						boolean b = false;
						for(Messages msg2 : lvMsgData){
							if(msg1.getId() == msg2.getId()){
								b = true;
								break;
							}
						}
						if(!b) lvMsgData.add(msg1);
					}
				}else{
					lvMsgData.addAll(mlist.getMessagelist());
				}*/
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
				try {					
					List<BaseImage> list = appContext.getImageList(catalog, pageIndex, isRefresh);	
					msg.what = list.size();
					msg.obj = list;
	            } catch (Exception e) {
	            	e.printStackTrace();
	            	msg.what = -1;
	            	msg.obj = e;
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
				/*String type = "";
				switch (catalog) {
				case BaseImage.CATALOG_SCENERY:
					type = BaseImage.TYPE_SCENERY;
					break;
				case BaseImage.CATALOG_OTHER:
					type = BaseImage.TYPE_OTHER;
					break;
				}*/
				try {
					//BlogList list = appContext.getBlogList(type, pageIndex, isRefresh);		
					List<BaseImage> list = appContext.getImageList(catalog, pageIndex, isRefresh);	
					for(BaseImage l:list) {
						System.out.println(l.toString());
					}
					msg.what = list.size();
					msg.obj = list;
	            } catch (AppException e) {
	            	e.printStackTrace();
	            	msg.what = -1;
	            	msg.obj = e;
	            }
				msg.arg1 = action;
				msg.arg2 = UIHelper.LISTVIEW_DATATYPE_SCENERY;
                if(curImageCatalog == catalog)
                	handler.sendMessage(msg);
			}
		}.start();
	} 
    
    @Override
    public void onResume() {
        super.onResume();
        mImageFetcher.setExitTasksEarly(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        mImageFetcher.setExitTasksEarly(true);
        mImageFetcher.flushCache();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mImageFetcher.closeCache();
    }
}
