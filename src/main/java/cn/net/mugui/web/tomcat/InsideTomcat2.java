package cn.net.mugui.web.tomcat;

import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.lang3.StringUtils;
import org.apache.coyote.CompressionConfig;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.UpgradeProtocol;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.apache.coyote.http2.Http2Protocol;
import org.apache.tomcat.websocket.server.WsSci;

import com.mugui.base.base.Autowired;
import com.mugui.base.base.Component;

import cn.net.mugui.net.web.app.ConnectWallet;
import lombok.Getter;
import lombok.Setter;

@Component
public class InsideTomcat2 {

	// tomcat的端口号
	@Setter
	private Integer port;
	// tomcat的字符编码集
	private String code;
	// 拦截请求路径
	private String hinderURL;
	// 请求转发路径
	private String shiftURL;
	// tomcat对象
	@Getter
	private Context context;
	@Getter
	private Tomcat tomcat;

	@Autowired
	private WebHandle webHandle;

	public InsideTomcat2() {

	}

	// 启动这个内嵌tomcat容器
	public void run() throws Exception {
		init();
		tomcat.init();

		// 启动tomcat
		tomcat.start();
		// 保持tomcat的启动状态
		tomcat.getServer().await();

	}

	private void customize(Http2Protocol protocol) {
		CompressionConfig compression = this.compression;
		protocol.setCompression(compression.getCompression());
		protocol.setCompressibleMimeType(compression.getCompression());
		protocol.setCompressionMinSize(compression.getCompressionMinSize());
		if (compression.getNoCompressionUserAgents() != null) {
			protocol.setNoCompressionUserAgents(compression.getNoCompressionUserAgents());
		}
	}

	private void customize(AbstractHttp11Protocol<?> protocol) {
		CompressionConfig compression = this.compression;
		protocol.setCompression(compression.getCompression());
		protocol.setCompressibleMimeType(compression.getCompression());
		protocol.setCompressionMinSize(compression.getCompressionMinSize());
		if (compression.getNoCompressionUserAgents() != null) {
			protocol.setNoCompressionUserAgents(compression.getNoCompressionUserAgents());
		}
	}

	public void setCompression(String on_or_off, Integer min_size, String mime_type) {
		compression = new CompressionConfig();
		compression.setCompression(on_or_off);
		if (min_size != null) {
			compression.setCompressionMinSize(min_size);
		}
		if (mime_type != null) {
			compression.setCompressibleMimeType(mime_type);
		}
	}

	CompressionConfig compression = null;

	// 初始化tomcat容器
	public void init() throws LifecycleException, ServletException {
		// 执行init方法时加载默认属性值
		// 为了演示这里写了死值进去
		if (port == null)
			port = 8080;
		code = "UTF-8";
		hinderURL = "/";
		shiftURL = "/web";
		setCompression("on", 64, null);

		tomcat = new Tomcat();
		ProtocolHandler protocolHandler = tomcat.getConnector().getProtocolHandler();
		if (protocolHandler instanceof AbstractHttp11Protocol) {
			((AbstractHttp11Protocol<?>) protocolHandler).setNoCompressionUserAgents("");
		}
		for (UpgradeProtocol upgradeProtocol : tomcat.getConnector().findUpgradeProtocols()) {
			if (upgradeProtocol instanceof Http2Protocol) {
				customize((Http2Protocol) upgradeProtocol);
			}
		}
		// 创建连接器
		Connector conn = tomcat.getConnector();
		conn.setAsyncTimeout(60000);
		conn.setPort(port);
		conn.setURIEncoding(code);
		conn.setMaxPostSize(128 * 1024);
		conn.setMaxSavePostSize(128 * 1024);
		// 设置Host
		Host host = tomcat.getHost();
		host.setAppBase("webapps");

		// 获取目录绝对路径
		String classPath = System.getProperty("user.dir");

		// 配置tomcat上下文
		context = tomcat.addContext(host, shiftURL, classPath + "/src/main/webapp/");
		context.setSessionTimeout(60000);
		context.setSessionCookieName("SESSION");
		context.setSessionCookiePath("/");

		// 配置websocket
		WsSci wsSci = new WsSci();
		HashSet<Class<?>> set = new HashSet<>();
		set.add(ConnectWallet.class);
		context.addServletContainerInitializer(wsSci, set);

		// 配置请求拦截转发
		Wrapper wrapper = tomcat.addServlet(shiftURL, "Servlet", webHandle);
		wrapper.init();
		wrapper.setAsyncSupported(true);
		wrapper.addMapping(shiftURL + "api/");
		wrapper.addMapping(shiftURL + "api");

		DefaultServlet defaultServlet = new DefaultServlet() {

			String[] main_url = new String[] { "/", "/index" };

			@Override
			protected void doGet(HttpServletRequest request, HttpServletResponse response)
					throws IOException, ServletException {
				System.out.println(request.getRequestURI());
				boolean equalsAny = StringUtils.equalsAny(request.getRequestURI().replace(shiftURL, ""), main_url);
				if (equalsAny) {
					response.sendRedirect(shiftURL + "/index.jsp");
					return;
				}
				PathListener pathListener = lMap.get(request.getRequestURI().replace(shiftURL, ""));
				if (pathListener != null) {
					try {
						boolean listener = pathListener.listener(request, response);
						if (!listener) {
							return;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
				super.doGet(request, response);
			}
			@Override
			protected void doPost(HttpServletRequest request, HttpServletResponse response)
					throws IOException, ServletException {
				PathListener pathListener = lMap.get(request.getRequestURI().replace(shiftURL, ""));
				if (pathListener != null) {
					try {
						boolean listener = pathListener.listener(request, response);
						if (!listener) {
							return;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
				super.doPost(request, response);
			}
		};


		Wrapper addServlet = tomcat.addServlet(shiftURL, "default", defaultServlet);
		addServlet.init();
		addServlet.setAsyncSupported(true);
		addServlet.addMapping("/");

	}

	ConcurrentHashMap<String, PathListener> lMap = new ConcurrentHashMap<>();

	public void addPathListener(PathListener listener) {
		if (listener == null) {
			return;
		}
		String path = listener.getPath();
		if (StringUtils.isBlank(path)) {
			return;
		}

		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		lMap.put(path, listener);

		boolean endsWith = path.endsWith("/");
		String path2 = null;
		if (endsWith) {
			lMap.put(path2 = path.substring(0, path.length() - 1), listener);
		} else {
			lMap.put(path + "/", listener);
			path2 = path;
		}
		String ends[] = new String[] { ".htm", ".html", ".jsp", ".php", ".lg" };
		for (String end : ends)
			lMap.put(path2 + end, listener);
	}

	public static interface PathListener {

		String getPath();

		/**
		 * 
		 * @param request
		 * @param response
		 * @return 是否继续传递
		 */
		boolean listener(HttpServletRequest request, HttpServletResponse response);
	}

	public String getWebappsPath() {
		String file = getClass().getClassLoader().getResource(".").getFile();
		return file.substring(0, file.indexOf("target")) + "src/main/webapp";
	}
}