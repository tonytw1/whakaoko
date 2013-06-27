package uk.co.eelpieconsulting.feedlistener.instagram;

public class InstagramSubscripton {

	private final String object;
	private final String objectId;

	public InstagramSubscripton(String object, String objectId) {
		this.object = object;
		this.objectId = objectId;
	}

	public String getObject() {
		return object;
	}

	public String getObjectId() {
		return objectId;
	}

	@Override
	public String toString() {
		return "InstagramSubscripton [object=" + object + ", objectId="
				+ objectId + "]";
	}
	
}
