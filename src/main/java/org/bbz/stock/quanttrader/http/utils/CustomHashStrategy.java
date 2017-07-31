package org.bbz.stock.quanttrader.http.utils;

import io.vertx.core.VertxException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

/**
 * 自定义密码生成类
 */
public enum CustomHashStrategy{
    INSTANCE;
    private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();


    public String computeHash( String password, String salt, String algo ){
        try {
            MessageDigest md = MessageDigest.getInstance( algo );
            String concat = (salt == null ? "" : salt) + password;
            byte[] bHash = md.digest( concat.getBytes( StandardCharsets.UTF_8 ) );
            return bytesToHex( bHash );
        } catch( NoSuchAlgorithmException e ) {
            throw new VertxException( e );
        }
    }

    /**
     * Generate a salt
     *
     * @return the generated salt
     */
    public static String generateSalt(){
        final Random r = new SecureRandom();
        byte[] salt = new byte[32];
        r.nextBytes( salt );
        return bytesToHex( salt );
    }

    private static String bytesToHex( byte[] bytes ){
        char[] chars = new char[bytes.length * 2];
        for( int i = 0; i < bytes.length; i++ ) {
            int x = 0xFF & bytes[i];
            chars[i * 2] = HEX_CHARS[x >>> 4];
            chars[1 + i * 2] = HEX_CHARS[0x0F & x];
        }
        return new String( chars );
    }

    /**
     * 根据密码明文和salt生成加密后的密码字符串
     * @param password  原始密码明文
     * @param salt      salt
     */
    public String cryptPassword( String password, String salt ){
        return computeHash( password, salt, "SHA-512" );
    }
}
