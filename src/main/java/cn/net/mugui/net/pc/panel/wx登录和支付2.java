package cn.net.mugui.net.pc.panel;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.io.FastByteArrayOutputStream;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpUtil;
import cn.net.mugui.net.pc.manager.FunctionUI;
import cn.net.mugui.net.pc.panel.comp.MuguiBrowser;
import cn.net.mugui.net.pc.util.SysConf;
import cn.net.mugui.web.tomcat.InsideTomcat2;
import cn.net.mugui.web.tomcat.InsideTomcat2.PathListener;
import com.alibaba.fastjson.JSONObject;
import com.mugui.Dui.DButton;
import com.mugui.base.base.Autowired;
import com.mugui.base.base.Component;
import com.mugui.base.client.net.bagsend.HTTPUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;

//@Component
public class wx登录和支付2 extends FunctionUI {
	public wx登录和支付2() {
		setTitle("wx登录和支付2");
		setMenu_name("wx登录和支付2");
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

		initToken();


	}

	/**
	 * 获取用户信息
	 * @param access_token
	 * @param openid
	 * @return
	 */
	private String getUserInfo(String access_token, String openid) {

		String url="https://api.weixin.qq.com/cgi-bin/user/info?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN";
		url=url.replace("ACCESS_TOKEN",access_token);
		url=url.replace("OPENID",openid);
		String s = HttpUtil.get(url, 10000);
		System.out.println(s);
		//{"subscribe":1,"openid":"oQoQX6q7gAUNrJoohhptUd9EsKeY","nickname":"","sex":0,"language":"zh_CN","city":"","province":"","country":"","headimgurl":"","subscribe_time":1663295340,"remark":"","groupid":0,"tagid_list":[],"subscribe_scene":"ADD_SCENE_QR_CODE","qr_scene":0,"qr_scene_str":""}
		return s;
	}

	//英雄测试公众号
	String APPID = "wxd396e21301f850a4";
	String SECRET = "177b4d11534bf983b530cd1784d2b9ee";
	//测试版
//	String APPID = "wxf5b56137bdfd74a4";
//	String SECRET = "14e6039ff08044489ffd022be84ffa5c";
	String SERVER_TOKEN="mugui123";
	String SERVER_KEY="5GoveHWyFPWjUrIF7MYhhGsfUApZKRu0dymB4PV0Uo8";
	String ACCESS_TOKEN=null;

	private void initToken() {
		String url="https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";
		url=url.replace("APPID",APPID);
		url=url.replace("APPSECRET",SECRET);
		String s = HttpUtil.get(url, 10000);
		System.out.println(s);
		ACCESS_TOKEN= JSONObject.parseObject(s).getString("access_token");

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



				value = value.replace("APPID", APPID);
				value = value.replace("SECRET", SECRET);
				value = value.replace("CODE", code);

				String string = (String) HTTPUtil.get(value);
				JSONObject object = JSONObject.parseObject(string);
				System.out.println(object.toString());

//				String access_token = object.getString("access_token");
//				String refresh_token = object.getString("refresh_token");
//				String openid = object.getString("openid");
//				String scope = object.getString("scope");
//				Integer expires_in = object.getInteger("expires_in");
//
//				String userInfo = getUserInfo(ACCESS_TOKEN, openid);
				//{
				//    "subscribe":1,
				//    "openid":"o_Ghg5x63tcOIJjk9SfZTqBsZZ9M",
				//    "nickname":"",
				//    "sex":0,
				//    "language":"zh_CN",
				//    "city":"",
				//    "province":"",
				//    "country":"",
				//    "headimgurl":"",
				//    "subscribe_time":1663318627,
				//    "unionid":"oWVTCvlk2NBm7aqBe0AiLJIYfqwE",
				//    "remark":"",
				//    "groupid":0,
				//    "tagid_list":[
				//
				//    ],
				//    "subscribe_scene":"ADD_SCENE_QR_CODE",
				//    "qr_scene":0,
				//    "qr_scene_str":""
				//}

				response.setCharacterEncoding("UTF-8");
				response.setContentType("text/html; charset=utf-8");
				writer = response.getWriter();
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
			System.out.println("接收到消息" + request.getRequestURI());

			PrintWriter writer;
			try {
				//token=mugui123&signature=d78c25a6063b82435c9c5a3ed6c5ecbea18a1fdf&echostr=3658648745998548894&timestamp=1663299332&nonce=486735166
				String token = SERVER_TOKEN;
				//随机字符串
				String echostr = request.getParameter("echostr");
				String timestamp = request.getParameter("timestamp");
				String nonce = request.getParameter("nonce");
				String signature = request.getParameter("signature");

				String s=DigestUtil.sha1Hex(ListUtil.toList(token,timestamp ,nonce).stream().sorted().collect(Collectors.joining()));
				String ret_msg="";
				if(signature.equals(s)){

					//签名正确
					if(echostr!=null){

						ret_msg=echostr;
					}else {
						FastByteArrayOutputStream read = IoUtil.read(request.getInputStream());
						String string = read.toString();
						read.close();
						System.out.println(string);

					}
				}else {
					//签名错误
					ret_msg="sign error";
				}
				response.setCharacterEncoding("UTF-8");
				response.setContentType("text/html; charset=utf-8");
				writer = response.getWriter();
				writer.write(ret_msg);
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
			return "wxMsg";
		}
	};


}
