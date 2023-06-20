package cn.net.mugui.net.pc.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JPanel;

import com.mugui.Dui.DButton;
import com.mugui.base.base.Autowired;

import cn.hutool.core.util.RandomUtil;
import cn.net.mugui.net.pc.manager.FunctionUI;
import cn.net.mugui.net.pc.panel.comp.MuguiBrowser;
import cn.net.mugui.net.pc.util.SysConf;
import cn.net.mugui.web.tomcat.InsideTomcat2;
import cn.net.mugui.web.tomcat.InsideTomcat2.PathListener;
import com.mugui.base.base.Component;
import com.mugui.base.client.net.bagsend.HTTPUtil;

@Component
public class wx登录和支付 extends FunctionUI {
	public wx登录和支付() {
		setTitle("wx登录和支付");
		setMenu_name("wx登录和支付");
		setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		add(panel, BorderLayout.NORTH);

		DButton 扫码登录 = new DButton("扫码登录", (Color) null);
		扫码登录.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String value = sysConf.getValue("wx_login_url");
				//https://open.weixin.qq.com/connect/qrconnect?appid=APPID&redirect_uri=REDIRECT_URI&response_type=code&scope=SCOPE&state=STATE#wechat_redirect

				String APPID="wx4c0baec7c11f27cf";
				String REDIRECT_URI="http://zx844174097.oicp.net/wxRedirect.html";
				String SCOPE="snsapi_login";
				String STATE= RandomUtil.randomString(8);//攻击防止

				value=value.replace("APPID",APPID);
				value=value.replace("REDIRECT_URI",REDIRECT_URI);
				value=value.replace("SCOPE",SCOPE);
				value=value.replace("STATE",STATE);

				muguiBrowser.openUrl(value);

			}
		});
		panel.add(扫码登录);

		DButton 文件上传 = new DButton("文件上传", (Color) null);
		panel.add(文件上传);

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
		tomcat.addPathListener(pathListener2);
	}

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
			System.out.println("接收到wx回调" + request.getRequestURI());
			String code = request.getParameter("code");
			String state = request.getParameter("state");

			PrintWriter writer;
			try {
				// https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
				String value = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";

				//彪哥微信
//				String APPID = "wx67267b021d00c8e0";
//				String SECRET = "a63706036980f19e4f742c93f9ff8cde";
				//彪哥公众号
//				String APPID = "wx4b1710f0054adc0c";
//				String SECRET = "27b88133db02829d034fe94bfd7937dd";
				//英雄测试公众号
//				String APPID = "wxd396e21301f850a4";
//				String SECRET = "177b4d11534bf983b530cd1784d2b9ee";
				//微信测试公众号
				String APPID = "wxf5b56137bdfd74a4";
				String SECRET = "b9a3c93e768b3c7e1655bd35c63fba26";

				value = value.replace("APPID", APPID);
				value = value.replace("SECRET", SECRET);
				value = value.replace("CODE", code);

				String string = (String) HTTPUtil.get(value);
//				muguiBrowser.openUrl(value);

				response.setCharacterEncoding("UTF-8");
				response.setContentType("text/html; charset=utf-8");
				writer = response.getWriter();
//				String redirect = "http://10.22.12.139:3000/login?code=" + code + "&state=" + state;
//				redirect = URLEncoder.DEFAULT.encode(redirect, Charset.forName("UTF-8"));
//				response.sendRedirect("http://zx844174097.oicp.net/wxLogin.html?redirect=" + redirect);
//				response.sendRedirect(redirect);
				writer.write(string);
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				try {
					writer = response.getWriter();
					writer.write(e.getMessage());
					writer.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			return false;
		}

		@Override
		public String getPath() {
			return "wxRedirect";
		}
	};


	/**
	 * 文件上传
	 */
	private PathListener pathListener2 = new PathListener() {

		@Override
		public boolean listener(HttpServletRequest request, HttpServletResponse response) {
			System.out.println("接收到文件" + request.getRequestURI());

			PrintWriter writer;
			try {

				response.setCharacterEncoding("UTF-8");
				response.setContentType("text/html; charset=utf-8");
				writer = response.getWriter();
				writer.write("成功");
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
				try {
					writer = response.getWriter();
					writer.write(e.getMessage());
					writer.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			return false;
		}

		@Override
		public String getPath() {
			return "wxRedirect";
		}
	};


}
