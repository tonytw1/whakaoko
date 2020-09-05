package uk.co.eelpieconsulting.feedlistener.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.ModelAndView
import uk.co.eelpieconsulting.common.views.ViewFactory

class PingController @Autowired constructor(val viewFactory: ViewFactory){

    @GetMapping("/healthz")
    fun ping(): ModelAndView? {
        return ModelAndView(viewFactory.getJsonView()).
        addObject("data", "ok")
    }

}