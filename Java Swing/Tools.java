package gahh;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

class Tools {
	private static byte[] keys = { 1, -1, 1, -1, 1, -1, 1, -1 };
	
    static String strTobin(String str) {

        byte[] bytes = str.getBytes();
        StringBuilder binary = new StringBuilder();
        for (byte b : bytes) {
            int val = b;
            for (int i = 0; i < 8; i++) {
                binary.append((val & 128) == 0 ? 0 : 1);
                val <<= 1;
            }
            binary.append(' ');
        }
        return binary.toString();
    }

    static String intTostr(String stream, int size) {

        String result = "";
        for (int i = 0; i < stream.length(); i += size) {
            result += (stream.substring(i, Math.min(stream.length(), i + size)) + " ");
        }
        String[] ss = result.split( " " );
        StringBuilder sb = new StringBuilder();
        for (String s : ss) {
            if (!s.equals("00000000")) {
                sb.append((char) Integer.parseInt(s, 2));
            }
        }
        return sb.toString();
    }

    static String CircularLeftShift(String s, int k) {

        String result = s.substring(k);
        for (int i = 0; i < k; i++) {
            result += s.charAt(i);
        }
        return result;
    }

    static String jdkBase64String(byte[] secretKey) {
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(secretKey);
    }

    static byte[] jdkBase64Decoder(String str) throws IOException {
        BASE64Decoder decoder = new BASE64Decoder();
        return decoder.decodeBuffer(str);
    }
    
    public static byte[] hex2byte(String str)
    {
       byte[] bytes = new byte[str.length() / 2];
       for (int i = 0; i < bytes.length; i++)
       {
          bytes[i] = (byte) Integer
                .parseInt(str.substring(2 * i, 2 * i + 2), 16);
       }
       return bytes;
    }
	
	public static String byte2hex(byte[] b)
    {
       String hs = "";
       String stmp = "";

       for (int n = 0; n < b.length; n++)
       {
          stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));

          if (stmp.length() == 1)
          {
             hs = hs + "0" + stmp;
          }
          else
          {
             hs = hs + stmp;
          }

          if (n < b.length - 1)
          {
             hs = hs + "";
          }
       }
       return hs;
    }
	
	public byte[] desEncrypt(byte[] value, String password) throws Exception { 
		Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, generateKey(password));
		byte[] encryptBytes = cipher.doFinal(value);
		String result = byte2hex(encryptBytes);
		System.out.println("DES Encrypt:" + result);
		return encryptBytes;
	}
	/*
	public static String desEncryptString(String value, String password) throws Exception { 
		Cipher cipher = Cipher.getInstance("DES");
		cipher.init(Cipher.ENCRYPT_MODE, generateKey(password));
		byte[] encryptBytes = cipher.doFinal(value.getBytes());
		String result = byte2hex(encryptBytes);
		System.out.println("DES Encrypt:" + result);
		return result;
	}
*/
	private SecretKey generateKey(String secretKey) throws Exception{
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
		DESKeySpec keySpec = new DESKeySpec(secretKey.getBytes("UTF-8"));
		keyFactory.generateSecret(keySpec);
		return keyFactory.generateSecret(keySpec);
	}
	
	public byte[] desDecrypt(byte[] value, String password) throws Exception {
		Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
		cipher.init(Cipher.DECRYPT_MODE, generateKey(password));
		byte[] decryptBytes = cipher.doFinal(value);
		String result = new String(decryptBytes);
		System.out.println("DES Decrypt:" + result);
		return decryptBytes;
	}
	/*
	public static String desDecryptString(String value, String password) throws Exception { 
		Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, generateKey(password));
		byte[] decryptBytes = cipher.doFinal(hex2byte(value));
		String result = new String(decryptBytes);
		System.out.println("DES Decrypt:" + result);
		return result;
	}
*/

    private static byte[] encryptByte(byte[] byteS, byte password[]) {
        byte[] byteFina = null;
        try {// 初始化加密/解密工具
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            DESKeySpec desKeySpec = new DESKeySpec(password);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
            IvParameterSpec iv = new IvParameterSpec(keys);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            byteFina = cipher.doFinal(byteS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return byteFina;
    }
  
    private static byte[] decryptByte(byte[] byteS, byte password[]) {
        byte[] byteFina = null;
        try {
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            DESKeySpec desKeySpec = new DESKeySpec(password);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
            IvParameterSpec iv = new IvParameterSpec(keys);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
            byteFina = cipher.doFinal(byteS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return byteFina;
    }
  
    public static String encryptStr(String strMing, byte md5key[]) {
        byte[] byteMi = null;
        byte[] byteMing = null;
        String strMi = "";
        try {
            byteMing = strMing.getBytes("utf-8");
            byteMi = encryptByte(byteMing, md5key);
            BASE64Encoder base64Encoder = new BASE64Encoder();
            strMi = base64Encoder.encode(byteMi);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            byteMing = null;
            byteMi = null;
        }
        return strMi;
    }
  
    public static String decryptStr(String strMi, byte md5key[]) {
        byte[] byteMing = null;
        String strMing = "";
        try {
            BASE64Decoder decoder = new BASE64Decoder();
            byteMing = decoder.decodeBuffer(strMi);
            byteMing = decryptByte(byteMing, md5key);
            strMing = new String(byteMing);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            byteMing = null;
        }
        return strMing;
    }

}
