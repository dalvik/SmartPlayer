package com.sky.drovik.player.engine;

import java.util.List;

import com.sky.drovik.player.exception.AppException;
import com.sky.drovik.player.pojo.BaseImage;

public class BeautyImageEngine extends ImageEngine {

	
	public BeautyImageEngine() {
		
	}
	
	@Override
	public List<BaseImage> fetchImage(String desc) throws AppException {
		return BaseImage.parse(http_get(desc));
	}

	
}
