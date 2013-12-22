package com.valge.champchat.list_view_adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.valge.champchat.R;
import com.valge.champchat.util.Message;

import java.util.ArrayList;

public class MessagingAdapter extends ArrayAdapter{
	private Context context;
	ArrayList<Message> message;
	
	public MessagingAdapter(Context context, ArrayList<Message> messageData) {
		super(context, R.layout.fragment_test, messageData);
		this.context = context;
		this.message = messageData;
	}
	
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        MessageView msgView = null;

        if(rowView == null)
        {
            // Get a new instance of the row layout view
        	LayoutInflater inflater = (LayoutInflater) context
    				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


            rowView = inflater.inflate(R.layout.message_list_item_one, null);
            //very2 buggy
            /*System.out.println("Mode : " + message.get(position).mode);
            if(message.get(position).mode == 2) {
                System.out.println("Mode 2");
                rowView = inflater.inflate(R.layout.message_list_item_two, null);
            }
            else {
                System.out.println("Mode 1");
                rowView = inflater.inflate(R.layout.message_list_item_one, null);
            }*/


            // Hold the view objects in an object,
            // so they don't need to be re-fetched
            msgView = new MessageView();
            msgView.message = (TextView) rowView.findViewById(R.id.message_adapter_text);
            msgView.from = (TextView) rowView.findViewById(R.id.message_adapter_from);
            msgView.date = (TextView) rowView.findViewById(R.id.message_adapter_date);
            msgView.time = (TextView) rowView.findViewById(R.id.message_adapter_time);

            // Cache the view objects in the tag,
            // so they can be re-accessed later
            rowView.setTag(msgView);
        } else {
            msgView = (MessageView) rowView.getTag();       
        }

        // Transfer the stock data from the data object
        // to the view objects      
        //MessageData currentMsg = (MessageData)messages.get(position);
        msgView.message.setText(message.get(position).text);
        msgView.from.setText(message.get(position).from);
        msgView.date.setText(message.get(position).date);
        msgView.time.setText(message.get(position).time);
        
        return rowView;
    }

	protected static class MessageView {
        protected TextView message;
        protected TextView from;
        protected TextView date;
        protected TextView time;
    }
}
