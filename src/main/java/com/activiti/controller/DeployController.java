package com.activiti.controller;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vaadin.terminal.StreamResource.StreamSource;

@RestController
@RequestMapping("/deploy")
public class DeployController implements ModelDataJsonConstants {

	protected static final Logger LOGGER = LoggerFactory.getLogger(DeployController.class);

	@Autowired
	private RepositoryService repositoryService;

	/**
	 * 根据model id部署
	 */
	@RequestMapping("/deploy")
	public Object deploy(String modelId) {
		try {
			Model modelData = repositoryService.getModel(modelId);
			ObjectNode modelNode = (ObjectNode) new ObjectMapper()
					.readTree(repositoryService.getModelEditorSource(modelData.getId()));
			byte[] bpmnBytes = null;
			BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
			bpmnBytes = new BpmnXMLConverter().convertToXML(model);
			String processName = modelData.getName() + ".bpmn20.xml";
			System.out.println(modelData.getName() + "======================");
			Deployment deployment = repositoryService.createDeployment().name(modelData.getName())
					.addString(processName, new String(bpmnBytes, "utf-8")).deploy();
			return deployment;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@RequestMapping("/list")
	public Object getDeploy() {
		List<ProcessDefinition> list2 = this.repositoryService.createProcessDefinitionQuery().list();
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for (int i = 0; i < list2.size(); i++) {
			ProcessDefinition m = list2.get(i);
			sb.append("{\"id\":\"" + m.getId() + "\",\"deploymentId\":\"" + m.getDeploymentId())
					.append("\",\"key\":\"" + m.getKey() + "\",\"name\":\"" + m.getName())
					.append("\",\"resourceName\":\"" + m.getResourceName())//
					.append("\",\"diagramResourceName\":\"" + m.getDiagramResourceName())
					.append("\",\"version\":\"" + m.getVersion());
			sb.append("\"}");
			if (i < list2.size() - 1) {
				sb.append(",");
			}
		}

		sb.append("]");
		return sb.toString();
	}

	@RequestMapping("/delete/{deploymentId}")
	public Object deleteDeploy(@PathVariable String deploymentId) {
		this.repositoryService.deleteDeployment(deploymentId, true);
		return true;
	}

	@RequestMapping("/showDeploySource")
	public Object showDeploySource(String deploymentId, String resourceName, HttpServletResponse response) {
		InputStream is = this.repositoryService.getResourceAsStream(deploymentId, resourceName);
		Reader reader = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(reader);// 缓冲流
		String str = null;
		StringBuffer sb = new StringBuffer();
		try {
			while ((str = br.readLine()) != null) {
				sb.append(str);
			}
			System.out.println(sb.toString());
			br.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return sb.toString();
	}

	@RequestMapping("/showDeploySourceByProcess")
	public Object showDeploySourceByProcess(String pdid, String resourceName, HttpServletResponse response)
			throws Exception {
		ProcessDefinition pd = this.repositoryService.createProcessDefinitionQuery().processDefinitionId(pdid)
				.singleResult();
		InputStream is = this.repositoryService.getResourceAsStream(pd.getDeploymentId(), resourceName);
		byte[] buffer = new byte[1024];
		int len = -1;
		while ((len = is.read(buffer, 0, 1024)) != -1) {
			response.getOutputStream().write(buffer, 0, len);
		}
		return null;
	}

	@RequestMapping("/showImage")
	public Object showImage(String deploymentId, String diagramResourceName, HttpServletResponse response) {
		InputStream is = this.repositoryService.getResourceAsStream(deploymentId, diagramResourceName);
		response.setContentType("application/x-msdownload;charset=UTF-8");
		response.reset();// 清除缓冲中的数据
		int temp;
		OutputStream os = null;
		try {
			os = response.getOutputStream();
			while ((temp = is.read()) != (-1)) {
				os.write(temp);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
				os.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return null;
	}
}
