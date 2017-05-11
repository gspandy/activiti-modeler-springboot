package com.activiti;

import java.util.stream.Stream;

import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.activiti.interceptor.LoginFilter;

@SpringBootApplication
@EnableAutoConfiguration(exclude = { SecurityAutoConfiguration.class })
public class ActivitiApplication implements CommandLineRunner {

	@Autowired
	private ApplicationContext applicationContext;

	public static void main(String[] args) {
		SpringApplication.run(ActivitiApplication.class, args);
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

	@Override
	public void run(String... args) throws Exception {
		String[] names = applicationContext.getBeanDefinitionNames();
		Stream.of(names).forEach(m -> {
			System.out.println(m + "==============================");
		});
	}

}
