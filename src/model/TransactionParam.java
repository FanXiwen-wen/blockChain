package model;
/**
 * ���ײ����ӿ�
 *
 */
public class TransactionParam {
	//���ͷ���Ǯ����ַ
	private String sender;
	//���շ���Ǯ����ַ
	private String recipient;
	//���ͽ��
	private int amount;
	
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getRecipient() {
		return recipient;
	}
	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	
}
