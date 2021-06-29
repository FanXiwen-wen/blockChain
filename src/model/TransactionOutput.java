package model;
/**
 * 交易输出
 *
 */
public class TransactionOutput {
	//交易金额
	private int value;
	//交易接收方的钱包公钥hash值
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
