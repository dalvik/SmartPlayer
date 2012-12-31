package com.sky.drovik.player.engine;

import java.util.Arrays;

import com.sky.drovik.player.pojo.BaseImage;

public class BeautyImage extends BaseImage {
	
	private String[] srcArr;

	private int srcSize;
	
	private boolean hasNew;
	
	private int newImageSize;
	
	public BeautyImage() {
		super();
	}

	public BeautyImage(String[] srcArr, int srcSize, boolean hasNew,
			int newImageSize) {
		super();
		this.srcArr = srcArr;
		this.srcSize = srcSize;
		this.hasNew = hasNew;
		this.newImageSize = newImageSize;
	}

	public String[] getSrcArr() {
		return srcArr;
	}

	public void setSrcArr(String[] srcArr) {
		this.srcArr = srcArr;
	}

	public int getSrcSize() {
		return srcSize;
	}

	public void setSrcSize(int srcSize) {
		this.srcSize = srcSize;
	}

	public boolean isHasNew() {
		return hasNew;
	}

	public void setHasNew(boolean hasNew) {
		this.hasNew = hasNew;
	}

	public int getNewImageSize() {
		return newImageSize;
	}

	public void setNewImageSize(int newImageSize) {
		this.newImageSize = newImageSize;
	}

	@Override
	public String toString() {
		return "BeautyImage [srcArr=" + Arrays.toString(srcArr) + ", srcSize="
				+ srcSize + ", hasNew=" + hasNew + ", newImageSize="
				+ newImageSize + "]";
	}

}
