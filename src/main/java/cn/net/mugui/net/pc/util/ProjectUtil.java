package cn.net.mugui.net.pc.util;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xerces.internal.dom.DeferredElementNSImpl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.XmlUtil;
import cn.hutool.setting.Setting;
import cn.net.mugui.net.pc.bean.ClassBean;
import cn.net.mugui.net.pc.bean.ProjectBean;
import cn.net.mugui.net.pc.panel.项目分析;

/**
 * 项目分析工具
 */
public class ProjectUtil {
	/**
	 * 处理pomXml文件
	 *
	 * @param file
	 * @return
	 */
	public static void handlePomXml(File file, ProjectBean projectBean) {
		File file1 = new File(file, "pom.xml");
		Document document = XmlUtil.readXML(file1);
		NodeList childNodes = document.getChildNodes().item(0).getChildNodes();

		for (int i = 0; i < childNodes.getLength(); i++) {
			if (StringUtils.isBlank(projectBean.getProject_name()) && childNodes.item(i).getNodeName().equals("artifactId")) {
				projectBean.setProject_name(childNodes.item(i).getTextContent());
				continue;
			} else if (childNodes.item(i).getNodeName().equals("dependencies")) {
				boolean contains = childNodes.item(i).getTextContent().contains("org.springframework");
				if (contains) {
					projectBean.setProject_type(ProjectBean.PROJECT_TYPE_0);
					break;
				} else if (childNodes.item(i).getTextContent().contains("com.jfinal")) {
					projectBean.setProject_type(ProjectBean.PROJECT_TYPE_1);
					break;
				}

			}
		}
	}

	/**
	 * 得到远程连接
	 *
	 * @param file
	 * @param projectBean
	 */
	public static void handleRemoteUrl(File file, ProjectBean projectBean) {
		File file1 = new File(file.getAbsolutePath() + "/.git", "config");
		if (!file1.exists()) {
			return;
		}
		try {
			Setting setting = new Setting(file1.getAbsolutePath(), false);
			String url = setting.get("remote \"origin\"", "url");
			projectBean.setProject_remote_url(url);
			int start = url.lastIndexOf("/");
			int end = url.lastIndexOf(".git");
			if (end == -1) {
				end = url.length();
			}
			String substring = url.substring(start + 1, end);
			projectBean.setProject_name(substring);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Remote url: " + projectBean);
		}

	}

	/**
	 * 得到项目源文件目录
	 *
	 * @param project_file_path
	 */
	public static File getSrcUrl(String project_file_path) {
		File file = new File(project_file_path);
		File[] files = file.listFiles((key, name) -> name.endsWith(".iml"));
		if (files.length > 0) {
			Document document = XmlUtil.readXML(files[0]);
			List<Node> node = XmlHandleUtil.findNode(document, "module.component.content.sourceFolder");
			if (node.isEmpty()) {
				return null;
			}
			for (Node one : node) {
				DeferredElementNSImpl key = ((DeferredElementNSImpl) one);
				String url = key.getAttribute("url");
				String isTestSource = key.getAttribute("isTestSource");
				String type = key.getAttribute("type");

				if (StringUtils.isBlank(type) && StringUtils.isNotBlank(isTestSource) && !Boolean.parseBoolean(isTestSource)) {
					return new File(project_file_path + url.replace("file://$MODULE_DIR$", ""));
				}
			}
		}
		return null;
	}

	/**
	 * 读取所有类
	 *  @param srcUrl
	 * @param classes
	 * @param handle
	 */
	public static void readAllClasses(File srcUrl, LinkedList<ClassBean> classes, 项目分析 handle) {
		handle.showRateLog("readAllClasses->" + srcUrl.getAbsolutePath());
		File[] files = srcUrl.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {//是包名
				readAllClasses(file, classes, handle);
			} else if (file.getName().endsWith(".java")) {//是java文件
				List<String> strings = FileUtil.readUtf8Lines(file);
				ClassBean classBean = null;
				TempISContinue  tempISContinue=new TempISContinue("");
				tempISContinue.is注释=false;

				TempCodeHeight tempCodeHeight = new TempCodeHeight();


				for (String string : strings) {
					//跳过注解
					tempISContinue.string=string;
					if(isContinueNote(tempISContinue)){
						continue;
					}
					string=tempISContinue.string;

					boolean package_ = string.trim().startsWith("package ");
					if (package_) {
						classBean = new ClassBean();
						String package_1 = string.replace("package ", "");
						classBean.setPackage_name(package_1.substring(0, package_1.length() - 1));
						classBean.setClass_file_content(StrUtil.join("\r\n", strings.toArray()));
						continue;
					}
					if (classBean == null) continue;
					String package_name = null;
					if(tempCodeHeight.code_height==0){
						package_name=classBean.getPackage_name();
					}
					if (tempCodeHeight.code_height == 1) {//只处理1层内部类
						package_name = classBean.getPackage_name() + "." + file.getName().split("[.]")[0];
					}
					if(package_name == null) continue;
					//发现一个类
					ClassBean newBean = null;
					if (string.indexOf("class ") == 0 || string.indexOf(" class ") > 0) {
						newBean = ClassBean.newBean(classBean);
						newBean.setClass_type(ClassBean.CLASS_TYPE_DEFAULT);

						int i=string.indexOf("class ");
						String name=string.substring(i);
						name = name.replace("{", "").split("\\s+")[1].trim();
						newBean.setClass_name(name);

						if (string.indexOf("abstract ") == 0 || string.indexOf(" abstract ") > 0) {
							newBean.setClass_type(ClassBean.CLASS_TYPE_ABSTRACT);
						}


					} else if (string.indexOf("interface ") == 0 || string.indexOf(" interface ") > 0) {
						newBean = ClassBean.newBean(classBean);
						newBean.setClass_type(ClassBean.CLASS_TYPE_INTERFACE);

						int i=string.indexOf("interface ");
//						String substring = string.substring(0, i);
//						substring = StringUtils.replaceEach(substring,
//								new String[] { "private ", "static ", "public ", "final " },
//								new String[] { "", "", "", "" });
//						substring = StrUtil.trim(substring);
//						if (StringUtils.isNotBlank(substring)) {
//							newBean = null;
//						} else {
							String name = string.substring(i);
							name = name.replace("{", "").split("\\s+")[1].trim();
							newBean.setClass_name(name);
//						}

					}
					if (newBean != null) {
						newBean.setPackage_name(package_name);
						newBean.setClass_file(file.getAbsolutePath());
						handleOneClassBean(newBean, string);
						classes.add(newBean);
					}
					handleCodeHeight(string,tempCodeHeight);
				}
			}
		}
	}

	/**
	 * 处理代码深度
	 * @param lineString
	 * @param tempCodeHeight
	 */
	 static void handleCodeHeight(String lineString,TempCodeHeight tempCodeHeight) {
		byte[] bytes = lineString.getBytes(StandardCharsets.UTF_8);
		int i=0;
		for (int j=0; j<bytes.length; j++) {
			if(bytes[j]=='"'&&(j-1<0||bytes[j-1]!='\\')){
				i++;
			}
			if(i==1){
				continue;
			}
			i=0;
			if (bytes[j]=='{') {
				tempCodeHeight.code_height++;
			}
			if (bytes[j]=='}') {
				tempCodeHeight.code_height--;
			}
		}
	}

	public static class TempISContinue {
		 boolean is注释=false;

		 String string=null;
		public TempISContinue(String string){
			this.string=string;
		}
	}
	public static class TempCodeHeight {

		Integer code_height=0;
	}
	/**
	 * 是否跳过该行，关于注释
	 */
	static boolean isContinueNote(TempISContinue tempISContinue) {

		while (true) {

			tempISContinue.string=StrUtil.trim(tempISContinue.string);
			char[] bytes1 = tempISContinue.string.toCharArray();
			if(bytes1.length==0){
				return true;
			}
			if(bytes1[0]=='/'&&bytes1[1]=='/'){ //单行注释
				return true;
			}
			if(tempISContinue.is注释){
				int i=0;
				for (;i<bytes1.length-1;i++) {
					if(bytes1[i]=='*'&&bytes1[i+1]=='/'){//多行注释结束
						tempISContinue.is注释=false;
						break;
					}
				}
				if(!tempISContinue.is注释){
					tempISContinue.string=tempISContinue.string.substring(i+2);
					continue;
				}
				return true;
			}

			if(bytes1[0]=='/'&&bytes1[1]=='*')
			{//多行注释开始
				boolean isStop=false;
				int i=0;
				for (;i<bytes1.length-1;i++) {
					if(bytes1[i]=='*'&&bytes1[i+1]=='/'){//多行注释结束
						isStop=true;
						break;
					}
				}
				if(!isStop){
					tempISContinue.is注释=true;
					return true;
				}
				tempISContinue.string=tempISContinue.string.substring(i+2);
				continue;
			}
			return false;
		}
	}

	private final  static void handleOneClassBean(ClassBean newBean, String string) {
		if (string.indexOf("public ") > -1) {
			newBean.setClass_scope(ClassBean.CLASS_SCOPE_PUBLIC);
		} else {
			newBean.setClass_scope(ClassBean.CLASS_SCOPE_DEFAULT);
		}
		newBean.setClass_is_final(string.indexOf("final ") > -1);
		newBean.setClass_is_static(string.indexOf("static ") > -1);
		newBean.setClass_is_extends(string.indexOf("extends ") > -1);
		newBean.setClass_is_implements(string.indexOf("implements ") > - 1);
	}
}

