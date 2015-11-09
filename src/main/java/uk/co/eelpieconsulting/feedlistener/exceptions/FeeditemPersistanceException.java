package uk.co.eelpieconsulting.feedlistener.exceptions;

public class FeeditemPersistanceException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public FeeditemPersistanceException(Exception e) {
		super(e);
	}

}
