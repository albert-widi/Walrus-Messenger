package com.valge.champchat.util;

import android.content.Context;
import android.util.Base64;

import java.security.MessageDigest;
import java.security.PrivateKey;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Albert Widiatmoko on 10/12/13.
 */
public class EncryptionUtil {
    public String decryptMessage(String message, String messageKey, String messageHash, Context context) {
        SharedPrefsUtil sharedPrefsUtil = new SharedPrefsUtil(context);
        String originalMessage = "";
        PrivateKey userPrivateKey = sharedPrefsUtil.getUserPrivateKey();
        byte[] messageByte = Base64.decode(message, Base64.DEFAULT);

        byte[] key = Base64.decode(messageKey, Base64.DEFAULT);
        //RSA decryption for key
        try {

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, userPrivateKey);
            key = cipher.doFinal(key);
            byte[] originalMessageByte = decrypt(key, messageByte);
            originalMessage = new String(originalMessageByte, "UTF-8");
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        //check if message manipulated
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(messageByte);
            byte[] messageDigest = digest.digest();

            StringBuffer hexString = new StringBuffer();
            int digestLength = messageDigest.length;
            for(int i = 0; i < digestLength; i++) {
                String h = Integer.toHexString(0xff & messageDigest[i]);
                while(h.length() < 2) {
                    h = "0" + h;
                }
                hexString.append(h);
            }
            String fixHexString = hexString.toString();

            if(fixHexString == messageHash) {
                return originalMessage;
            }
            else {
                return "Message had been manipulated";
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }
}
