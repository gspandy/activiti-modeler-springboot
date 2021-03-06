package com.activiti.conf;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.AbstractFormType;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.explorer.form.MonthFormType;
import org.activiti.explorer.form.ProcessDefinitionFormType;
import org.activiti.explorer.form.UserFormType;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ActivitiEngineConfiguration {

	@Autowired
	protected Environment environment;
	@Autowired
	protected DataSource dataSource;
	@Autowired
	protected PlatformTransactionManager transactionManager;

	@Bean(name = "processEngineFactoryBean")
	public ProcessEngineFactoryBean processEngineFactoryBean() {
		ProcessEngineFactoryBean factoryBean = new ProcessEngineFactoryBean();
		factoryBean.setProcessEngineConfiguration(processEngineConfiguration());
		return factoryBean;
	}

	@Bean(name = "processEngine")
	public ProcessEngine processEngine() {
		// Safe to call the getObject() on the @Bean annotated
		// processEngineFactoryBean(), will be
		// the fully initialized object instanced from the factory and will NOT
		// be created more than once
		try {
			return processEngineFactoryBean().getObject();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Bean(name = "processEngineConfiguration")
	public ProcessEngineConfigurationImpl processEngineConfiguration() {
		SpringProcessEngineConfiguration processEngineConfiguration = new SpringProcessEngineConfiguration();
		processEngineConfiguration.setDataSource(dataSource);
		processEngineConfiguration.setDatabaseSchemaUpdate(environment.getProperty("engine.schema.update", "true"));
		processEngineConfiguration.setTransactionManager(transactionManager);
		processEngineConfiguration.setJobExecutorActivate(
				Boolean.valueOf(environment.getProperty("engine.activate.jobexecutor", "false")));
		processEngineConfiguration.setAsyncExecutorEnabled(
				Boolean.valueOf(environment.getProperty("engine.asyncexecutor.enabled", "true")));
		processEngineConfiguration.setAsyncExecutorActivate(
				Boolean.valueOf(environment.getProperty("engine.asyncexecutor.activate", "true")));
		processEngineConfiguration.setHistory(environment.getProperty("engine.history.level", "full"));
		// 支持中文图片
		processEngineConfiguration.setActivityFontName("微软雅黑");
		processEngineConfiguration.setLabelFontName("微软雅黑");

		// 邮箱
		processEngineConfiguration.setMailServerHost("smtp.qq.com");
		processEngineConfiguration.setMailServerPort(465);
		processEngineConfiguration.setMailServerDefaultFrom("352004760@qq.com");
		processEngineConfiguration.setMailServerUsername("352004760@qq.com");
		processEngineConfiguration.setMailServerPassword("*****");
		processEngineConfiguration.setMailServerUseSSL(true);
		String mailEnabled = environment.getProperty("engine.email.enabled");
		if ("true".equals(mailEnabled)) {
			processEngineConfiguration.setMailServerHost(environment.getProperty("engine.email.host"));
			int emailPort = 1025;
			String emailPortProperty = environment.getProperty("engine.email.port");
			if (StringUtils.isNotEmpty(emailPortProperty)) {
				emailPort = Integer.valueOf(emailPortProperty);
			}
			processEngineConfiguration.setMailServerPort(emailPort);
			String emailUsernameProperty = environment.getProperty("engine.email.username");
			if (StringUtils.isNotEmpty(emailUsernameProperty)) {
				processEngineConfiguration.setMailServerUsername(emailUsernameProperty);
			}

			String emailPasswordProperty = environment.getProperty("engine.email.password");
			if (StringUtils.isNotEmpty(emailPasswordProperty)) {
				processEngineConfiguration.setMailServerPassword(emailPasswordProperty);
			}
		}

		List<AbstractFormType> formTypes = new ArrayList<AbstractFormType>();
		formTypes.add(new UserFormType());
		formTypes.add(new ProcessDefinitionFormType());
		formTypes.add(new MonthFormType());
		processEngineConfiguration.setCustomFormTypes(formTypes);

		return processEngineConfiguration;
	}

	@Bean
	public RepositoryService repositoryService() {
		return processEngine().getRepositoryService();
	}

	@Bean
	public RuntimeService runtimeService() {
		return processEngine().getRuntimeService();
	}

	@Bean
	public TaskService taskService() {
		return processEngine().getTaskService();
	}

	@Bean
	public HistoryService historyService() {
		return processEngine().getHistoryService();
	}

	@Bean
	public FormService formService() {
		return processEngine().getFormService();
	}

	@Bean
	public IdentityService identityService() {
		return processEngine().getIdentityService();
	}

	@Bean
	public ManagementService managementService() {
		return processEngine().getManagementService();
	}
}
