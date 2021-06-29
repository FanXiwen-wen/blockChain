package model;

import java.util.Map;

import com.alibaba.fastjson.JSON;

import security.CryptoUtil;
import security.RSACoder;

/**
 * Ǯ��
 *
 */
public class Wallet {
	//��Կ
	private String publicKey;
	//˽Կ
	private String privateKey;
	
	public String getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
	public String getPrivateKey() {
		return privateKey;
	}
	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}
	
	public Wallet() {
		
	}
	/**
	 * ֻ������Կ��Ǯ����ַ�������������ڵ�ת�˵�ʱ���õ�
	 */
	public Wallet(String publicKey) {
		this.publicKey = publicKey;
	}
	
	public Wallet(String publicKey , String privateKey) {
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}
	
	/**
	 * ����Ǯ��
	 */
	public static Wallet generateWallet() {
		Map<String,Object> initKey;
		try {
			//�������ɹ�˽Կ��
			initKey = RSACoder.initKey();
			String publicKey = RSACoder.getPublicKey(initKey);
			String privateKey = RSACoder.getPrivateKey(initKey);
			return new Wallet(publicKey,privateKey);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * ����Ǯ���Ĺ�Կ��ϣ
	 */
	public static String hashPubKey(String publicKey) {
		return CryptoUtil.SHA256(publicKey);
	}
	/**
	 * ��ȡǮ���Ĺ�Կ��ϣ
	 */
	public String getHashPubKey() {
		return CryptoUtil.SHA256(publicKey);
	}
	/**
	 * ����Ǯ����Կ����Ǯ����ַ
	 */
	public static String getAddress(String publicKey) {
		//����Ǯ���Ĺ�Կ��ϣ
		String publicKeyHash = hashPubKey(publicKey);
		//�ٽ���Կ��ϣ����MD5��������Ǯ����ַ
		return CryptoUtil.MD5(publicKeyHash);
	}
	/**
	 * ��ȡǮ����ַ
	 */
	public String getAddress() {
		String publicKeyHash = hashPubKey(publicKey);
		return CryptoUtil.MD5(publicKeyHash);
	}
	
}
