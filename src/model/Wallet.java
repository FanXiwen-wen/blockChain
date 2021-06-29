package model;

import java.util.Map;

import com.alibaba.fastjson.JSON;

import security.CryptoUtil;
import security.RSACoder;

/**
 * 钱包
 *
 */
public class Wallet {
	//公钥
	private String publicKey;
	//私钥
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
	 * 只包含公钥的钱包地址，用来给其他节点转账的时候用到
	 */
	public Wallet(String publicKey) {
		this.publicKey = publicKey;
	}
	
	public Wallet(String publicKey , String privateKey) {
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}
	
	/**
	 * 产生钱包
	 */
	public static Wallet generateWallet() {
		Map<String,Object> initKey;
		try {
			//本地生成公私钥对
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
	 * 生成钱包的公钥哈希
	 */
	public static String hashPubKey(String publicKey) {
		return CryptoUtil.SHA256(publicKey);
	}
	/**
	 * 获取钱包的公钥哈希
	 */
	public String getHashPubKey() {
		return CryptoUtil.SHA256(publicKey);
	}
	/**
	 * 根据钱包公钥生成钱包地址
	 */
	public static String getAddress(String publicKey) {
		//生成钱包的公钥哈希
		String publicKeyHash = hashPubKey(publicKey);
		//再将公钥哈希进行MD5加密生成钱包地址
		return CryptoUtil.MD5(publicKeyHash);
	}
	/**
	 * 获取钱包地址
	 */
	public String getAddress() {
		String publicKeyHash = hashPubKey(publicKey);
		return CryptoUtil.MD5(publicKeyHash);
	}
	
}
