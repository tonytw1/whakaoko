package uk.co.eelpieconsulting.feedlistener

import com.google.common.collect.Maps
import net.spy.memcached.AddrUtil
import net.spy.memcached.MemcachedClient
import org.apache.velocity.app.Velocity
import org.apache.velocity.spring.VelocityEngineFactoryBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import uk.co.eelpieconsulting.common.dates.DateFormatter
import uk.co.eelpieconsulting.common.shorturls.resolvers.*
import uk.co.eelpieconsulting.feedlistener.http.HttpFetcher
import uk.co.eelpieconsulting.spring.views.velocity.VelocityViewResolver
import java.io.IOException
import java.util.*

@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class, MongoAutoConfiguration::class])
@EnableScheduling
@ComponentScan("uk.co.eelpieconsulting")
@Configuration
open class Main : WebMvcConfigurer {
    @Bean
    @Throws(IOException::class)
    open fun memcachedClient(@Value("\${memcached.urls}") memcachedUrls: String?): MemcachedClient {
        return MemcachedClient(AddrUtil.getAddresses(memcachedUrls))
    }

    @Bean
    open fun shortUrlResolverService(): CompositeUrlResolver {
        return CompositeUrlResolver(
            SquizUrlResolver(),
            BitlyUrlResolver(),
            FeedBurnerRedirectResolver(),
            TinyUrlResolver(),
            TwitterShortenerUrlResolver()
        )
    }

    @Bean
    open fun httpFetcher(): HttpFetcher {
        return HttpFetcher("Whakaoko", 90000)
    }

    @Bean("rssPollerTaskExecutor")
    open fun taskExecutor(): TaskExecutor {
        val taskExecutor = ThreadPoolTaskExecutor()
        taskExecutor.corePoolSize = 5
        taskExecutor.maxPoolSize = 10
        taskExecutor.queueCapacity = 5000
        return taskExecutor
    }

    @Bean
    open fun dateFormatter(): DateFormatter {
        return DateFormatter("UTC")
    }

    @Bean("velocityEngine")
    open fun velocityEngineFactoryBean(): VelocityEngineFactoryBean {
        val velocityEngineFactory = VelocityEngineFactoryBean()
        val velocityProperties = Properties()
        velocityProperties.setProperty(Velocity.INPUT_ENCODING, "UTF-8")
        velocityProperties.setProperty(
            Velocity.EVENTHANDLER_REFERENCEINSERTION,
            "org.apache.velocity.app.event.implement.EscapeHtmlReference"
        )
        velocityProperties.setProperty("resource.loader", "class")
        velocityProperties.setProperty(
            "class.resource.loader.class",
            "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader"
        )
        velocityProperties.setProperty("velocimacro.library", "spring.vm")
        velocityProperties.setProperty("resource.loader.class.cache", "true")
        // When resource.manager.cache.default_size is set to 0, then the default implementation uses the standard Java ConcurrentHashMap.
        velocityProperties.setProperty("resource.manager.cache.default_size", "0")
        velocityEngineFactory.setVelocityProperties(velocityProperties)
        return velocityEngineFactory
    }

    @Bean
    open fun velocityViewResolver(
        dateFormatter: DateFormatter?,
        urlBuilder: UrlBuilder?
    ): VelocityViewResolver {
        val viewResolver = VelocityViewResolver()
        viewResolver.isCache = true
        viewResolver.setSuffix(".vm")
        viewResolver.setContentType("text/html;charset=UTF-8")
        val attributes: MutableMap<String, Any?> = Maps.newHashMap()
        attributes["dateFormatter"] = dateFormatter
        attributes["urlBuilder"] = urlBuilder
        viewResolver.attributesMap = attributes
        return viewResolver
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/openapi.json")
            .allowedOrigins("*")
            .allowedMethods("GET")
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(Main::class.java, *args)
        }
    }
}
