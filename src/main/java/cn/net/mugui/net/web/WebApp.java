package cn.net.mugui.net.web;

import com.mugui.base.base.ApplicationContext;
import com.mugui.base.client.net.baghandle.NetBagModuleManager;
import com.mugui.base.client.net.classutil.DataSave;
import com.mugui.sql.DBConf;

import cn.net.mugui.web.tomcat.InsideTomcat2;

/**
 * Web init
 *
 */
public class WebApp {

	public static void main(String[] args) {
		// wc:f5a5dede-856c-483b-b89d-d32045f3af48@1?bridge=https%3A%2F%2Fy.bridge.walletconnect.org&key=bde1156e6e2350a4eaa03fc962c6bc61ed76a17f635939bd7011329c1b3d584c


		DBConf.getDefaultDBConf().readConf("org.sqlite.JDBC", "jdbc:sqlite:" + DataSave.APP_PATH + "//sqlite.dll",
				"root", "mugui123");
		DataSave.context = new ApplicationContext();
		DataSave.context.init("cn.net.mugui", "com.mugui");
		DataSave.context.getBean(NetBagModuleManager.class).init(WebApp.class);
		try {
			DataSave.context.getBean(InsideTomcat2.class).run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
