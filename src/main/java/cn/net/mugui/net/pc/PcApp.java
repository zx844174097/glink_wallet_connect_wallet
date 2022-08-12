package cn.net.mugui.net.pc;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.alibaba.fastjson.JSONObject;
import com.mugui.base.base.ApplicationContext;
import com.mugui.base.client.net.baghandle.NetBagModuleManager;
import com.mugui.base.client.net.classutil.DataSave;
import com.mugui.sql.DBConf;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.digest.HmacAlgorithm;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.log.Log;
import cn.hutool.log.dialect.log4j2.Log4j2LogFactory;
import cn.net.mugui.net.pc.util.SysConf;
import cn.net.mugui.web.tomcat.InsideTomcat2;


/**
 * Hello world! 同一连接 连接
 * {"id":1660303715160379,"jsonrpc":"2.0","method":"wc_sessionRequest","params":[{"peerId":"0bfe1251-1e21-4ee0-a58b-ec8d0f928f69","peerMeta":{"description":"","url":"https://example.walletconnect.org","icons":["https://example.walletconnect.org/favicon.ico"],"name":"WalletConnect
 * Example"},"chainId":null}]} 同意连接
 * {"id":1660303715160379,"jsonrpc":"2.0","result":{"approved":true,"chainId":1,"networkId":0,"accounts":["0x994cB652e962Ec1d8DB89E58e9c242Dec3615658"],"rpcUrl":"","peerId":"8a038fcb-a30b-4618-89f5-c85044457c53","peerMeta":{"description":"","url":"https://test.walletconnect.org","icons":["https://test.walletconnect.org/favicon.ico","https://test.walletconnect.org/apple-touch-icon.png"],"name":"WalletConnect
 * Test Wallet"}}}
 *
 * {"id":1660318430955336,"jsonrpc":"2.0","method":"personal_sign","params":["0x4d7920656d61696c206973206a6f686e40646f652e636f6d202d204672692c2031322041756720323032322031353a33333a353020474d54","0x994cB652e962Ec1d8DB89E58e9c242Dec3615658"]}
 * 
 */
public class PcApp {
	public static void main(String[] args) {
		{

			String payload = "{\"data\":\"2d95591c028d1dbb8716fa6351ecada61fa493e68d47025fd4bd13e44498eb5f95426e1fcd1625e20e574e7431f33873b2b79d2ef48ee5390d6c6f8323a392815e3214fa62e0c09a7ced49bd3a995675125a5cc93bcbf4f68f7b3206ab5f6441b2e96e0de7f7608bb369c9ac3c1acc5d5c92074bf691c6be37f26e4841604bfd69c003433d58bd194dd66a53460e8b5efc21af1539c21077b78582ea3457b9792ce3a1727e27f51da431198e671fc13f380bafb83842e2b393ab1fea2e6581fc022a4152b91ae3172769c0fa0f4db0756db03e50bf86209efc48dce4ec61056a5e05cf454329fe783e160678df2d1a96\",\"hmac\":\"4059018915d77bdc0a150991799f09c261dc6c2d84d0fffb5ccbecedccce4271\",\"iv\":\"41917fd68d02d641b7379bc690d83155\"}";
			String key = "d1a0dfe3d95fa7b80d88f01d48e7b5ba60013d05a0067a66ee0dd1eff54f2494";
			JSONObject parse = JSONObject.parseObject(payload);

			byte[] keys = HexUtil.decodeHex(key);
			// 签名校验
			byte[] digest = DigestUtil.hmac(HmacAlgorithm.HmacSHA256, keys)
					.digest(HexUtil.decodeHex(parse.getString("data") + parse.getString("iv")));
			if (!HexUtil.encodeHexStr(digest).equals(parse.get("hmac"))) {
				// 签名校验失败
				return;
			}

			byte[] iv = HexUtil.decodeHex(parse.getString("iv"));

			AES aes = new AES(Mode.CBC, Padding.PKCS5Padding, keys, iv);
			byte[] decrypt = aes.decrypt(HexUtil.decodeHex(parse.getString("data")));
			System.out.println(new String(decrypt));
		}

		LogInit();
		DataSave.APP_PATH(PcApp.class);
		DBConf.getDefaultDBConf().readConf("org.sqlite.JDBC",
				"jdbc:sqlite:" + DataSave.APP_PATH + "//sqlite.dll?journal_mode=WAL", "root", "mugui123");

		DataSave.context = new ApplicationContext();
		DataSave.context.init("cn.net.mugui", "com.mugui");
		DataSave.context.getBean(NetBagModuleManager.class).init(PcApp.class);

		SysConf conf = DataSave.context.getBean(SysConf.class);
		conf.init();
		conf.setValue("web_port", "5678");
		String value = conf.getValue("web_port");
		// 清空服务器
		System.out.println(DataSave.APP_PATH);
		try {
			AppFrame bean = DataSave.context.getBean(AppFrame.class);
			bean.updateTitle("登录和支付");
			bean.init();
			bean.validate();
			bean.setVisible(true);
			InsideTomcat2 bean1 = DataSave.context.getBean(InsideTomcat2.class);
			bean1.setPort(Integer.valueOf(value));
			bean1.init();
			bean1.getTomcat().init();
			bean1.getTomcat().start();
			System.out.println(conf.getValue("base_url"));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	 /**
		 * 日志系统初始化
		 *
		 */
		private static  void LogInit() {
			Log createLog = Log4j2LogFactory.create().createLog("Console");

			PrintStream out = new PrintStream(System.out) {
				DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
				String classname = this.getClass().getName();
				@Override
				public void print(String s) {
					createLog.info(classname, null, s);
				}

				@Override
				public void println(String x) {
					print(x);
				}

				@Override
				public void print(Object obj) {
					print(String.valueOf(obj));
				}

				@Override
				public void println(Object x) {
					println(String.valueOf(x));
				}

				@Override
				public void print(char c) {
					print(String.valueOf(c));
				}

				@Override
				public void print(boolean b) {
					print(String.valueOf(b));
				}

				@Override
				public void print(float f) {
					print(String.valueOf(f));
				}

				@Override
				public void print(int i) {
					print(String.valueOf(i));
				}

				@Override
				public void println(char c) {
					println(String.valueOf(c));
				}

				@Override
				public void println(boolean b) {
					println(String.valueOf(b));
				}

				@Override
				public void println(float f) {
					println(String.valueOf(f));
				}

				@Override
				public void println(int i) {
					println(String.valueOf(i));
				}
			};
			System.setOut(out);
		}
}
