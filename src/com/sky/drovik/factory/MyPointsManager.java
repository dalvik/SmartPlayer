package com.sky.drovik.factory;

import java.util.List;

import net.youmi.android.appoffers.EarnedPointsNotifier;
import net.youmi.android.appoffers.EarnedPointsOrder;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.widget.Toast;

import com.drovik.utils.ToastUtils;
import com.sky.drovik.player.BuildConfig;
import com.sky.drovik.player.R;
import com.sky.drovik.player.media.MovieList;

public class MyPointsManager implements EarnedPointsNotifier {

	//private static final String KEY_FILE_POINTS="Points";
	public static final String KEY_POINTS="BEYING";
	private static final String KEY_FILE_ORDERS="Orders";
	

	private static MyPointsManager instance;
	
	private String TAG = "MyPointsManager";
	
	private boolean DEBUG = true;

	public static MyPointsManager getInstance() {
		if (instance == null) {
			instance = new MyPointsManager();
		}

		return instance;
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
	 * 查询积分
	 * @param context
	 * @return
	 */
	public int queryPoints(Context context) {
		SharedPreferences sp = context.getSharedPreferences(MovieList.class.getName(), Context.MODE_PRIVATE);
		return sp.getInt(KEY_POINTS, 0);
	}

	/**
	 * 消费积分
	 * 
	 * @param context
	 * @param amount
	 * @return
	 */
	public boolean spendPoints(Context context, int amount) {
		if (amount <= 0) {
			return false;
		}
		SharedPreferences sp = context.getSharedPreferences(MovieList.class.getName(), Context.MODE_PRIVATE);
		int p = sp.getInt(KEY_POINTS, 0);
		if (p < amount) {
			return false;
		}
		p -= amount;
		return sp.edit().putInt(KEY_POINTS, p).commit();
	}

	/**
	 * 奖励积分
	 * @param context
	 * @param amount
	 * @return
	 */
	public boolean awardPoints(Context context, int amount) {
		if (amount <= 0) {
			return false;
		}
		SharedPreferences sp = context.getSharedPreferences(MovieList.class.getName(), Context.MODE_PRIVATE);
		int p = sp.getInt(KEY_POINTS, 0);

		p += amount;

		return sp.edit().putInt(KEY_POINTS, p).commit();
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
					SharedPreferences settings = context.getSharedPreferences(MovieList.class.getName(), Context.MODE_PRIVATE);
					int p = settings.getInt(KEY_POINTS, 0);
					p += order.getPoints();
					settings.edit().putInt(KEY_POINTS, p).commit();
					if(BuildConfig.DEBUG && DEBUG) {
						Log.e(TAG, "### store points = " + p );
					}
					if(p<100) {
						Toast.makeText(context, context.getString(R.string.drovik_play_regester_uncommplete_str, (100-p)), Toast.LENGTH_SHORT).show();
					}else {
						ToastUtils.showToast(context, R.string.drovik_play_regester_success_str);
					}
				}
			}
		} catch (Exception e) {
			if(BuildConfig.DEBUG && DEBUG) {
				Log.e(TAG, "### storePoints " + e.getLocalizedMessage());
			}
		}
	}

	private void recordOrder(Context context, EarnedPointsOrder order) {
		try {
			if (order != null) {
				// 可以处理这些订单详细信息，这里只是作为简单的记录.
				StringBuilder stringBuilder = new StringBuilder(256);
				stringBuilder.append("[").append("订单号 => ")
						.append(order.getOrderId()).append("]\t[")
						.append("渠道号 => ").append(order.getChannelId())
						.append("]\t[").append("设置的用户Id(md5) => ")
						.append(order.getUserId()).append("]\t[")
						.append("获得的积分 => ").append(order.getPoints())
						.append("]\t[")
						.append("获得积分的类型(1为有收入的积分，2为无收入的积分) => ")
						.append(order.getStatus()).append("]\t[")
						.append("积分的结算时间(格林威治时间，单位秒) => ")
						.append(order.getTime()).append("]\t[")
						.append("本次获得积分的描述信息 => ").append(order.getMessage())
						.append("]");

				String msg = stringBuilder.toString();

				SharedPreferences sp = context.getSharedPreferences(KEY_FILE_ORDERS,
						Context.MODE_PRIVATE);

				Editor editor = sp.edit();
				editor.putString(
						order.getOrderId() != null ? order.getOrderId() : Long
								.toString(System.currentTimeMillis()), msg);

				editor.commit();

				infoMsg(msg);
			}

		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	private void errMsg(String msg) {
		Log.e("MyPointsManager", msg);
	}

	private void infoMsg(String msg) {
		Log.e("MyPointsManager", msg);
	}

}
