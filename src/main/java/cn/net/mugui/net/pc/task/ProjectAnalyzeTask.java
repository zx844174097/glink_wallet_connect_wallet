package cn.net.mugui.net.pc.task;


import java.io.File;
import java.io.FilenameFilter;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import cn.net.mugui.net.pc.bean.ClassAttrBean;
import cn.net.mugui.net.pc.bean.ClassBean;
import cn.net.mugui.net.pc.bean.ProjectBean;
import cn.net.mugui.net.pc.dao.Sql;
import cn.net.mugui.net.pc.util.JfinalProjectUtil;
import cn.net.mugui.net.pc.util.ProjectUtil;
import com.mugui.base.base.Autowired;
import com.mugui.base.base.Component;
import com.mugui.base.client.net.auto.AutoTask;
import com.mugui.base.client.net.base.Task;
import com.mugui.base.client.net.task.TaskCycleImpl;
import com.mugui.bean.JsonBean;

import cn.hutool.core.thread.ThreadUtil;
import cn.net.mugui.net.pc.panel.项目分析;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 工程分析任务
 *
 * @author root
 */
@Component
@AutoTask
@Task
public class ProjectAnalyzeTask extends TaskCycleImpl<cn.net.mugui.net.pc.task.ProjectAnalyzeTask.TempBean> {

	@Autowired
	private 项目分析 handle;

	public void add(String file) {
		File file1 = new File(file.toString());
		if (!file1.isDirectory()) {
			return;
		}
		add(new TempBean().setFile(file1).setType(TempBean.type_0));
	}

	@Override
	public void add(TempBean data) {

		super.add(data);
	}

	@Getter
	@Setter
	@Accessors(chain = true)
	static class TempBean extends JsonBean {

		/**
		 * 目录分析
		 */
		static final int type_0 = 0;

		/**
		 * 项目初始分析
		 */
		static final int type_1 = 1;
		/**
		 * 项目接口分析
		 */
		static final int type_2 = 2;

		/**
		 * 任务类型
		 */
		private Integer type;
		/**
		 * 任务实体
		 */
		private JsonBean obj;
		/**
		 * 所在文件
		 */
		private File file;

	}

	ThreadPoolExecutor newExecutor = null;

	@Override
	public void init() {
		super.init();
		dao.createTable(ProjectBean.class);
		dao.createTable(ClassBean.class);
		dao.createTable(ClassAttrBean.class);
		ThreadFactory build = ThreadUtil.createThreadFactoryBuilder().setNamePrefix("analyze").build();
		newExecutor = ThreadUtil.newExecutor(2, 8);
		newExecutor.setThreadFactory(build);
	}

	@Override
	protected void handle(TempBean poll) {
		if (newExecutor.getActiveCount() > newExecutor.getCorePoolSize() / 2) {
			newExecutor.setCorePoolSize(newExecutor.getCorePoolSize() * 2);
		} else if (newExecutor.getCorePoolSize() > 8 && newExecutor.getActiveCount() < newExecutor.getCorePoolSize() / 4) {
			newExecutor.setCorePoolSize(newExecutor.getCorePoolSize() / 2);
		}
		newExecutor.execute(new Runnable() {
			@Override
			public void run() {
				switch (poll.type) {
					case TempBean.type_0:
						analyzeDir(poll);
						break;
					case TempBean.type_1:
						analyzePoject(poll);
						break;

					case TempBean.type_2:
						analyzeApi(poll);
						break;
					default:
						break;
				}
			}
		});
	}
	@Autowired
	JfinalProjectUtil jfinalProjectUtil;
	/**
	 * 分析控制层接口
	 * @param poll
	 */
	private void analyzeApi(TempBean poll) {
		ProjectBean project= (cn.net.mugui.net.pc.bean.ProjectBean) poll.getObj();
		handle.showRateLog("analyzeApi->"+project.getProject_file_path());
		switch (project.getProject_type()) {
			case ProjectBean.PROJECT_TYPE_0:
				break;
			case ProjectBean.PROJECT_TYPE_1:
				jfinalProjectUtil.controllerAll(project);
				break;
			case ProjectBean.PROJECT_TYPE_e:
				break;
		}
	}

	@Autowired
	private Sql dao;

	/**
	 * 分析项目
	 *
	 * @param poll
	 */
	private void analyzePoject(TempBean poll) {
		handle.showRateLog("analyzePoject->" + poll.file.getAbsolutePath());
		ProjectBean projectBean = new ProjectBean().setProject_file_path(poll.file.getAbsolutePath());
		ProjectUtil.handleRemoteUrl(poll.file, projectBean);
		ProjectUtil.handlePomXml(poll.file, projectBean);
		ProjectBean select = dao.select(new ProjectBean().setProject_name(projectBean.getProject_name()));
		if (select != null) {
			select.setProject_file_path(projectBean.getProject_file_path());
			select.setProject_type(projectBean.getProject_type());
			select.setProject_remote_url(projectBean.getProject_remote_url());
		} else {
			select = dao.save(projectBean);
		}
		dao.updata(select);
		//得到源文件目录
		File srcUrl = ProjectUtil.getSrcUrl(select.getProject_file_path());
		if(srcUrl==null||!srcUrl.exists()){
			handle.showRateLog("analyzeApi srcUrl-> NULL" );
			throw new RuntimeException("analyzeApi srcUrl-> NULL");
		}
		LinkedList<ClassBean> classes = new LinkedList<ClassBean>();
		//读取所有的类
		ProjectUtil.readAllClasses(srcUrl,classes,handle);
		dao.updateSql("delete from `class` where project_id=?",select.getProject_id());
		for (ClassBean aClass : classes) {
			handle.showRateLog("analyzePoject classbean->" + aClass.getClass_file());
			aClass.setProject_id(select.getProject_id());
			dao.save(aClass);
		}
		add(new TempBean().setObj(select).setType(TempBean.type_2));
	}

	/**
	 * 分析目录
	 *
	 * @param poll
	 */
	private void analyzeDir(TempBean poll) {
		handle.showRateLog(poll.file.getAbsolutePath());
		// 判断当前是否拥有pom.xml 文件
		File file = poll.file;
		File file2 = new File(file, "pom.xml");
		if (file2.exists()) {
			add(new TempBean().setFile(file).setType(TempBean.type_1));
			return;
		}
		file.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				File file1 = new File(dir, name);
				if (file1.isDirectory()) {
					if (new File(file1, "pom.xml").exists()) {
						add(new TempBean().setFile(file1).setType(TempBean.type_1));
						return false;
					}
					add(new TempBean().setFile(file1).setType(TempBean.type_0));
				}
				return false;
			}
		});

	}

}
