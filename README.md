# Whakaoko (verb. to listen)

A service for aggregating 3rd party content from a variety of sources (RSS / Twitter / Instagram) into an easy to consume local JSON or RSS feed.

Provides a local API for applications to consume the aggregated content via a single HTTP/JSON interface.

MongoDB 3.4 is used for storage. Memcached is used for caching.


## Concepts

### Subscriptions

A subscription represents a source of 3rd party content.
The following sources are currently supported.

- RSS / ATOM feeds

Content, media RSS and geoRSS tags.

- Twitter

Receives Twitter posts via the Twitter real time API.
The real time API allowed considerably more content to be retrieved than polling the REST API.
The following subscriptions are currently supported:

	Twitter tag
	Content tagged with a twitter hashtag. ie. #twickenham


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

#### /twitter/authorise

Initiate linking of a Twitter account by redirecting to the Twitter authorise url.


# Setup

## Run locally

Start Mongo and Memcached dependencies as Docker images:

```
docker-compose -f docker/docker-compose.yml up
```

Run using the Spring Boot plugin:

```
mvn spring-boot:run
```

The service will be visible on localhost port 8080.


## Create a user

Using the user interface:
```
http://localhost:8080/ui/newuser
```

or curl:
```
curl -XPOST http://localhost:8080/users?username=new-user
```

## Generate an access token

Using the user interface sign in as your new user.

```
http://localhost:8080/ui
```

Then click Generate to generate an access token.

![Generate a token](generate-token.png)


## Create a channel

Either using the UI or with an API call.

```
curl -X POST http://localhost:8080/channels -H "Authorization: Bearer YOUR_TOKEN" -H "Content-Type: application/json" -d "{\"name\":\"A channel\"}"
```

Note the returned channel id. You will use it in the next request.


## Create a feed subscription

Either using the UI or with an API call.

```
curl -X POST http://localhost:8080/subscriptions -H "Authorization: Bearer YOUR_TOKEN" -H  "Content-Type: application/json" -d "{\"channel\":\"YOUR_CHANNEL_ID\",\"url\":\"http://www.victoria.ac.nz/home/about/newspubs/news/newslatest/news-rss-feed\"}"
\",\"url\":\"http://www.victoria.ac.nz/home/about/newspubs/news/newslatest/news-rss-feed\"}"
```

Note the returned subscription id. You will use it in the next request

## Read feed items

```
curl http://localhost:8080/subscriptions/YOUR_SUBSCRIPTION_ID/items | json_pp
```

![Feed items JSON](feeditems.png)


