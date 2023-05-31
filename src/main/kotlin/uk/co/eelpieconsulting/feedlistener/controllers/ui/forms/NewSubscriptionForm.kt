package uk.co.eelpieconsulting.feedlistener.controllers.ui.forms

import jakarta.validation.constraints.NotEmpty

class NewSubscriptionForm {

    @NotEmpty
    var url: String = ""

}
