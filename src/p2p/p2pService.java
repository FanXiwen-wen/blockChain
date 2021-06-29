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
 * p2p����������
 * @author Administrator
 *
 */
public class p2pService {
	//socket����
	private List<WebSocket> sockets;
	//p2p�������������������ķ���
	private BlockService blockService;
	//��ѯ���µ�����
	public final static int QUERY_LATEST_BLOCK = 0;
	//��ѯ����������
	public final static int QUERY_BLOCKCHAIN = 1;
	//��ѯ���׼���
	public final static int QUERY_TRANSACTION = 2;
	//��ѯ�Ѵ���Ľ��׼���
	public final static int QUERY_PACKED_TRANSACTION = 3;
	//��ѯǮ������
	public final static int QUERY_WALLET = 4;
	//����õ���������
	public final static int RESPONSE_BLOCKCHAIN = 5;
	//����㲥�õ��Ľ��׼���
	public final static int RESPONSE_TRANSACTION = 6;
	//����㲥�õ����Ѵ���Ľ��׼���
	public final static int RESPONSE_PACKED_TRANSACTION = 7;
	//����㲥�õ���Ǯ������
	public final static int RESPONSE_WALLET = 8;
	
	public p2pService(BlockService blockService) {
		this.blockService = blockService;
		this.sockets = new ArrayList<WebSocket>();
	}
	
	/**
	 * ���������׽���
	 */
	public List<WebSocket> getSockets(){
		return sockets;
	}
	
	/**
	 * ������յ�����Ϣ
	 */
	public void handleMessage(WebSocket webSocket, String msg, List<WebSocket> sockets) {
		try {
			Message message = JSON.parseObject(msg, Message.class);
			System.out.println("���յ�" + webSocket.getRemoteSocketAddress().getPort() + "��p2p��Ϣ"
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
			System.out.println("����p2p��Ϣ����:" + e.getMessage());
		}
	}
	
	/**
	 * ������Ϣ
	 */
	public void write(WebSocket ws , String message) {
		System.out.println("���͸�" + ws.getRemoteSocketAddress().getPort() + "��p2p��Ϣ" + message);
		ws.send(message);
	}
	
	/**
	 * �㲥��Ϣ
	 */
	public void broadcast(String message) {
		if(sockets.size() == 0) {
			return;
		}
		System.out.println("======�㲥��Ϣ��ʼ��");
		for(WebSocket socket:sockets) {
			this.write(socket, message);
		}
		System.out.println("======�㲥��Ϣ����");
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
	 * ������������
	 */
	public String responseLatestBlockMsg() {
		Block[] blocks = {blockService.getLatestBlock()};
		return JSON.toJSONString(new Message(RESPONSE_BLOCKCHAIN , JSON.toJSONString(blocks)));
	}
	
	/**
	 * ��������������
	 */
	public String responseBlockChainMsg() {
		return JSON.toJSONString(new Message(RESPONSE_BLOCKCHAIN , JSON.toJSONString(blockService.getBlockChain())));
	}
	
	/**
	 * ��ѯ���׼���
	 */
	public String responseTransactions() {
		return JSON.toJSONString(new Message(RESPONSE_TRANSACTION,JSON.toJSONString(blockService.getAllTransactions())));
	}
	
	/**
	 * ��ѯ�Ѵ���Ľ��׼���
	 */
	public String responsePackedTransactions() {
		return JSON.toJSONString(new Message(RESPONSE_PACKED_TRANSACTION,JSON.toJSONString(blockService.getPackedTransactions())));
	}
	
	/**
	 * ��ѯǮ������
	 */
	public String responseWallets() {
		List<Wallet> wallets = new ArrayList<Wallet>();
		//����Ǳ��ڵ��Ǯ������ôҪ������ֻ������Կ��Ǯ��
		blockService.getMyWalletMap().forEach((address,wallet) -> {
			wallets.add(new Wallet(wallet.getPublicKey()));
		});
		blockService.getOtherWalletMap().forEach((address,wallet) -> {
			wallets.add(wallet);
		});
		return JSON.toJSONString(new Message(RESPONSE_WALLET,JSON.toJSONString(wallets)));
	}
	
	/**
	 * ����õ����鼯�ϵ���Ϣ
	 */
	public synchronized void handleBlockChainResponse(String message , List<WebSocket> sockets) {
		//�õ����յ���������
		List<Block> receiveBlockChain = JSON.parseArray(message,Block.class);
		//�Խ��յ����������е����鰴��index����
		Collections.sort(receiveBlockChain,new Comparator<Block>() {
			public int compare(Block block1 , Block block2) {
				return block1.getIndex() - block2.getIndex();
			}
		});
		
		Block latestBlockReceived = receiveBlockChain.get(receiveBlockChain.size()-1);
		Block latestBlock = blockService.getLatestBlock();
		//������յ������ڵ�㲥���������������������űȱ��ڵ������������������Ŵ���ô��Ҫ���±��ڵ��������
		if(latestBlockReceived.getIndex() > latestBlock.getIndex()) {
			//������յ�������������Ǳ��ڵ���������ĺ�һ�����飬��ô��ֱ�ӽ���������ӵ����ڵ���������ĺ��漴��
			if(latestBlock.getHash().equals(latestBlockReceived.getPreviousHash())) {
				System.out.println("���½��յ���������뵽���ص�������");
				if(blockService.addBlock(latestBlockReceived)) {
					//����ɹ���Ҫ�Ѽ���������������Ϣ�㲥��ȥ
					broadcast(responseLatestBlockMsg());
				}
			}else if (receiveBlockChain.size()==1) {
				System.out.println("��ѯ����ͨѶ�ڵ��ϵ�������");
				broadcast(queryBlockChainMsg());
			}else {
				//�ó������汾�صĶ�������ʶ����
				blockService.replaceChain(receiveBlockChain);
			}
		}else {
			System.out.println("���յ������������ȱ�������������������");
		}
	}
	
	/**
	 * ����㲥�õ��Ľ��׼���
	 */
	public void handleTransactionResponse(String message) {
		List<Transaction> txs = JSON.parseArray(message,Transaction.class);
		blockService.getAllTransactions().addAll(txs);
	}
	
	/**
	 * ����㲥�õ����Ѵ���Ľ��׼���
	 */
	public void handlePackedTransactionResponse(String message) {
		List<Transaction> txs = JSON.parseArray(message,Transaction.class);
		blockService.getPackedTransactions().addAll(txs);
	}
	/**
	 * ����㲥�õ���Ǯ������
	 */
	public void handleWalletResponse(String message) {
		List<Wallet> wallets = JSON.parseArray(message,Wallet.class);
		wallets.forEach(wallet -> {
			blockService.getOtherWalletMap().put(wallet.getAddress(), wallet);
		});
	}
}
