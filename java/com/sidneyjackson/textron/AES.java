package com.sidneyjackson.textron;

/**
 * Created by sidneyjackson on 11/5/17.
 */

import java.security.MessageDigest;
import java.util.Arrays;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {

    public static String getCipher(String plainText, String encryptionKey, byte[] iv) {
        String cipherText = "";
        try {
            byte[] cipher = cipherText.getBytes();
            cipher = encrypt(plainText, encryptionKey, iv);
            for (int i=0; i<cipher.length; i++) { // Repeat this Method of Obtaining intger to test Arduino/
                cipherText+=(new Integer(cipher[i]) + " ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cipherText;
    }

    public static byte[] encrypt(String plainText, String encryptionKey, byte[] iv) throws Exception {
        //Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding", "SunJCE");
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
        //cipher.init(Cipher.ENCRYPT_MODE, key,new IvParameterSpec(IV.getBytes("UTF-8")));
        cipher.init(Cipher.ENCRYPT_MODE, key,new IvParameterSpec(iv));
        return cipher.doFinal(plainText.getBytes("UTF-8"));
    }

    public static String decrypt(byte[] cipherText, String encryptionKey, byte[] iv) throws Exception{
        //Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding", "SunJCE");
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
        //cipher.init(Cipher.DECRYPT_MODE, key,new IvParameterSpec(IV.getBytes("UTF-8")));
        cipher.init(Cipher.DECRYPT_MODE, key,new IvParameterSpec(iv));
        return new String(cipher.doFinal(cipherText),"UTF-8");
    }
}

