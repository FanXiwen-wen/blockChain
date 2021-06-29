package model;

import java.util.ArrayList;
import java.util.List;


/**
 * 区块结构
 * 区块链的共识机制：谁先挖矿挖出来听谁的、最长链原则
 */
public class Block {
	//区块索引号
	private int index;
	//当前区块的哈希值，也就是区块的唯一标识
	private String hash;
	//生成区块的时间戳
	private long timestamp;
	//当前区块的交易集合
	private List<Transaction> transactions;
	//工作量证明：计算出正确哈希值的次数
	private int nouce;
	//前一个区块的hash值
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
