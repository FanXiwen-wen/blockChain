package model;

import com.alibaba.fastjson.JSON;

import security.CryptoUtil;
import security.RSACoder;

/**
 * 交易
 *
 */
public class Transaction {
	//交易的唯一标识
	private String id;
	//交易输入
	private TransactionInput txIn;
	//交易输出
	private TransactionOutput txOut;
	
	public Transaction() {
		super();
	}
	public Transaction(String id, TransactionInput txIn, TransactionOutput txOut) {
		super();
		this.id = id;
		this.txIn = txIn;
		this.txOut = txOut;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public TransactionInput getTxIn() {
		return txIn;
	}
	public void setTxIn(TransactionInput txIn) {
		this.txIn = txIn;
	}
	public TransactionOutput getTxOut() {
		return txOut;
	}
	public void setTxOut(TransactionOutput txOut) {
		this.txOut = txOut;
	}
	/**
	 * 生成当前交易的哈希
	 * 存储数据采用的哈希表结构，元素的存取顺序不能保证一致。由于要保证键的唯一、不重复，需要重写键的hashCode()方法、equals()方法
	 */
	public String hash() {
		return CryptoUtil.SHA256(JSON.toJSONString(this));
	}
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id==null) ? 0 :id.hashCode());
		return result;
	}
	public boolean equals(Object obj) {
		if(this == obj) 
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		Transaction other = (Transaction) obj;
		if(id == null) {
			if(other.id != null)
				return false;
		}else if (!id.equals(other.id))
			return false;
		return true;
	}
	/**
	 * 判断这笔交易是不是生成区块时系统的奖励交易
	 */
	public boolean coinBaseTx() {
		return txIn.getTxId().equals("0") && getTxIn().getValue() == -1;
	}
	/**
	 * 用于交易签名的交易记录副本 把交易克隆一遍
	 */
	public Transaction cloneTx() {
		TransactionInput transactionInput = new TransactionInput(txIn.getTxId(),txIn.getValue(),null,null);
		TransactionOutput transactionOutput = new TransactionOutput(txOut.getValue(),txOut.getPublicKeyhash());
		return new Transaction(id,transactionInput,transactionOutput);
	}
	/**
	 * 用私钥生成交易签名
	 */
	public void sign(String privateKey , Transaction prevTx) {
		if(coinBaseTx()) {
			return;
		}
		if(!prevTx.getId().equals(txIn.getTxId())) {
			System.out.println("交易签名失败：当前交易输入引用的前一笔交易与传入的前一笔交易不匹配");
		}
		//先把交易克隆一遍
		Transaction txClone = cloneTx();
		// 把这一笔交易交易输入的公钥设置为前一笔交易交易输出的公钥哈希
		txClone.getTxIn().setPublicKey(prevTx.getTxOut().getPublicKeyhash());
		String sign = "";
		try {
			//对交易的哈希值用私钥进行加密
			sign = RSACoder.sign(txClone.hash().getBytes(),privateKey);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		txIn.setSignature(sign);
	}
	/**
	 * 验证交易签名，是在打包的时候被验证，写到账本里去的时候再验证
	 */
	public boolean verify(Transaction prevTx) {
		if(coinBaseTx()) {
			return true;
		}
		if(!prevTx.getId().equals(txIn.getTxId())) {
			System.err.println("验证交易签名失败：当前交易输入引用的前一笔交易与传入的前一笔交易不匹配");
		}
		Transaction txClone = cloneTx();
		txClone.getTxIn().setPublicKey(prevTx.getTxOut().getPublicKeyhash());
		boolean result = false;
		try {
			result = RSACoder.verify(txClone.hash().getBytes(), txIn.getPublicKey(), txIn.getSignature());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
}
