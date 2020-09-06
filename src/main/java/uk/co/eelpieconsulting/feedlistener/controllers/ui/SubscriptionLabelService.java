package uk.co.eelpieconsulting.feedlistener.controllers.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.feedlistener.UnknownSubscriptionException;
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO;

@Component
public class SubscriptionLabelService {

    private final SubscriptionsDAO subscriptionsDAO;

    @Autowired
    public SubscriptionLabelService(SubscriptionsDAO subscriptionsDAO) {
        this.subscriptionsDAO = subscriptionsDAO;
    }

    public String label(String subscriptionId) {
        try {
            return subscriptionsDAO.getById(subscriptionId).getName();
        } catch (UnknownSubscriptionException e) {
            return subscriptionId.toUpperCase();
        }
    }

}
