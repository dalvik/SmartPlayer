package com.sky.drovik.factory;

import android.content.Context;

import com.sky.drovik.player.pojo.Balance;

public abstract class IRegisterFoctory {

	public abstract boolean isRegister(Context context);
	
	public abstract void gotoRegister(Context context);
	
	public abstract Balance viewScore(Context context);
	
}
