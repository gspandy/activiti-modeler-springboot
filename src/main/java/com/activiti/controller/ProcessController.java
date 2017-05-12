package com.activiti.controller;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.engine.FormService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.activiti.model.User;

@Controller
@RequestMapping("/process")
public class ProcessController implements ModelDataJsonConstants {

	protected static final Logger LOGGER = LoggerFactory.getLogger(ProcessController.class);

	@Autowired
	private FormService formService;
	@Autowired
	private IdentityService identityService;
	@Autowired
	private RepositoryService repositoryService;

	/**
	 * 开启流程
	 */
	@RequestMapping("/start")
	public Object start(String processDefinitionId, HttpServletRequest request) {
		StartFormData formData = formService.getStartFormData(processDefinitionId);
		String formKey = formData.getFormKey();
		Map<String, String> map = new HashMap<>();
		if (!"".equals(formKey)) {
			Map<String, String[]> parameterMap = request.getParameterMap();
			parameterMap.forEach((m, n) -> {
				if (m.startsWith("fp_")) {
					map.put(m.split("_")[1], n[0]);
				}
			});
		} else {
			List<FormProperty> list = formData.getFormProperties();
			list.forEach(m -> {
				String value = request.getParameter(m.getId());
				map.put(m.getId(), value);
			});
		}

		User user = (User) request.getSession().getAttribute("user");
		identityService.setAuthenticatedUserId(user.getUserId());
		ProcessInstance processInstance = formService.submitStartFormData(processDefinitionId, map);
		System.out.println(processInstance.getId() + "====processInstance.getId=========");
		return "redirect:/home.html";
	}

	/**
	 * 获取表单数据
	 * 
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping("/getForm")
	@ResponseBody
	public Object getForm(String processDefinitionId) throws UnsupportedEncodingException {
		ProcessDefinition processDefinition = this.repositoryService.createProcessDefinitionQuery()
				.processDefinitionId(processDefinitionId).singleResult();
		boolean hasStartFormKey = processDefinition.hasStartFormKey();
		if (hasStartFormKey) {
			Object renderedStartForm = formService.getRenderedStartForm(processDefinitionId);
			return renderedStartForm;
		} else {
			StartFormData startFormData = this.formService.getStartFormData(processDefinitionId);
			List<FormProperty> list = startFormData.getFormProperties();
			StringBuffer sb = new StringBuffer();
			sb.append("[");
			for (int i = 0; i < list.size(); i++) {
				FormProperty m = list.get(i);
				sb.append("{\"id\":\"" + m.getId() + "\",\"name\":\"" + m.getName())
						.append("\",\"value\":\"" + m.getValue()).append("\",\"type\":\"" + m.getType().getName());
				sb.append("\"}");
				if (i < list.size() - 1) {
					sb.append(",");
				}
			}
			sb.append("]");
			return sb.toString();
		}

	}
}
