package uk.co.eelpieconsulting.feedlistener.instagram.callbacks;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class InstagramCallbackController {
	
	private static Logger log = Logger.getLogger(InstagramCallbackController.class);

	@RequestMapping(value="/instagram/callback", method=RequestMethod.GET)
	public ModelAndView subscribeCallback(@RequestParam(value="hub.mode", required=false) String hubMode,
			@RequestParam(value="hub.challenge", required=false) String hubChallenge,
			@RequestParam(value="hub.verify_token", required=false) String hubToken,
			HttpServletResponse response) throws IOException {
		
		log.info("Received callback: " + hubMode + ", " + hubChallenge + ", " + hubToken);
		response.getOutputStream().print(hubChallenge);
		response.flushBuffer();
		return null;
	}
	
	@RequestMapping(value="/instagram/callback", method=RequestMethod.POST)
	public ModelAndView dataCallback(@RequestBody String body) throws IOException {		
		log.info("Received post: " + body);		
		return null;
	}
	
}
