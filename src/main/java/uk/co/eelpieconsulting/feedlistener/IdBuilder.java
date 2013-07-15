package uk.co.eelpieconsulting.feedlistener;

import org.springframework.stereotype.Component;

@Component
public class IdBuilder {

	public String makeIdFor(String text) {
		String result = text.toLowerCase().trim().replaceAll("\\s", "-");
		result = result.replaceAll("[^\\-a-z0-9_]", "");
		result = result.replaceAll("--+", "-");
		return result;
	}

}
