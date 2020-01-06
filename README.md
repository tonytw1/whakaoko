# Whakaoko (verb. to listen)

A service for aggregating 3rd party content from a variety of sources (RSS / Twitter / Instagram) into an easy to consume local JSON or RSS feed.

Provides an local API for applications to consume the aggregated content via a single HTTP/JSON interface.

MongoDB is used for storage.


## Concepts

### Subscriptions

A subscription represents a source of 3rd party content.
The following sources are currently supported.

- RSS / ATOM feeds
Content, media RSS and geoRSS tags.

- Twitter
Receives Twitter posts via the Twitter real time API.
The real time API allows considerably more content to be retrived within the default rate limits, than polling the REST API.
The following subscriptions are currently supported:

	Twitter tag
	Content tagged with a twitter hashtag. ie. #twickenham

- Instagram
Receives Instagram posts via the Instagram call back API.
The following subscriptions are currently supported:

	Instagram tag
	Content tagged with an instragram tag. ie. #twickenham

	Instagram geography
	Geotagged Instagram content within a given latitude/longitude/radius boundary.


### Channels

A channel is a collection of related subscriptions.
The content from each subscription in the channel is aggregated together.



### Output formats

Content is output is either JSON or RSS.

The RSS format encodes images as media RSS tags and geotags as geoRSS tags.



### API end points

##### GET /{userid}/channels

Lists the channels defined for this user.


#### GET /{userid}/channels/{channelid}

Details for a specific channel.


#### GET /{userid}/channels/{channelid}/subscriptions

Lists the subscriptions into this channel.


#### GET /{userid}/channels/{channelid}/items

|Parameter	|	Description |
|----------|----------------|
| format | json / rss |
| page | pagination number |
	
Show content for this channel.

Contains all content items received from all of the subscriptions in this channel.


#### GET /{userid}/subscriptions/{subscriptionid}

Details for a specific subscription.


#### GET /{userid}/subscriptions/{subscriptionid}/items

|Parameter	|	Description |
|----------|----------------|
|format | json / rss |
|page  | pagination number |

Show content from this subscription.


#### POST /{username}/subscriptions/feeds

| Parameter	|	Description |
|----------|----------------|
|channel | The channel id to add this subscription to|
|url | The url of an RSS or ATOM feed|
	
Request a new subscription to a RSS or ATOM feed url.

#### POST /subscriptions/twitter/tags

|Parameter |	Description |
|----------|---------------- |
|channel | The channel id to add this subscription to|
|tag | The twitter tag to subscribed to|
	
Request a new subscription to a Twitter tag.
Requires the API users account to be linked to a Twitter account.


#### POST /subscriptions/instagram/tags


| Parameter	|	Description |
|----------|----------------|
|channel|The channel id to add this subscription to|
|tag|The twitter tag to subscribed to|
	
Request a new subscription to an Instagram tag.
Requires the API users account to be linked to an Instagram account.

	
#### POST /subscriptions/instagram/geography

| Parameter	|	Description |
|----------|---------------- |
|channel | The channel id to add this subscription to|
|latitude | The latitude of the center of circle to follow|
|longitude | The longitude of the center of circle to follow|
|radius | The radius in metres of the circle to follow (1 - 5000)|

Request a new subscription to an Instagram geography. An Instagram geography is a circle
centered on a given latitude/longitude with a radius upto 5000 metres.

Requires the API users account to be linked to an Instagram account.


#### /instagram/authorise
	
Initiate linking of an Instragram account by redirecting to the Instagram authorise url.


#### /twitter/authorise

Initiate linking of an Twitter account by redirecting to the Twitter authorise url.


# Setup

## Run as a Docker image



## Create a user

```
curl -XPOST http://localhost:9100/users?username=new-user
```
or
```
http://localhost:9100/ui/newuser
```

