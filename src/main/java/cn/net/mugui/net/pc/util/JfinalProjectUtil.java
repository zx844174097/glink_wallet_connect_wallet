package cn.net.mugui.net.pc.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.net.mugui.net.pc.bean.ClassAttrBean;
import cn.net.mugui.net.pc.bean.ClassBean;
import cn.net.mugui.net.pc.bean.ProjectBean;
import cn.net.mugui.net.pc.dao.Sql;
import com.mugui.base.base.Autowired;
import com.mugui.base.base.Component;

import java.util.List;

/**
 * Jfinal项目控制层分析
 */
@Component
public class JfinalProjectUtil {

	@Autowired
	private Sql dao;


	/**
	 * 得到jfinal 的控制层分析
	 * @param project
	 */
	public  void controllerAll(ProjectBean project) {
		List<ClassBean> classBeans = dao.selectList(new ClassBean().setProject_id(project.getProject_id()).setClass_type(ClassBean.CLASS_TYPE_DEFAULT).setClass_scope(ClassBean.CLASS_SCOPE_PUBLIC).setClass_is_static(false).setClass_is_extends(true));
		if(classBeans.isEmpty()){
			return;
		}
		for (ClassBean classBean : classBeans) {

			ProjectUtil.TempISContinue tempISContinue=new ProjectUtil.TempISContinue("");
			tempISContinue.is注释=false;
			//代码深度
			ProjectUtil.TempCodeHeight tempCodeHeight=new ProjectUtil.TempCodeHeight();


			String[] split = StrUtil.split(classBean.getClass_file_content(), "\r\n");
			ClassAttrBean classAttrBean=null;

			dao.updateSql("delete from `class_attr` where class_id=?",classBean.getClass_id());

			for (int index=0; index<split.length;index++) {
				String string=split[index];
				//跳过注释
				tempISContinue.string=string;
				if(ProjectUtil.isContinueNote(tempISContinue)){
					continue;
				}
				string=tempISContinue.string;

				if(tempCodeHeight.code_height==0){
					if(string.contains("@ControllerBind")){//拥有控制层
						classAttrBean=new ClassAttrBean();
						classAttrBean.setMethod_type(ClassAttrBean.METHOD_TYPE_JFINAL_CONTROLLER);
						classAttrBean.setClass_id(classBean.getClass_id());
						int start = string.indexOf("\"");
						int end=string.lastIndexOf("\"");
						classAttrBean.setMethod_type_extra(string.substring(start+1, end));
					}
				}else if(tempCodeHeight.code_height==1){
					if(classAttrBean!=null){
						if(StrUtil.trim(string).startsWith("@")){//是一个注解
							continue;
						}
						String method_scope=ClassAttrBean.METHOD_SCOPE_DEFAULT;
						if(StrUtil.trim(string).startsWith("public ") ){
							method_scope=ClassAttrBean.METHOD_SCOPE_PUBLIC;
						}else if(StrUtil.trim(string).startsWith("private ") ){
							method_scope=ClassAttrBean.METHOD_SCOPE_PRIVATE;
						}else if(StrUtil.trim(string).startsWith("protected ")){
							method_scope=ClassAttrBean.METHOD_SCOPE_PROTECTED;
						}
						boolean method_is_static=false;
						string=StrUtil.trim(string).replace(method_scope,"");
						if(StrUtil.trim(string).startsWith("static ")){
							method_is_static=true;
						}
						if(method_is_static)
							string=StrUtil.trim(string).replace("static ","");
						if(StrUtil.trim(string).contains("(")){//是一个方法
							int i = string.indexOf("(");
							string=string.substring(0,i);
							String[] split1 = StrUtil.trim(string).split("\\s+");
							if(split1.length ==2){
								ClassAttrBean one = ClassAttrBean.newBean(classAttrBean);
								one.setMethod_name(split1[1]);
								one.setMethod_scope(method_scope);
								one.setMethod_is_static(method_is_static);
								if(method_scope.equals(ClassAttrBean.METHOD_SCOPE_PUBLIC)&&!one.getMethod_is_static()&&split1[0].equals("void")){

									one.setMethod_type_extra(one.getMethod_type_extra()+"/"+one.getMethod_name());
									one.setMethod_type(ClassAttrBean.METHOD_TYPE_JFINAL_CONTROLLER);
								}else {
									one.setMethod_type(ClassAttrBean.METHOD_TYPE_DEFAULT);
								}
								dao.save(one);
							}
						}
					}
				}
				ProjectUtil.handleCodeHeight(tempISContinue.string,tempCodeHeight);
			}
		}
	}
}
