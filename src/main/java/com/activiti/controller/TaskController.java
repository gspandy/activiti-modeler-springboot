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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

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
	@ResponseBody
	public Object getAllTask(HttpServletRequest request) {
		User user = (User) request.getSession().getAttribute("user");
		List<Task> doingTasks = this.taskService.createTaskQuery().taskAssignee(user.getUserId()).list();
		List<Task> waitingClaimTasks = this.taskService.createTaskQuery().taskCandidateUser(user.getUserId()).list();
		List<Task> list = new ArrayList<Task>();
		list.addAll(doingTasks);
		list.addAll(waitingClaimTasks);
		// 可以使用下面一句代替
		// list=this.taskService.createTaskQuery().taskCandidateOrAssigned(user.getUserId()).list();
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for (int i = 0; i < list.size(); i++) {
			Task m = list.get(i);
			sb.append("{\"id\":\"" + m.getId() + "\",\"name\":\"" + m.getName())
					.append("\",\"processInstanceId\":\"" + m.getProcessInstanceId())
					.append("\",\"createTime\":\"" + m.getCreateTime()).append("\",\"assignee\":\"" + m.getAssignee());
			sb.append("\"}");
			if (i < list.size() - 1) {
				sb.append(",");
			}
		}
		sb.append("]");
		return sb.toString();
	}

	@RequestMapping("/getForm")
	@ResponseBody
	public Object getForm(String taskId) {
		TaskFormData taskFormData = this.formService.getTaskFormData(taskId);
		String formKey = taskFormData.getFormKey();
		if (!"".equals(formKey)) {
			Object taskForm = this.formService.getRenderedTaskForm(taskId);
			return taskForm;
		}
		List<FormProperty> list = taskFormData.getFormProperties();
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for (int i = 0; i < list.size(); i++) {
			FormProperty m = list.get(i);
			sb.append("{\"id\":\"" + m.getId() + "\",\"name\":\"" + m.getName())
					.append("\",\"value\":\"" + m.getValue()).append("\",\"type\":\"" + m.getType().getName())
					.append("\",\"information\":\"" + m.getType().getInformation("values"))
					.append("\",\"readable\":\"" + m.isReadable()).append("\",\"writable\":\"" + m.isWritable());
			sb.append("\"}");
			if (i < list.size() - 1) {
				sb.append(",");
			}
			System.out.println(m.getType().getInformation("values"));
		}
		sb.append("]");
		return sb.toString();
	}

	@RequestMapping(value = "/complete", method = RequestMethod.POST)
	public Object completeTask(String taskId, HttpServletRequest request) {
		TaskFormData taskFormData = formService.getTaskFormData(taskId);
		Map<String, String> map = new HashMap<>();
		String formKey = taskFormData.getFormKey();
		if (!"".equals(formKey)) {
			Map<String, String[]> parameMap = request.getParameterMap();
			parameMap.forEach((m, n) -> {
				if (m.startsWith("fp_")) {
					map.put(m.split("_")[1], n[0]);
				}
			});

		} else {
			List<FormProperty> list = taskFormData.getFormProperties();
			for (FormProperty f : list) {
				if (f.isWritable()) {
					String value = request.getParameter(f.getId());
					map.put(f.getId(), value);
				}
			}
		}

		formService.submitTaskFormData(taskId, map);
		return "redirect:/home.html";
	}
}
