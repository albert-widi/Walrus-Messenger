package com.valge.champchat.util;

/**
 * Created by Albert Widiatmoko on 07/12/13.
 */
public class Friend {
    public String name;
    public String phoneNumber;
    public String gcmId;
    public byte[] publicKey;

    public Friend(String name, String phoneNumber, String gcmId, byte[] publicKey) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.gcmId = gcmId;
        this.publicKey = publicKey;
    }
}
