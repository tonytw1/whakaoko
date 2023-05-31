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

    val jsonView: JsonView
        get() {
            return JsonView(
                jsonSerializer,
                etagGenerator
            )
        }

    fun getJsonView(maxAge: Int): JsonView {
        val view = JsonView(
            jsonSerializer,
            etagGenerator
        )
        view.setMaxAge(maxAge)
        return view
    }

    fun getRssView(title: String?, link: String?, description: String?): RssView {
        return RssView(etagGenerator, title, link, description)
    }

    fun getRssView(maxAge: Int, title: String?, link: String?, description: String?): RssView {
        val view = RssView(etagGenerator, title, link, description)
        view.setMaxAge(maxAge)
        return view
    }
}

