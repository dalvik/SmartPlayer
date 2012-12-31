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
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (hasNew ? 1231 : 1237);
		result = prime * result + newImageSize;
		result = prime * result + Arrays.hashCode(srcArr);
		result = prime * result + srcSize;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		BeautyImage other = (BeautyImage) obj;
		if (hasNew != other.hasNew)
			return false;
		if (newImageSize != other.newImageSize)
			return false;
		if (!Arrays.equals(srcArr, other.srcArr))
			return false;
		if (srcSize != other.srcSize)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "BeautyImage [srcArr=" + Arrays.toString(srcArr) + ", srcSize="
				+ srcSize + ", hasNew=" + hasNew + ", newImageSize="
				+ newImageSize + "]";
	}

}
