package com.sky.drovik.player.engine;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.sky.drovik.player.R;

public class HistoryListAdpater extends BaseExpandableListAdapter {

	private List<Map<String,Object>> parentList;
	
	private List<List<Map<String,Object>>> childList;
	
	private LayoutInflater inflater;
	
	public HistoryListAdpater(Context context, List<Map<String,Object>> parentList, List<List<Map<String,Object>>> childList) {
		this.parentList = parentList;
		this.childList = childList;
		inflater = LayoutInflater.from(context);
	}
	
	@Override
	public String getChild(int groupPosition, int childPosition) {
		return childList.get(groupPosition).get(childPosition).get("title").toString();
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
			ViewGroup parent) {
		ViewHolder view = new ViewHolder();
		if(convertView == null) {
			convertView = inflater.inflate(R.layout.layout_histroy_list_item, null);
			view.path = (TextView) convertView.findViewById(R.id.movie_path);
		}else {
			view = (ViewHolder)convertView.getTag();
		}
		view.path.setText(childList.get(groupPosition).get(childPosition).get("title").toString());
		convertView.setTag(view);
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return childList.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return parentList.get(groupPosition).get("list").toString();
	}

	@Override
	public int getGroupCount() {
		return parentList.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean arg1, View convertView, ViewGroup parent) {
		ViewHolder view = new ViewHolder();
		if(convertView == null) {
			convertView = inflater.inflate(R.layout.layout_histroy_label_item, null);
			view.path = (TextView) convertView.findViewById(R.id.movie_history_group_label);
		}else {
			view = (ViewHolder)convertView.getTag();
		}
		view.path.setText(parentList.get(groupPosition).get("list").toString());
		convertView.setTag(view);
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	static class ViewHolder {
		
		public TextView path;

	}
}
