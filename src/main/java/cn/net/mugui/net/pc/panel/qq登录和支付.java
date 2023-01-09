package cn.net.mugui.net.pc.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JPanel;

import com.mugui.Dui.DButton;
import com.mugui.base.base.Autowired;
import com.mugui.base.base.Component;

import cn.hutool.core.util.NumberUtil;
import cn.net.mugui.net.pc.manager.FunctionUI;
import cn.net.mugui.net.pc.panel.comp.MuguiBrowser;
import cn.net.mugui.net.pc.util.SysConf;
import cn.net.mugui.web.tomcat.InsideTomcat2;
import cn.net.mugui.web.tomcat.InsideTomcat2.PathListener;

//@Component
public class qq登录和支付 extends FunctionUI {
	public qq登录和支付() {
		setTitle("qq登录和支付");
		setMenu_name("qq登录和支付");
		setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		add(panel, BorderLayout.NORTH);

		DButton 扫码登录 = new DButton("扫码登录", (Color) null);
		扫码登录.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String value = sysConf.getValue("qq_login_url");
				StringJoiner stringJoiner = new StringJoiner("&");
				stringJoiner.add("response_type=code");
				stringJoiner.add("client_id=102010527");
				stringJoiner
						.add("redirect_uri=http://other.yingxiong.com/dev_mainland/sdkcom/c1/pc/login/qqRedirect.ul");
				stringJoiner.add("state=1_com.hero.pc.sdk");
				muguiBrowser.openUrl(value + stringJoiner);

			}
		});

		DButton 日志 = new DButton("日志", (Color) null);
		日志.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				muguiBrowser.openUrl("http://10.22.12.138:5678/web/SmsLog.html");
			}
		});
		日志.setText("日志");
		panel.add(日志);
		panel.add(扫码登录);

		DButton 扫码支付 = new DButton("扫码支付", (Color) null);
		panel.add(扫码支付);

	}

	@Autowired
	private SysConf sysConf;
	/**
	 * 
	 */
	private static final long serialVersionUID = -868828111166057976L;

	@Autowired
	MuguiBrowser muguiBrowser;

	@Autowired
	private InsideTomcat2 tomcat;

	@Override
	public void init() {

		add(muguiBrowser, BorderLayout.CENTER);
		tomcat.addPathListener(pathListener);
		tomcat.addPathListener(sdkconfListener);
		top = 0;
		if (!thread.isAlive())
			thread.start();

		if (!thread2.isAlive())
			thread2.start();
	}

	int top = 0;
	ConcurrentHashMap<Integer, String> newsLine = new ConcurrentHashMap<>();
	Thread thread = new Thread() {
		public void run() {
			while (true) {
				try {
					RandomAccessFile file = new RandomAccessFile(
							new File("C:\\Users\\root\\.SmartTomcat\\mainland-srv-com\\sdkcom\\logs\\iossrv.log"), "r");
					String str;
					int index = 0;
					boolean bool = true;
					StringBuffer buffer = new StringBuffer();

					while (true) {
						if ((str = file.readLine()) != null) {
							if (bool) {
								bool = false;
							}
							index++;
							if (top != 0) {
								byte[] bytes = str.getBytes(Charset.forName("ISO-8859-1"));
								buffer.append(new String(bytes, "UTF-8") + "\n");
							}
						} else {
							if (!bool) {
								newsLine.put(index, buffer.toString());
								int size = newsLine.size();
								if (size > 500) {
									Iterator<Entry<Integer, String>> iterator = newsLine.entrySet().iterator();
									while (iterator.hasNext() && size > 500) {
										iterator.next();
										iterator.remove();
										size--;
									}
								}
								buffer = new StringBuffer();
							}
							bool = true;
							top = index;
							Thread.sleep(500);
						}

					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		};
	};

	Thread thread2 = new Thread() {
		public void run() {
			while (true) {
				try {
					RandomAccessFile file = new RandomAccessFile(
							new File("C:\\Users\\root\\.SmartTomcat\\mainland-srv-com\\sdkcom\\logs\\debug.log"), "r");
					String str;
					int index = 0;
					boolean bool = true;
					StringBuffer buffer = new StringBuffer();

					while (true) {
						if ((str = file.readLine()) != null) {
							if (bool) {
								bool = false;
							}
							index++;
							if (top != 0) {
								byte[] bytes = str.getBytes(Charset.forName("ISO-8859-1"));
								buffer.append(new String(bytes, "UTF-8") + "\n");
							}
						} else {
							if (!bool) {
								newsLine.put(index, buffer.toString());
								int size = newsLine.size();
								if (size > 500) {
									Iterator<Entry<Integer, String>> iterator = newsLine.entrySet().iterator();
									while (iterator.hasNext() && size > 500) {
										iterator.next();
										iterator.remove();
										size--;
									}
								}
								buffer = new StringBuffer();
							}
							bool = true;
							top = index;
							Thread.sleep(500);
						}

					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		};
	};
	@Override
	public void quit() {

	}

	@Override
	public void dataInit() {

	}

	@Override
	public void dataSave() {

	}

	/**
	 * qq登录或者支付回调
	 */
	private PathListener pathListener = new PathListener() {

		@Override
		public boolean listener(HttpServletRequest request, HttpServletResponse response) {
			System.out.println("接收到qq回调" + request.getRequestURI());
			Enumeration<String> attributeNames = request.getAttributeNames();
			while (attributeNames.hasMoreElements()) {
				String nextElement = attributeNames.nextElement();
				System.out.println(nextElement);
			}
			ServletInputStream inputStream;
			try {
				inputStream = request.getInputStream();

				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				int len = 0;
				byte[] bytes = new byte[1024];
				while ((len = inputStream.read(bytes)) > -1) {
					outputStream.write(bytes, 0, len);
				}
				inputStream.close();
				outputStream.close();
				System.out.println(outputStream.toString());
				response.getWriter().write("你好世界!");
				response.getWriter().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		}

		@Override
		public String getPath() {
			return "qqRedirect";
		}
	};

	private PathListener sdkconfListener = new PathListener() {

		@Override
		public boolean listener(HttpServletRequest request, HttpServletResponse response) {
	        response.setCharacterEncoding("UTF-8");
	        response.setContentType("text/html; charset=utf-8");
			response.setContentType("application/json; charset=utf-8");
	        // 设置支持跨域
	        response.setHeader("Access-Control-Allow-Origin", "*");
	        response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE");
	        response.setHeader("Access-Control-Max-Age", "360000");
	        response.setHeader("Access-Control-Allow-Headers", "x-requested-with");
	        try {
				String parameter = request.getParameter("line");
				int index = -1;
				if (NumberUtil.isInteger(parameter)) {
					index = Integer.parseInt(parameter);
				}
		        PrintWriter writer = response.getWriter();
				Iterator<Entry<Integer, String>> iterator = newsLine.entrySet().iterator();
				com.alibaba.fastjson.JSONObject object = new com.alibaba.fastjson.JSONObject();
				object.put("top", top);
				if (top < index) {
					index = -1;
				}
				StringBuffer stringBuffer = new StringBuffer();
				while (iterator.hasNext()) {
					Entry<Integer, String> next = iterator.next();
					if (next.getKey() > index) {
						stringBuffer.append(next.getValue());
					}
				}
				object.put("data", stringBuffer);
				writer.append(object.toJSONString());
				writer.flush();
				writer.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
	        
			return false;
		}

		@Override
		public String getPath() {
			return "SmsLogNew";
		}
	};

}
