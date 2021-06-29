package p2p;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.java_websocket.WebSocket;

import com.alibaba.fastjson.JSON;

import block.BlockService;
import model.Block;
import model.Transaction;
import model.Wallet;

/**
 * p2p公共服务类
 * @author Administrator
 *
 */
public class p2pService {
	//socket集合
	private List<WebSocket> sockets;
	//p2p服务中引用了区块链的服务
	private BlockService blockService;
	//查询最新的区块
	public final static int QUERY_LATEST_BLOCK = 0;
	//查询整个区块链
	public final static int QUERY_BLOCKCHAIN = 1;
	//查询交易集合
	public final static int QUERY_TRANSACTION = 2;
	//查询已打包的交易集合
	public final static int QUERY_PACKED_TRANSACTION = 3;
	//查询钱包集合
	public final static int QUERY_WALLET = 4;
	//处理得到的区块链
	public final static int RESPONSE_BLOCKCHAIN = 5;
	//处理广播得到的交易集合
	public final static int RESPONSE_TRANSACTION = 6;
	//处理广播得到的已打包的交易集合
	public final static int RESPONSE_PACKED_TRANSACTION = 7;
	//处理广播得到的钱包集合
	public final static int RESPONSE_WALLET = 8;
	
	public p2pService(BlockService blockService) {
		this.blockService = blockService;
		this.sockets = new ArrayList<WebSocket>();
	}
	
	/**
	 * 返回所有套接字
	 */
	public List<WebSocket> getSockets(){
		return sockets;
	}
	
	/**
	 * 处理接收到的消息
	 */
	public void handleMessage(WebSocket webSocket, String msg, List<WebSocket> sockets) {
		try {
			Message message = JSON.parseObject(msg, Message.class);
			System.out.println("接收到" + webSocket.getRemoteSocketAddress().getPort() + "的p2p消息"
			        + JSON.toJSONString(message));
			switch (message.getType()) {
			case QUERY_LATEST_BLOCK:
				write(webSocket, responseLatestBlockMsg());
				break;
			case QUERY_BLOCKCHAIN:
				write(webSocket, responseBlockChainMsg());
				break;
			case QUERY_TRANSACTION:
				write(webSocket, responseTransactions());
				break;
			case QUERY_PACKED_TRANSACTION:
				write(webSocket, responsePackedTransactions());
				break;
			case QUERY_WALLET:
				write(webSocket, responseWallets());
				break;
			case RESPONSE_BLOCKCHAIN:
				handleBlockChainResponse(message.getData(), sockets);
				break;
			case RESPONSE_TRANSACTION:
				handleTransactionResponse(message.getData());
				break;
			case RESPONSE_PACKED_TRANSACTION:
				handlePackedTransactionResponse(message.getData());
				break;
			case RESPONSE_WALLET:
				handleWalletResponse(message.getData());
				break;
			}
		} catch (Exception e) {
			System.out.println("处理p2p消息错误:" + e.getMessage());
		}
	}
	
	/**
	 * 发送消息
	 */
	public void write(WebSocket ws , String message) {
		System.out.println("发送给" + ws.getRemoteSocketAddress().getPort() + "的p2p消息" + message);
		ws.send(message);
	}
	
	/**
	 * 广播消息
	 */
	public void broadcast(String message) {
		if(sockets.size() == 0) {
			return;
		}
		System.out.println("======广播消息开始：");
		for(WebSocket socket:sockets) {
			this.write(socket, message);
		}
		System.out.println("======广播消息结束");
	}
	
	public String queryLatestBlockMsg() {
		return JSON.toJSONString(new Message(QUERY_LATEST_BLOCK));
	}
	
	public String queryBlockChainMsg() {
		return JSON.toJSONString(new Message(QUERY_BLOCKCHAIN));
	}
	
	public String queryTransactionMsg() {
		return JSON.toJSONString(new Message(QUERY_TRANSACTION));
	}
	
	public String queryPackedTransactionMsg() {
		return JSON.toJSONString(new Message(QUERY_PACKED_TRANSACTION));
	}
	
	public String queryWalletMsg() {
		return JSON.toJSONString(new Message(QUERY_WALLET));
	}
	
	/**
	 * 返回最新区块
	 */
	public String responseLatestBlockMsg() {
		Block[] blocks = {blockService.getLatestBlock()};
		return JSON.toJSONString(new Message(RESPONSE_BLOCKCHAIN , JSON.toJSONString(blocks)));
	}
	
	/**
	 * 返回整个区块链
	 */
	public String responseBlockChainMsg() {
		return JSON.toJSONString(new Message(RESPONSE_BLOCKCHAIN , JSON.toJSONString(blockService.getBlockChain())));
	}
	
	/**
	 * 查询交易集合
	 */
	public String responseTransactions() {
		return JSON.toJSONString(new Message(RESPONSE_TRANSACTION,JSON.toJSONString(blockService.getAllTransactions())));
	}
	
	/**
	 * 查询已打包的交易集合
	 */
	public String responsePackedTransactions() {
		return JSON.toJSONString(new Message(RESPONSE_PACKED_TRANSACTION,JSON.toJSONString(blockService.getPackedTransactions())));
	}
	
	/**
	 * 查询钱包集合
	 */
	public String responseWallets() {
		List<Wallet> wallets = new ArrayList<Wallet>();
		//如果是本节点的钱包，那么要新生成只包含公钥的钱包
		blockService.getMyWalletMap().forEach((address,wallet) -> {
			wallets.add(new Wallet(wallet.getPublicKey()));
		});
		blockService.getOtherWalletMap().forEach((address,wallet) -> {
			wallets.add(wallet);
		});
		return JSON.toJSONString(new Message(RESPONSE_WALLET,JSON.toJSONString(wallets)));
	}
	
	/**
	 * 处理得到区块集合的消息
	 */
	public synchronized void handleBlockChainResponse(String message , List<WebSocket> sockets) {
		//得到接收到的区块链
		List<Block> receiveBlockChain = JSON.parseArray(message,Block.class);
		//对接收到的区块链中的区块按照index排序
		Collections.sort(receiveBlockChain,new Comparator<Block>() {
			public int compare(Block block1 , Block block2) {
				return block1.getIndex() - block2.getIndex();
			}
		});
		
		Block latestBlockReceived = receiveBlockChain.get(receiveBlockChain.size()-1);
		Block latestBlock = blockService.getLatestBlock();
		//如果接收到其他节点广播的区块链中最新区块的序号比本节点区块链最新区块的序号大，那么就要更新本节点的区块链
		if(latestBlockReceived.getIndex() > latestBlock.getIndex()) {
			//如果接收到的最新区块就是本节点最新区块的后一个区块，那么就直接将最新区块加到本节点最新区块的后面即可
			if(latestBlock.getHash().equals(latestBlockReceived.getPreviousHash())) {
				System.out.println("将新接收到的区块加入到本地的区块链");
				if(blockService.addBlock(latestBlockReceived)) {
					//加入成功就要把加入的最新区块的消息广播出去
					broadcast(responseLatestBlockMsg());
				}
			}else if (receiveBlockChain.size()==1) {
				System.out.println("查询所有通讯节点上的区块链");
				broadcast(queryBlockChainMsg());
			}else {
				//用长链代替本地的短链：共识机制
				blockService.replaceChain(receiveBlockChain);
			}
		}else {
			System.out.println("接收到的区块链不比本地区块链长，不处理");
		}
	}
	
	/**
	 * 处理广播得到的交易集合
	 */
	public void handleTransactionResponse(String message) {
		List<Transaction> txs = JSON.parseArray(message,Transaction.class);
		blockService.getAllTransactions().addAll(txs);
	}
	
	/**
	 * 处理广播得到的已打包的交易集合
	 */
	public void handlePackedTransactionResponse(String message) {
		List<Transaction> txs = JSON.parseArray(message,Transaction.class);
		blockService.getPackedTransactions().addAll(txs);
	}
	/**
	 * 处理广播得到的钱包集合
	 */
	public void handleWalletResponse(String message) {
		List<Wallet> wallets = JSON.parseArray(message,Wallet.class);
		wallets.forEach(wallet -> {
			blockService.getOtherWalletMap().put(wallet.getAddress(), wallet);
		});
	}
}
