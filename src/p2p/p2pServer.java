package p2p;

import java.net.InetSocketAddress;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

/**
 * p2p服务端
 * @author Administrator
 *
 */
public class p2pServer {
	
	private p2pService p2pservice;
	
	public p2pServer(p2pService p2pservice) {
		this.p2pservice = p2pservice;
	}
	
	//服务端初始化
	public void initp2pServer(int port) {
		final WebSocketServer socketServer = new WebSocketServer(new InetSocketAddress(port)) {
			//回调方法：连接打开
			public void onOpen(WebSocket webSocket , ClientHandshake clientHandshake) {
				p2pservice.getSockets().add(webSocket);
			}
			
			//连接关闭
			public void onClose(WebSocket webSocket,int i,String s,boolean b) {
				System.out.println("connection failed to peer:" +webSocket.getRemoteSocketAddress());
				p2pservice.getSockets().remove(webSocket);
			}
			
			//接收到来自客户端的消息
			public void onMessage(WebSocket webSocket,String msg) {
				p2pservice.handleMessage(webSocket, msg, p2pservice.getSockets());
			}
			
			public void onError(WebSocket webSocket,Exception e) {
				System.out.println("connection failed to peer:" + webSocket.getRemoteSocketAddress());
				p2pservice.getSockets().remove(webSocket);
			}
			
			public void onStart() {
				
			}
		};
		socketServer.start();
		System.out.println("Listening websocket p2p port on: " + port );
	}
}
