package uk.co.eelpieconsulting.feedlistener.controllers.ui

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.feedlistener.UnknownSubscriptionException
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO

@Component
open class SubscriptionLabelService @Autowired constructor(val subscriptionsDAO: SubscriptionsDAO) {
    // TODO open for mocking only

   fun labelForSubscription(subscriptionId: String): String? {
       return try {
           subscriptionsDAO.getById(subscriptionId).name
       } catch (e: UnknownSubscriptionException) {
           subscriptionId.toUpperCase()
       }
   }

}