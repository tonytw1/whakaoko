Whakaoko (verb. to listen)

The Whakaoko API is a system for aggregating 3rd party content from a variety of sources into an easy to consume JSON or RSS feed.

Consuming social media content has become increasing difficult due to authentication and rate limiting requirements. 
Access to real time and callback APIs can require public facing infrastructure which is not available in development environments.

This API allows developers to consume 3rd party feeds and social media content via a single HTTP/JSON interface.
This API is not to be exposed in public facing applications.


##Concepts

###Subscriptions

A subscription is a reference to a source of 3rd party content.
The following sources are currently supported.

- RSS / ATOM feeds
Content, media RSS and geoRSS tags.

- Instagram
Receives Instagram posts via the Instagram call back API.
The following subscriptions are currently supported:

	Instagram tag
	Content tagged with an instragram tag. ie. #twickenham 

	Instagram geography	
	Geotagged Instagram content within a given latitude/longitude/radius boundary.


- Twitter
Receives Twitter posts via the Twitter real time API.
The real time API allows considerably more content to be retrived within the default rate limits, than polling the REST API.
The following subscriptions are currently supported:

	Twitter tag
	Content tagged with a twitter hashtag. ie. #twickenham


###Channels
A channel is a collection of subscriptions relating to a common theme.
The content from each channel subscription is aggregated together to populate the channel.


###Output formats

Aggregated content is output is either JSON or RSS.
RSS output encodes images as media RSS tags and geotags as geoRSS tags.


###API end points

#####/{userid}/channels

Lists the channels defined for this user.


####/{userid}/channels/{channelid}

Details for a specific channel.


####/{userid}/channels/{channelid}/subscriptions

Lists the subscriptions which make up a given channel.


####/{userid}/channels/{channelid}/items
	format - [json|rss]
	page - pagination number
	
Content items received from this channel. Contains all content items 
received from all of the subscriptions which make up this channel.


####/{userid}/subscriptions/{subscriptionid}

Details for a specific subscription.


####/{userid}/subscriptions/{subscriptionid}/items
	format - [json|rss]
	page - pagination number

Content items received from this subscription.


#### POST /{username}/subscriptions/feeds
	channel - The channel id to add this subscription to.
	url - The url of an RSS or ATOM feed
	
Request a new subscription to a RSS or ATOM feed url.

#### POST /subscriptions/twitter/tags
	channel - The channel id to add this subscription to.
	tag - The twitter tag to subscribed to.
	
Request a new subscription to a Twitter tag.
Requires the API users account to be linked to a Twitter account.


#### POST /subscriptions/instagram/tags
	channel - The channel id to add this subscription to.
	tag - The twitter tag to subscribed to.
	
Request a new subscription to an Instagram tag.
Requires the API users account to be linked to an Instagram account.

	
#### POST /subscriptions/instagram/geography	
	channel - The channel id to add this subscription to.
	latitude - The latitude of the center of circle to follow.
	longitude - The longitude of the center of circle to follow.
	radius - The radius in metres of the circle to follow (1 - 5000).

Request a new subscription to an Instagram geography. An Instagram geography is a circle
centered on a given latitude/longitude with a radius upto 5000 metres.

Requires the API users account to be linked to an Instagram account.


#### /instagram/authorise
	
Initiate linking of an Instragram account by redirecting to the Instagram authorise url.


#### /twitter/authorise

Initiate linking of an Twitter account by redirecting to the Twitter authorise url.