package uk.co.eelpieconsulting.feedlistener.exceptions

class FeeditemPersistanceException(e: Exception?) : RuntimeException(e) {
    companion object {
        private const val serialVersionUID = 1L
    }
}
