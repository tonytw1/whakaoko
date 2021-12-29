package uk.co.eelpieconsulting.spring;

import org.springframework.stereotype.Component;

@Component
public class VelocityEngineUtils extends org.apache.velocity.spring.VelocityEngineUtils {
    // To allow use by composition rather than extension.
    public VelocityEngineUtils() {
    }
}
