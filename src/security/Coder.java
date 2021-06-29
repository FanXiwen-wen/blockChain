package security;

import java.security.MessageDigest;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * 
 * �����������
 *
 */
public abstract class Coder {
	public static final String KEY_SHA = "SHA";
	public static final String KEY_MD5 = "MD5";
	/**
	 * ��ϣ�㷨(��ϢժҪ�㷨)���ص㣺���ܹ��̲���Ҫ��Կ�����Ҿ������ܵ������޷������ܣ�ֻ��������ͬ���������ݾ�����ͬ����ϢժҪ�㷨���ܵõ���ͬ������
	 * MAC�㷨����Ϣ��ɢ��ֵֻ��ͨ��˫��֪������ԿK���ơ�������hash��MAC�㷨�ͽ���HMAC
	 * HMAC�������MD��SHA����ϵ����ϢժҪ�㷨,HMAC�������ù�ϣ�㷨����һ����Կ��һ����ϢΪ���룬����һ����ϢժҪ��Ϊ�����
	 * ����ѡ�����µ��㷨��ΪMAC�㷨��
	 * HmacMD5
	 * HmacSHA1
	 * HmacSHA256
	 * HmacSHA384
	 * HmacSHA512
	 */
	public static final String KEY_MAC = "HmacMD5";
	
	/**
	 * BASE64����
	 */
	public static String encryptBASE64(byte[] key) throws Exception{
		return (new BASE64Encoder()).encodeBuffer(key);
	}
	
	/**
	 * BASE64����
	 * @throws Exception 
	 */
	public static byte[] decryptBASE64(String key) throws Exception {
		return (new BASE64Decoder()).decodeBuffer(key);
	}
	
	/**
	 * MD5����
	 * @throws Exception 
	 */
	public static byte[] encryptMD5(byte[] data) throws Exception {
		//�ṩ��ϢժҪ�㷨��MD5
		MessageDigest md5 = MessageDigest.getInstance(KEY_MD5);
		//MD5��������
		md5.update(data);
		//���ܺ�����ݷ��ص��Ƕ�������ʽ
		return md5.digest();
	}
	
	/**
	 * SHA����
	 * @throws Exception 
	 */
	public static byte[] encryptSHA(byte[] data) throws Exception {
		//�ṩ��ϢժҪ�㷨��SHA
		MessageDigest sha = MessageDigest.getInstance(KEY_SHA);
		sha.update(data);
		return sha.digest();
	}
	
	/**
	 * ��ʼ��HMAC��Կ
	 * @throws Exception 
	 */
	public static String initMacKey() throws Exception {
		//��ʼ����Կ������
		KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_MAC);
		//������Կ
		SecretKey secretKey = keyGenerator.generateKey();
		//secretKey.getEncoded()���ػ��������ʽ����Կ���ٽ�����Կ����BASE64����
		return encryptBASE64(secretKey.getEncoded());
	}
	
	/**
	 * HMAC����
	 * @throws Exception 
	 */
	public static byte[] encryptHMAC(byte[] data, String key) throws Exception {
		//��һ������Ϊ���ܺ����Կ�ֽڣ��ڶ���ΪHmacMD5�����㷨��HmacMD5�㷨�ǽ����ݼ��ܺ��ٽ����ù�ϣֵ����Կ��Ͻ���һ�ι�ϣ����
		SecretKey secretKey = new SecretKeySpec(decryptBASE64(key),KEY_MAC);
		Mac mac = Mac.getInstance(secretKey.getAlgorithm());
		mac.init(secretKey);
		return mac.doFinal(data);
	}
}
