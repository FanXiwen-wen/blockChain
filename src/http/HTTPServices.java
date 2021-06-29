package http;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.java_websocket.WebSocket;
import com.alibaba.fastjson.JSON;
import block.BlockService;
import model.Block;
import model.Transaction;
import model.TransactionParam;
import model.Wallet;
import p2p.Message;
import p2p.p2pService;



/**
 * �����������http����
 * @author Administrator
 *
 */
public class HTTPServices {
	private BlockService blockService;
	private p2pService p2pservice;
	
	public HTTPServices(BlockService blockService, p2pService p2pservice) {
		this.blockService = blockService;
		this.p2pservice = p2pservice;
	}
	
	public void initHTTPServer(int port) {
		try {
			Server server = new Server(port);
			System.out.println("listening http port on : " + port);
			ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
			context.setContextPath("/");
			server.setHandler(context);
			
			//��ѯ������:GET����
			context.addServlet(new ServletHolder(new ChainServlet()), "/chain");
			//����Ǯ����POST����
			context.addServlet(new ServletHolder(new CreateWalletServlet()), "/wallet/create");
			//��ѯǮ����GET����
			context.addServlet(new ServletHolder(new GetWalletsServlet()), "/wallet/get");
			//�ڿ�POST����
			context.addServlet(new ServletHolder(new MineServlet()), "/mine");
			//ת�˽��ף�POST����
			context.addServlet(new ServletHolder(new NewTransactionServlet()), "/transactions/new");
			//��ѯδ����Ľ��ף�GET����
			context.addServlet(new ServletHolder(new GetUnpackedTransactionServlet()), "/transactions/unpacked/get");
			//��ѯǮ����GET����
			context.addServlet(new ServletHolder(new GetWalletBalanceServlet()), "/wallet/balance/get");
			//��ѯ���е�socket�ڵ㣺GET����
			context.addServlet(new ServletHolder(new PeersServlet()), "/peers");
			
			server.start();
			server.join();
		}catch(Exception e) {
			System.out.println("init http server is error: " + e.getMessage());
		}
	}
	
	/**
	 * ��ѯ������
	 * @author Administrator
	 *
	 */
	private class ChainServlet extends HttpServlet {
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.setCharacterEncoding("UTF-8");
			resp.getWriter().print("��ǰ��������" + JSON.toJSONString(blockService.getBlockChain()));
		}
	}

	/**
	 * ����Ǯ��
	 */
	private class CreateWalletServlet extends HttpServlet {
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.setCharacterEncoding("UTF-8");
			Wallet wallet = blockService.createWallet();
			Wallet[] wallets = {new Wallet(wallet.getPublicKey())};
			String msg = JSON.toJSONString(new Message(p2pservice.RESPONSE_WALLET,JSON.toJSONString(wallets)));
			p2pservice.broadcast(msg);
			resp.getWriter().print("����Ǯ���ɹ���Ǯ����ַ��" + wallet.getAddress());
		}
	}
	
	/**
	 * ��ѯ��ǰ�ڵ������Ǯ��Ǯ��
	 */
	private class GetWalletsServlet extends HttpServlet {
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.setCharacterEncoding("UTF-8");
			resp.getWriter().print("��ǰ�ڵ�Ǯ����" + JSON.toJSONString(blockService.getMyWalletMap().values()));
		}
	}
	
	/**
	 * �ڿ�
	 */
	private class MineServlet extends HttpServlet {
		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.setCharacterEncoding("UTF-8");
			String address = req.getParameter("address");
			Wallet myWallet = blockService.getMyWalletMap().get(address);
			if(myWallet == null) {
				resp.getWriter().print("�ڿ�ָ����Ǯ��������");
				return;
			}
			Block newBlock = blockService.mine(address);
			if(newBlock == null) {
				resp.getWriter().print("�ڿ�ʧ�ܣ������������ڵ��Ѿ��ڳ�������");
				return;
			}
			Block[] blocks = {newBlock};
			String msg = JSON.toJSONString(new Message(p2pservice.RESPONSE_BLOCKCHAIN,JSON.toJSONString(blocks)));
			p2pservice.broadcast(msg);
			resp.getWriter().print("�ڿ����ɵ������飺" + JSON.toJSONString(newBlock));
		}
	}

	
	/**
	 * ת�˽���
	 */
	private class NewTransactionServlet extends HttpServlet {
		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.setCharacterEncoding("UTF-8");
			TransactionParam txParam = JSON.parseObject(getReqBody(req), TransactionParam.class);
			
			Wallet senderWallet = blockService.getMyWalletMap().get(txParam.getSender());
			Wallet recipientWallet = blockService.getMyWalletMap().get(txParam.getRecipient());
			if(recipientWallet == null ) {
				//������շ����Ǳ��ڵ��Ǯ��
				recipientWallet = blockService.getOtherWalletMap().get(txParam.getRecipient());
			}
			if(senderWallet == null || recipientWallet == null) {
				resp.getWriter().print("Ǯ��������");
				return;
			}
			
			Transaction newTransaction = blockService.createTransaction(senderWallet, recipientWallet, txParam.getAmount());
			if(newTransaction == null) {
				resp.getWriter().print("Ǯ��" + txParam.getSender() + "��������Ҳ���һ�ʵ���" + txParam.getAmount() + "BTC��UTXO");
			}else {
				resp.getWriter().print("�����ɽ��ף�"+JSON.toJSONString(newTransaction));
				Transaction[] txs = {newTransaction};
				String msg = JSON.toJSONString(new Message(p2pservice.RESPONSE_TRANSACTION,JSON.toJSONString(txs)));
				p2pservice.broadcast(msg);
			}
		}
	}
	
	/**
	 * ��ѯδ����Ľ���
	 */
	private class GetUnpackedTransactionServlet extends HttpServlet {
		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.setCharacterEncoding("UTF-8");
			List<Transaction> transactions = new ArrayList<>(blockService.getAllTransactions());
			transactions.removeAll(blockService.getPackedTransactions());
			resp.getWriter().print("���ڵ�δ����Ľ��ף�" + JSON.toJSONString(transactions));
		}
	}
	
	/**
	 * ��ѯǮ�����
	 */
	private class GetWalletBalanceServlet extends HttpServlet {
		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.setCharacterEncoding("UTF-8");
			String address = req.getParameter("address");
			resp.getWriter().print("Ǯ�����Ϊ��"+blockService.getWalletBalance(address) + "BTC");
		}
	}
	
	/**
	 * ��ѯ����socket�ڵ�
	 */
	 private class PeersServlet extends HttpServlet {
	        @Override
	        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	            resp.setCharacterEncoding("UTF-8");
	            for(WebSocket socket : p2pservice.getSockets()) {
	            	InetSocketAddress remoteSocketAddress = socket.getRemoteSocketAddress();
	            	resp.getWriter().print(remoteSocketAddress.getHostName() + ":" +remoteSocketAddress.getPort() + "  ");
	            }
	        }
	    }
	
	/**
	 * �õ�������
	 */
	private String getReqBody(HttpServletRequest req) throws IOException {
		BufferedReader br = req.getReader();
		String str, body = "";
		while ((str = br.readLine()) != null) {
			body += str;
		}
		return body;
	}
}
