package com.activiti.controller;

import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.engine.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/process")
public class ProcessController implements ModelDataJsonConstants {

	protected static final Logger LOGGER = LoggerFactory.getLogger(ProcessController.class);

	@Autowired
	private RepositoryService repositoryService;

	/**
	 * 根据model id部署
	 */
	@RequestMapping("/start/{processDefinitionId}")
	public Object start(@PathVariable String processDefinitionId) {

		return null;
	}

}
