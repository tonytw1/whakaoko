package uk.co.eelpieconsulting.feedlistener;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import uk.co.eelpieconsulting.common.views.ViewFactory;

@Component
public class ExceptionHandler implements HandlerExceptionResolver, Ordered  {

	private final Logger log = Logger.getLogger(ExceptionHandler.class);

	private final ViewFactory viewFactory;
	
	@Autowired
	public ExceptionHandler(ViewFactory viewFactory) {
		this.viewFactory = viewFactory;
	}

	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception e) {
		if (e instanceof UnknownSubscriptionException) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
			return new ModelAndView(viewFactory.getJsonView()).addObject("data", "Not found");
		}

		log.error("Returing unexpected 500 error", e);
		return new ModelAndView(viewFactory.getJsonView()).addObject("data", "500");
	}

	@Override
	public int getOrder() {
        return Integer.MIN_VALUE;
	}

}