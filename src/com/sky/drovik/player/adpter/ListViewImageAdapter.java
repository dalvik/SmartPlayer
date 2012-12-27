package com.sky.drovik.player.adpter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sky.drovik.entity.Image;

public class ListViewImageAdapter extends BaseAdapter {

	private Context 					context;//����������
	private List<Image> 				listItems;//���ݼ���
	private LayoutInflater 				listContainer;//��ͼ����
	private int 						itemViewResource;//�Զ�������ͼԴ
	
	public ListViewImageAdapter(Context context, List<Image> data,int resource) {
		this.context = context;
		listItems = data;
		itemViewResource = resource;
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
		return null;
	}

	static class ListItemView{				//�Զ���ؼ�����  
        public TextView title;  
	    public TextView author;
	    public TextView date;  
	    public TextView count;
	    public ImageView flag;
 }  
	
}
