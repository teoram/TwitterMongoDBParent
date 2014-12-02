package be.ordina.twimon.service;

import java.util.List;

import com.mongodb.DBObject;

import be.ordina.twimon.dto.FestivalRankDTO;


public interface TweetService {
	

	void retrieveTweetsByPaging();

	String rankFestivalOnWord(String word);

	FestivalRankDTO rankFestivalOnWords(String ... words);

	List<DBObject> retrieveTweetsByPagingCollection();

	DBObject handleRetrieveTweetsForSingleQuery(DBObject toFollowEntry);

}
