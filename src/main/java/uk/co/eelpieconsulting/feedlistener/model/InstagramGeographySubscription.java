package uk.co.eelpieconsulting.feedlistener.model;

import org.apache.commons.codec.digest.DigestUtils;

import uk.co.eelpieconsulting.common.geo.model.LatLong;

public class InstagramGeographySubscription extends InstagramSubscription {

	private LatLong latLong;
	private int radius;
	private long geoId;
	
	public InstagramGeographySubscription() {
	}

	public InstagramGeographySubscription(LatLong latLong, int radius, long subscriptionId, long geoId, String channelId, String username) {
		this.latLong = latLong;
		this.radius = radius;
		this.subscriptionId = subscriptionId;
		this.geoId = geoId;
		this.setId("instagram-" + DigestUtils.md5Hex("geography" + geoId));
		
		setName(generateName());
		setChannelId(channelId);
		setUsername(username);
	}
	
	public LatLong getLatLong() {
		return latLong;
	}

	public int getRadius() {
		return radius;
	}

	public long getGeoId() {
		return geoId;
	}
	
	public void setGeoId(long geoId) {
		this.geoId = geoId;		
	}
	
	@Override
	public String toString() {
		return "InstagramGeographySubscription [geoId=" + geoId + ", latLong="
				+ latLong + ", radius=" + radius + ", subscriptionId="
				+ subscriptionId + "]";
	}
	
	private String generateName() {
		return "Instagram - " + latLong + ", " + radius;
	}

}
