package model;
/**
 * ��������
 *
 */
public class TransactionInput {
	//ǰһ�ν��׵�id
	private String txId;
	//���׽��
	private int value;
	//����ǩ��
	private String signature;
	//���׷��ͷ���Ǯ����Կ����ʶǮ����˵����˭����ת��
	private String publicKey;
	
	public TransactionInput() {
		super();
	}
	public TransactionInput(String txId , int value , String signature , String publicKey) {
		super();
		this.txId = txId;
		this.value = value;
		this.signature = signature;
		this.publicKey = publicKey;
	}
	public String getTxId() {
		return txId;
	}
	public void setTxId(String txId) {
		this.txId = txId;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	public String getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
	
}
