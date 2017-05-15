package com.activiti.conf;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.Filter;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.activiti.interceptor.LoginFilter;

@Configuration
public class ApplicationConfiguration {

	public ApplicationConfiguration(Environment environment, SqlSessionFactory sqlSessionFactory) {
		String flag = environment.getProperty("create.new.table");
		if ("true".equals(flag)) {
			CreateCustomerTable c = new CreateCustomerTable(sqlSessionFactory);
			try {
				c.executeSchemaResource(null);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 配置过滤器
	 * 
	 * @return
	 */
	@Bean
	public FilterRegistrationBean someFilterRegistration() {
		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setFilter(LoginFilter());
		registration.addUrlPatterns("/*");
		registration.addInitParameter("paramName", "paramValue");
		registration.setName("loginFilter");
		return registration;
	}

	/**
	 * 创建一个bean
	 * 
	 * @return
	 */
	@Bean(name = "loginFilter")
	public Filter LoginFilter() {
		return new LoginFilter();
	}

}
