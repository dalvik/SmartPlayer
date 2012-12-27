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
import com.sky.drovik.player.pojo.BaseImage;
import com.sky.drovik.utils.ImageFetcher;

public class ListViewImageAdapter extends BaseAdapter {

	private Context context;// 运行上下文
	private List<BaseImage> listItems;// 数据集合
	private LayoutInflater listContainer;// 视图容器
	private int itemViewResource;// 自定义项视图源

	private ImageFetcher mImageFetcher;
	
	public ListViewImageAdapter(Context context, List<BaseImage> data,
			int resource, ImageFetcher mImageFetcher) {
		this.context = context;
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
			itemView.name = (TextView)convertView.findViewById(R.id.image_list_item_name);
			itemView.desc = (TextView)convertView.findViewById(R.id.image_list_item_desc);
			itemView.src= (ImageView)convertView.findViewById(R.id.image_list_item_thumbnail);
			
			//设置控件集到convertView
			convertView.setTag(itemView);
		}else {
			itemView = (ListItemView)convertView.getTag();
		}
		
		BaseImage image = listItems.get(position);
		itemView.name.setText(image.getName());
		itemView.desc.setText(image.getDesc());
		image.setThumbnail("http://www.eoeandroid.com/uc_server/data/avatar/000/64/74/76_avatar_middle.jpg");
		mImageFetcher.loadImage(image.getThumbnail(), itemView.src);
		return convertView;
	}

	static class ListItemView { // 自定义控件集合
		public ImageView src;
		public TextView name;
		public TextView desc;
		public TextView date;
		public TextView count;
	}

}
