package cn.net.mugui.net.pc.bean;

import com.mugui.bean.JsonBean;
import com.mugui.sql.SQLDB;
import com.mugui.sql.SQLField;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * class 属性表
 */
@Getter
@Setter
@Accessors(chain = true)
@SQLDB(TABLE = "class_attr",KEY = "class_attr_id")
public class ClassAttrBean extends JsonBean {

	@SQLField(AUTOINCREMENT = true,PRIMARY_KEY = true,AUTOINCREMENT_value = "AUTOINCREMENT")
	private Integer class_attr_id;

	@SQLField(NULL = false)
	private Integer class_id;

	/**
	 * 方法名
	 */
	@SQLField(DATA_TYPE = "varchar(256)")
	private  String method_name;

	/**
	 * jfinal 网络通信控制层
	 */
	public static final String METHOD_TYPE_JFINAL_CONTROLLER="jfinal_controller";


	/**
	 * 一般方法
	 */
	public static final String METHOD_TYPE_DEFAULT="default";


	/**
	 * spring 网络通信控制层
	 */
	public static final String METHOD_TYPE_SPRING_CONTROLLER="spring_controller";

	/**
	 * 方法类型
	 */
	@SQLField(DATA_TYPE = "varchar(32)")
	private String method_type;

	/**
	 * 方法类型额外描述
	 */
	@SQLField(DATA_TYPE = "TEXT")
	private String method_type_extra;

	/**
	 * 表明该成员变量或方法对所有类或对象都是可见的，所有类或对象都可以直 接访问。
	 */
	public static final String METHOD_SCOPE_PUBLIC="public";

	/**
	 * 表明该成员变量或方法是私有的，只有当前类对其具有访问权限，除此之外 的其他类或者对象都没有访问权限。
	 */
	public static final String METHOD_SCOPE_PRIVATE="private";

	/**
	 * 表明该成员变量或方法对自己及其子类是可见的，即自己和子类具有权限 访问。除此之外的其他类或对象都没有访问权限。
	 */
	public static final String METHOD_SCOPE_PROTECTED="protected";

	/**
	 * 表明该成员变量或方法只有自己和与其位于同一包内的类可见。若父类与子 类位于同一个包内，则子类对父类的default成员变量或方法都有访问权限;若父类与子类位 于不同的package (包)内，则没有访问权限。
	 */
	public static final String METHOD_SCOPE_DEFAULT="default";

	/**
	 * 方法作用域
	 */
	@SQLField(DATA_TYPE = "varchar(12)")
	private String method_scope;
	

	@SQLField
	private Boolean method_is_static;

	@SQLField
	private Boolean method_is_final;



	/**
	 * 入口参数
	 */
	@SQLField(DATA_TYPE = "TEXT")
	private String entry_par;

	/**
	 * 出口参数
	 */
	@SQLField(DATA_TYPE = "TEXT")
	private String export_par;


	
}
