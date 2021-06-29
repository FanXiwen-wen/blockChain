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
 * 区块链对外的http服务
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
			
			//查询区块链:GET请求
			context.addServlet(new ServletHolder(new ChainServlet()), "/chain");
			//创建钱包：POST请求
			context.addServlet(new ServletHolder(new CreateWalletServlet()), "/wallet/create");
			//查询钱包：GET请求
			context.addServlet(new ServletHolder(new GetWalletsServlet()), "/wallet/get");
			//挖矿：POST请求
			context.addServlet(new ServletHolder(new MineServlet()), "/mine");
			//转账交易：POST请求
			context.addServlet(new ServletHolder(new NewTransactionServlet()), "/transactions/new");
			//查询未打包的交易：GET请求
			context.addServlet(new ServletHolder(new GetUnpackedTransactionServlet()), "/transactions/unpacked/get");
			//查询钱包余额：GET请求
			context.addServlet(new ServletHolder(new GetWalletBalanceServlet()), "/wallet/balance/get");
			//查询所有的socket节点：GET请求
			context.addServlet(new ServletHolder(new PeersServlet()), "/peers");
			
			server.start();
			server.join();
		}catch(Exception e) {
			System.out.println("init http server is error: " + e.getMessage());
		}
	}
	
	/**
	 * 查询区块链
	 * @author Administrator
	 *
	 */
	private class ChainServlet extends HttpServlet {
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.setCharacterEncoding("UTF-8");
			resp.getWriter().print("当前区块链：" + JSON.toJSONString(blockService.getBlockChain()));
		}
	}

	/**
	 * 创建钱包
	 */
	private class CreateWalletServlet extends HttpServlet {
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.setCharacterEncoding("UTF-8");
			Wallet wallet = blockService.createWallet();
			Wallet[] wallets = {new Wallet(wallet.getPublicKey())};
			String msg = JSON.toJSONString(new Message(p2pservice.RESPONSE_WALLET,JSON.toJSONString(wallets)));
			p2pservice.broadcast(msg);
			resp.getWriter().print("创建钱包成功，钱包地址：" + wallet.getAddress());
		}
	}
	
	/**
	 * 查询当前节点的所有钱包钱包
	 */
	private class GetWalletsServlet extends HttpServlet {
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.setCharacterEncoding("UTF-8");
			resp.getWriter().print("当前节点钱包：" + JSON.toJSONString(blockService.getMyWalletMap().values()));
		}
	}
	
	/**
	 * 挖矿
	 */
	private class MineServlet extends HttpServlet {
		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.setCharacterEncoding("UTF-8");
			String address = req.getParameter("address");
			Wallet myWallet = blockService.getMyWalletMap().get(address);
			if(myWallet == null) {
				resp.getWriter().print("挖矿指定的钱包不存在");
				return;
			}
			Block newBlock = blockService.mine(address);
			if(newBlock == null) {
				resp.getWriter().print("挖矿失败，可能有其他节点已经挖出该区块");
				return;
			}
			Block[] blocks = {newBlock};
			String msg = JSON.toJSONString(new Message(p2pservice.RESPONSE_BLOCKCHAIN,JSON.toJSONString(blocks)));
			p2pservice.broadcast(msg);
			resp.getWriter().print("挖矿生成的新区块：" + JSON.toJSONString(newBlock));
		}
	}

	
	/**
	 * 转账交易
	 */
	private class NewTransactionServlet extends HttpServlet {
		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.setCharacterEncoding("UTF-8");
			TransactionParam txParam = JSON.parseObject(getReqBody(req), TransactionParam.class);
			
			Wallet senderWallet = blockService.getMyWalletMap().get(txParam.getSender());
			Wallet recipientWallet = blockService.getMyWalletMap().get(txParam.getRecipient());
			if(recipientWallet == null ) {
				//如果接收方不是本节点的钱包
				recipientWallet = blockService.getOtherWalletMap().get(txParam.getRecipient());
			}
			if(senderWallet == null || recipientWallet == null) {
				resp.getWriter().print("钱包不存在");
				return;
			}
			
			Transaction newTransaction = blockService.createTransaction(senderWallet, recipientWallet, txParam.getAmount());
			if(newTransaction == null) {
				resp.getWriter().print("钱包" + txParam.getSender() + "余额不足或者找不到一笔等于" + txParam.getAmount() + "BTC的UTXO");
			}else {
				resp.getWriter().print("新生成交易："+JSON.toJSONString(newTransaction));
				Transaction[] txs = {newTransaction};
				String msg = JSON.toJSONString(new Message(p2pservice.RESPONSE_TRANSACTION,JSON.toJSONString(txs)));
				p2pservice.broadcast(msg);
			}
		}
	}
	
	/**
	 * 查询未打包的交易
	 */
	private class GetUnpackedTransactionServlet extends HttpServlet {
		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.setCharacterEncoding("UTF-8");
			List<Transaction> transactions = new ArrayList<>(blockService.getAllTransactions());
			transactions.removeAll(blockService.getPackedTransactions());
			resp.getWriter().print("本节点未打包的交易：" + JSON.toJSONString(transactions));
		}
	}
	
	/**
	 * 查询钱包余额
	 */
	private class GetWalletBalanceServlet extends HttpServlet {
		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.setCharacterEncoding("UTF-8");
			String address = req.getParameter("address");
			resp.getWriter().print("钱包余额为："+blockService.getWalletBalance(address) + "BTC");
		}
	}
	
	/**
	 * 查询所有socket节点
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
	 * 得到请求体
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
