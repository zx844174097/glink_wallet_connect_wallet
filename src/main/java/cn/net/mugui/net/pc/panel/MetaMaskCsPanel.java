package cn.net.mugui.net.pc.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

import javax.servlet.ServletInputStream;
import javax.swing.JPanel;
import javax.websocket.Session;

import org.apache.commons.lang3.StringUtils;
import org.web3j.crypto.ECDSASignature;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mugui.Dui.DButton;
import com.mugui.base.base.Autowired;
import com.mugui.base.base.Component;
import com.mugui.bean.JsonBean;

import cn.hutool.cache.Cache;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.net.URLEncoder;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.digest.HmacAlgorithm;
import cn.hutool.crypto.symmetric.AES;
import cn.net.mugui.net.pc.manager.FunctionUI;
import cn.net.mugui.net.pc.panel.comp.MuguiBrowser;
import cn.net.mugui.net.pc.util.SysConf;
import cn.net.mugui.web.tomcat.InsideTomcat2;
import cn.net.mugui.web.tomcat.InsideTomcat2.PathListener;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@SuppressWarnings("serial")
@Component
public class MetaMaskCsPanel extends FunctionUI {
	private JPanel panel;
	private DButton btnAndroid;
	private DButton btnIos;

	public MetaMaskCsPanel() {
		setTitle("浏览器连接钱包");
		setMenu_name("浏览器连接钱包");
		setLayout(new BorderLayout(0, 0));
		add(getPanel(), BorderLayout.NORTH);

	}

	@Override
	public void init() {
		add(muguiBrowser, BorderLayout.CENTER);
		tomcat.addPathListener(pathListener);
		tomcat.addPathListener(walletLoginNonce);
		tomcat.addPathListener(walletConnectUrl);
		tomcat.addPathListener(pushMsg);
	}

	@Override
	public void quit() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dataInit() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dataSave() {
		// TODO Auto-generated method stub

	}

	@Autowired
	MuguiBrowser muguiBrowser;

	private JPanel getPanel() {
		if (panel == null) {
			panel = new JPanel();
			panel.add(getBtnAndroid());
			panel.add(getBtnIos());
			panel.add(getBtnPc());
		}
		return panel;
	}

	private DButton getBtnAndroid() {
		if (btnAndroid == null) {
			btnAndroid = new DButton((String) null, (Color) null);
			btnAndroid.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String value = sysConf.getValue("wallet_android_url");
					muguiBrowser.openUrl(value);
				}
			});
			btnAndroid.setText("android");
		}
		return btnAndroid;
	}

	@Autowired
	private SysConf sysConf;
	private DButton btnPc;

	@Autowired
	private InsideTomcat2 tomcat;

	private DButton getBtnIos() {
		if (btnIos == null) {
			btnIos = new DButton((String) null, (Color) null);
			btnIos.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String value = sysConf.getValue("wallet_ios_url");
					muguiBrowser.openUrl(value);
				}
			});
			btnIos.setText("IOS");
		}
		return btnIos;
	}

	private DButton getBtnPc() {
		if (btnPc == null) {
			btnPc = new DButton((String) null, (Color) null);
			btnPc.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					String value = sysConf.getValue("wallet_pc_url");
					if (java.awt.Desktop.isDesktopSupported()) {
						try {
							// 创建一个URI实例
							java.net.URI uri = java.net.URI.create(value);
							// 获取当前系统桌面扩展
							java.awt.Desktop dp = java.awt.Desktop.getDesktop();
							// 判断系统桌面是否支持要执行的功能
							if (dp.isSupported(java.awt.Desktop.Action.BROWSE)) {
								// 获取系统默认浏览器打开链接
								dp.browse(uri);
							}

						} catch (Exception e2) {
							e2.printStackTrace();
						}
					}
//					muguiBrowser.openUrl(value);
				}
			});
			btnPc.setText("PC");
		}
		return btnPc;
	}

	private PathListener pathListener = new PathListener() {
		public String getPath() {
			return "walletLogin";

		}

		public boolean listener(javax.servlet.http.HttpServletRequest request,
				javax.servlet.http.HttpServletResponse response) {

			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/html; charset=utf-8");
			response.setContentType("application/json; charset=utf-8");
			// 设置支持跨域
			response.setHeader("Access-Control-Allow-Origin", "*");
			response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE");
			response.setHeader("Access-Control-Max-Age", "360000");
			response.setHeader("Access-Control-Allow-Headers", "x-requested-with");

			System.out.println("接收到登录消息" + request.getRequestURI());

			ServletInputStream inputStream;
			try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();) {
				inputStream = request.getInputStream();
				byte[] bytes = new byte[1024];
				int i;
				while ((i = inputStream.read(bytes)) > 0) {
					byteArrayOutputStream.write(bytes, 0, i);
				}
				String string = byteArrayOutputStream.toString("utf-8");
				JSONObject parseObject = JSONObject.parseObject(string);

				String message = parseObject.getString("msg");
				String address = JSONObject.parseArray(message).getString(1);
				String msg = JSONObject.parseArray(message).getString(0).split("code:")[1];
				if (cache.get(address + ":" + msg, false) == null) {
					response.getWriter().write("登录失败");
					response.getWriter().close();
					return false;
				}
				String verifySignature = verifySignature(parseObject);
				if (verifySignature != null) {
					response.getWriter().write("登录成功");
				} else {
					response.getWriter().write("登录失败");

				}
				response.getWriter().close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}

	};

	TimedCache<String, Object> cache = new TimedCache<>(1000 * 5 * 60);
	{
		cache.schedulePrune(1000 * 5 * 60);
	}
	private PathListener walletLoginNonce = new PathListener() {
		public String getPath() {
			return "walletLoginNonce";

		}

		public boolean listener(javax.servlet.http.HttpServletRequest request,
				javax.servlet.http.HttpServletResponse response) {

			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/html; charset=utf-8");
			response.setContentType("application/json; charset=utf-8");
			// 设置支持跨域
			response.setHeader("Access-Control-Allow-Origin", "*");
			response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE");
			response.setHeader("Access-Control-Max-Age", "360000");
			response.setHeader("Access-Control-Allow-Headers", "x-requested-with");

			System.out.println("接收到walletLoginNonce:" + request.getRequestURI());

			ServletInputStream inputStream;
			try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();) {
				inputStream = request.getInputStream();
				byte[] bytes = new byte[1024];
				int i;
				while ((i = inputStream.read(bytes)) > 0) {
					byteArrayOutputStream.write(bytes, 0, i);
				}
				String string = byteArrayOutputStream.toString("utf-8");
				JSONObject parseObject = JSONObject.parseObject(string);
				String address = parseObject.getString("address");
				System.out.println(address.length());

				JSONObject object = new JSONObject();
				object.put("code", 0);

				if (StringUtils.isBlank(address) || !address.startsWith("0x")) {
					object.put("code", -1);
					object.put("msg", "地址错误");
					response.getWriter().write(object.toString());
					response.getWriter().close();
					return false;
				}

				String randomString = RandomUtil.randomString(64);
				cache.put(address + ":" + randomString, true);
				object.put("msg", randomString);
				response.getWriter().write(object.toString());
				response.getWriter().close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}

	};

	/**
	 * 验证签名
	 * 
	 * @param parseObject
	 * @return
	 */
	private String verifySignature(JSONObject parseObject) {
		String msg = parseObject.getString("msg");
		String sign = parseObject.getString("sign");
		return getVerifySignAddress(sign, msg);
	}

	/**
	 * 根据私钥的签名推算得到地址
	 *
	 * @return
	 */
	public String getVerifySignAddress(String signature, String message) {
		System.out.println(signature + ":" + message);
		String address = JSONObject.parseArray(message).getString(1);
		message = JSONObject.parseArray(message).getString(0);
		final String personalMessagePrefix = "\u0019Ethereum Signed Message:\n";
		final String prefix = personalMessagePrefix + message.getBytes().length;
		final byte[] msgHash = Hash.sha3((prefix + message).getBytes());
		final byte[] signatureBytes = Numeric.hexStringToByteArray(signature);
		byte v = signatureBytes[64];
		if (v < 27) {
			v += 27;
		}

		final Sign.SignatureData sd = new Sign.SignatureData(v, Arrays.copyOfRange(signatureBytes, 0, 32),
				Arrays.copyOfRange(signatureBytes, 32, 64));

		String addressRecovered = null;
		for (int i = 0; i < 4; i++) {
			final BigInteger publicKey = Sign.recoverFromSignature((byte) i,
					new ECDSASignature(new BigInteger(1, sd.getR()), new BigInteger(1, sd.getS())), msgHash);
			if (publicKey != null) {
				addressRecovered = "0x" + Keys.getAddress(publicKey);
				System.out.println("addressRecovered=" + addressRecovered);
				if (address.equalsIgnoreCase(addressRecovered))
					return addressRecovered;
			}
		}
		return null;

	}

	// 得到会话连接
	private PathListener walletConnectUrl = new PathListener() {
		public String getPath() {
			return "walletConnectUrl";

		}

		public boolean listener(javax.servlet.http.HttpServletRequest request,
				javax.servlet.http.HttpServletResponse response) {

			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/html; charset=utf-8");
			response.setContentType("application/json; charset=utf-8");
			// 设置支持跨域
			response.setHeader("Access-Control-Allow-Origin", "*");
			response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE");
			response.setHeader("Access-Control-Max-Age", "360000");
			response.setHeader("Access-Control-Allow-Headers", "x-requested-with");

			System.out.println("接收到walletConnectUrl:" + request.getRequestURI());
			try {

				String parameter = request.getParameter("market");

				// 会话连接
				WalletConnectBean walletConnectBean = new WalletConnectBean();
				walletConnectBean.setKey(HexUtil.encodeHexStr(RandomUtil
						.randomBytes("bde1156e6e2350a4eaa03fc962c6bc61ed76a17f635939bd7011329c1b3d584c".length() / 2)));
				walletConnectBean.setTopic(UUID.fastUUID().toString().toLowerCase());

				String value = sysConf.getValue("wallet_server_handle_url");
				walletConnectBean.setServerUrl(value);

				cache.put("connect:" + walletConnectBean.getTopic(), walletConnectBean);

				value = URLEncoder.createQuery().encode(value, Charset.forName("utf-8"));
				if (parameter.equalsIgnoreCase("android")) {
					// 得到安卓连接二维码
					String url = "wc:";

					url = url + walletConnectBean.getTopic() + "@1?bridge=" + value + "&key="
							+ walletConnectBean.getKey();
					System.out.println(url);
					response.getWriter().write(url);
				} else {
					response.getWriter().write("www.baidu.com");
				}
				response.getWriter().close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}

	};

	private WalletConnectBean getWalletConnectBean(String topic) {
		WalletConnectBean object = (WalletConnectBean) cache.get("connect:" + topic);
		return object;
	}

	@Getter
	@Setter
	@Accessors(chain = true)
	public static class WalletConnectBean extends JsonBean {
		// 会话连接
		String topic;
		// 处理服务器
		String serverUrl;
		// 验证密码
		String key;
	}

	/**
	 * 钱包会话处理
	 * 
	 * @param message
	 * @param session
	 * @throws IOException
	 */
	public void handle(String message, Session session) throws IOException {

		if (StringUtils.isBlank(message)) {
			return;
		}
		MsgBean msgBean = MsgBean.newBean(MsgBean.class, message);
		System.out.println(message.toString());
		if (!session.isOpen()) {
			session.close();
			return;
		}
		switch (msgBean.type) {
		case "sub":
			handleSub(msgBean, session);
			break;
		case "pub":
			handlePub(msgBean, session);
			break;
		default:
			break;
		}
	}

	TimedCache<String, Session> sessionCache = new TimedCache<>(5 * 60 * 1000);
	{
		sessionCache.schedulePrune(5 * 60 * 1000);
	}

	/**
	 * 处理受到的推送消息
	 * 
	 * @param msgBean
	 * @param session
	 */
	private void handlePub(MsgBean msgBean, Session session) {

		Peer peer = peermap.get(session.getId());
		if (peer == null) {
			return;
		}
		String string = peer.peer[0];
		WalletConnectBean walletConnectBean = getWalletConnectBean(string);
		if (walletConnectBean == null) {
			return;
		}
		String decryptMsg = decryptMsg(msgBean, walletConnectBean);
		System.out.println(decryptMsg);
		Collection<MsgBean> collection = pushMsgCache.get(string + "_ret");
		if (collection == null) {
			collection = new ConcurrentLinkedDeque<>();
			pushMsgCache.put(string + "_ret", collection);
		}
		msgBean.payload = JSONObject.parseObject(decryptMsg).toString();
		collection.add(msgBean);
		sessionCache.put(string, session);
	}

	TimedCache<String, Peer> peermap = new TimedCache<>(5 * 60 * 1000);
	{
		peermap.schedulePrune(5 * 60 * 1000);
	}

	public static final class Peer {
		/**
		 * [0]为应用端 ，[1]为钱包端
		 */
		String[] peer = new String[2];
	}

	/**
	 * {"payload":"","silent":true,"topic":"803838dc-5b47-4384-bc7b-f82af2f38e7getWalletConnectBeane","type":"sub"}
	 * 
	 * @param session
	 * 
	 * @param msgBean
	 * @throws IOException
	 */
	private void handleSub(MsgBean msgBean, Session session) throws IOException {

		Peer peer = peermap.get(session.getId());
		if (peer == null) {
			peermap.put(session.getId(), peer = new Peer());
		}
		WalletConnectBean walletConnectBean = getWalletConnectBean(msgBean.topic);
		if (walletConnectBean == null) {// 钱包端
			peer.peer[1] = msgBean.topic;
		} else {
			// 应用端
			peer.peer[0] = msgBean.topic;
		}

		if (StringUtils.isNotBlank(msgBean.payload)) {
			if (walletConnectBean == null)
				walletConnectBean = getWalletConnectBean(peer.peer[0]);
			if (walletConnectBean == null) {
				return;
			}
			String decryptMsg = decryptMsg(msgBean, walletConnectBean);
			System.out.println(decryptMsg);
		}
		// 应用端向钱包端发送连接请求
		if (peer.peer[0] != null && peer.peer[1] != null) {
			Collection<MsgBean> collection = pushMsgCache.get(peer.peer[0]);
			Iterator<MsgBean> iterator = collection.iterator();
			while (iterator.hasNext()) {
				MsgBean next = iterator.next();
				iterator.remove();
				MsgBean encryptMsg = encryptMsg(next, peer.peer[1]);
				if (encryptMsg != null)
					sendMsg(session, encryptMsg.toString());
			}
		}

	}

	/**
	 * 加密数据
	 * 
	 * @param next
	 * @param peer
	 */
	private MsgBean encryptMsg(MsgBean next, String peer) {
		String payload = next.getPayload();
		JSONObject object = new JSONObject();
		WalletConnectBean walletConnectBean = getWalletConnectBean(next.topic);
		if (walletConnectBean == null) {
			return null;
		}
		byte[] iv = RandomUtil.randomBytes("3fe986f042daeab682f4ac83900f959b".length() / 2);
		byte[] key = HexUtil.decodeHex(walletConnectBean.key);

		AES aes = new AES(Mode.CBC, Padding.PKCS5Padding, key, iv);
		String encodeHexStr = HexUtil.encodeHexStr(aes.encrypt(payload));

		object.put("data", encodeHexStr);
		String digestHex = DigestUtil.hmac(HmacAlgorithm.HmacSHA256, key)
				.digestHex(HexUtil.decodeHex(encodeHexStr + HexUtil.encodeHexStr(iv)));
		object.put("hmac", digestHex);
		object.put("iv", HexUtil.encodeHexStr(iv));
		next.setPayload(object.toString());
		next.topic = peer;
		return next;
	}

	/**
	 * 解密数据
	 * 
	 * { //payload:
	 * "{\"data\":\"b25c4c8934720ea17a1a2ddc508a6d0a1d93ec731694ad15f9fc960e1e36a3059474d14ca11fc9c150e69d14b896d7486c4d41ba29fc534dc14fab75e2d5044aaebf184247e68446b19dd98e66756954220157ee49dea95f3c1fab18235f405ac682f81690c5fc32821f767bd61cfab2e5640baf08222bf3afec46737263a024e4903ce2ac95f082d02bf4e28c1e05bc08edadd10bdb65949906c73202ae941aee1770dbb97c79a4761c85756e9339a4dba04dddc2a118b5ab47d401163a6efc8bd8e925db0ee6935d949e1a2f11eece513dcd31d730929caaa470348222a2319485d63be6db87949e37a0a2bf1a3a39258da2692321504d80e3cc4e831f0123319c43fb999f866af016e1cf5c62ae6b476158e2e9950c889abfc73542245a46194514cc716f160f0e45737391dc011c4e293dc5d5146169bdf9547dff327ce0\",\"hmac\":\"0576ed20e46db0d0f739239b0bc1efba24afe5677f62b671441ee68d6ccb5078\",\"iv\":\"3fe986f042daeab682f4ac83900f959b\"}"
	 * byte[] keys =
	 * HexUtil.decodeHex("bde1156e6e2350a4eaa03fc962c6bc61ed76a17f635939bd7011329c1b3d584c");
	 * byte[] iv = HexUtil.decodeHex("3fe986f042daeab682f4ac83900f959b"); byte[]
	 * body = HexUtil.decodeHex(
	 * "b25c4c8934720ea17a1a2ddc508a6d0a1d93ec731694ad15f9fc960e1e36a3059474d14ca11fc9c150e69d14b896d7486c4d41ba29fc534dc14fab75e2d5044aaebf184247e68446b19dd98e66756954220157ee49dea95f3c1fab18235f405ac682f81690c5fc32821f767bd61cfab2e5640baf08222bf3afec46737263a024e4903ce2ac95f082d02bf4e28c1e05bc08edadd10bdb65949906c73202ae941aee1770dbb97c79a4761c85756e9339a4dba04dddc2a118b5ab47d401163a6efc8bd8e925db0ee6935d949e1a2f11eece513dcd31d730929caaa470348222a2319485d63be6db87949e37a0a2bf1a3a39258da2692321504d80e3cc4e831f0123319c43fb999f866af016e1cf5c62ae6b476158e2e9950c889abfc73542245a46194514cc716f160f0e45737391dc011c4e293dc5d5146169bdf9547dff327ce0");
	 * AES aes = new AES(Mode.CBC, Padding.PKCS5Padding, keys, iv); byte[] decrypt =
	 * aes.decrypt(body); System.out.println(new String(decrypt)); //
	 * 0576ed20e46db0d0f739239b0bc1efba24afe5677f62b671441ee68d6ccb5078 //
	 * 40ee44fdc3a0ea071d86545e8f3be95713684e2643519577d83ceedef5cfc93a //
	 * 62f8dfa40a060ce3c7af58fcd29d8a7a5dbfabbd341a60e3084604580369fe98 String
	 * digestHex = DigestUtil .hmac(HmacAlgorithm.HmacSHA256, keys) .
	 * digestHex(HexUtil.decodeHex("" +
	 * "b25c4c8934720ea17a1a2ddc508a6d0a1d93ec731694ad15f9fc960e1e36a3059474d14ca11fc9c150e69d14b896d7486c4d41ba29fc534dc14fab75e2d5044aaebf184247e68446b19dd98e66756954220157ee49dea95f3c1fab18235f405ac682f81690c5fc32821f767bd61cfab2e5640baf08222bf3afec46737263a024e4903ce2ac95f082d02bf4e28c1e05bc08edadd10bdb65949906c73202ae941aee1770dbb97c79a4761c85756e9339a4dba04dddc2a118b5ab47d401163a6efc8bd8e925db0ee6935d949e1a2f11eece513dcd31d730929caaa470348222a2319485d63be6db87949e37a0a2bf1a3a39258da2692321504d80e3cc4e831f0123319c43fb999f866af016e1cf5c62ae6b476158e2e9950c889abfc73542245a46194514cc716f160f0e45737391dc011c4e293dc5d5146169bdf9547dff327ce0"
	 * + "3fe986f042daeab682f4ac83900f959b")); System.out.println(digestHex);
	 * 
	 * }
	 * 
	 * @param next
	 * @param walletConnectBean
	 */
	private String decryptMsg(MsgBean next, WalletConnectBean walletConnectBean) {

		JSONObject parse = JSONObject.parseObject(next.payload);
		byte[] keys = HexUtil.decodeHex(walletConnectBean.key);
		// 签名校验
		byte[] digest = DigestUtil.hmac(HmacAlgorithm.HmacSHA256, keys)
				.digest(HexUtil.decodeHex(parse.getString("data") + parse.getString("iv")));
		if (!HexUtil.encodeHexStr(digest).equals(parse.get("hmac"))) {
			// 签名校验失败
			return null;
		}

		byte[] iv = HexUtil.decodeHex(parse.getString("iv"));

		AES aes = new AES(Mode.CBC, Padding.PKCS5Padding, keys, iv);
		byte[] decrypt = aes.decrypt(HexUtil.decodeHex(parse.getString("data")));
		return new String(decrypt);
	}

	private void sendMsg(Session session, String string) throws IOException {
		System.out.println("sendMsg->" + string);
		if (session.isOpen()) {
			session.getBasicRemote().sendText(string);
		}
	}

	@Getter
	@Setter
	@Accessors(chain = true)
	public static final class MsgBean extends JsonBean {
		private String payload;
		private Boolean silent;
		private String topic;
		private String type;
	}

	private Cache<String, Collection<MsgBean>> pushMsgCache = new TimedCache<>(60 * 1000 * 60);
	{
		cache.schedulePrune(1000 * 5 * 60);
	}

	private PathListener pushMsg = new PathListener() {
		public String getPath() {
			return "pushMsg";

		}

		public boolean listener(javax.servlet.http.HttpServletRequest request,
				javax.servlet.http.HttpServletResponse response) {

			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/html; charset=utf-8");
			response.setContentType("application/json; charset=utf-8");
			// 设置支持跨域
			response.setHeader("Access-Control-Allow-Origin", "*");
			response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE");
			response.setHeader("Access-Control-Max-Age", "360000");
			response.setHeader("Access-Control-Allow-Headers", "x-requested-with");

			ServletInputStream inputStream;
			try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();) {
				inputStream = request.getInputStream();
				byte[] bytes = new byte[1024];
				int i;
				while ((i = inputStream.read(bytes)) > 0) {
					byteArrayOutputStream.write(bytes, 0, i);
				}
				String string = byteArrayOutputStream.toString("utf-8");

				MsgBean newBean = MsgBean.newBean(MsgBean.class, string);
				if (newBean != null && StringUtils.isNotBlank(newBean.topic)) {
					Collection<MsgBean> list = pushMsgCache.get(newBean.topic + "_ret");
					if (list != null && !list.isEmpty()) {
						String jsonString = JSONArray.toJSONString(list);
						response.getWriter().write(jsonString);
						list.clear();
					}
				}
				if (StringUtils.isNotBlank(newBean.payload) && !newBean.payload.equals("{}")) {
					System.out.println("body->" + string);
				} else {
					response.getWriter().close();
					return false;
				}

				Session session = sessionCache.get(newBean.topic);
				if (session != null && session.isOpen()) {
					Peer peer = peermap.get(session.getId());
					if (peer != null) {
						MsgBean encryptMsg = encryptMsg(newBean, peer.peer[1]);
						sendMsg(session, encryptMsg.toString());
						response.getWriter().close();
						return false;
					}
				}
				Collection<MsgBean> list = pushMsgCache.get(newBean.topic);
				if (list == null) {
					list = new ConcurrentLinkedDeque<MsgBean>();
					pushMsgCache.put(newBean.topic, list);
				}
				if (newBean != null && newBean.topic != null && StringUtils.isNotBlank(newBean.payload)) {
					list.add(newBean);
				}
				response.getWriter().close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}

	};
	/**
	 * 
	 * byte[] keys =
	 * HexUtil.decodeHex("bde1156e6e2350a4eaa03fc962c6bc61ed76a17f635939bd7011329c1b3d584c");
	 * byte[] iv = HexUtil.decodeHex("3fe986f042daeab682f4ac83900f959b");
	 * 
	 * AES aes = new AES(Mode.CBC, Padding.PKCS5Padding, keys, iv); byte[] decrypt =
	 * aes.decrypt(HexUtil.decodeHex(
	 * "b25c4c8934720ea17a1a2ddc508a6d0a1d93ec731694ad15f9fc960e1e36a3059474d14ca11fc9c150e69d14b896d7486c4d41ba29fc534dc14fab75e2d5044aaebf184247e68446b19dd98e66756954220157ee49dea95f3c1fab18235f405ac682f81690c5fc32821f767bd61cfab2e5640baf08222bf3afec46737263a024e4903ce2ac95f082d02bf4e28c1e05bc08edadd10bdb65949906c73202ae941aee1770dbb97c79a4761c85756e9339a4dba04dddc2a118b5ab47d401163a6efc8bd8e925db0ee6935d949e1a2f11eece513dcd31d730929caaa470348222a2319485d63be6db87949e37a0a2bf1a3a39258da2692321504d80e3cc4e831f0123319c43fb999f866af016e1cf5c62ae6b476158e2e9950c889abfc73542245a46194514cc716f160f0e45737391dc011c4e293dc5d5146169bdf9547dff327ce0"));
	 * System.out.println(new String(decrypt));
	 * 
	 */

}
