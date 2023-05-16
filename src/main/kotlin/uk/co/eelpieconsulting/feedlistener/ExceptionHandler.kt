package uk.co.eelpieconsulting.feedlistener

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.HandlerExceptionResolver
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.views.ViewFactory

@Component
class ExceptionHandler @Autowired constructor(private val viewFactory: ViewFactory) : HandlerExceptionResolver,
    Ordered {
    private val log = LogManager.getLogger(ExceptionHandler::class.java)
    override fun resolveException(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        e: Exception
    ): ModelAndView {
        if (e is ResponseStatusException) {
            response.status = e.statusCode.value()
            return ModelAndView(viewFactory.jsonView).addObject("data", e.reason)
        }
        log.error("Returning unexpected 500 error", e)
        return ModelAndView(viewFactory.jsonView).addObject("data", "500")
    }

    override fun getOrder(): Int {
        return Int.MIN_VALUE
    }
}