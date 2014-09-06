package be.ordina.twimon.service.impl;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.UserMentionEntity;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import be.ordina.twimon.dto.FestivalRankDTO;
import be.ordina.twimon.dto.FestivalWordCountDTO;
import be.ordina.twimon.service.TweetService;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

@Component
public class TweetServiceImpl implements TweetService {

	private ConfigurationBuilder cb;
	private Configuration twitterConfiguration;
	private DB db;
	private DBCollection toFollowCollection;
	private DBCollection tweetCollection;
	
	private String twitterConsumerKey;
	private String twitterConsumerSecret;
	private String twitterAccessToken;
	private String twitterAccessTokenSecret;
	
	
	
	public TweetServiceImpl(String twitterConsumerKey, String twitterConsumerSecret, String twitterAccessToken
			, String twitterAccessTokenSecret) {
		this.twitterAccessToken = twitterAccessToken;
		this.twitterAccessTokenSecret = twitterAccessTokenSecret;
		this.twitterConsumerKey = twitterConsumerKey;
		this.twitterConsumerSecret = twitterConsumerSecret;
		
		initMongoDB();
	}

	private void initMongoDB() throws MongoException {
		try {
			System.out.println("Connecting to Mongo DB..");
			Mongo mongo = new Mongo("127.0.0.1");
			db = mongo.getDB("tweetDB");
		} catch (UnknownHostException ex) {
			System.out.println("MongoDB Connection Errro :" + ex.getMessage());
		}

		cb = initializeConfigurationBuilderTwitter();
	}

	private ConfigurationBuilder initializeConfigurationBuilderTwitter() {
		ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
		configurationBuilder.setDebugEnabled(true);
		configurationBuilder.setOAuthConsumerKey(twitterConsumerKey);
		configurationBuilder.setOAuthConsumerSecret(twitterConsumerSecret);
		configurationBuilder.setOAuthAccessToken(twitterAccessToken);
		configurationBuilder.setOAuthAccessTokenSecret(twitterAccessTokenSecret);
		
		return configurationBuilder;
	}
	
	@Override
	public void retrieveTweetsByPaging(){
		if (toFollowCollection == null) {
			toFollowCollection = db.getCollection("toFollow");
		}
		if (tweetCollection == null) {
			tweetCollection = db.getCollection("tweetColl");
		}

		DBCursor cursor = toFollowCollection.find(new BasicDBObject());

		List<DBObject> toFollowList = new ArrayList<DBObject>();
		while (cursor.hasNext()) {
			toFollowList.add(cursor.next());
		}

		System.out.println(toFollowList);
		
		for (DBObject toFollowEntry : toFollowList) {
			if (twitterConfiguration == null) {
				twitterConfiguration = cb.build();
			}
			Long maxIdRetrieved = null;
			Long sinceIdRetrieved = null;

			TwitterFactory tf = new TwitterFactory(twitterConfiguration);
			Twitter twitter = tf.getInstance();
			
			try {
				Query queryMaxTwitter = createMaxIdQuery(toFollowEntry);
				
				List<Status> tweets = getTweetsFromTwitter(toFollowEntry,
						twitter, queryMaxTwitter);
				
				if (toFollowEntry.get("sinceId") != null) {
					Query querySinceTwitter = createSinceIdQuery(toFollowEntry);
					tweets.addAll(getTweetsFromTwitter(toFollowEntry,
							twitter, querySinceTwitter));
				}
				
				for (Status tweet : tweets) {
					BasicDBObject basicObj = createTweetDBObject(tweet, toFollowEntry.get("festival").toString());
					try {
						tweetCollection.insert(basicObj);
					} catch (Exception e) {
						System.out.println("MongoDB Error : "
								+ e.getMessage());
					}
					
					if (maxIdRetrieved == null || tweet.getId() < maxIdRetrieved) {
						System.out.println("update max id to  " + tweet.getId());
						maxIdRetrieved = tweet.getId();
					}
					
					if (sinceIdRetrieved == null ||tweet.getId() > sinceIdRetrieved) {
						System.out.println("update since id to  " + tweet.getId());
						sinceIdRetrieved = tweet.getId();
					}
				}
				
				// Printing fetched records from DB.
			} catch (TwitterException te) {
				handleTwitterException(te);
			}
			
			System.out.println("with Max ID retrieved... " + maxIdRetrieved);
			System.out.println("with Since ID retrieved... " + sinceIdRetrieved);
			updateIdsRetrieved(toFollowEntry, maxIdRetrieved, sinceIdRetrieved);
			
		}
	}

	private Query createMaxIdQuery(DBObject toFollowEntry) {
		Query queryTwitter = new Query(toFollowEntry.get("follow").toString());
		queryTwitter.setCount(100);
		if (toFollowEntry.get("maxId") != null) {
			queryTwitter.setMaxId( (Long) toFollowEntry.get("maxId"));
		}
		return queryTwitter;
	}
	
	private Query createSinceIdQuery(DBObject toFollowEntry) {
		Query queryTwitter = new Query(toFollowEntry.get("follow").toString());
		queryTwitter.setCount(80);
		if (toFollowEntry.get("sinceId") != null) {
			queryTwitter.setSinceId( (Long) toFollowEntry.get("sinceId"));
		}
		return queryTwitter;
	}

	private List<Status> getTweetsFromTwitter(DBObject toFollowEntry,
			Twitter twitter, Query queryTwitter) throws TwitterException {
		System.out.println("====================== > Get tweets for " + queryTwitter.getQuery());
		System.out.println("with Max ID ... " + queryTwitter.getMaxId());
		System.out.println("with Since ID ... " + queryTwitter.getSinceId());
		QueryResult result = twitter.search(queryTwitter);
		
		System.out.println("Got Tweets " + result.getTweets().size() + "...for " + toFollowEntry.get("follow").toString());
		List<Status> tweets = result.getTweets();
		return tweets;
	}

	private void handleTwitterException(TwitterException te) {
		System.out.println("te.getErrorCode() " + te.getErrorCode());
		System.out.println("te.getExceptionCode() "
				+ te.getExceptionCode());
		System.out.println("te.getStatusCode() " + te.getStatusCode());
		if (te.getStatusCode() == 401) {
			System.out
					.println("Twitter Error : \nAuthentication "
							+ "credentials (https://dev.twitter.com/pages/auth) "
							+ "were missing or incorrect.\nEnsure that you have "
							+ "set valid consumer key/secret, access "
							+ "token/secret, and the system clock is in sync.");
		} else {
			System.out.println("Twitter Error : " + te.getMessage());
		}
		
		te.printStackTrace();
	}

	private BasicDBObject createTweetDBObject(Status tweet, String festival) {
		BasicDBObject basicObj = new BasicDBObject();
		basicObj.put("user_name", tweet.getUser().getScreenName());
		basicObj.put("retweet_count", tweet.getRetweetCount());
		basicObj.put("tweet_followers_count", tweet.getUser()
				.getFollowersCount());
		UserMentionEntity[] mentioned = tweet
				.getUserMentionEntities();
		basicObj.put("tweet_mentioned_count", mentioned.length);
		basicObj.put("tweet_ID", tweet.getId());
		basicObj.put("tweet_text", tweet.getText());
		basicObj.put("festival", festival);
		return basicObj;
	}

	private void updateIdsRetrieved(DBObject toFollowEntry,
			Long maxIdRetrieved, Long sinceIdRetrieved) {
		if (maxIdRetrieved != null) {
			System.out.println("update maxID to " + maxIdRetrieved);
			toFollowEntry.put("maxId", maxIdRetrieved);
		}
		if (sinceIdRetrieved != null) {
			System.out.println("update maxID to " + maxIdRetrieved);
			toFollowEntry.put("sinceId", sinceIdRetrieved);
		}
		
		BasicDBObject searchQuery = new BasicDBObject().append("_id", toFollowEntry.get("_id"));
		toFollowCollection.update(searchQuery, toFollowEntry);
	}
	
	/*
	 * db.tweetColl.aggregate([{$match:{$text:{$search:"tomorrowland"}}}
	 * 	,{$group:{_id:"$festival", count:{$sum:1}}},{$sort:{count:-1}}]
	 * 
	 * 1. match on the text index to find the tweets containing the searched word
	 * 2. group on the festival field and do a count of the tweets
	 * 3. sort so highest count is on top
	 * 
	 *  
	 *> db.toFollow.update({},{'$unset':{"maxId":true}},{'multi':true});
	 *> db.toFollow.update({},{'$unset':{"sinceId":true}},{'multi':true});
	 *> db.tweetColl.remove({});
	 * (non-Javadoc)
	 * @see be.ordina.twimon.service.TweetService#rankFestivalOnWord(java.lang.String)
	 */
	@Override
	public String rankFestivalOnWord(String word) {
		
		DBObject match = new BasicDBObject("$match"
				, new BasicDBObject("$text", new BasicDBObject("$search", word)));
		
		DBObject groupFields = new BasicDBObject( "_id", "$festival");
		groupFields.put("count", new BasicDBObject( "$sum", 1));
		DBObject group = new BasicDBObject("$group", groupFields);
		
		DBObject sort = new BasicDBObject("$sort", new BasicDBObject("count", -1));
		
		if (tweetCollection == null) {
			tweetCollection = db.getCollection("tweetColl");
		}
		AggregationOutput output = tweetCollection.aggregate(match, group, sort);
		for (DBObject result : output.results()) {
		    System.out.println(result);
		}
		System.out.println("===== raw result:" + output.results().toString());
		
		return output.results().toString();
		
	}
	
	@Override
	public FestivalRankDTO rankFestivalOnWords(String ...words) {
		FestivalRankDTO festivalRankDTO = new FestivalRankDTO();
		
		for (String word : words) {
			System.out.println("Retrieve data for word  " + word);
			
			word = word.trim();
			DBObject match = new BasicDBObject("$match"
					, new BasicDBObject("$text", new BasicDBObject("$search", word)));
			
			DBObject groupFields = new BasicDBObject( "_id", "$festival");
			groupFields.put("count", new BasicDBObject( "$sum", 1));
			DBObject group = new BasicDBObject("$group", groupFields);
			
			DBObject sort = new BasicDBObject("$sort", new BasicDBObject("count", -1));
			
			if (tweetCollection == null) {
				tweetCollection = db.getCollection("tweetColl");
			}
			List<FestivalWordCountDTO> countRecords = new ArrayList<FestivalWordCountDTO>();
			
			AggregationOutput output = tweetCollection.aggregate(match, group, sort);
			for (DBObject record : output.results()) {
				countRecords.add(new FestivalWordCountDTO(word, record.get("_id").toString()
						, Integer.valueOf(record.get("count").toString())));
			}
			countRecords.sort(FestivalWordCountDTO.festivalWordCountComparator);
			festivalRankDTO.getFestivalsPerWord().put(word, countRecords);
			
			System.out.println("Found " + countRecords.size() + " records for word " + word);
		}
		
		return calculateRank(festivalRankDTO);
		
	}
	
	@SuppressWarnings("unchecked")
	private FestivalRankDTO calculateRank(FestivalRankDTO festivalRankDTO) {
		for (String word : festivalRankDTO.getFestivalsPerWord().keySet()) {
			List<FestivalWordCountDTO> festivalWordCounts = festivalRankDTO.getFestivalsPerWord().get(word);
			
			Integer size = 5;
			for (FestivalWordCountDTO dto : festivalWordCounts) {
				Integer rankScore = size * 5; //1st gets 25 points, second 20, ...
				
				if (!festivalRankDTO.getRank().containsKey(dto.getFestival())) {
					festivalRankDTO.getRank().put(dto.getFestival(), 0);
				}
				Integer currentScore = festivalRankDTO.getRank().get(dto.getFestival());
				Integer newScore = currentScore + rankScore;
				festivalRankDTO.getRank().put(dto.getFestival(), newScore);
				
				size = size -1;
				if (size <= 0) {
					break;
				}
			}
		}
		
		return festivalRankDTO;
	}
		
	

	@Override
	public void retrieveTweets() {
		if (toFollowCollection == null) {
			toFollowCollection = db.getCollection("toFollow");
		}
		if (tweetCollection == null) {
			tweetCollection = db.getCollection("tweetColl");
		}

		BasicDBObject fields = new BasicDBObject("_id", true).append("follow",
				true).append("festival", true);
		DBCursor cursor = toFollowCollection.find(new BasicDBObject(), fields);

		List<String> toFollowList = new ArrayList<String>();
		while (cursor.hasNext()) {
			toFollowList.add(cursor.next().get("follow").toString());
		}

		System.out.println(toFollowList);
		
		for (String toFollow : toFollowList) {
			if (twitterConfiguration == null) {
				twitterConfiguration = cb.build();
			}

			TwitterFactory tf = new TwitterFactory(twitterConfiguration);
			Twitter twitter = tf.getInstance();
			try {
				Query query = new Query(toFollow);
				query.setCount(50);
				QueryResult result;
				result = twitter.search(query);
				System.out.println("Getting Tweets...");
				List<Status> tweets = result.getTweets();
				for (Status tweet : tweets) {
					BasicDBObject basicObj = createTweetDBObject(tweet, "dummy");
					try {
						tweetCollection.insert(basicObj);
					} catch (Exception e) {
						System.out.println("MongoDB Connection Error : "
								+ e.getMessage());
					}
				}
				// Printing fetched records from DB.
			} catch (TwitterException te) {
				handleTwitterException(te);
			}

		}

	}

}
