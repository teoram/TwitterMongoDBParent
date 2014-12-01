package be.ordina.twimon.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import be.ordina.twimon.dto.FestivalRankDTO;
import be.ordina.twimon.service.TweetService;

@RestController
public class HelloController {
	
	private TweetService tweetService;

	@Autowired
	public HelloController (TweetService tweetService) {
		this.tweetService = tweetService;
	}

    
    @RequestMapping("/retrieveTweetsPage")
    public String retrievalPage(@RequestParam(value = "message", required = false) String message
    		, Model model) {
    	model.addAttribute("message", message);
    	String response = "{\"message\":\"" + message + "\"}";
    	
    	
    	return response;
    }
    
    
    @RequestMapping("/retrieveTweets")
    public String retrieveTweets(Model model) {
    	tweetService.retrieveTweetsByPaging();
    	
    	return retrievalPage("Tweets retrieved", model);
    }
    
    @RequestMapping("/rankFestivalOnWord")
    public @ResponseBody FestivalRankDTO rankFestivalOnWord(Model model, @RequestParam(value = "word") String query) {
    	if (query.contains(",")) {
    		String [] words = query.split(",");
    		return tweetService.rankFestivalOnWords(words);
    	} else {
    		return tweetService.rankFestivalOnWords(query);
    	}
    }

    
}