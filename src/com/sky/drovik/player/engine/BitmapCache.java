package com.sky.drovik.player.engine;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Hashtable;

import android.content.Context;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;

import com.drovik.utils.BitmapUtil;
import com.sky.drovik.player.BuildConfig;
import com.sky.drovik.player.pojo.MovieInfo;

/**
 * ��ֹ���
 *
 */
public class BitmapCache {
	
	private boolean DEBUG = true;
	
	private String TAG = "BitmapCache";
	
	static private BitmapCache cache;
	/** ����Chche���ݵĴ洢 */
	private Hashtable<String, BtimapRef> bitmapRefs;
	/** ����Reference�Ķ��У������õĶ����Ѿ������գ��򽫸����ô�������У� */
	private ReferenceQueue<Bitmap> q;

	/**
	 * �̳�SoftReference��ʹ��ÿһ��ʵ�������п�ʶ��ı�ʶ��
	 */
	private class BtimapRef extends SoftReference<Bitmap> {
		private String _key = "";

		public BtimapRef(Bitmap bmp, ReferenceQueue<Bitmap> q, String key) {
			super(bmp, q);
			_key = key;
		}
	}

	private BitmapCache() {
		bitmapRefs = new Hashtable<String, BtimapRef>();
		q = new ReferenceQueue<Bitmap>();

	}

	/**
	 * ȡ�û�����ʵ��
	 */
	public static BitmapCache getInstance() {
		if (cache == null) {
			cache = new BitmapCache();
		}
		return cache;

	}

	/**
	 * �������õķ�ʽ��һ��Bitmap�����ʵ���������ò����������
	 */
	private void addCacheBitmap(Bitmap bmp, String key) {
		cleanCache();// �����������
		BtimapRef ref = new BtimapRef(bmp, q, key);
		bitmapRefs.put(key, ref);
	}

	/**
	 * ������ָ�����ļ�����ȡͼƬ
	 */
	public Bitmap getBitmap(Context context, MovieInfo imageInfo) {
		String fileName = imageInfo.thumbnailPath;
		if(BuildConfig.DEBUG && !DEBUG) {
			Log.d(TAG, "### file name of thumbnail = " + fileName);
		}
		Bitmap bitmapImage = null;
		// �������Ƿ��и�Bitmapʵ���������ã�����У�����������ȡ�á�
		if (bitmapRefs.containsKey(fileName)) {
			BtimapRef ref = (BtimapRef) bitmapRefs.get(fileName);
			bitmapImage = (Bitmap) ref.get();
		}
		// ���û�������ã����ߴ��������еõ���ʵ����null�����¹���һ��ʵ����
		// �����������½�ʵ����������
		if (bitmapImage == null) {
			Bitmap bitmap = MediaStore.Video.Thumbnails.getThumbnail(context.getContentResolver(), imageInfo.magic_id, MediaStore.Video.Thumbnails.MICRO_KIND, null);
			if(bitmap != null) {
				bitmapImage = BitmapUtil.getRoundedCornerBitmap(bitmap, 10.0f);
				this.addCacheBitmap(bitmapImage, fileName);
				if(bitmap != null && !bitmap.isRecycled()) {
					bitmap.recycle();
					bitmap = null;
				}
				//bitmapImage = BitmapUtil.getRoundedCornerBitmap(BitmapFactory.decodeResource(, id), 10.0f);
			}
			/*BitmapFactory.Options options = new BitmapFactory.Options();
			options.inTempStorage = new byte[16 * 1024];
			// bitmapImage = BitmapFactory.decodeFile(filename, options);
			BufferedInputStream buf = null;
			InputStream is = null;
			try {
				is = new FileInputStream(filename);
				buf = new BufferedInputStream(is);
				bitmapImage = BitmapFactory.decodeStream(buf);
				this.addCacheBitmap(bitmapImage, filename);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if(is != null) {
					try {
						is.close();
						is = null;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(buf != null) {
					try {
						buf.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}*/

		}
		return bitmapImage;
	}

	private void cleanCache() {
		BtimapRef ref = null;
		while ((ref = (BtimapRef) q.poll()) != null) {
			bitmapRefs.remove(ref._key);
		}
	}

	// ���Cache�ڵ�ȫ�����ݮ�
	public void clearCache() {
		cleanCache();
		bitmapRefs.clear();
		System.gc();
		System.runFinalization();
	}

}
