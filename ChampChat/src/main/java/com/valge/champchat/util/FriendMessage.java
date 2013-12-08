package com.valge.champchat.util;

/**
 * Created by Albert Widiatmoko on 07/12/13.
 */
public class FriendMessage extends Friend{
    public String lastMessage;
    public String lastMessageFrom;
    public String lastMessageDate;
    public String lastMessageTime;

    public FriendMessage(String name, String phoneNumber, String gcmId, byte[] publicKey) {
        super(name, phoneNumber, gcmId, publicKey);
    }
}
