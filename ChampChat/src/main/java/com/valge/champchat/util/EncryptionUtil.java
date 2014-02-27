package com.valge.champchat.util;

import android.content.Context;
import android.util.Base64;

import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Albert Widiatmoko on 10/12/13.
 */
public class EncryptionUtil {
    public String createMDSHAHash(String originalString) {
        String hashString = "";
        try {
            byte[] originalStringBytes = originalString.getBytes("UTF-8");
            MessageDigest digestMD5 = MessageDigest.getInstance("MD5");
            MessageDigest digestSHA1 = MessageDigest.getInstance("SHA1");
            digestMD5.update(originalStringBytes);
            byte[] md5ShaStringBytes = digestMD5.digest();
            digestSHA1.update(md5ShaStringBytes);
            md5ShaStringBytes = digestSHA1.digest();

            StringBuffer hexString = new StringBuffer();
            int digestLength = md5ShaStringBytes.length;
            for(int i = 0; i < digestLength; i++) {
                String h = Integer.toHexString(0xff & md5ShaStringBytes[i]);
                 while(h.length() < 2) {
                    h = "0" + h;
                }
                hexString.append(h);
            }
            String fixHexString = hexString.toString();
            System.out.println("Encrypt : Hex String : " + fixHexString);
            hashString = fixHexString;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return hashString;
    }

    public MessageEncrypt encryptMessage(String message, byte[] publicKeyByte, Context context) {
        MessageEncrypt messageEncrypt = new MessageEncrypt();
        SharedPrefsUtil sharedPrefsUtil = new SharedPrefsUtil(context);
        sharedPrefsUtil.loadApplicationPrefs();

        try {
            //AES ENCRYPTION FOR MESSAGE
            System.out.println("Encrypt : Encrypt message with AES");
            byte[] messageByte = message.getBytes("UTF-8");
            byte[] keyStart = sharedPrefsUtil.secretKey.getBytes();
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.setSeed(keyStart);
            keyGen.init(128, sr);

            SecretKey sKey = keyGen.generateKey();
            byte[] key =  sKey.getEncoded();
            byte[] encryptedMessageByte = encrypt(key, messageByte);
            String encryptedMessage = Base64.encodeToString(encryptedMessageByte, Base64.DEFAULT);
            System.out.println("Encrypt : Encoded Encrypted Message : " + encryptedMessage);

            //RSA ENCRYPTION FOR KEY
            System.out.println("Encrypt : Encrypt key with RSA");
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKeyByte));
            Cipher cipherRSA = Cipher.getInstance("RSA");
            cipherRSA.init(Cipher.ENCRYPT_MODE, publicKey);
            key = cipherRSA.doFinal(key);
            String encodedKey = Base64.encodeToString(key, Base64.DEFAULT);
            System.out.println("Encrypt : Encoded Key : " + encodedKey);

            //MESSAGE HASH
            String fixHexString = createMDSHAHash(message);
            System.out.println("Encrypt : Hex String : " + fixHexString);

            messageEncrypt.encryptedMessage = encryptedMessage;
            messageEncrypt.messageKey = encodedKey;
            messageEncrypt.messageHash = fixHexString;
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return messageEncrypt;
    }

    public String decryptMessage(String message, String messageKey, String messageHash, Context context) {
        SharedPrefsUtil sharedPrefsUtil = new SharedPrefsUtil(context);
        String originalMessage = "";
        PrivateKey userPrivateKey = sharedPrefsUtil.getUserPrivateKey();
        byte[] messageByte = Base64.decode(message, Base64.DEFAULT);

        byte[] key = Base64.decode(messageKey, Base64.DEFAULT);

        try {
            //RSA decryption for key
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, userPrivateKey);
            key = cipher.doFinal(key);
            //AES decryption for message
            byte[] originalMessageByte = decrypt(key, messageByte);
            originalMessage = new String(originalMessageByte, "UTF-8");
            System.out.println("Decryption Complete : " + originalMessage);
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        //check if message manipulated
        if(originalMessage != "") {
            try {
                String fixHexString = createMDSHAHash(originalMessage);
                System.out.println("Orinal Hash : " + messageHash);
                System.out.println("Current Hash : " + fixHexString);

                //return originalMessage;
                if(fixHexString.equals(messageHash)) {
                    return originalMessage;
                }
                else {
                    return "Message had been manipulated";
                }

                //return originalMessage;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            originalMessage = "Cannot decrypt message";
            return originalMessage;
        }

        return "";
    }

    private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }

    private static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }
}
