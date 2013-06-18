package com.sky.drovik.player.adpter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sky.drovik.player.R;
import com.sky.drovik.player.bitmapfun.ImageFetcher;
import com.sky.drovik.player.engine.ImageLoaderTask;
import com.sky.drovik.player.pojo.MovieInfo;
import com.sky.drovik.player.utils.StringUtils;

public class ListViewLocalVideoAdapter extends BaseAdapter {

	private Context context;// 运行上下文
	private List<MovieInfo> listItems;// 数据集合
	private LayoutInflater listContainer;// 视图容器
	private int itemViewResource;// 自定义项视图源

	private ImageFetcher mImageFetcher;
	
	public ListViewLocalVideoAdapter(Context context, List<MovieInfo> info,
			int resource, ImageFetcher mImageFetcher) {
		this.context = context;
		this.listContainer = LayoutInflater.from(context);
		listItems = info;
		itemViewResource = resource;
		this.mImageFetcher = mImageFetcher;
	}

	@Override
	public int getCount() {
		return listItems.size();
	}

	@Override
	public Object getItem(int position) {
		return listItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ListItemView itemView = null;
		if(convertView == null) {
			convertView = listContainer.inflate(itemViewResource, null);
			itemView = new ListItemView();
			//获取控件对象
			itemView.name = (TextView)convertView.findViewById(R.id.video_item_name);
			itemView.intro = (TextView) convertView.findViewById(R.id.video_item_intro);
			itemView.desc = (TextView)convertView.findViewById(R.id.video_item_desc);
			itemView.src= (ImageView)convertView.findViewById(R.id.video_item_thumbnail);
			/*itemView.star[0]= (ImageView)convertView.findViewById(R.id.ic_star_gray_0);
			itemView.star[1] = (ImageView)convertView.findViewById(R.id.ic_star_gray_1);
			itemView.star[2] = (ImageView)convertView.findViewById(R.id.ic_star_gray_2);
			itemView.star[3] = (ImageView)convertView.findViewById(R.id.ic_star_gray_3);
			itemView.star[4] = (ImageView)convertView.findViewById(R.id.ic_star_gray_4);*/
			//设置控件集到convertView
			convertView.setTag(itemView);
		}else {
			itemView = (ListItemView)convertView.getTag();
		}
		MovieInfo video = (MovieInfo)listItems.get(position);
		itemView.name.setText(video.title);
		String mimeType = video.mimeType.toString();
		itemView.desc.setText(context.getText(R.string.video_foramt_name) + mimeType.substring(mimeType.indexOf("/")+1));
		itemView.intro.setText(context.getText(R.string.video_duration_name) + StringUtils.makeTimeString(context,video.duration/1000));
		/*StringBuilder sb = new StringBuilder();
		sb.append("共有 ");
		int firstStart = sb.toString().length();
		sb.append(image.getSrcSize() + " 张图片，其中 ");
		int secondStart = sb.toString().length();
		if(image.isHasNew()) {
			sb.append(image.getNewImageSize() +" 张更新");
		}else {
			sb.append("0 张更新");
		}
		SpannableStringBuilder ssb = new SpannableStringBuilder(sb);
		ssb.setSpan(new ForegroundColorSpan(Color.RED), firstStart, firstStart + getIntLength(image.getSrcSize()), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		ssb.setSpan(new ForegroundColorSpan(Color.RED), secondStart, secondStart + getIntLength(image.getNewImageSize()), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		itemView.intro.setText(ssb);*/
		/*for(int i=0;i<itemView.star.length;i++) {
			itemView.star[i].setImageResource(R.drawable.ic_star_gray);
		}
		int starLevel = video.starLevel;
		int num = starLevel/2;
		int den = starLevel % 2;
		for(int i=0;i<num;i++) {
			itemView.star[i].setImageResource(R.drawable.ic_star);
		}
		if(den != 0) {
			itemView.star[num].setImageResource(R.drawable.ic_star_half);
		}*/
		mImageFetcher.loadVideoThumbnail(video, itemView.src);
		return convertView;
	}

	static class ListItemView { // 自定义控件集合
		public ImageView src;
		public TextView name;
		public TextView intro;
		public TextView desc;
		//public ImageView star[] = new ImageView[5];
	}

	private int getIntLength(int num) {
		return String.valueOf(num).length();
	}
}
