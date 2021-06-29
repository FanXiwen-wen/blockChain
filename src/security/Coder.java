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
 * 基础加密组件
 *
 */
public abstract class Coder {
	public static final String KEY_SHA = "SHA";
	public static final String KEY_MD5 = "MD5";
	/**
	 * 哈希算法(消息摘要算法)的特点：加密过程不需要密钥，并且经过加密的数据无法被解密，只有输入相同的明文数据经过相同的消息摘要算法才能得到相同的密文
	 * MAC算法：消息的散列值只有通信双方知道的密钥K控制。而利用hash的MAC算法就叫做HMAC
	 * HMAC：结合了MD和SHA两大系列消息摘要算法,HMAC运算利用哈希算法，以一个密钥和一个消息为输入，生成一个消息摘要作为输出。
	 * 可以选用以下的算法作为MAC算法：
	 * HmacMD5
	 * HmacSHA1
	 * HmacSHA256
	 * HmacSHA384
	 * HmacSHA512
	 */
	public static final String KEY_MAC = "HmacMD5";
	
	/**
	 * BASE64加密
	 */
	public static String encryptBASE64(byte[] key) throws Exception{
		return (new BASE64Encoder()).encodeBuffer(key);
	}
	
	/**
	 * BASE64解密
	 * @throws Exception 
	 */
	public static byte[] decryptBASE64(String key) throws Exception {
		return (new BASE64Decoder()).decodeBuffer(key);
	}
	
	/**
	 * MD5加密
	 * @throws Exception 
	 */
	public static byte[] encryptMD5(byte[] data) throws Exception {
		//提供信息摘要算法：MD5
		MessageDigest md5 = MessageDigest.getInstance(KEY_MD5);
		//MD5加密数据
		md5.update(data);
		//加密后的数据返回的是二进制形式
		return md5.digest();
	}
	
	/**
	 * SHA加密
	 * @throws Exception 
	 */
	public static byte[] encryptSHA(byte[] data) throws Exception {
		//提供信息摘要算法：SHA
		MessageDigest sha = MessageDigest.getInstance(KEY_SHA);
		sha.update(data);
		return sha.digest();
	}
	
	/**
	 * 初始化HMAC密钥
	 * @throws Exception 
	 */
	public static String initMacKey() throws Exception {
		//初始化密钥生成器
		KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_MAC);
		//生成密钥
		SecretKey secretKey = keyGenerator.generateKey();
		//secretKey.getEncoded()返回基本编码格式的密钥，再将此密钥进行BASE64加密
		return encryptBASE64(secretKey.getEncoded());
	}
	
	/**
	 * HMAC加密
	 * @throws Exception 
	 */
	public static byte[] encryptHMAC(byte[] data, String key) throws Exception {
		//第一个参数为解密后的密钥字节，第二个为HmacMD5加密算法，HmacMD5算法是将数据加密后再将所得哈希值与密钥混合进行一次哈希运算
		SecretKey secretKey = new SecretKeySpec(decryptBASE64(key),KEY_MAC);
		Mac mac = Mac.getInstance(secretKey.getAlgorithm());
		mac.init(secretKey);
		return mac.doFinal(data);
	}
}
