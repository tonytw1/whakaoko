package uk.co.eelpieconsulting.feedlistener.controllers.ui

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.co.eelpieconsulting.feedlistener.UnknownSubscriptionException
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO

@Component
class SubscriptionLabelService @Autowired constructor(val subscriptionsDAO: SubscriptionsDAO) {

   fun labelForSubscription(subscriptionId: String): String? {
       return try {
           subscriptionsDAO.getById(subscriptionId).getName()
       } catch (e: UnknownSubscriptionException) {
           subscriptionId.toUpperCase()
       }
   }

}