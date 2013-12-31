package com.valge.champchat.util;

/**
 * Created by Albert Widiatmoko on 07/12/13.
 */
public class Message {
    public long id = 0;
    public String text;
    public String from;
    public String date;
    public String time;
    public String status;
    public int mode;

    public Message(String text, String from, String date, String time, String status, int mode) {
        // TODO Auto-generated constructor stub
        this.text = text;
        this.from = from;
        this.date = date;
        this.time = time;
        this.status = status;
        this.mode = mode;
    }
}
