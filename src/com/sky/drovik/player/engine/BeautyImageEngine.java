package com.sky.drovik.player.engine;

import java.io.IOException;
import java.util.List;

import com.sky.drovik.player.pojo.BaseImage;

public class BeautyImageEngine extends ImageEngine {

	@Override
	public List<BaseImage> fetchImage(String desc) throws IOException {
		return BaseImage.parse(http_get(desc));
	}

	
}
