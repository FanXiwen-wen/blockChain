package model;
/**
 * �������
 *
 */
public class TransactionOutput {
	//���׽��
	private int value;
	//���׽��շ���Ǯ����Կhashֵ
	private String publicKeyhash;
	
	public TransactionOutput() {
		super();
	}
	public TransactionOutput(int value , String publicKeyHash) {
		this.value = value;
		this.publicKeyhash = publicKeyHash;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public String getPublicKeyhash() {
		return publicKeyhash;
	}
	public void setPublicKeyhash(String publicKeyhash) {
		this.publicKeyhash = publicKeyhash;
	}
	
}
