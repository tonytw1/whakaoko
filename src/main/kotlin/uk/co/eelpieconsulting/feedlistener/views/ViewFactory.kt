package uk.co.eelpieconsulting.feedlistener.views

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.views.EtagGenerator
import uk.co.eelpieconsulting.common.views.json.JsonSerializer
import uk.co.eelpieconsulting.common.views.json.JsonView
import uk.co.eelpieconsulting.common.views.rss.RssView

@Component("kotlinAwareViewFactory")
class ViewFactory @Autowired constructor(private val etagGenerator: EtagGenerator) {

    private val jsonSerializer = JsonSerializer(ObjectMapper().registerKotlinModule())

    fun jsonView(): JsonView {
        return JsonView(
            jsonSerializer,
            etagGenerator
        )
    }

    fun rssView(title: String?, link: String?, description: String?): RssView {
        return RssView(etagGenerator, title, link, description)
    }

}

