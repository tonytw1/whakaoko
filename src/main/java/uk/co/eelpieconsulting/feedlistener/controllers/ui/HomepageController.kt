package uk.co.eelpieconsulting.feedlistener.controllers.ui

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.servlet.ModelAndView

@Controller
class HomepageController {

    @RequestMapping(value = ["/"], method = [RequestMethod.GET])
    fun homepage(): ModelAndView? {
        return ModelAndView("homepage")
    }

}