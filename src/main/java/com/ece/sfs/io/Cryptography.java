package com.ece.sfs.io;

import org.springframework.beans.factory.annotation.Autowired;

import javax.crypto.*;
import javax.crypto.spec.ChaCha20ParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


public class Cryptography {

    private int counter = 1;

    private final SecretKey secretKey;
    private final byte[] nonce;

    private static final String TRANSMISSION = "ChaCha20";

    @Autowired
    public Cryptography(SecretKey secretKey, byte[] nonce) {
        this.secretKey = secretKey;
        this.nonce = nonce;
    }

    public byte[] encrypt(byte[] plainText) throws
            NoSuchPaddingException,
            NoSuchAlgorithmException,
            InvalidAlgorithmParameterException,
            InvalidKeyException,
            IllegalBlockSizeException,
            BadPaddingException {

        Cipher cipher = Cipher.getInstance(TRANSMISSION);

        ChaCha20ParameterSpec param = param(nonce, counter);

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, param);

        return cipher.doFinal(plainText);
    }


    public byte[] decrypt(byte[] encryptedText) throws
            NoSuchPaddingException,
            NoSuchAlgorithmException,
            InvalidAlgorithmParameterException,
            InvalidKeyException,
            IllegalBlockSizeException,
            BadPaddingException {

        Cipher cipher = Cipher.getInstance(TRANSMISSION);

        ChaCha20ParameterSpec param = param(nonce, counter);

        cipher.init(Cipher.DECRYPT_MODE, secretKey, param);

        return cipher.doFinal(encryptedText);
    }

    private static ChaCha20ParameterSpec param(byte[] nonce, int counter) {
        return new ChaCha20ParameterSpec(nonce, counter);
    }

    public static String toB64Str(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes).replace("/", "-");
    }

    public static byte[] b64StrToBytes(String str) {
        return Base64.getDecoder().decode(str.replace("-", "/"));
    }
}
