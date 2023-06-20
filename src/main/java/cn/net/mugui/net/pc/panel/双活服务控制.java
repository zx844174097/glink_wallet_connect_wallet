package cn.net.mugui.net.pc.panel;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import cn.net.mugui.net.pc.manager.FunctionUI;
import cn.net.mugui.net.pc.panel.comp.MuguiBrowser;
import cn.net.mugui.net.pc.util.CMD;
import cn.net.mugui.net.pc.util.SSHClient;
import cn.net.mugui.net.pc.util.SysConf;
import cn.net.mugui.web.tomcat.InsideTomcat2;
import com.mugui.Dui.DButton;
import com.mugui.base.base.Autowired;
import com.mugui.base.base.Component;
import com.mugui.util.Other;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

@Component
public class 双活服务控制 extends FunctionUI {


	@Autowired
	private MuguiBrowser muguiBrowser;

	public 双活服务控制() {
		setTitle("双活服务控制");
		setMenu_name("双活服务控制");

		setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		add(panel, BorderLayout.NORTH);

		DButton 双活服务控制 = new DButton("双活服务控制", (Color) null);
		双活服务控制.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				String value = sysConf.getValue("qq_login_url");
//				StringJoiner stringJoiner = new StringJoiner("&");
//				stringJoiner.add("response_type=code");
//				stringJoiner.add("client_id=102010527");
//				stringJoiner
//						.add("redirect_uri=http://other.yingxiong.com/dev_mainland/sdkcom/c1/pc/login/qqRedirect.ul");
//				stringJoiner.add("state=1_com.hero.pc.sdk");
				muguiBrowser.openUrl("http://10.22.12.123:5679/web/dualActivityServiceControl.html");

			}
		});
		panel.add(双活服务控制);
	}

	@Autowired
	private InsideTomcat2 tomcat;

	@Autowired
	private SysConf sysConf;

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

	SSHClient cmd99 = new SSHClient("10.22.200.99", 22, "root", "E:\\mugui123", "mugui123");
	SSHClient cmd200 = new SSHClient("10.22.200.200", 22, "root", "E:\\mugui123", "mugui123");
	SSHClient cmd127 = new SSHClient("10.22.12.121", 22, "mugui", "mugui123");


	/**
	 * 服务控制
	 */
	private InsideTomcat2.PathListener pathListener = new InsideTomcat2.PathListener() {

		@Override
		public boolean listener(HttpServletRequest request, HttpServletResponse response) {
			System.out.println("服务控制" + request.getRequestURI());
			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/html; charset=utf-8");
			//跨域
			response.setHeader("Access-Control-Allow-Origin", "*");
			response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
			response.setHeader("Access-Control-Max-Age", "3600");
			response.setHeader("Access-Control-Allow-Headers", "x-requested-with");
			response.setHeader("Access-Control-Allow-Credentials", "true");

			String service = request.getParameter("service");
			String type = request.getParameter("type");

			SSHClient cmd = cmd99;

			try {
				String retMsg = "无法处理";

				String dockerId = null;

				//ali-service huawei-service ali-database huawei-database ali-cache huawei-cache ali-gateway huawei-gateway gateway
				switch (type) {
					case "ali-service":
						//链接到99服务器
						dockerId = "6488ab75c456";
						break;
					case "huawei-service":
						//链接到99服务器
						dockerId = "7e2c015d07c6";
						break;
					case "ali-pay-service":
						dockerId = "140051186bba";
						break;
					case "huawei-pay-service":
						dockerId = "c08f1bb5c63c";
						break;


					case "ali-database":
						dockerId = "fa75472894e2";
						break;
					case "huawei-database":
						dockerId = null;
						break;
					case "ali-cache":

						dockerId = "931696a3c58d";
//						//链接到200服务器
//						cmd = cmd200;
//						switch (service) {
//							case "on":
//								cmd.send("/data/redis/runRedis.sh");
//								retMsg = "开启成功";
//								break;
//							case "off":
//								cmd.send("/data/redis/stopRedis.sh");
//								retMsg = "关闭成功";
//
//								break;
//							case "status":
//								retMsg = cmd.send("ps -ef|grep redis|grep 60");
//								break;
//						}
						break;
					case "huawei-cache":
						//链接到99服务器
						dockerId = "a8989061a1ba";
						break;
					case "ali-gateway":
						//
						cmd = cmd127;
						switch (service) {
							case "on":
								cmd.send("sed -i '26s/./7/9' /etc/nginx/conf.d/default.conf");
								cmd.send("echo mugui123|sudo nginx -s reload");
								retMsg = "开启成功";
								break;
							case "off":
								cmd.send("sed -i '26s/./8/9' /etc/nginx/conf.d/default.conf");
								cmd.send("echo mugui123|sudo nginx -s reload");
								retMsg = "关闭成功";
								break;
							case "status":
								retMsg = cmd.send("cat /etc/nginx/conf.d/default.conf");
								break;
						}
						cmd.send("nginx -s reload");
						break;
					case "huawei-gateway":
						//
						cmd = cmd127;
						switch (service) {
							case "on":
								cmd.send("sed -i '42s/./6/9' /etc/nginx/conf.d/default.conf");
								cmd.send("echo mugui123|sudo nginx -s reload");
								retMsg = "开启成功";
								break;
							case "off":
								cmd.send("sed -i '42s/./9/9' /etc/nginx/conf.d/default.conf");
								cmd.send("echo mugui123|sudo nginx -s reload");
								retMsg = "关闭成功";
								break;
							case "status":
								retMsg = cmd.send("cat /etc/nginx/conf.d/default.conf");
								break;
						}
						cmd.send("nginx -s reload");
						break;


					case "ali-pay-gateway":
						//
						cmd = cmd127;
						switch (service) {
							case "on":
								cmd.send("sed -i '26s/./5/9' /etc/nginx/conf.d/sdksrv.conf");
								cmd.send("echo mugui123|sudo nginx -s reload");
								retMsg = "开启成功";
								break;
							case "off":
								cmd.send("sed -i '26s/./8/9' /etc/nginx/conf.d/sdksrv.conf");
								cmd.send("echo mugui123|sudo nginx -s reload");
								retMsg = "关闭成功";
								break;
							case "status":
								retMsg = cmd.send("cat /etc/nginx/conf.d/sdksrv.conf");
								break;
						}
						cmd.send("nginx -s reload");
						break;
					case "huawei-pay-gateway":
						//
						cmd = cmd127;
						switch (service) {
							case "on":
								cmd.send("sed -i '42s/./4/9' /etc/nginx/conf.d/sdksrv.conf");
								cmd.send("echo mugui123|sudo nginx -s reload");
								retMsg = "开启成功";
								break;
							case "off":
								cmd.send("sed -i '42s/./9/9' /etc/nginx/conf.d/sdksrv.conf");
								cmd.send("echo mugui123|sudo nginx -s reload");
								retMsg = "关闭成功";
								break;
							case "status":
								retMsg = cmd.send("cat /etc/nginx/conf.d/sdksrv.conf");
								break;
						}
						cmd.send("nginx -s reload");
						break;

					case "gateway":
						cmd = cmd127;
						switch (service) {
							case "ali":
								cmd.send("sed -i '4s/.*/    server  10.22.12.121:7074;/' /etc/nginx/conf.d/default.conf");
								cmd.send("sed -i '6s/.*/    #server  10.22.12.121:6074;/' /etc/nginx/conf.d/default.conf");
								cmd.send("echo mugui123|sudo nginx -s reload");
								retMsg = "切换到阿里网关";
								break;
							case "huawei":
								cmd.send("sed -i '4s/.*/    #server  10.22.12.121:7074;/' /etc/nginx/conf.d/default.conf");
								cmd.send("sed -i '6s/.*/    server  10.22.12.121:6074;/' /etc/nginx/conf.d/default.conf");
								cmd.send("echo mugui123|sudo nginx -s reload");
								retMsg = "切换到华为网关";
								break;
							case "off":
								cmd.send("sed -i '4s/.*/    server  10.22.12.121:7074;/' /etc/nginx/conf.d/default.conf");
								cmd.send("sed -i '6s/.*/    server  10.22.12.121:6074;/' /etc/nginx/conf.d/default.conf");
								cmd.send("echo mugui123|sudo nginx -s reload");
								retMsg = "取消限制网关";
								break;
							case "status":
								retMsg = cmd.send("cat /etc/nginx/conf.d/default.conf");
								break;
						}

						break;


					case "pay-gateway":
						cmd = cmd127;
						switch (service) {
							case "ali":
								cmd.send("sed -i '4s/.*/    server  127.0.0.1:5074;/' /etc/nginx/conf.d/sdksrv.conf");
								cmd.send("sed -i '6s/.*/    #server  127.0.0.1:4074;/' /etc/nginx/conf.d/sdksrv.conf");
								cmd.send("echo mugui123|sudo nginx -s reload");
								retMsg = "切换到阿里网关";
								break;
							case "huawei":
								cmd.send("sed -i '4s/.*/    #server  127.0.0.1:5074;/' /etc/nginx/conf.d/sdksrv.conf");
								cmd.send("sed -i '6s/.*/    server  127.0.0.1:4074;/' /etc/nginx/conf.d/sdksrv.conf");
								cmd.send("echo mugui123|sudo nginx -s reload");
								retMsg = "切换到华为网关";
								break;
							case "off":
								cmd.send("sed -i '4s/.*/    server  127.0.0.1:5074;/' /etc/nginx/conf.d/sdksrv.conf");
								cmd.send("sed -i '6s/.*/    server  127.0.0.1:4074;/' /etc/nginx/conf.d/sdksrv.conf");
								cmd.send("echo mugui123|sudo nginx -s reload");
								retMsg = "取消限制网关";
								break;
							case "status":
								retMsg = cmd.send("cat /etc/nginx/conf.d/sdksrv.conf");
								break;
						}

						break;


					case "service":
						switch (service) {
							case "ali":
								try {
									HttpUtil.get("http://10.22.200.99:24235/sdkcom/forwarder/admin/update?forward_sign=869feb15-afa5-4b82-95b6-3947d0478c6c&forward_stop=0&forward_force=1", 1000);
								} catch (Exception e) {
									e.printStackTrace();
								}
								try {
									HttpUtil.get("http://10.22.200.99:25235/sdkcom/forwarder/admin/update?forward_sign=869feb15-afa5-4b82-95b6-3947d0478c6c&forward_stop=1&forward_force=0", 1000);
								} catch (Exception e) {
									e.printStackTrace();
								}
								retMsg = "仅ali开启成功";
								break;
							case "huawei":
								try {
									HttpUtil.get("http://10.22.200.99:24235/sdkcom/forwarder/admin/update?forward_sign=869feb15-afa5-4b82-95b6-3947d0478c6c&forward_stop=1&forward_force=0", 1000);
								} catch (Exception e) {
									e.printStackTrace();
								}
								try {
									HttpUtil.get("http://10.22.200.99:25235/sdkcom/forwarder/admin/update?forward_sign=869feb15-afa5-4b82-95b6-3947d0478c6c&forward_stop=0&forward_force=1", 1000);
								} catch (Exception e) {
									e.printStackTrace();
								}
								retMsg = "仅huawei开启成功";
								break;
							case "both":
								try {
									HttpUtil.get("http://10.22.200.99:24235/sdkcom/forwarder/admin/update?forward_sign=869feb15-afa5-4b82-95b6-3947d0478c6c&forward_stop=0&forward_force=0", 1000);
								} catch (Exception e) {
									e.printStackTrace();
								}
								try {
									HttpUtil.get("http://10.22.200.99:25235/sdkcom/forwarder/admin/update?forward_sign=869feb15-afa5-4b82-95b6-3947d0478c6c&forward_stop=0&forward_force=0", 1000);
								} catch (Exception e) {
									e.printStackTrace();
								}
								retMsg = "双方开启成功";
								break;
							case "status":
								try {
									String s = HttpUtil.get("http://10.22.200.99:24235/sdkcom/forwarder/admin/status?forward_sign=869feb15-afa5-4b82-95b6-3947d0478c6c", 3000);
									retMsg = JSONUtil.formatJsonStr(s);
								} catch (Exception e) {
									e.printStackTrace();
								}

								try {
									String s2 = HttpUtil.get("http://10.22.200.99:25235/sdkcom/forwarder/admin/status?forward_sign=869feb15-afa5-4b82-95b6-3947d0478c6c", 3000);
									retMsg += "\n" + JSONUtil.formatJsonStr(s2);
								} catch (Exception e) {
									e.printStackTrace();
								}
								break;
						}
						break;


					case "pay-service":
						switch (service) {
							case "ali":
								try {
									HttpUtil.get("http://10.22.12.121:4074/sdksrv/forwarder/admin/update?forward_sign=869feb15-afa5-4b82-95b6-3947d0478c6c&forward_stop=0&forward_force=1", 1000);
								} catch (Exception e) {
									e.printStackTrace();
								}
								try {
									HttpUtil.get("http://10.22.12.121:5074/sdksrv/forwarder/admin/update?forward_sign=869feb15-afa5-4b82-95b6-3947d0478c6c&forward_stop=1&forward_force=0", 1000);
								} catch (Exception e) {
									e.printStackTrace();
								}
								retMsg = "仅ali开启成功";
								break;
							case "huawei":
								try {
									HttpUtil.get("http://10.22.12.121:4074/sdksrv/forwarder/admin/update?forward_sign=869feb15-afa5-4b82-95b6-3947d0478c6c&forward_stop=1&forward_force=0", 1000);
								} catch (Exception e) {
									e.printStackTrace();
								}
								try {
									HttpUtil.get("http://10.22.12.121:5074/sdksrv/forwarder/admin/update?forward_sign=869feb15-afa5-4b82-95b6-3947d0478c6c&forward_stop=0&forward_force=1", 1000);
								} catch (Exception e) {
									e.printStackTrace();
								}
								retMsg = "仅huawei开启成功";
								break;
							case "both":
								try {
									HttpUtil.get("http://10.22.12.121:5074/sdksrv/forwarder/admin/update?forward_sign=869feb15-afa5-4b82-95b6-3947d0478c6c&forward_stop=0&forward_force=0", 1000);
								} catch (Exception e) {
									e.printStackTrace();
								}
								try {
									HttpUtil.get("http://10.22.12.121:4074/sdksrv/forwarder/admin/update?forward_sign=869feb15-afa5-4b82-95b6-3947d0478c6c&forward_stop=0&forward_force=0", 1000);
								} catch (Exception e) {
									e.printStackTrace();
								}
								retMsg = "双方开启成功";
								break;
							case "status":
								try {
									String s = HttpUtil.get("http://10.22.12.121:5074/sdksrv/forwarder/admin/status?forward_sign=869feb15-afa5-4b82-95b6-3947d0478c6c", 3000);
									retMsg = JSONUtil.formatJsonStr(s);
								} catch (Exception e) {
									e.printStackTrace();
								}

								try {
									String s2 = HttpUtil.get("http://10.22.12.121:4074/sdksrv/forwarder/admin/status?forward_sign=869feb15-afa5-4b82-95b6-3947d0478c6c", 3000);
									retMsg += "\n" + JSONUtil.formatJsonStr(s2);
								} catch (Exception e) {
									e.printStackTrace();
								}
								break;
						}
						break;


					case "ali-canal":
						//链接到99服务器
						cmd = cmd99;
						dockerId = "1cee830d8ba3";
						break;
					case "huawei-canal":
						//链接到99服务器
						cmd = cmd99;

						dockerId = "aa2bc53318bd";
						break;
					case "dual-activity":
						//链接到99服务器
						cmd = cmd99;
						dockerId = "e8225c057435";
						break;
					default:
						break;
				}

				if (dockerId != null) {
					switch (service) {
						case "on":
							cmd.send("docker start " + dockerId + "");
							retMsg = "开启成功";
							break;
						case "off":
							cmd.send("docker stop " + dockerId + "");
							retMsg = "关闭成功";
							break;
						case "status":
							retMsg = cmd.send("docker ps|grep " + dockerId + "");
							break;
					}
				}
				if (retMsg == null) {
					retMsg = "无结果";
				}
				response.getWriter().write(retMsg);


			} catch (IOException e) {
				e.printStackTrace();
			}

			return false;
		}

		@Override
		public String getPath() {
			return "dualActivityServiceApi";
		}
	};


}
