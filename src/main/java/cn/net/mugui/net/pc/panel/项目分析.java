package cn.net.mugui.net.pc.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mugui.Dui.DButton;
import com.mugui.Dui.DTextField;
import com.mugui.base.base.Autowired;

import cn.net.mugui.net.pc.manager.FunctionUI;
import cn.net.mugui.net.pc.task.ProjectAnalyzeTask;
import cn.net.mugui.net.pc.util.WindowsFileUtil;

@SuppressWarnings("serial")
//@Component
public class 项目分析 extends FunctionUI {

	public 项目分析() {
		setTitle("项目分析");
		setMenu_name("项目分析");
		setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		add(panel, BorderLayout.NORTH);

		JLabel lblNewLabel = new JLabel("项目路径：");
		panel.add(lblNewLabel);

		DTextField textField = new DTextField(1024);
		textField.setColumns(50);
		panel.add(textField);

		DButton button = new DButton("选择文件", (Color) null);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textField.setText("");
				String open = windowsFileUtil.openDirectory();
				if (open != null) {
					File select_file = new File(open);
					if (!select_file.isDirectory()) {
						return;
					}
					textField.setText(select_file.getAbsolutePath());
				}
			}
		});
		panel.add(button);

		DButton button_1 = new DButton("开始分析", (Color) null);
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				projectAnalyzeTask.add(textField.getText());
			}
		});
		panel.add(button_1);

		JLabel lblNewLabel_1 = new JLabel("项目分析");
		panel.add(lblNewLabel_1);

		JPanel panel_1 = new JPanel();
		add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));

		JPanel panel_2 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		panel_1.add(panel_2, BorderLayout.NORTH);

		rate = new JLabel("正在分析：");
		panel_2.add(rate);


	}

	public void showRateLog(String msg) {
		rate.setText("正在分析：" + msg);
	}

	@Autowired
	private ProjectAnalyzeTask projectAnalyzeTask;

	@Autowired
	private WindowsFileUtil windowsFileUtil;
	private JLabel rate;

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
