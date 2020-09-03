package uk.co.eelpieconsulting.feedlistener.controllers.ui

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.ModelAndView

@Controller
class HomepageController {

    @GetMapping("/")
    fun homepage(): ModelAndView? {
        return ModelAndView("homepage")
    }

}