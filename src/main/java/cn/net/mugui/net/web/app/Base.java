package cn.net.mugui.net.web.app;

import com.mugui.Mugui;
import com.mugui.base.base.Autowired;
import com.mugui.base.base.Component;
import com.mugui.base.client.net.base.Module;
import com.mugui.base.client.net.bean.Message;
import com.mugui.base.client.net.bean.NetBag;


@Component
@Module(name = "base", type = "method")
public class Base implements Mugui {


	public Message getPublicServer(NetBag bag) {
		return Message.ok();
	}

}
