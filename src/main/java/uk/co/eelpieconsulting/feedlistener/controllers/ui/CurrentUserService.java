package uk.co.eelpieconsulting.feedlistener.controllers.ui;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserService {

	private final HttpServletRequest request;

	@Autowired
	public CurrentUserService(HttpServletRequest request) {
		this.request = request;
	}
	
	public String getCurrentUser() {
		return request.getRequestURI().split("/")[2];
	}
	
}
