package com.sky.drovik.factory;

import net.youmi.android.appoffers.YoumiOffersManager;
import android.content.Context;
import android.content.SharedPreferences;

import com.sky.drovik.player.media.MovieList;
import com.sky.drovik.player.pojo.Balance;
import com.sky.drovik.player.pojo.PointBalance;

public class DrovikRegisterFactory extends IRegisterFoctory {

	@Override
	public boolean isRegister(Context context) {
		SharedPreferences settings = context.getSharedPreferences(MovieList.class.getName(), 0);
        return settings.getInt(MyPointsManager.KEY_POINTS, 0)>=100;
	}

	@Override
	public void gotoRegister(Context context) {
		context.getPackageName();
		YoumiOffersManager.showOffers(context, YoumiOffersManager.TYPE_REWARD_OFFERS, MyPointsManager.getInstance());
	}

	@Override
	public Balance viewScore(Context context) {
		return new PointBalance(MyPointsManager.getInstance().queryPoints(context));
	}
	

}
