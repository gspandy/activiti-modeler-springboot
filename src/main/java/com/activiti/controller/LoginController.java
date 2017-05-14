package com.activiti.controller;

import javax.servlet.http.HttpSession;

import org.activiti.editor.constants.ModelDataJsonConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.activiti.model.User;

@Controller
@RequestMapping("/login")
public class LoginController implements ModelDataJsonConstants {

	protected static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

	/**
	 * 根据model id部署
	 */
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public Object login(String username, String password, HttpSession session) {
		if (!"".equals(username) && username != null) {
			if (username.equals(password)) {
				User user = new User();
				user.setUserName(username);
				user.setPassWord(password);
				user.setUserId(username);
				User user2 = (User) session.getAttribute("user");
				if (user2 == null) {
					session.setAttribute("user", user);
				}
				return "redirect:/home.html";
			}
		}
		return "redirect:/index.html";
	}

	/**
	 * 根据model id部署
	 */
	@RequestMapping(value = "/logout")
	public Object logout(HttpSession session) {
		session.setMaxInactiveInterval(0);
		session.invalidate();
		return "redirect:/index.html";
	}

	/**
	 * 根据model id部署
	 */
	@RequestMapping(value = "/getSession")
	@ResponseBody
	public Object getSession(HttpSession session) {
		User user = (User) session.getAttribute("user");
		return user;
	}
}
