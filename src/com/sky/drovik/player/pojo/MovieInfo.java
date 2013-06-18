/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.sky.drovik.player.pojo;

import android.content.Intent;
import android.net.Uri;
import android.widget.ImageView;

/**
 * Represents a launchable application. An application is made of a name (or title), an intent
 * and an icon.
 */
public class MovieInfo {
	
	public static final int CATALOG_LOCAL_VIDEO = 0;
	
	public static final int CATALOG_MOVIE_VIDEO = 1;
	
	public static final int CATALOG_RECREATION_VIDEO = 2;
	
	public static final int CATALOG_OTHER_VIDEO = 3;
	
    /**
     * The application name.
     */
    public CharSequence title;

    /**
     * The intent used to start the application.
     */
    public Intent intent;

    /**
     * The application icon.
     */
    //public Bitmap icon;
    public String path= "";

    public String mimeType;
    
    public String size = "";
    
    public long duration = 0;
    
    public String resulation = "";
    
    public String thumbnailPath = "";
    
    public ImageView imageView;
    
    public long magic_id;
    
    public int starLevel = 0;
    
    /**
     * When set to true, indicates that the icon has been resized.
     */
    public boolean filtered;

    /**
     * Creates the application intent based on a component name and various launch flags.
     *
     * @param className the class name of the component representing the intent
     * @param launchFlags the launch flags
     */
    public final void setActivity(Uri uri,String mimeType, int launchFlags) {
        intent = new Intent("com.sky.drovik.action.PLAYVER_VIEW");
        intent.setDataAndType(uri, mimeType); 
        intent.setFlags(launchFlags);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MovieInfo)) {
            return false;
        }

        MovieInfo that = (MovieInfo) o;
        return title.equals(that.title) && thumbnailPath.equals(that.thumbnailPath);
    }

    @Override
    public int hashCode() {
        int result;
        result = (title != null ? title.hashCode() : 0);
        final String name = intent.getComponent().getClassName();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

	@Override
	public String toString() {
		return "MovieInfo [title=" + title + ", intent=" + intent + ", path="
				+ path + ", mimeType=" + mimeType + ", size=" + size
				+ ", duration=" + duration + ", resulation=" + resulation
				+ ", thumbnailPath=" + thumbnailPath + ", imageView="
				+ imageView + ", magic_id=" + magic_id + ", filtered="
				+ filtered + "]";
	}

    
}
