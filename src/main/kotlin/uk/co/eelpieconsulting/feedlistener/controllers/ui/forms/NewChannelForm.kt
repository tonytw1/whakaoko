package uk.co.eelpieconsulting.feedlistener.controllers.ui.forms

import jakarta.validation.constraints.NotEmpty

class NewChannelForm {

    @NotEmpty
    var name: String = ""

}
