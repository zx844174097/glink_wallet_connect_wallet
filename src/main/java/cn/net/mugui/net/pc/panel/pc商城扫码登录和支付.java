package cn.net.mugui.net.pc.panel;

import cn.hutool.core.util.RandomUtil;
import cn.net.mugui.net.pc.manager.FunctionUI;
import cn.net.mugui.net.pc.panel.comp.MuguiBrowser;
import cn.net.mugui.net.pc.util.SysConf;
import cn.net.mugui.web.tomcat.InsideTomcat2;
import cn.net.mugui.web.tomcat.InsideTomcat2.PathListener;
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

@Component
public class pc商城扫码登录和支付 extends FunctionUI {
	public pc商城扫码登录和支付() {
		setTitle("pc商城扫码登录和支付");
		setMenu_name("pc商城扫码登录和支付");
		setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		add(panel, BorderLayout.NORTH);

		DButton 扫码登录 = new DButton("扫码登录", (Color) null);
		扫码登录.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String value = sysConf.getValue("mall_login_url");
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
		tomcat.addPathListener(webMallqc);
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
	 * pc扫码登录
	 */
	private PathListener webMallqc = new PathListener() {

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
			return "webMallqc";
		}
	};


}
