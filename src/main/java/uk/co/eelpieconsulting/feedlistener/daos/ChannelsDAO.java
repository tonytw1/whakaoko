package uk.co.eelpieconsulting.feedlistener.daos;

import java.util.List;

import dev.morphia.query.experimental.filters.Filters;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.eelpieconsulting.feedlistener.model.Channel;

import com.mongodb.MongoException;

@Component
public class ChannelsDAO {

    private static Logger log = Logger.getLogger(ChannelsDAO.class);

    private final DataStoreFactory dataStoreFactory;

    @Autowired
    public ChannelsDAO(DataStoreFactory dataStoreFactory) {
        this.dataStoreFactory = dataStoreFactory;
    }

    public List<Channel> getChannels(String username) {
        try {
            return dataStoreFactory.getDs().find(Channel.class).filter(Filters.eq("username", username)).iterator().toList();
        } catch (MongoException e) {
            throw new RuntimeException(e);
        }
    }

    public Channel getById(String username, String id) {
        try {
            return dataStoreFactory.getDs().find(Channel.class).filter(Filters.eq("id", id), Filters.eq("username", username)).first();
        } catch (MongoException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void add(String username, Channel channel) {
        if (!channelExists(username, channel)) {
            log.info("Adding new channel: " + channel);
            save(channel);
        } else {
            log.warn("Ignoring existing channel: " + channel);
        }
    }

    public void save(Channel channel) {
        try {
            dataStoreFactory.getDs().save(channel);
        } catch (MongoException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean channelExists(String username, Channel channel) {
        for (Channel existingChannel : getChannels(username)) {
            if (existingChannel.getId().equals(channel.getId())) {
                return true;
            }
        }
        return false;
    }

}
