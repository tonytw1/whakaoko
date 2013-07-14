package uk.co.eelpieconsulting.feedlistener.daos;

import java.net.UnknownHostException;
import java.util.List;

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

	public List<Channel> getChannels() {
		try {
			return dataStoreFactory.getDatastore().find(Channel.class).asList();
			
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		} catch (MongoException e) {
			throw new RuntimeException(e);
		}
	}

	public Channel getById(String id) {
        try {
			return dataStoreFactory.getDatastore().find(Channel.class, "id", id).get();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		} catch (MongoException e) {
			throw new RuntimeException(e);
		}
	}
	
	public synchronized void add(Channel channel) {		
		if (!channelExists(channel)) {
			log.info("Adding new channel: " + channel);
			save(channel);
		} else {
			log.warn("Ignoring existing channel: " + channel);
		}
	}
	
	public void save(Channel channel) {
		try {
			dataStoreFactory.getDatastore().save(channel);			
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		} catch (MongoException e) {
			throw new RuntimeException(e);
		}
	}
	
	private boolean channelExists(Channel channel) {
		log.info(getChannels());
		for (Channel existingChannel : getChannels()) {
			if (existingChannel.getId().equals(channel.getId())) {
				return true;
			}
		}
		return false;
	}
	
}
