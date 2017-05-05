package com.activiti.controller;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

	@Autowired
	private DataSource dataSource;

	@RequestMapping("/test")
	public Object test(String name) {
		System.out.println(dataSource + "===========================");
		return "OK  " + name;
	}
}
