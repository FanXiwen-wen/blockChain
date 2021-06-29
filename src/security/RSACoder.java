package security;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;

/**
 * RSA加密组件
 *
 */
public abstract class RSACoder extends Coder {
	public static final String KEY_ALGORITHM = "RSA";
	public static final String SIGNATURE_ALGORITHM = "MD5withRSA";
	private static final String PUBLIC_KEY = "RSAPublicKey";
	private static final String PRIVATE_KEY = "RSAPrivateKey";
	/**
	 * 私钥对信息生成数字签名
	 * @throws Exception 
	 */
	public static String sign(byte[] data , String privateKey) throws Exception {
		//解密由BASE64编码的私钥
		byte[] keyBytes = decryptBASE64(privateKey);
		//构造PKCS8EncodedKeySpec对象：密钥规范管理的编码格式
		PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
		//使用KEY_ALGORITHM指定的加密算法
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		//获取私钥对象
		PrivateKey priKey = keyFactory.generatePrivate(pkcs8KeySpec);
		//用私钥对信息生成数字签名
		Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
		signature.initSign(priKey);
		signature.update(data);
		
		return encryptBASE64(signature.sign());
	}
	/**
	 * 用公钥验证数字签名
	 * @throws Exception 
	 */
	public static boolean verify(byte[] data , String publicKey , String sign) throws Exception {
		//解密由BASE64编码的公钥
		byte[] keyBytes = decryptBASE64(publicKey);
		//构造X509EncodedKeySpec对象
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
		//KEY_ALGORITHM指定的加密算法
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		//取公钥对象
		PublicKey pubKey = keyFactory.generatePublic(keySpec);
		//用公钥解密
		Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
		signature.initVerify(pubKey);
		signature.update(data);
		
		//验证签名是否正确
		return signature.verify(decryptBASE64(sign));
	}
	/**
	 * 用私钥解密
	 * @throws Exception 
	 */
	public static byte[] decryptByPrivateKey(byte[] data , String key) throws Exception {
		//对密钥进行解密
		byte[] keyBytes = decryptBASE64(key);
		//取得私钥
		PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		Key privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
		//对数据进行解密
		Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		return cipher.doFinal(data);
	}
	/**
	 * 用公钥解密
	 * @throws Exception 
	 */
	public static byte[] decryptByPublicKey(byte[] data , String key) throws Exception {
		//对密钥解密
		byte[] keyBytes = decryptBASE64(key);
		//取得公钥
		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		Key publicKey = keyFactory.generatePublic(x509KeySpec);
		//对数据进行解密
		Cipher cipher  = Cipher.getInstance(keyFactory.getAlgorithm());
		cipher.init(Cipher.DECRYPT_MODE, publicKey);
		return cipher.doFinal(data);
	}
	/**
	 * 用公钥加密
	 * @throws Exception 
	 */
	public static byte[] encryptByPublicKey(byte[] data , String key) throws Exception {
		//对公钥解密
		byte[] keyBytes = decryptBASE64(key);
		//取得公钥
		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		Key publicKey = keyFactory.generatePublic(x509KeySpec);
		//对数据加密
		Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		return cipher.doFinal(data);
	}
	/**
	 * 用私钥加密
	 */
	public static byte[] encryptByPrivateKey(byte[] data, String key) throws Exception {
		// 对密钥解密
		byte[] keyBytes = decryptBASE64(key);
		// 取得私钥
		PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		Key privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
		// 对数据加密
		Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
		cipher.init(Cipher.ENCRYPT_MODE, privateKey);
		return cipher.doFinal(data);
	}
	/**
	 * 初始化密钥
	 * @throws Exception 
	 */
	public static Map<String,Object> initKey() throws Exception {
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
		keyPairGen.initialize(1024);
		//生成一对公私钥
		KeyPair keyPair = keyPairGen.generateKeyPair();
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		Map<String,Object> keyMap = new HashMap<String,Object>(2);
		keyMap.put(PUBLIC_KEY, publicKey);
		keyMap.put(PRIVATE_KEY, privateKey);
		return keyMap;
	}
	/**
	 * 获取私钥
	 * @throws Exception 
	 */
	public static String getPrivateKey(Map<String,Object> keyMap) throws Exception {
		Key key = (Key) keyMap.get(PRIVATE_KEY);
		return encryptBASE64(key.getEncoded());
	}
	/**
	 * 获取公钥
	 * @throws Exception 
	 */
	public static String getPublicKey(Map<String,Object> keyMap) throws Exception {
		Key key = (Key) keyMap.get(PUBLIC_KEY);
		return encryptBASE64(key.getEncoded());
	}
	public static void main(String[] args) throws Exception{
		String s="fanxiwen";
		//测试公钥加密，私钥解密
		Map<String,Object> map=RSACoder.initKey();
		String publicKey=RSACoder.getPublicKey(map);
		String privateKey=RSACoder.getPrivateKey(map);
		System.out.println("公钥："+publicKey);
		System.out.println("私钥："+privateKey);
		//公钥加密
		byte[] msg=RSACoder.encryptByPublicKey(s.getBytes(), publicKey);
		//私钥解密
		msg=RSACoder.decryptByPrivateKey(msg,privateKey);
		System.out.println("解密后的数据："+new String(msg));
		
		//验证私钥签名，公钥验证签名
		String sign=RSACoder.sign(s.getBytes(), privateKey);
		System.out.println("签名内容："+sign);
		//验证签名
		boolean status=RSACoder.verify(s.getBytes(), publicKey, sign);
		System.out.println("签名正确："+status);
	}
}
