package be.ordina.twimon.service;

import be.ordina.twimon.dto.FestivalRankDTO;


public interface TweetService {
	
	void retrieveTweets();

	void retrieveTweetsByPaging();

	String rankFestivalOnWord(String word);

	FestivalRankDTO rankFestivalOnWords(String ... words);

}
