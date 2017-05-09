package com.activiti.interceptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.activiti.model.User;

public class LoginFilter implements Filter {
	private List<String> ignoreList = new ArrayList<String>();

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		String paramName = filterConfig.getInitParameter("paramName");
		System.out.println(paramName + "====paramName==================");
		ignoreList.add("/bootstrap");
		ignoreList.add("/diagram-viewer");
		ignoreList.add("/editor-app");
		ignoreList.add("/index.html");
		ignoreList.add("/login");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest hrequest = (HttpServletRequest) request;
		HttpServletResponse hresponse = (HttpServletResponse) response;
		String contextPath = hrequest.getContextPath();
		String path = hrequest.getRequestURI().substring(contextPath.length());
		int indexSlash = path.indexOf("/", 1);
		String firstPart = null;
		if (indexSlash > 0) {
			firstPart = path.substring(0, indexSlash);
		} else {
			firstPart = path;
		}
		if (ignoreList.contains(firstPart)) {
			chain.doFilter(request, response);
		} else {
			HttpSession session = hrequest.getSession();
			User user = (User) session.getAttribute("user");
			if (user == null) {
				hresponse.sendRedirect("/index.html");
			} else {
				chain.doFilter(request, response);
			}
		}
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}
