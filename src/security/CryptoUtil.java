package security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * 加密工具类
 *
 */
public class CryptoUtil {
	private CryptoUtil() {
		
	}
	/**
	 * SHA256加密
	 * @param str
	 * @return
	 */
	public static String SHA256(String str) {
		MessageDigest messageDigest;
		String encodeStr = "";
		try {
			messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(str.getBytes("UTF-8"));
			encodeStr = byte2Hex(messageDigest.digest());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("getSHA256 is error" + e.getMessage());
		}
		return encodeStr;
	}
	
	/**
	 * MD5加密算法
	 */
	public static String MD5(String str) {
		try {
			StringBuffer buffer = new StringBuffer();
			char[] chars = new char[]{'0','1','2','3',  
	                '4','5','6','7','8','9','A','B','C','D','E','F'};  
			byte[] bytes = str.getBytes();
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			byte[] targ = messageDigest.digest(bytes);
			for(byte b:targ) {  
                buffer.append(chars[(b>>4)&0x0F]);  
                buffer.append(chars[b&0x0F]);  
            }  
            return buffer.toString(); 
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static String UUID() {
		return UUID.randomUUID().toString().replaceAll("\\-", "");
	}
	
	/**
	 * 将字节数组转换成为十六进制
	 */
	public static String byte2Hex(byte[] bytes) {
		StringBuilder builder = new StringBuilder();
		String temp;
		for(int i = 0 ; i < bytes.length ; i++) {
			temp = Integer.toHexString(bytes[i] & 0xFF);
			//得到一位的进行补0操作
			if(temp.length() == 1) {
				builder.append("0");
			}
			builder.append(temp);
		}
		return builder.toString();
	}
}
