package model;

import com.alibaba.fastjson.JSON;

import security.CryptoUtil;
import security.RSACoder;

/**
 * ����
 *
 */
public class Transaction {
	//���׵�Ψһ��ʶ
	private String id;
	//��������
	private TransactionInput txIn;
	//�������
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
	 * ���ɵ�ǰ���׵Ĺ�ϣ
	 * �洢���ݲ��õĹ�ϣ��ṹ��Ԫ�صĴ�ȡ˳���ܱ�֤һ�¡�����Ҫ��֤����Ψһ�����ظ�����Ҫ��д����hashCode()������equals()����
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
	 * �ж���ʽ����ǲ�����������ʱϵͳ�Ľ�������
	 */
	public boolean coinBaseTx() {
		return txIn.getTxId().equals("0") && getTxIn().getValue() == -1;
	}
	/**
	 * ���ڽ���ǩ���Ľ��׼�¼���� �ѽ��׿�¡һ��
	 */
	public Transaction cloneTx() {
		TransactionInput transactionInput = new TransactionInput(txIn.getTxId(),txIn.getValue(),null,null);
		TransactionOutput transactionOutput = new TransactionOutput(txOut.getValue(),txOut.getPublicKeyhash());
		return new Transaction(id,transactionInput,transactionOutput);
	}
	/**
	 * ��˽Կ���ɽ���ǩ��
	 */
	public void sign(String privateKey , Transaction prevTx) {
		if(coinBaseTx()) {
			return;
		}
		if(!prevTx.getId().equals(txIn.getTxId())) {
			System.out.println("����ǩ��ʧ�ܣ���ǰ�����������õ�ǰһ�ʽ����봫���ǰһ�ʽ��ײ�ƥ��");
		}
		//�Ȱѽ��׿�¡һ��
		Transaction txClone = cloneTx();
		// ����һ�ʽ��׽�������Ĺ�Կ����Ϊǰһ�ʽ��׽�������Ĺ�Կ��ϣ
		txClone.getTxIn().setPublicKey(prevTx.getTxOut().getPublicKeyhash());
		String sign = "";
		try {
			//�Խ��׵Ĺ�ϣֵ��˽Կ���м���
			sign = RSACoder.sign(txClone.hash().getBytes(),privateKey);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		txIn.setSignature(sign);
	}
	/**
	 * ��֤����ǩ�������ڴ����ʱ����֤��д���˱���ȥ��ʱ������֤
	 */
	public boolean verify(Transaction prevTx) {
		if(coinBaseTx()) {
			return true;
		}
		if(!prevTx.getId().equals(txIn.getTxId())) {
			System.err.println("��֤����ǩ��ʧ�ܣ���ǰ�����������õ�ǰһ�ʽ����봫���ǰһ�ʽ��ײ�ƥ��");
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
