package cn.net.mugui.net.web.app;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mugui.base.client.net.classutil.DataSave;

import cn.net.mugui.net.pc.panel.MetaMaskCsPanel;

//configurator = HttpSessionConfigurator.class 为了获取Httpsession
@ServerEndpoint(value = "/connectWallet.html")
public class ConnectWallet {
	private static ConcurrentHashMap<String, ConnectWallet> webSocketSet = new ConcurrentHashMap<>();
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	// 与某个客户端的连接会话，需要通过它来给客户端发送数据
	private Session session;
	private HttpSession httpSession;

	MetaMaskCsPanel metaMaskCsPanel;
	{
		metaMaskCsPanel = DataSave.context.getBean(MetaMaskCsPanel.class);
	}
	public HttpSession getHttpSession() {
		return httpSession;
	}

	public void setHttpSession(HttpSession httpSession) {
		this.httpSession = httpSession;
	}

	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
		this.session = session;
	}


	@OnClose
	public void onClose() {
		try {
			session.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@OnMessage
	public void onMessage(String message, Session session) throws IOException {
		metaMaskCsPanel.handle(message, session);
	}

	@OnError
	public void onError(Session session, Throwable error) {
		System.out.println("webSocket发生错误");
		error.printStackTrace();
	}
}
