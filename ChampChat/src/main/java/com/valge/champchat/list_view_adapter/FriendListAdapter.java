package com.valge.champchat.list_view_adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.valge.champchat.R;
import com.valge.champchat.util.Friend;

import java.util.ArrayList;

public class FriendListAdapter extends ArrayAdapter {
	private final Context context;
	private final ArrayList<Friend> friendList;
	
	public FriendListAdapter(Context context, ArrayList<Friend> friend) {
		super(context, R.layout.friend_list_item, friend);
		this.context = context;
		friendList = friend;
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View rowView = inflater.inflate(R.layout.friend_list_item, parent, false);
		TextView nameTextView = (TextView) rowView.findViewById(R.id.adapter_friend_name);
		
		nameTextView.setText(friendList.get(position).name);
		
		return rowView;
	}
}
