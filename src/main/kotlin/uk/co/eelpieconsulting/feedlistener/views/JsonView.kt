package uk.co.eelpieconsulting.feedlistener.views

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.servlet.View
import uk.co.eelpieconsulting.common.views.EtagGenerator
class JsonView(private val jsonSerializer: JsonSerializer, private val etagGenerator: EtagGenerator) : View {
    private var maxAge: Int? = null
    private var dataField = "data"

    fun setMaxAge(maxAge: Int?) {
        this.maxAge = maxAge
    }

    fun setDataField(dataField: String) {
        this.dataField = dataField
    }

    override fun getContentType(): String {
        return "application/json"
    }

    override fun render(model: MutableMap<String, *>?, request: HttpServletRequest, response: HttpServletResponse) {
        response.characterEncoding = "UTF-8"
        response.contentType = this.contentType
        if (maxAge != null) {
            response.setHeader("Cache-Control", "max-age=" + maxAge)
        }
        val json = jsonSerializer.serialize(model!![dataField])
        response.setHeader("Etag", etagGenerator.makeEtagFor(json))
        var callbackFunction: String? = null
        if (model.containsKey("callback")) {
            callbackFunction = model["callback"] as String?
            response.writer.write("$callbackFunction(")
        }
        response.writer.write(json)
        if (callbackFunction != null) {
            response.writer.write(");")
        }
        response.writer.flush()
    }

}
