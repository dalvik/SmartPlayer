package com.sky.drovik.player.adpter;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sky.drovik.player.R;
import com.sky.drovik.player.bitmapfun.ImageFetcher;
import com.sky.drovik.player.engine.BeautyImage;
import com.sky.drovik.player.pojo.BaseImage;

public class ListViewOtherImageAdapter extends BaseAdapter {

	//private Context context;// 运行上下文
	private List<BaseImage> listItems;// 数据集合
	private LayoutInflater listContainer;// 视图容器
	private int itemViewResource;// 自定义项视图源

	private ImageFetcher mImageFetcher;
	private ImageView[] star;
	
	public ListViewOtherImageAdapter(Context context, List<BaseImage> data,
			int resource, ImageFetcher mImageFetcher) {
		//this.context = context;
		this.listContainer = LayoutInflater.from(context);
		listItems = data;
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
			itemView.name = (TextView)convertView.findViewById(R.id.other_image_list_item_name);
			itemView.intro = (TextView) convertView.findViewById(R.id.other_image_list_item_intro);
			itemView.desc = (TextView)convertView.findViewById(R.id.other_image_list_item_desc);
			itemView.src= (ImageView)convertView.findViewById(R.id.other_image_list_item_thumbnail);
			star = new ImageView[5];
			star[0] = (ImageView)convertView.findViewById(R.id.ic_star_gray_0);
			star[1] = (ImageView)convertView.findViewById(R.id.ic_star_gray_1);
			star[2] = (ImageView)convertView.findViewById(R.id.ic_star_gray_2);
			star[3] = (ImageView)convertView.findViewById(R.id.ic_star_gray_3);
			star[4] = (ImageView)convertView.findViewById(R.id.ic_star_gray_4);
			//设置控件集到convertView
			convertView.setTag(itemView);
		}else {
			itemView = (ListItemView)convertView.getTag();
		}
		
	/*	BaseImage image = listItems.get(position);
		itemView.name.setText(image.getName());
		itemView.desc.setText(image.getDesc());
		//image.setThumbnail("http://www.eoeandroid.com/uc_server/data/avatar/000/64/74/76_avatar_middle.jpg");
		mImageFetcher.loadImage(image.getThumbnail(), itemView.src);*/

		BeautyImage image = (BeautyImage)listItems.get(position);
		itemView.name.setText(image.getName());
		StringBuilder sb = new StringBuilder();
		sb.append("共有 ");
		int firstStart = sb.toString().length();
		sb.append(image.getSrcSize() + " 张图片，其中 ");
		itemView.desc.setText(image.getDesc());
		int secondStart = sb.toString().length();
		if(image.isHasNew()) {
			sb.append(image.getNewImageSize() +" 张更新");
		}else {
			sb.append("0 张更新");
		}
		SpannableStringBuilder ssb = new SpannableStringBuilder(sb);
		ssb.setSpan(new ForegroundColorSpan(Color.RED), firstStart, firstStart + getIntLength(image.getSrcSize()), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		ssb.setSpan(new ForegroundColorSpan(Color.RED), secondStart, secondStart + getIntLength(image.getNewImageSize()), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		itemView.intro.setText(ssb);
		int starLevel = image.getStarLevel();
		int num = starLevel/2;
		int den = starLevel % 2;
		for(int i=0;i<num;i++) {
			star[i].setImageResource(R.drawable.ic_star);
		}
		if(den != 0) {
			star[num].setImageResource(R.drawable.ic_star_half);
		}
		mImageFetcher.loadImage(image.getThumbnail(), itemView.src);
		return convertView;
	}

	static class ListItemView { // 自定义控件集合
		public ImageView src;
		public TextView name;
		public TextView intro;
		public TextView desc;
	}

	private int getIntLength(int num) {
		return String.valueOf(num).length();
	}
}
