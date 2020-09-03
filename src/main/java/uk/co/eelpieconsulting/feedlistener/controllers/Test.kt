package uk.co.eelpieconsulting.feedlistener.controllers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.ModelAndView

@Controller
class Test {
    @GetMapping("/hello")
    fun test(): ModelAndView {
        return ModelAndView("homepage")
    }
}
