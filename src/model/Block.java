package model;

import java.util.ArrayList;
import java.util.List;


/**
 * ����ṹ
 * �������Ĺ�ʶ���ƣ�˭���ڿ��ڳ�����˭�ġ����ԭ��
 */
public class Block {
	//����������
	private int index;
	//��ǰ����Ĺ�ϣֵ��Ҳ���������Ψһ��ʶ
	private String hash;
	//���������ʱ���
	private long timestamp;
	//��ǰ����Ľ��׼���
	private List<Transaction> transactions;
	//������֤�����������ȷ��ϣֵ�Ĵ���
	private int nouce;
	//ǰһ�������hashֵ
	private String previousHash;
	

	public Block(int index , long timestamp , List<Transaction> transactions , int nouce , String previousHash , String hash) {
		super();
		this.index = index;
		this.timestamp = timestamp;
		this.transactions = transactions;
		this.nouce = nouce;
		this.previousHash = previousHash;
		this.hash = hash;
	}
	public Block(int index , long timestamp , ArrayList<Transaction> transactions , int nouce , String previousHash , String hash) {
		super();
		this.index = index;
		this.timestamp = timestamp;
		this.transactions = transactions;
		this.nouce = nouce;
		this.previousHash = previousHash;
		this.hash = hash;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public List<Transaction> getTransactions() {
		return transactions;
	}

	public void setTransactions(List<Transaction> transactions) {
		this.transactions = transactions;
	}

	public int getNouce() {
		return nouce;
	}

	public void setNouce(int nouce) {
		this.nouce = nouce;
	}

	public String getPreviousHash() {
		return previousHash;
	}

	public void setPreviousHash(String previousHash) {
		this.previousHash = previousHash;
	}
	
}
