package com.activiti.controller;

import java.util.List;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;

/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Tijs Rademakers
 */
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
		List<Deployment> list = this.repositoryService.createDeploymentQuery().list();
		for (Deployment d : list) {
			System.out.println(d.getId() + "===============");
			System.out.println(d.getName());
			System.out.println(d.getDeploymentTime());
		}
		List<ProcessDefinition> list2 = this.repositoryService.createProcessDefinitionQuery().list();
		list2.forEach(m -> {
			System.out.println(m.getId());
			System.out.println(m.getDeploymentId());
			System.out.println(m.getKey());
		});
		return list2;
	}
}