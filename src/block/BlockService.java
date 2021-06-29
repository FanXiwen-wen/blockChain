package block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSON;

import model.Block;
import model.Transaction;
import model.TransactionInput;
import model.TransactionOutput;
import model.Wallet;
import security.CryptoUtil;

/**
 * �������ĺ��ķ���
 *
 */
public class BlockService {
	/**
	 * �������Ĵ洢�ṹ
	 */
	private List<Block> blockChain = new ArrayList<Block>();
	
	/**
	 * ��ǰ�ڵ�Ǯ������
	 * ǰ��Ϊ��������Ϊֵ �洢Ϊ��ϣ��ṹ
	 */
	private Map<String,Wallet> myWalletMap = new HashMap<>();
	
	/**
	 * �����ڵ�Ǯ�����ϣ�Ǯ��ֻ������Կ
	 */
	private Map<String,Wallet> otherWalletMap = new HashMap<>();
	
	/**
	 * ת�˽��׼���
	 */
	private List<Transaction> allTransactions = new ArrayList<>();
	
	/**
	 * �Ѵ����ת�˽���
	 */
	private List<Transaction> packedTransactions = new ArrayList<>();
	
	//��ʼ�������������ɴ�������
	public BlockService() {
		Block genesisBlock = new Block(1, System.currentTimeMillis(), new ArrayList<Transaction>(), 1, "1", "1");
		blockChain.add(genesisBlock);
		System.out.println("���ɴ������飺" +JSON.toJSONString(genesisBlock));
	}
	
	/**
	 * �����µ�����
	 */
	private Block createNewBlock(int nouce , String previousHash , String hash , List<Transaction> blockTxs) {
		Block block = new Block(blockChain.size() + 1 , System.currentTimeMillis() , blockTxs , nouce , previousHash , hash);
		if(addBlock(block)) {
			return block;
		}
		return null;
	}
	
	/**
	 * ���������hashֵ
	 * �����ϣֵ��SHA256(ǰһ�������hashֵ+���׼�¼��Ϣ+�����)
	 */
	private String calculateHash(String previousHash , List<Transaction> currentTransactions , int nounce) {
		return CryptoUtil.SHA256(previousHash + JSON.toJSONString(currentTransactions) +nounce) ;
	}
	
	/**
	 * ��֤hashֵ�Ƿ�����ϵͳ���� �����趨�����hashֵǮ4λȫΪ0
	 */
	private boolean isValidHash(String hash) {
		return hash.startsWith("0000");
	}
	
	/**
	 * ��ȡ���µ����飬����ǰ���ϵ����һ������
	 */
	public Block getLatestBlock() {
		return blockChain.size() > 0 ? blockChain.get(blockChain.size() - 1) : null;
	}

	/**
	 * ��֤�������Ƿ���Ч 
	 */
	public boolean isValidNewBlock(Block newBlock , Block previousBlock) {
		if(!previousBlock.getHash().equals(newBlock.getPreviousHash())) {
			System.out.println("�������ǰһ������hash��֤��ͨ��");
			return false;
		}else {
			//��֤������hashֵ����ȷ��
			String hash = calculateHash(newBlock.getPreviousHash(),newBlock.getTransactions(),newBlock.getNouce());
			if(!hash.equals(newBlock.getHash())) {
				System.out.println("�������hash��Ч" + hash + " " + newBlock.getHash());
				return false;
			}
			if(!isValidHash(newBlock.getHash())) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * ���������
	 */
	public boolean addBlock(Block newBlock) {
		if(isValidNewBlock(newBlock,getLatestBlock())) {
			blockChain.add(newBlock);
			//������Ľ�����Ҫ���뵽�Ѿ�����Ľ��׼�����ȥ
			packedTransactions.addAll(newBlock.getTransactions());
			return true;
		}
		return false;
	}
	
	/**
	 * ��֤�����������Ƿ���Ч
	 */
	private boolean isValidChain(List<Block> chain) {
		Block block = null;
		Block lastBlock = chain.get(0);
		int currentIndex = 1;
		//ѭ������ÿһ��������֤�Ƿ���ȷ
		while(currentIndex < chain.size()) {
			block = chain.get(currentIndex);
			
			if(!isValidNewBlock(block,lastBlock)) {
				return false;
			}
			
			lastBlock = block;
			currentIndex++;
		}
		return true;
	}
	
	/**
	 * �滻���ص�������
	 */
	public void replaceChain(List<Block> newBlocks) {
		if(isValidChain(newBlocks) && newBlocks.size() > blockChain.size()) {
			blockChain = newBlocks;
			//�����Ѵ���Ľ��׼���
			packedTransactions.clear();
			blockChain.forEach(block -> {
				packedTransactions.addAll(block.getTransactions());
			});
		}else {
			System.out.println("���յ���������Ч");
		}
	}
	
	
	
	
	/**
	 * ����ϵͳ��������
	 * �ڿ�ɹ�֮��ϵͳ�����ɽ�������
	 */
	public Transaction newCoinBaseTx(String toAddress) {
		TransactionInput txIn = new TransactionInput("0" , -1 ,null , null);
		Wallet wallet = myWalletMap.get(toAddress);
		//ָ����������Ľ���Ϊ10BTC
		TransactionOutput txOut = new TransactionOutput(10,wallet.getHashPubKey());
		return new Transaction(CryptoUtil.UUID(),txIn,txOut);
	}
	
	
	/**
	 * ��֤���н����Ƿ���Ч
	 */
	private void verifyAllTransactions(List<Transaction> blockTxs) {
		List<Transaction> invalidTxs = new ArrayList<>();
		for(Transaction tx : blockTxs) {
			if(!verifyTransaction(tx)) {
				invalidTxs.add(tx);
			}
		}
		//�ڵ�ǰ���׼�����ȥ��������Ч����
		blockTxs.removeAll(invalidTxs);
		//���ܵĽ�����Ҫȥ����Ч����
		allTransactions.removeAll(invalidTxs);
	}
	
	/**
	 * �ڿ�
	 */
	public Block mine(String toAddress) {
		//����ϵͳ��������
		allTransactions.add(newCoinBaseTx(toAddress));
		//ȥ���Ѵ��������Ľ���
		List<Transaction> blockTxs = new ArrayList<Transaction>(allTransactions);
		blockTxs.removeAll(packedTransactions);
		//��֤����δ����Ľ����Ƿ���Ч
		verifyAllTransactions(blockTxs);
		
		String newBlockHash = "";
		int nouce = 0;
		long start = System.currentTimeMillis();
		System.out.println("��ʼ�ڿ�");
		
		while(true) {
			//�����������hashֵ
			newBlockHash = calculateHash(getLatestBlock().getHash(),blockTxs,nouce);
			//У��hashֵ
			if(isValidHash(newBlockHash)) {
				System.out.println("�ڿ�ɹ�����ȷ��hashֵ��" + newBlockHash);
				System.out.println("�ڿ�ķ�ʱ�䣺" + (System.currentTimeMillis()-start) + "ms");
				break;
			}
			System.out.println("�����hashֵ��" + newBlockHash);
			nouce++;
		}
		
		//�����µ�����
		Block block = createNewBlock(nouce , getLatestBlock().getHash() , newBlockHash , blockTxs);
		return block;
	}
	
	/**
	 * ����δ�����ѵĽ���
	 */
	private List<Transaction> findUnspentTransactions(String address) {
		List<Transaction> unspentTxs = new ArrayList<Transaction>();
		//Set���ϲ���洢�ظ���Ԫ��
		Set<String> spentTxs = new HashSet<String>();
		for(Transaction tx:allTransactions) {
			if(tx.coinBaseTx()) {
				continue;
			}
			if(address.equals(Wallet.getAddress(tx.getTxIn().getPublicKey()))) {
				spentTxs.add(tx.getTxIn().getTxId());
			}
		}
		for(Block block : blockChain) {
			List<Transaction> transactions = block.getTransactions();
			for(Transaction tx:transactions) {
				if(address.equals(CryptoUtil.MD5(tx.getTxOut().getPublicKeyhash()))) {
					if(!spentTxs.contains(tx.getId())) {
						unspentTxs.add(tx);
					}
				}
			}
		}
		return unspentTxs;
	}
	
	/**
	 * �ý���id��ѯ����
	 */
	private Transaction findTransaction(String id) {
		for(Transaction tx : allTransactions) {
			if(id.equals(tx.getId())) {
				return tx;
			}
		}
		return null;
	}
	
	/**
	 * ��֤�����Ƿ���Ч �����������Ƿ����
	 */
	private boolean verifyTransaction(Transaction tx) {
		if(tx.coinBaseTx()) {
			return true;
		}
		Transaction prevTx = findTransaction(tx.getTxIn().getTxId());
		return tx.verify(prevTx);
	}
	
	/**
	 * ��������
	 */
	public Transaction createTransaction(Wallet senderWallet , Wallet recipientWallet , int amount) {
		List<Transaction> unspentTxs = findUnspentTransactions(senderWallet.getAddress());
		Transaction prevTx = null;
		for(Transaction transaction : unspentTxs) {
			//���� ����ֻ���˷��ͷ�����δ�����ѵĽ��Ľ��׵��ڱ��Ƚ��׽������
			if(transaction.getTxOut().getValue() == amount) {
				prevTx = transaction;
				break;
			}
		}
		if(prevTx == null) {
			return null;
		}
		TransactionInput txIn = new TransactionInput(prevTx.getId(),amount,null,senderWallet.getPublicKey());
		TransactionOutput txOut = new TransactionOutput(amount,recipientWallet.getHashPubKey());
		Transaction transaction = new Transaction(CryptoUtil.UUID(),txIn,txOut);
		//�ý��׵�hashֵ�ͷ��ͷ���˽Կ����ǩ��
		transaction.sign(senderWallet.getPrivateKey(), prevTx);
		allTransactions.add(transaction);
		return transaction;
	}
	
	
	
	/**
	 * ����Ǯ��
	 * @return
	 */
	public Wallet createWallet() {
		Wallet wallet = Wallet.generateWallet();
		String address = wallet.getAddress();
		//����Ǯ����Կ���ɵ�Ǯ����ַ��Ψһ�ģ����Կ�����Ϊ��
		myWalletMap.put(address, wallet);
		return wallet;
	}
	
	/**
	 * ��ȡǮ�����
	 */
	public int getWalletBalance(String address) {
		List<Transaction> unspentTxs = findUnspentTransactions(address);
		int balance = 0;
		for(Transaction transaction : unspentTxs) {
			balance += transaction.getTxOut().getValue();
		}
		return balance;
	}
	
	/**
	 * ��ȡ������
	 * @return
	 */
	public List<Block> getBlockChain(){
		return blockChain;
	}
	
	/**
	 * ����������
	 */
	public void setBlockChain(List<Block> blockChain) {
		this.blockChain = blockChain;
	}
	
	/**
	 * ��ȡ��ǰ�ڵ��Ǯ������
	 */
	public Map<String,Wallet> getMyWalletMap(){
		return myWalletMap;
	}
	
	/**
	 * ���õ�ǰ�ڵ��Ǯ������
	 */
	public void setMyWalletMap(Map<String,Wallet> myWalletMap) {
		this.myWalletMap = myWalletMap;
	}
	
	/**
	 * ��ȡ�����ڵ��Ǯ������
	 * @return
	 */
	public Map<String, Wallet> getOtherWalletMap() {
		return otherWalletMap;
	}

	/**
	 * ���������ڵ��Ǯ������
	 * @param otherWalletMap
	 */
	public void setOtherWalletMap(Map<String, Wallet> otherWalletMap) {
		this.otherWalletMap = otherWalletMap;
	}
	
	/**
	 * ��ȡ���н���
	 */
	public List<Transaction> getAllTransactions() {
		return allTransactions;
	}

	/**
	 * �������н��׼���
	 * @param allTransactions
	 */
	public void setAllTransactions(List<Transaction> allTransactions) {
		this.allTransactions = allTransactions;
	}

	/**
	 * ��ȡ�����Ѿ�����Ľ���
	 * @return
	 */
	public List<Transaction> getPackedTransactions() {
		return packedTransactions;
	}

	/**
	 * ���������Ѿ�����Ľ��׼���
	 * @param packedTransactions
	 */
	public void setPackedTransactions(List<Transaction> packedTransactions) {
		this.packedTransactions = packedTransactions;
	}
}
