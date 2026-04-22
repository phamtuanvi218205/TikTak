package com.example.TikTok.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
public class EncryptionUtil {
    private static final String SECRET_KEY = System.getenv("AES_SECRET_KEY");
    private static final String ALGORITHM="AES";
    public static String encrypt(String plainText){
        if (plainText==null||plainText.isEmpty()){
            return plainText;
        }
        try{
            SecretKeySpec key=new SecretKeySpec(SECRET_KEY.getBytes(),ALGORITHM);
            Cipher cipher=Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE,key);
            byte[] encryptedBytes= cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi mã hóa tin nhắn");
        }
    }
    public static String decrypt(String encryptedText){
        if (encryptedText==null||encryptedText.isEmpty()){
            return encryptedText;
        }
        try{
            SecretKeySpec key=new SecretKeySpec(SECRET_KEY.getBytes(),ALGORITHM);
            Cipher cipher=Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE,key);
            byte[] decryptedBytes=cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi giải mã tin nhắn", e);
        }
    }
}
