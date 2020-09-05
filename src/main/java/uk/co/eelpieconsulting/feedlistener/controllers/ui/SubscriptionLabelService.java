package uk.co.eelpieconsulting.feedlistener.controllers.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.feedlistener.UnknownSubscriptionException;
import uk.co.eelpieconsulting.feedlistener.daos.SubscriptionsDAO;

@Component
public class SubscriptionLabelService {

    private final SubscriptionsDAO subscriptionsDAO;
    private final CurrentUserService currentUserService;

    @Autowired
    public SubscriptionLabelService(CurrentUserService currentUserService, SubscriptionsDAO subscriptionsDAO) {
        this.currentUserService = currentUserService;
        this.subscriptionsDAO = subscriptionsDAO;
    }

    public String label(String subscriptionId) {
        try {
            return subscriptionsDAO.getById(currentUserService.getCurrentUserUser().getUsername(), subscriptionId).getName();
        } catch (UnknownSubscriptionException e) {
            return subscriptionId.toUpperCase();
        }
    }

}
