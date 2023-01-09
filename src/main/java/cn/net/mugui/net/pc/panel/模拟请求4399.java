package cn.net.mugui.net.pc.panel;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.net.mugui.net.pc.manager.FunctionUI;
import cn.net.mugui.net.pc.util.SignUtils;
import cn.net.mugui.net.pc.util.SysConf;
import com.alibaba.fastjson.JSONObject;
import com.mugui.Dui.DButton;
import com.mugui.base.base.Autowired;
import com.mugui.base.base.Component;
import com.mugui.util.Other;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

@Component
public class 模拟请求4399 extends FunctionUI {
	public 模拟请求4399() {
		setTitle("模拟请求4399");
		setMenu_name("模拟请求4399");
		setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		add(panel, BorderLayout.NORTH);

		DButton tank = new DButton("巅峰坦克模拟测试", (Color) null);
		tank.addActionListener(new ActionListener() {
			Thread tankThread=null;
			public void actionPerformed(ActionEvent e) {
				if(tankThread==null||!tankThread.isAlive()){
					tankThread=new Thread(new Runnable() {
						@Override
						public void run() {
							while(tankThread!=null){
								try {

									handle("tank");
								}catch (Exception e) {
									e.printStackTrace();
								}
								for(int i=0;i<100;i++){
									if(tankThread==null){
										return;
									}
									Other.sleep(100);
								}
							}

						}
					});
					tankThread.start();
				}else {
					tankThread=null;
				}

			}
		});
		panel.add(tank);
		DButton zhanjian = new DButton("巅峰战舰模拟测试", (Color) null);
		zhanjian.addActionListener(new ActionListener() {
			Thread tankThread=null;
			public void actionPerformed(ActionEvent e) {
				if(tankThread==null||!tankThread.isAlive()){
					tankThread=new Thread(new Runnable() {
						@Override
						public void run() {
							while(tankThread!=null){
								try {
									handle("zhanjian");
								}catch (Exception e) {
									e.printStackTrace();
								}
								for(int i=0;i<100;i++){
									if(tankThread==null){
										return;
									}
									Other.sleep(100);
								}
							}

						}
					});
					tankThread.start();
				}else {
					tankThread=null;
				}

			}
		});
		panel.add(zhanjian);

		DButton linghun = new DButton("灵魂潮汐模拟测试", (Color) null);
		linghun.addActionListener(new ActionListener() {
			Thread tankThread=null;
			public void actionPerformed(ActionEvent e) {
				if(tankThread==null||!tankThread.isAlive()){
					tankThread=new Thread(new Runnable() {
						@Override
						public void run() {
							while(tankThread!=null){
								try {
									handle("linghun");
								}catch (Exception e) {
									e.printStackTrace();
								}
								for(int i=0;i<100;i++){
									if(tankThread==null){
										return;
									}
									Other.sleep(100);
								}
							}

						}
					});
					tankThread.start();
				}else {
					tankThread=null;
				}

			}
		});
		panel.add(linghun);


		DButton qiangzhan = new DButton("全民枪战2模拟测试", (Color) null);
		qiangzhan.addActionListener(new ActionListener() {
			Thread tankThread=null;
			public void actionPerformed(ActionEvent e) {
				if(tankThread==null||!tankThread.isAlive()){
					tankThread=new Thread(new Runnable() {
						@Override
						public void run() {
							while(tankThread!=null){
								try {
									handle("qiangzhan");
								}catch (Exception e) {
									e.printStackTrace();
								}
								for(int i=0;i<100;i++){
									if(tankThread==null){
										return;
									}
									Other.sleep(100);
								}
							}

						}
					});
					tankThread.start();
				}else {
					tankThread=null;
				}

			}
		});
		panel.add(qiangzhan);

	}
	private void handle(String tank) {
		String infoRoleUrl = sysConf.getValue("infoRole.url."+tank);
		String receive = sysConf.getValue("receive.url."+tank);
		String syn_key = sysConf.getValue("syn_key."+tank);
		String pcode = sysConf.getValue("pcode."+tank);
		String uid = sysConf.getValue("uid."+tank);
		String gf_code = sysConf.getValue("gf_code."+tank);
		String rid = sysConf.getValue("rid."+tank);
		String sid = sysConf.getValue("sid."+tank);
		String cid = sysConf.getValue("cid."+tank);
		if(StrUtil.isNotBlank(infoRoleUrl)){

			JSONObject object = new JSONObject();
			object.put("uid", uid);
			object.put("cid", cid);
			String ret = sendMsg(object.toString(), syn_key, infoRoleUrl, Integer.valueOf(pcode));
			System.out.println(ret);
		}
		if(StrUtil.isNotBlank(receive)){

			JSONObject object=new JSONObject();
			object.put("uid",uid);
			object.put("cid",cid);
			object.put("sid",sid);
			object.put("rid",rid);
			object.put("gf_code",gf_code);
			String ret = sendMsg(object.toString(), syn_key, receive, Integer.valueOf(pcode));
			System.out.println(ret);
		}
	}


	/**
	 * @param msg     数据
	 * @param syncKey key
	 * @param pushUrl 推送地址
	 */
	public static String sendMsg(String msg, String syncKey, String pushUrl,Integer pcode) {
		String result = "";
		try {
			if (StringUtils.isNotBlank(pushUrl)) {
				Map<String, Object> queryParas = new HashMap<String, Object>();
				String data = Base64.encode(msg.getBytes());
				queryParas.put("data", data);
				queryParas.put("pcode",pcode.toString());
				queryParas.put("timestamp",System.currentTimeMillis()+"");
				queryParas.put("sign", SignUtils.markSign(syncKey, "data", data,"pcode",pcode.toString(),"timestamp",queryParas.get("timestamp").toString()));

				result = HttpRequest.post(pushUrl).form(queryParas).header("Content-Type","application/x-www-form-urlencoded").timeout(10000).execute().body();


			} else {
				throw new RuntimeException("暂未配置游戏的回调地址");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
		return result;
	}












	@Autowired
	private SysConf sysConf;

	@Override
	public void init() {

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
}
