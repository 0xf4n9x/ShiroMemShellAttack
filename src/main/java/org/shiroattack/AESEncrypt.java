package org.shiroattack;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

public class AESEncrypt {

    public static String encrypt(byte[] data, byte[] key, String mode) throws Exception {
        if (mode == "CBC") {
            return encryptCBC(data, key);
        }

        return encryptGCM(data, key);
    }

    public static String encryptCBC(byte[] data, byte[] key) throws Exception {
        byte[] iv = new byte[16];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);

        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        byte[] encryptedData = cipher.doFinal(data);

        byte[] encryptedDataWithIv = new byte[16 + encryptedData.length];
        System.arraycopy(iv, 0, encryptedDataWithIv, 0, 16);
        System.arraycopy(encryptedData, 0, encryptedDataWithIv, 16, encryptedData.length);

        return org.apache.shiro.codec.Base64.encodeToString(encryptedDataWithIv);
//        return java.util.Base64.getEncoder().encodeToString(encryptedDataWithIv);
    }


    public static String encryptGCM(byte[] data, byte[] key) throws Exception {
        byte[] iv = new byte[16];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);

        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

        byte[] encryptedData = cipher.doFinal(data);

        byte[] encryptedDataWithIv = new byte[16 + encryptedData.length];
        System.arraycopy(iv, 0, encryptedDataWithIv, 0, 16);
        System.arraycopy(encryptedData, 0, encryptedDataWithIv, 16, encryptedData.length);

        return org.apache.shiro.codec.Base64.encodeToString(encryptedDataWithIv);
    }

}
