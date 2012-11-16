/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sky.drovik.player.media;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;

public class LocalDataSource implements DataSource {
    private static final String TAG = "LocalDataSource";
    public static final String URI_ALL_MEDIA = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString();
    public static final DiskCache sThumbnailCache = new DiskCache("local-image-thumbs");
    public static final DiskCache sThumbnailCacheVideo = new DiskCache("local-video-thumbs");

    public static final String CAMERA_STRING = "Camera";
    public static final String DOWNLOAD_STRING = "download";
    public static final String CAMERA_BUCKET_NAME = Environment.getExternalStorageDirectory().toString() + "/DCIM/" + CAMERA_STRING;
    public static final String DOWNLOAD_BUCKET_NAME = Environment.getExternalStorageDirectory().toString() + "/" + DOWNLOAD_STRING;
    public static final int CAMERA_BUCKET_ID = getBucketId(CAMERA_BUCKET_NAME);
    public static final int DOWNLOAD_BUCKET_ID = getBucketId(DOWNLOAD_BUCKET_NAME);
    
    /**
     * Matches code in MediaProvider.computeBucketValues. Should be a common
     * function.
     */
    public static int getBucketId(String path) {
        return (path.toLowerCase().hashCode());
    }
    
    private final String mUri;
    private final String mBucketId;
    private boolean mDone;;
    private final boolean mSingleUri;
    private final boolean mAllItems;
    private final boolean mFlattenAllItems;
    private final DiskCache mDiskCache;
    private boolean mIncludeImages;
    private boolean mIncludeVideos;
    private Context mContext;

    public LocalDataSource(final Context context, final String uri, final boolean flattenAllItems) {
        this.mUri = uri;
        mContext = context;
        mIncludeImages = true;
        mIncludeVideos = false;
        String bucketId = Uri.parse(uri).getQueryParameter("bucketId");
        if (bucketId != null && bucketId.length() > 0) {
            mBucketId = bucketId;
        } else {
            mBucketId = null;
        }
        mFlattenAllItems = flattenAllItems;
        if (mBucketId == null) {
            if (uri.equals(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString())) {
                mAllItems = true;
            } else {
                mAllItems = false;
            }
        } else {
            mAllItems = false;
        }
        mSingleUri = isSingleImageMode(uri) && mBucketId == null;
        mDone = false;
        mDiskCache = mUri.startsWith(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString())
                || mUri.startsWith(MediaStore.Video.Media.EXTERNAL_CONTENT_URI.toString())
                || mUri.startsWith("file://") ? sThumbnailCache
                : null;
    }
    
    public void setMimeFilter(boolean includeImages, boolean includeVideos) {
        mIncludeImages = includeImages;
        mIncludeVideos = includeVideos;
    }

    public void shutdown() {

    }

    public boolean isSingleImage() {
        return mSingleUri;
    }

    private static boolean isSingleImageMode(String uriString) {
        return !uriString.equals(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString())
                && !uriString.equals(MediaStore.Images.Media.INTERNAL_CONTENT_URI.toString());
    }

    public DiskCache getThumbnailCache() {
        return mDiskCache;
    }


    private static boolean isImage(String uriString) {
        return !uriString.startsWith(MediaStore.Video.Media.EXTERNAL_CONTENT_URI.toString());
    }

    
    public String[] getDatabaseUris() {
        return new String[] {Images.Media.EXTERNAL_CONTENT_URI.toString(), Video.Media.EXTERNAL_CONTENT_URI.toString()};
    }


}
