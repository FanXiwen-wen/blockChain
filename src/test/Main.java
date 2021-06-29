package test;

import block.BlockService;
import http.HTTPServices;
import p2p.p2pClient;
import p2p.p2pServer;
import p2p.p2pService;

public class Main {
	public static void main(String[] args) {
		//args[0]������������http�˿�
		//args[1]�����ڵ���Ϊ����˼����Ķ˿�
		//args[2]�����ڵ���Ϊ�ͻ���Ҫ���ӵķ���˵Ķ˿�
		if(args != null && (args.length == 1 || args.length == 2 || args.length == 3 )) {
			try {
				BlockService blockService = new BlockService();
				p2pService p2pservice = new p2pService(blockService);
				startP2PServer(args,p2pservice);
				HTTPServices httpService = new HTTPServices(blockService,p2pservice);
				int httpPort = Integer.valueOf(args[0]);
				httpService.initHTTPServer(httpPort);
			}catch(Exception e) {
				System.out.println("start up is error: " + e.getMessage());
			}
		}else {
			System.out.println("���δ���");
		}
	}
	
	/**
	 * ����p2p����
	 */
	public static void startP2PServer(String[] args,p2pService p2pservice) {
		p2pServer p2pserver = new p2pServer(p2pservice);
		p2pClient p2pclient = new p2pClient(p2pservice);
		int p2pPort = Integer.valueOf(args[1]);
		//���������
		p2pserver.initp2pServer(p2pPort);
		//��Ϊ�ͻ���ȥ���ӷ����
		if(args.length == 3 && args[2] != null) {
			p2pclient.connectToPeer(args[2]);
		}
	}
}
