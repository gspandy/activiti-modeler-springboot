package com.activiti.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.engine.FormService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.activiti.model.User;

@Controller
@RequestMapping("/task")
public class TaskController implements ModelDataJsonConstants {

	protected static final Logger LOGGER = LoggerFactory.getLogger(TaskController.class);

	@Autowired
	private TaskService taskService;
	@Autowired
	private FormService formService;

	// 查询所有任务根据登录人
	@RequestMapping("/getAllTask")
	public Object getAllTask(HttpServletRequest request) {
		User user = (User) request.getSession().getAttribute("user");
		List<Task> doingTasks = this.taskService.createTaskQuery().taskAssignee(user.getUserId()).list();
		List<Task> waitingClaimTasks = this.taskService.createTaskQuery().taskCandidateUser(user.getUserId()).list();
		List<Task> list = new ArrayList<Task>();
		list.addAll(doingTasks);
		list.addAll(waitingClaimTasks);
		// 可以使用下面一句代替
		// list=this.taskService.createTaskQuery().taskCandidateOrAssigned(user.getUserId()).list();
		return list;
	}

	@RequestMapping("/getForm")
	public Object getForm(String taskId) {
		TaskFormData taskFormData = this.formService.getTaskFormData(taskId);
		List<FormProperty> list = taskFormData.getFormProperties();
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for (int i = 0; i < list.size(); i++) {
			FormProperty m = list.get(i);
			sb.append("{\"id\":\"" + m.getId() + "\",\"name\":\"" + m.getName())
					.append("\",\"value\":\"" + m.getValue());
			sb.append("\"}");
			if (i < list.size() - 1) {
				sb.append(",");
			}
		}
		sb.append("]");
		return sb.toString();
	}

	@RequestMapping("/complete")
	public Object completeTask(String taskId, HttpServletRequest request) {
		TaskFormData taskFormData = formService.getTaskFormData(taskId);
		List<FormProperty> list = taskFormData.getFormProperties();
		Map<String, String> map = new HashMap<>();
		for (FormProperty f : list) {
			if (f.isWritable()) {
				String value = request.getParameter(f.getName());
				map.put(f.getId(), value);
			}
		}
		formService.submitTaskFormData(taskId, map);
		return "OK";
	}
}
