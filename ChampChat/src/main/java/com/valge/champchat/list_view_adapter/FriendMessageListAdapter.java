package com.valge.champchat.list_view_adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.valge.champchat.R;
import com.valge.champchat.util.FriendMessage;

import java.util.ArrayList;

public class FriendMessageListAdapter extends ArrayAdapter {
	private final Context context;
	private final ArrayList<FriendMessage> friend;
	
	public FriendMessageListAdapter(Context context, ArrayList<FriendMessage> friendList) {
		super(context, R.layout.chat_list_item, friendList);
		this.context = context;
		friend = friendList;
		
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View rowView = inflater.inflate(R.layout.chat_list_item, parent, false);
		TextView nameTextView = (TextView) rowView.findViewById(R.id.adapter_message_friend_name);
		TextView lastMessageTextView = (TextView) rowView.findViewById(R.id.adapter_message_last_message);
		TextView lastMessageDateTextView = (TextView) rowView.findViewById(R.id.adapter_message_last_message_date);
		TextView lastMessageTimeTextView = (TextView) rowView.findViewById(R.id.adapter_message_last_message_time);
		
		nameTextView.setText(friend.get(position).name);
        lastMessageTextView.setText(friend.get(position).lastMessage);
        lastMessageDateTextView.setText(friend.get(position).lastMessageDate);
        lastMessageTimeTextView.setText(friend.get(position).lastMessageTime);
		return rowView;
	}
}
