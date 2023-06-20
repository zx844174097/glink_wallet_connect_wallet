package cn.net.mugui.net.pc.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;

import com.jcraft.jsch.*;
import com.mugui.util.Other;

public class SSHClient {

	private String host;
	private int port;
	private String user;
	private String privateKeyPath;
	private JSch jsch;
	private String pwd;

	public SSHClient(String host, int port, String user, String privateKeyPath, String pwd) {
		this.host = host;
		this.port = port;
		this.user = user;
		this.pwd = pwd;
		this.privateKeyPath = privateKeyPath;
		jsch = new JSch();
	}

	public SSHClient(String host, int port, String user, String pwd) {
		this.host = host;
		this.port = port;
		this.user = user;
		this.pwd = pwd;
		jsch = new JSch();
	}

	public CompletableFuture<String> executeCommand(String command) throws Exception {
		CompletableFuture<String> future = new CompletableFuture<>();
		Session session = jsch.getSession(user, host, port);
		if (privateKeyPath != null) {
			jsch.addIdentity(privateKeyPath, pwd);
		} else {
			session.setPassword(pwd);
		}
		session.setConfig("StrictHostKeyChecking", "no");
		session.connect();
		ChannelExec channel = (ChannelExec) session.openChannel("exec");
		channel.setCommand(command);
		InputStream in = channel.getInputStream();
		OutputStream out = channel.getOutputStream();
		channel.connect();
		StringBuilder result = new StringBuilder();
		CompletableFuture.runAsync(() -> {
			Other.sleep(1000);
			byte[] buffer = new byte[1024];
			int len;
			try {
				while ((len = in.read(buffer)) > 0) {
					result.append(new String(buffer, 0, len));
				}
			} catch (Exception e) {
				future.completeExceptionally(e);
			}
			future.complete(result.toString());
			try {
				out.close();
			} catch (Exception e) {
				future.completeExceptionally(e);
			} finally {
				channel.disconnect();
				session.disconnect();
			}
		});
		return future;
	}

	public String send(String s) {
		try {
			CompletableFuture<String> stringCompletableFuture = executeCommand(s);
			String s1 = stringCompletableFuture.get();
			return s1;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}