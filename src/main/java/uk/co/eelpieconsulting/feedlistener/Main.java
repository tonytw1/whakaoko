package uk.co.eelpieconsulting.feedlistener;

import com.google.common.collect.Maps;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.spring.VelocityEngineFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.co.eelpieconsulting.common.dates.DateFormatter;
import uk.co.eelpieconsulting.common.shorturls.resolvers.*;
import uk.co.eelpieconsulting.feedlistener.http.HttpFetcher;
import uk.co.eelpieconsulting.spring.VelocityViewResolver;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, MongoAutoConfiguration.class})
@EnableScheduling
@ComponentScan("uk.co.eelpieconsulting")
@Configuration
public class Main implements WebMvcConfigurer {

    private static ApplicationContext ctx;

    public static void main(String[] args) {
        ctx = SpringApplication.run(Main.class, args);
    }

    @Bean
    public MemcachedClient memcachedClient(@Value("${memcached.urls}") String memcachedUrls) throws IOException {
        return new MemcachedClient(AddrUtil.getAddresses(memcachedUrls));
    }

    @Bean
    CompositeUrlResolver shortUrlResolverService() {
        return new CompositeUrlResolver(
                new BitlyUrlResolver(),
                new FeedBurnerRedirectResolver(),
                new TinyUrlResolver(),
                new TwitterShortenerUrlResolver()
        );
    }

    @Bean
    public HttpFetcher httpFetcher() {
        return new HttpFetcher("Whakaoko", 90000);
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(5);
        taskExecutor.setMaxPoolSize(10);
        taskExecutor.setQueueCapacity(5000);
        return taskExecutor;
    }

    @Bean
    public DateFormatter dateFormatter() {
        return new DateFormatter("UTC");
    }


    @Bean("velocityEngine")
    public VelocityEngineFactoryBean velocityEngineFactoryBean() {
        VelocityEngineFactoryBean velocityEngineFactory = new VelocityEngineFactoryBean();
        Properties velocityPropertiesMap = new Properties();
        velocityPropertiesMap.setProperty(Velocity.INPUT_ENCODING, "UTF-8");
        velocityPropertiesMap.setProperty(Velocity.EVENTHANDLER_REFERENCEINSERTION, "org.apache.velocity.app.event.implement.EscapeHtmlReference");
        velocityPropertiesMap.setProperty("resource.loader", "class");
        velocityPropertiesMap.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        velocityPropertiesMap.setProperty("velocimacro.library", "spring.vm");
        velocityEngineFactory.setVelocityProperties(velocityPropertiesMap);
        return velocityEngineFactory;
    }

    @Bean
    public VelocityViewResolver velocityViewResolver(DateFormatter dateFormatter,
                                                     UrlBuilder urlBuilder) {
        final VelocityViewResolver viewResolver = new VelocityViewResolver();
        viewResolver.setCache(true);
        viewResolver.setSuffix(".vm");
        viewResolver.setContentType("text/html;charset=UTF-8");

        final Map<String, Object> attributes = Maps.newHashMap();
        attributes.put("dateFormatter", dateFormatter);
        attributes.put("urlBuilder", urlBuilder);
        viewResolver.setAttributesMap(attributes);
        return viewResolver;
    }

    @Override
    public void addCorsMappings(final CorsRegistry registry) {
        registry.addMapping("/openapi.json")
                .allowedOrigins("*")
                .allowedMethods("GET");
    }

}
