package p2p;

import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

/**
 * p2p客户端
 * @author Administrator
 *
 */
public class p2pClient {
	
	private p2pService p2pservice;
	
	public p2pClient(p2pService p2pservice) {
		this.p2pservice = p2pservice;
	}
	
	public void connectToPeer(String peer) {
		try {
			final WebSocketClient socketClient = new WebSocketClient(new URI(peer)) {
				//建立连接
				public void onOpen(ServerHandshake serverHandShake) {
					p2pservice.write(this, p2pservice.queryLatestBlockMsg());
					p2pservice.write(this, p2pservice.queryTransactionMsg());
					p2pservice.write(this, p2pservice.queryPackedTransactionMsg());
					p2pservice.write(this, p2pservice.queryWalletMsg());
					p2pservice.getSockets().add(this);
				}
				
				//接收到消息时
				public void onMessage(String msg) {
					p2pservice.handleMessage(this, msg, p2pservice.getSockets());
				}
				
				public void onClose(int i,String msg,boolean b) {
					System.out.println("connection failed");
					p2pservice.getSockets().remove(this);
				}
				
				public void onError(Exception e) {
					System.out.println("connection failed");
					p2pservice.getSockets().remove(this);
				}
			};
			socketClient.connect();
		}catch(URISyntaxException e) {
			System.out.println("p2p connect is error:" + e.getMessage());
		}
	}
}
