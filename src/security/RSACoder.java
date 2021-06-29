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
 * RSA�������
 *
 */
public abstract class RSACoder extends Coder {
	public static final String KEY_ALGORITHM = "RSA";
	public static final String SIGNATURE_ALGORITHM = "MD5withRSA";
	private static final String PUBLIC_KEY = "RSAPublicKey";
	private static final String PRIVATE_KEY = "RSAPrivateKey";
	/**
	 * ˽Կ����Ϣ��������ǩ��
	 * @throws Exception 
	 */
	public static String sign(byte[] data , String privateKey) throws Exception {
		//������BASE64�����˽Կ
		byte[] keyBytes = decryptBASE64(privateKey);
		//����PKCS8EncodedKeySpec������Կ�淶����ı����ʽ
		PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
		//ʹ��KEY_ALGORITHMָ���ļ����㷨
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		//��ȡ˽Կ����
		PrivateKey priKey = keyFactory.generatePrivate(pkcs8KeySpec);
		//��˽Կ����Ϣ��������ǩ��
		Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
		signature.initSign(priKey);
		signature.update(data);
		
		return encryptBASE64(signature.sign());
	}
	/**
	 * �ù�Կ��֤����ǩ��
	 * @throws Exception 
	 */
	public static boolean verify(byte[] data , String publicKey , String sign) throws Exception {
		//������BASE64����Ĺ�Կ
		byte[] keyBytes = decryptBASE64(publicKey);
		//����X509EncodedKeySpec����
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
		//KEY_ALGORITHMָ���ļ����㷨
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		//ȡ��Կ����
		PublicKey pubKey = keyFactory.generatePublic(keySpec);
		//�ù�Կ����
		Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
		signature.initVerify(pubKey);
		signature.update(data);
		
		//��֤ǩ���Ƿ���ȷ
		return signature.verify(decryptBASE64(sign));
	}
	/**
	 * ��˽Կ����
	 * @throws Exception 
	 */
	public static byte[] decryptByPrivateKey(byte[] data , String key) throws Exception {
		//����Կ���н���
		byte[] keyBytes = decryptBASE64(key);
		//ȡ��˽Կ
		PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		Key privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
		//�����ݽ��н���
		Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		return cipher.doFinal(data);
	}
	/**
	 * �ù�Կ����
	 * @throws Exception 
	 */
	public static byte[] decryptByPublicKey(byte[] data , String key) throws Exception {
		//����Կ����
		byte[] keyBytes = decryptBASE64(key);
		//ȡ�ù�Կ
		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		Key publicKey = keyFactory.generatePublic(x509KeySpec);
		//�����ݽ��н���
		Cipher cipher  = Cipher.getInstance(keyFactory.getAlgorithm());
		cipher.init(Cipher.DECRYPT_MODE, publicKey);
		return cipher.doFinal(data);
	}
	/**
	 * �ù�Կ����
	 * @throws Exception 
	 */
	public static byte[] encryptByPublicKey(byte[] data , String key) throws Exception {
		//�Թ�Կ����
		byte[] keyBytes = decryptBASE64(key);
		//ȡ�ù�Կ
		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		Key publicKey = keyFactory.generatePublic(x509KeySpec);
		//�����ݼ���
		Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		return cipher.doFinal(data);
	}
	/**
	 * ��˽Կ����
	 */
	public static byte[] encryptByPrivateKey(byte[] data, String key) throws Exception {
		// ����Կ����
		byte[] keyBytes = decryptBASE64(key);
		// ȡ��˽Կ
		PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		Key privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
		// �����ݼ���
		Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
		cipher.init(Cipher.ENCRYPT_MODE, privateKey);
		return cipher.doFinal(data);
	}
	/**
	 * ��ʼ����Կ
	 * @throws Exception 
	 */
	public static Map<String,Object> initKey() throws Exception {
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
		keyPairGen.initialize(1024);
		//����һ�Թ�˽Կ
		KeyPair keyPair = keyPairGen.generateKeyPair();
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		Map<String,Object> keyMap = new HashMap<String,Object>(2);
		keyMap.put(PUBLIC_KEY, publicKey);
		keyMap.put(PRIVATE_KEY, privateKey);
		return keyMap;
	}
	/**
	 * ��ȡ˽Կ
	 * @throws Exception 
	 */
	public static String getPrivateKey(Map<String,Object> keyMap) throws Exception {
		Key key = (Key) keyMap.get(PRIVATE_KEY);
		return encryptBASE64(key.getEncoded());
	}
	/**
	 * ��ȡ��Կ
	 * @throws Exception 
	 */
	public static String getPublicKey(Map<String,Object> keyMap) throws Exception {
		Key key = (Key) keyMap.get(PUBLIC_KEY);
		return encryptBASE64(key.getEncoded());
	}
	public static void main(String[] args) throws Exception{
		String s="fanxiwen";
		//���Թ�Կ���ܣ�˽Կ����
		Map<String,Object> map=RSACoder.initKey();
		String publicKey=RSACoder.getPublicKey(map);
		String privateKey=RSACoder.getPrivateKey(map);
		System.out.println("��Կ��"+publicKey);
		System.out.println("˽Կ��"+privateKey);
		//��Կ����
		byte[] msg=RSACoder.encryptByPublicKey(s.getBytes(), publicKey);
		//˽Կ����
		msg=RSACoder.decryptByPrivateKey(msg,privateKey);
		System.out.println("���ܺ�����ݣ�"+new String(msg));
		
		//��֤˽Կǩ������Կ��֤ǩ��
		String sign=RSACoder.sign(s.getBytes(), privateKey);
		System.out.println("ǩ�����ݣ�"+sign);
		//��֤ǩ��
		boolean status=RSACoder.verify(s.getBytes(), publicKey, sign);
		System.out.println("ǩ����ȷ��"+status);
	}
}
