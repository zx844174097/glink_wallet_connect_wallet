package cn.net.mugui.net.pc.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

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

//@Component
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

				String APPID="wx3bd6e7ce6b64a4aa";
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

//			PrintWriter writer;
			try {
				// https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
//				String value = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";
//
//				String APPID = "wx3bd6e7ce6b64a4aa";
//				String SECRET = "665a96a5de48e196918380a81c5edb63";
//
//				value = value.replace("APPID", APPID);
//				value = value.replace("SECRET", SECRET);
//				value = value.replace("CODE", code);
//
//				String string = (String) HTTPUtil.get(value);
//				muguiBrowser.openUrl(value);

				response.setCharacterEncoding("UTF-8");
				response.setContentType("text/html; charset=utf-8");
//				writer = response.getWriter();
				String redirect = "http://10.22.12.139:3000/login?code=" + code + "&state=" + state;
//				redirect = URLEncoder.DEFAULT.encode(redirect, Charset.forName("UTF-8"));
//				response.sendRedirect("http://zx844174097.oicp.net/wxLogin.html?redirect=" + redirect);
				response.sendRedirect(redirect);
//				writer.write(string);
//				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}

		@Override
		public String getPath() {
			return "wxRedirect";
		}
	};

}
