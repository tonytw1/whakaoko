package uk.co.eelpieconsulting.feedlistener;

import org.springframework.stereotype.Component;

@Component
public class IdBuilder {

    public String makeIdFor(String text) {
        return text.toLowerCase().trim().
                replaceAll("\\s", "-").
                replaceAll("[^\\-a-z0-9_]", "").
                replaceAll("--+", "-");
    }

}
