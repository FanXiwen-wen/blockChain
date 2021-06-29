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
 * 区块链的核心服务
 *
 */
public class BlockService {
	/**
	 * 区块链的存储结构
	 */
	private List<Block> blockChain = new ArrayList<Block>();
	
	/**
	 * 当前节点钱包集合
	 * 前者为键，后者为值 存储为哈希表结构
	 */
	private Map<String,Wallet> myWalletMap = new HashMap<>();
	
	/**
	 * 其他节点钱包集合，钱包只包含公钥
	 */
	private Map<String,Wallet> otherWalletMap = new HashMap<>();
	
	/**
	 * 转账交易集合
	 */
	private List<Transaction> allTransactions = new ArrayList<>();
	
	/**
	 * 已打包的转账交易
	 */
	private List<Transaction> packedTransactions = new ArrayList<>();
	
	//初始化区块链，生成创世区块
	public BlockService() {
		Block genesisBlock = new Block(1, System.currentTimeMillis(), new ArrayList<Transaction>(), 1, "1", "1");
		blockChain.add(genesisBlock);
		System.out.println("生成创世区块：" +JSON.toJSONString(genesisBlock));
	}
	
	/**
	 * 生成新的区块
	 */
	private Block createNewBlock(int nouce , String previousHash , String hash , List<Transaction> blockTxs) {
		Block block = new Block(blockChain.size() + 1 , System.currentTimeMillis() , blockTxs , nouce , previousHash , hash);
		if(addBlock(block)) {
			return block;
		}
		return null;
	}
	
	/**
	 * 计算区块的hash值
	 * 区块哈希值：SHA256(前一个区块的hash值+交易记录信息+随机数)
	 */
	private String calculateHash(String previousHash , List<Transaction> currentTransactions , int nounce) {
		return CryptoUtil.SHA256(previousHash + JSON.toJSONString(currentTransactions) +nounce) ;
	}
	
	/**
	 * 验证hash值是否满足系统条件 这里设定区块的hash值钱4位全为0
	 */
	private boolean isValidHash(String hash) {
		return hash.startsWith("0000");
	}
	
	/**
	 * 获取最新的区块，即当前链上的最后一个区块
	 */
	public Block getLatestBlock() {
		return blockChain.size() > 0 ? blockChain.get(blockChain.size() - 1) : null;
	}

	/**
	 * 验证新区块是否有效 
	 */
	public boolean isValidNewBlock(Block newBlock , Block previousBlock) {
		if(!previousBlock.getHash().equals(newBlock.getPreviousHash())) {
			System.out.println("新区块的前一个区块hash验证不通过");
			return false;
		}else {
			//验证新区块hash值的正确性
			String hash = calculateHash(newBlock.getPreviousHash(),newBlock.getTransactions(),newBlock.getNouce());
			if(!hash.equals(newBlock.getHash())) {
				System.out.println("新区块的hash无效" + hash + " " + newBlock.getHash());
				return false;
			}
			if(!isValidHash(newBlock.getHash())) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 添加新区块
	 */
	public boolean addBlock(Block newBlock) {
		if(isValidNewBlock(newBlock,getLatestBlock())) {
			blockChain.add(newBlock);
			//新区块的交易需要加入到已经打包的交易集合里去
			packedTransactions.addAll(newBlock.getTransactions());
			return true;
		}
		return false;
	}
	
	/**
	 * 验证整个区块链是否有效
	 */
	private boolean isValidChain(List<Block> chain) {
		Block block = null;
		Block lastBlock = chain.get(0);
		int currentIndex = 1;
		//循环遍历每一个区块验证是否正确
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
	 * 替换本地的区块链
	 */
	public void replaceChain(List<Block> newBlocks) {
		if(isValidChain(newBlocks) && newBlocks.size() > blockChain.size()) {
			blockChain = newBlocks;
			//更新已打包的交易集合
			packedTransactions.clear();
			blockChain.forEach(block -> {
				packedTransactions.addAll(block.getTransactions());
			});
		}else {
			System.out.println("接收的区块链无效");
		}
	}
	
	
	
	
	/**
	 * 生成系统奖励交易
	 * 挖矿成功之后系统会生成奖励交易
	 */
	public Transaction newCoinBaseTx(String toAddress) {
		TransactionInput txIn = new TransactionInput("0" , -1 ,null , null);
		Wallet wallet = myWalletMap.get(toAddress);
		//指定生成区块的奖励为10BTC
		TransactionOutput txOut = new TransactionOutput(10,wallet.getHashPubKey());
		return new Transaction(CryptoUtil.UUID(),txIn,txOut);
	}
	
	
	/**
	 * 验证所有交易是否有效
	 */
	private void verifyAllTransactions(List<Transaction> blockTxs) {
		List<Transaction> invalidTxs = new ArrayList<>();
		for(Transaction tx : blockTxs) {
			if(!verifyTransaction(tx)) {
				invalidTxs.add(tx);
			}
		}
		//在当前交易集合中去除所有无效交易
		blockTxs.removeAll(invalidTxs);
		//在总的交易中要去除无效交易
		allTransactions.removeAll(invalidTxs);
	}
	
	/**
	 * 挖矿
	 */
	public Block mine(String toAddress) {
		//创建系统奖励交易
		allTransactions.add(newCoinBaseTx(toAddress));
		//去除已打包进区块的交易
		List<Transaction> blockTxs = new ArrayList<Transaction>(allTransactions);
		blockTxs.removeAll(packedTransactions);
		//验证所有未打包的交易是否都有效
		verifyAllTransactions(blockTxs);
		
		String newBlockHash = "";
		int nouce = 0;
		long start = System.currentTimeMillis();
		System.out.println("开始挖矿");
		
		while(true) {
			//计算新区块的hash值
			newBlockHash = calculateHash(getLatestBlock().getHash(),blockTxs,nouce);
			//校验hash值
			if(isValidHash(newBlockHash)) {
				System.out.println("挖矿成功，正确的hash值：" + newBlockHash);
				System.out.println("挖矿耗费时间：" + (System.currentTimeMillis()-start) + "ms");
				break;
			}
			System.out.println("错误的hash值：" + newBlockHash);
			nouce++;
		}
		
		//创建新的区块
		Block block = createNewBlock(nouce , getLatestBlock().getHash() , newBlockHash , blockTxs);
		return block;
	}
	
	/**
	 * 查找未被消费的交易
	 */
	private List<Transaction> findUnspentTransactions(String address) {
		List<Transaction> unspentTxs = new ArrayList<Transaction>();
		//Set集合不会存储重复的元素
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
	 * 用交易id查询交易
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
	 * 验证交易是否有效 看交易输入是否存在
	 */
	private boolean verifyTransaction(Transaction tx) {
		if(tx.coinBaseTx()) {
			return true;
		}
		Transaction prevTx = findTransaction(tx.getTxIn().getTxId());
		return tx.verify(prevTx);
	}
	
	/**
	 * 创建交易
	 */
	public Transaction createTransaction(Wallet senderWallet , Wallet recipientWallet , int amount) {
		List<Transaction> unspentTxs = findUnspentTransactions(senderWallet.getAddress());
		Transaction prevTx = null;
		for(Transaction transaction : unspentTxs) {
			//找零 这里只做了发送方所有未被消费的金额的交易等于本比交易金额的情况
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
		//用交易的hash值和发送方的私钥进行签名
		transaction.sign(senderWallet.getPrivateKey(), prevTx);
		allTransactions.add(transaction);
		return transaction;
	}
	
	
	
	/**
	 * 创建钱包
	 * @return
	 */
	public Wallet createWallet() {
		Wallet wallet = Wallet.generateWallet();
		String address = wallet.getAddress();
		//根据钱包公钥生成的钱包地址是唯一的，所以可以作为键
		myWalletMap.put(address, wallet);
		return wallet;
	}
	
	/**
	 * 获取钱包余额
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
	 * 获取区块链
	 * @return
	 */
	public List<Block> getBlockChain(){
		return blockChain;
	}
	
	/**
	 * 设置区块链
	 */
	public void setBlockChain(List<Block> blockChain) {
		this.blockChain = blockChain;
	}
	
	/**
	 * 获取当前节点的钱包集合
	 */
	public Map<String,Wallet> getMyWalletMap(){
		return myWalletMap;
	}
	
	/**
	 * 设置当前节点的钱包集合
	 */
	public void setMyWalletMap(Map<String,Wallet> myWalletMap) {
		this.myWalletMap = myWalletMap;
	}
	
	/**
	 * 获取其他节点的钱包集合
	 * @return
	 */
	public Map<String, Wallet> getOtherWalletMap() {
		return otherWalletMap;
	}

	/**
	 * 设置其他节点的钱包集合
	 * @param otherWalletMap
	 */
	public void setOtherWalletMap(Map<String, Wallet> otherWalletMap) {
		this.otherWalletMap = otherWalletMap;
	}
	
	/**
	 * 获取所有交易
	 */
	public List<Transaction> getAllTransactions() {
		return allTransactions;
	}

	/**
	 * 设置所有交易集合
	 * @param allTransactions
	 */
	public void setAllTransactions(List<Transaction> allTransactions) {
		this.allTransactions = allTransactions;
	}

	/**
	 * 获取所有已经打包的交易
	 * @return
	 */
	public List<Transaction> getPackedTransactions() {
		return packedTransactions;
	}

	/**
	 * 设置所有已经打包的交易集合
	 * @param packedTransactions
	 */
	public void setPackedTransactions(List<Transaction> packedTransactions) {
		this.packedTransactions = packedTransactions;
	}
}
