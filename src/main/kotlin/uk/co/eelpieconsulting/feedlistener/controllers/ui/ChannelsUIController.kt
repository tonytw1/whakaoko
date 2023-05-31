package uk.co.eelpieconsulting.feedlistener.controllers.ui

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import uk.co.eelpieconsulting.feedlistener.IdBuilder
import uk.co.eelpieconsulting.feedlistener.UrlBuilder
import uk.co.eelpieconsulting.feedlistener.controllers.ConditionalLoads
import uk.co.eelpieconsulting.feedlistener.controllers.CurrentUserService
import uk.co.eelpieconsulting.feedlistener.controllers.FeedItemPopulator
import uk.co.eelpieconsulting.feedlistener.controllers.ui.forms.NewChannelForm
import uk.co.eelpieconsulting.feedlistener.daos.ChannelsDAO
import uk.co.eelpieconsulting.feedlistener.daos.FeedItemDAO
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO
import uk.co.eelpieconsulting.feedlistener.model.Channel
import uk.co.eelpieconsulting.feedlistener.model.User

@Controller
class ChannelsUIController @Autowired constructor(val subscriptionsDAO: SubscriptionsDAO,
                                                  private val feedItemPopulator: FeedItemPopulator,
                                                  private val channelsDAO: ChannelsDAO,
                                                  private val feedItemDAO: FeedItemDAO,
                                                  private val idBuilder: IdBuilder,
                                                  val urlBuilder: UrlBuilder,
                                                  private val conditionalLoads: ConditionalLoads,
                                                  currentUserService: CurrentUserService,
                                                  request: HttpServletRequest) : WithSignedInUser(currentUserService, request) {

    @GetMapping("/ui/channels/new")
    fun newChannelPrompt(newChannelForm: NewChannelForm): ModelAndView {
        return forCurrentUser { ModelAndView("newChannel") }
    }

    @PostMapping("/ui/channels/new")
    fun addChannel(@Valid newChannelForm: NewChannelForm, bindingResult: BindingResult): ModelAndView {
        fun executeAddChannel(user: User): ModelAndView {
            if (bindingResult.hasErrors()) {
                return ModelAndView("newChannel").addObject("newChannelForm", newChannelForm)
            }

            val proposedId = idBuilder.makeIdForChannel()
            val newChannel = Channel(ObjectId.get(), proposedId, newChannelForm.name, user.username)
            if (channelsDAO.usersChannelByName(user, newChannelForm.name) == null) {
                channelsDAO.save(newChannel)
                return ModelAndView(RedirectView(urlBuilder.getChannelUrl(newChannel)))

            } else {
                bindingResult.addError(org.springframework.validation.FieldError("newChannelForm", "name", "Channel name already exists"))
                return ModelAndView("newChannel").addObject("newChannelForm", newChannelForm)
            }
        }
        return forCurrentUser(::executeAddChannel)
    }

    @GetMapping("/ui/channels/{channelId}")
    fun channel(@PathVariable channelId: String,
                @RequestParam(required = false) page: Int?,
                @RequestParam(required = false) q: String?
    ): ModelAndView {
        return forCurrentUser { user ->
            conditionalLoads.withChannelForUser(channelId, user) { channel ->
                val subscriptionsForChannel = subscriptionsDAO.getSubscriptionsForChannel(channel.id, null)
                val mv = ModelAndView("channel").
                addObject("channel", channel).
                addObject("subscriptions", subscriptionsForChannel)
                if (subscriptionsForChannel.isNotEmpty()) {
                    val results = feedItemDAO.getChannelFeedItemsResult(channel, page, q, null)
                    feedItemPopulator.populateFeedItems(results, mv, "feedItems")
                }
                mv
            }
        }
    }

}