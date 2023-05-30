package uk.co.eelpieconsulting.feedlistener.views

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.common.views.EtagGenerator
import uk.co.eelpieconsulting.common.views.json.JsonSerializer
import uk.co.eelpieconsulting.common.views.json.JsonView
import uk.co.eelpieconsulting.common.views.rss.RssView

@Component
class ViewFactory @Autowired constructor(private val etagGenerator: EtagGenerator) {

    val jsonView: JsonView
        get() = JsonView(
            JsonSerializer(),
            etagGenerator
        )

    fun getJsonView(maxAge: Int): JsonView {
        val view = JsonView(
            JsonSerializer(),
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

