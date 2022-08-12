package cn.net.mugui.net.pc.bean;

import com.mugui.bean.JsonBean;
import com.mugui.sql.SQLDB;
import com.mugui.sql.SQLField;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 类属性
 *
 * @author Administrator
 *
 */
@SuppressWarnings("serial")
@Getter
@Setter
@Accessors(chain = true)
@SQLDB(TABLE = "class", KEY = "class_id")
public class ClassBean extends JsonBean {

	@SQLField(AUTOINCREMENT = true,PRIMARY_KEY = true,AUTOINCREMENT_value = "AUTOINCREMENT")
	private Integer class_id;

	@SQLField(NULL = false)
	private Integer project_id;

	@SQLField(DATA_TYPE = "varchar(256)")
	private String class_name;

	@SQLField(DATA_TYPE = "varchar(256)")
	private String package_name;

	@SQLField(DATA_TYPE = "varchar(1024)")
	private String class_file;

	/**
	 * 表明该成员变量或方法对所有类或对象都是可见的，所有类或对象都可以直 接访问。
	 */
	public static final String CLASS_SCOPE_PUBLIC="public";


	/**
	 * 表明该成员变量或方法是私有的，只有当前类对其具有访问权限，除此之外 的其他类或者对象都没有访问权限。
	 */
	public static final String CLASS_SCOPE_PRIVATE="private";

	/**
	 * 表明该成员变量或方法对自己及其子类是可见的，即自己和子类具有权限 访问。除此之外的其他类或对象都没有访问权限。
	 */
	public static final String CLASS_SCOPE_PROTECTED="protected";

	/**
	 * 表明该成员变量或方法只有自己和与其位于同一包内的类可见。若父类与子 类位于同一个包内，则子类对父类的default成员变量或方法都有访问权限;若父类与子类位 于不同的package (包)内，则没有访问权限。
	 */
	public static final String CLASS_SCOPE_DEFAULT="default";

	/**
	 * 类作用域
	 */
	@SQLField(DATA_TYPE = "varchar(12)")
	private String class_scope;

	/**
	 * 是否静态类
	 */
	@SQLField()
	private Boolean class_is_static;


	/**
	 * 是否不可改变
	 */
	@SQLField()
	private Boolean class_is_final;


	/**
	 * 是否有继承
	 */
	@SQLField()
	private Boolean class_is_extends;


	/**
	 * 是否有实现
	 */
	@SQLField()
	private Boolean class_is_implements;


	public  static final String CLASS_TYPE_DEFAULT="default";

	/**
	 * 接口类
	 */
	public static final String CLASS_TYPE_INTERFACE="interface";

	/**
	 * 抽象类
	 */
	public static final String CLASS_TYPE_ABSTRACT="abstract";

	@SQLField(DATA_TYPE = "varchar(12)")
	private  String class_type;


	@SQLField(DATA_TYPE = "TEXT")
	private String class_file_content;

}
