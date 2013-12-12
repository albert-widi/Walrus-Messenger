package com.valge.champchat.util;

/**
 * Created by Albert Widiatmoko on 07/12/13.
 */
public class Friend {
    public int id;
    public String name;
    public String phoneNumber;
    public byte[] publicKey;

    public Friend(int id, String name, String phoneNumber, byte[] publicKey) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.publicKey = publicKey;
    }
}
