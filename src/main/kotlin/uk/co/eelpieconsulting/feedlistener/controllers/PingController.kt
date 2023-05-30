package uk.co.eelpieconsulting.feedlistener.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.ModelAndView

import uk.co.eelpieconsulting.feedlistener.views.ViewFactory

@Controller
class PingController @Autowired constructor(private val viewFactory: ViewFactory){

    @GetMapping("/healthz")
    fun ping(): ModelAndView? {
        return ModelAndView(viewFactory.jsonView).
        addObject("data", "ok")
    }

}