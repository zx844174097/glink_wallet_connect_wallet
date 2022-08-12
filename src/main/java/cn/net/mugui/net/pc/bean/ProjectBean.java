package cn.net.mugui.net.pc.bean;

import com.mugui.bean.JsonBean;
import com.mugui.sql.SQLDB;
import com.mugui.sql.SQLField;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@SQLDB(TABLE = "project",KEY = "project_id")
public class ProjectBean extends JsonBean {


	@SQLField(AUTOINCREMENT = true,PRIMARY_KEY = true,AUTOINCREMENT_value = "AUTOINCREMENT")
	private Integer project_id;

	/**
	 * 未知类型
	 */
	public static final String PROJECT_TYPE_e="NONE";

	public static final String PROJECT_TYPE_0="spring";

	public static final String PROJECT_TYPE_1="jfinal";

	@SQLField
	private String project_type;

	@SQLField
	private String project_name;

	@SQLField(DATA_TYPE = "varchar(1024)")
	private String project_file_path;

	@SQLField(DATA_TYPE = "varchar(1024)")
	private String project_remote_url;

}
