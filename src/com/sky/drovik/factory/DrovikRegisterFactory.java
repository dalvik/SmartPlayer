package com.sky.drovik.factory;

import android.content.Context;
import android.content.SharedPreferences;

import com.sky.drovik.player.media.MovieList;
import com.sky.drovik.player.pojo.Balance;

public class DrovikRegisterFactory extends IRegisterFoctory {

	@Override
	public boolean isRegister(Context context) {
		SharedPreferences settings = context.getSharedPreferences(MovieList.class.getName(), 0);
        //settings.get("", defValue)
		return false;
	}

	@Override
	public void gotoRegister(Context context) {
		// TODO Auto-generated method stub

	}

	@Override
	public Balance viewScore(Context context) {
		// TODO Auto-generated method stub
		return null;
	}

}
