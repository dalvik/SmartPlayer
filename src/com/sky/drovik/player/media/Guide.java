package com.sky.drovik.player.media;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.baidu.mobstat.StatService;
import com.sky.drovik.player.BuildConfig;
import com.sky.drovik.player.R;

public class Guide extends Activity implements OnClickListener, OnPageChangeListener ,OnTouchListener{

	private ViewPager viewPager;

	private ViewPagerAdapter viewPagerAdapter;

	private List<View> views;

	private ImageView[] dots;

	private int currentIndex;
	
	private int lastX = 0;
	
	private boolean startFlag = true;
	
	private SharedPreferences settings = null;

	private static final int[] pics = {R.drawable.drovik_four_guide_one, R.drawable.drovik_four_guide_two, R.drawable.drovik_four_guide_three };

	private String TAG = "Guide";

	private int width = 0;
	
	private int height = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		settings = getSharedPreferences(MovieList.class.getName(), 0);
		DisplayMetrics metrics = new DisplayMetrics();
		getWindow().getWindowManager().getDefaultDisplay().getMetrics(metrics);
		width = metrics.widthPixels;
		height = metrics.heightPixels;
		if(settings.getBoolean("IS_INIT", true)) {
        	settings.edit().putBoolean("IS_INIT", false).commit();
        	setContentView(R.layout.layout_guide);
        	views = new ArrayList<View>();
        	LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(
        			LinearLayout.LayoutParams.WRAP_CONTENT,
        			LinearLayout.LayoutParams.FILL_PARENT);
        	
        	for (int i = 0; i < pics.length; i++) {
        		ImageView iv = new ImageView(this);
        		iv.setLayoutParams(mParams);
        		iv.setImageResource(pics[i]);
        		views.add(iv);
        	}
        	viewPager = (ViewPager) findViewById(R.id.viewpager);
        	viewPagerAdapter = new ViewPagerAdapter(views);
        	viewPager.setOnTouchListener(this);
        	viewPager.setAdapter(viewPagerAdapter);
        	viewPager.setOnPageChangeListener(this);
        	viewPager.setOnClickListener(this);
        	
        	initBottomDots();
        }else {
        	//startActivity(new Intent(this, Welcome.class));
        	startActivity(new Intent(this, Main.class));
        	Guide.this.finish();
        }

	}

	private void initBottomDots() {
		LinearLayout ll = (LinearLayout) findViewById(R.id.ll);
		dots = new ImageView[pics.length];
		for (int i = 0; i < pics.length; i++) {
			dots[i] = (ImageView) ll.getChildAt(i);
			dots[i].setEnabled(true);
			dots[i].setOnClickListener(this);
			dots[i].setTag(i);
		}
		currentIndex = 0;
		dots[currentIndex].setEnabled(false);
	}

	@Override
	protected void onResume() {
		super.onResume();
		StatService.onResume(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPause(this);
	}
	
	@Override
	public void onClick(View v) {
		int position = (Integer) v.getTag();
		setCurView(position);
		setCurDot(position);
	}

	@Override
	public void onPageScrollStateChanged(int state) {
	}

	@Override
	public void onPageScrolled(int index, float arg1, int dis) {
		currentIndex = index;
	}

	@Override
	public void onPageSelected(int index) {
		setCurDot(index);
	}

	private void setCurDot(int positon) {
		if (positon < 0 || positon > pics.length - 1 || currentIndex == positon) {
			return;
		}
		dots[positon].setEnabled(false);
		dots[currentIndex].setEnabled(true);
		currentIndex = positon;
	}

	private void setCurView(int position) {
		if (position < 0 || position >= pics.length) {
			return;
		}
		viewPager.setCurrentItem(position);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			lastX = (int)event.getX();
			if(event.getY() >= height * 2/3 && (event.getX() >width/3 && event.getX()<= width *2/3) && currentIndex == 2) {
				boolean shutCutFlag = settings.getBoolean("CREATE_SHUT_CUT", false);
				if(!shutCutFlag) {
					createShutcut(this, "com.sky.drovik.player", ".media.Guide");
		        }
				startActivity(new Intent(this, Welcome.class));
				StatService.onEvent(Guide.this, "首次安装", "点击完成向导按钮", 1);
	        	Guide.this.finish();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if(startFlag && (lastX - event.getX()) >100 && (currentIndex == views.size() -1)){
				startFlag = false;
				boolean shutCutFlag = settings.getBoolean("CREATE_SHUT_CUT", false);
				if(!shutCutFlag) {
					createShutcut(this, "com.sky.drovik.player", ".media.Guide");
		        }
				startActivity(new Intent(this, Main.class));
				Guide.this.finish();
			}
			break;
		default:
			break;
		}
		return false;
	}

	private void createShutcut(Context context, String pkg, String main) {
		// 快捷方式名  
	    String title = "unknown";  
	   // MainActivity完整名  
	   String mainAct = null;  
	   // 应用图标标识  
	   int iconIdentifier = 0;  
	   // 根据包名寻找MainActivity  
	   PackageManager pkgMag = context.getPackageManager();  
	   Intent queryIntent = new Intent(Intent.ACTION_MAIN, null);  
	   queryIntent.addCategory(Intent.CATEGORY_LAUNCHER);  
	   List<ResolveInfo> list = pkgMag.queryIntentActivities(queryIntent,  
	           PackageManager.GET_ACTIVITIES);  
	   for (int i = 0; i < list.size(); i++) {  
	       ResolveInfo info = list.get(i);  
	       if (info.activityInfo.packageName.equals(pkg)) {  
	           title = info.loadLabel(pkgMag).toString();  
	           mainAct = info.activityInfo.name;  
	           iconIdentifier = info.activityInfo.applicationInfo.icon; 
	           if(BuildConfig.DEBUG) {
	        	   Log.d(TAG, "### title = " + title + "  mainAct = " + mainAct + "  iconIdentifier = " + iconIdentifier);
	           }
	           break;  
	       }  
	   }  
	  
	   Intent shortcutIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");  
	   // 快捷方式的名称  
	   shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, context.getString(R.string.app_name));  
	   //不允许重复创建  
	   shortcutIntent.putExtra("duplicate", false);   
	   ComponentName comp = new ComponentName(pkg, main);  
	   shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT,  
	           new Intent(Intent.ACTION_MAIN).setComponent(comp));  
	   // 快捷方式的图标  
	   ShortcutIconResource iconRes = Intent.ShortcutIconResource.fromContext(context, R.drawable.ic_launcher);  
	   shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);  
	   // 发送广播，让接收者创建快捷方式  
	   // 需权限<uses-permission  
	   // android:name="com.android.launcher.permission.INSTALL_SHORTCUT" />  
	   context.sendBroadcast(shortcutIntent);  
	   settings.edit().putBoolean("CREATE_SHUT_CUT", true).commit();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
}
