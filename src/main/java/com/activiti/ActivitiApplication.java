package com.activiti;

import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
@EnableAutoConfiguration(exclude = { SecurityAutoConfiguration.class })
public class ActivitiApplication implements CommandLineRunner {
	
	@Autowired
	private ApplicationContext applicationContext;
	

	public static void main(String[] args) {

		SpringApplication.run(ActivitiApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		
		String[] names = applicationContext.getBeanDefinitionNames();
		Stream.of(names).forEach(m -> {
			System.out.println(m + "==============================");
		});
	}

}
