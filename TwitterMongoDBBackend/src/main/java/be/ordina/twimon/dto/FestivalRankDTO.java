package be.ordina.twimon.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FestivalRankDTO {
	
	private Map<String, Integer> rank;
	
	private Map<String, List> festivalsPerWord;
	
	public FestivalRankDTO() {
		this.rank = new HashMap<String, Integer>();
		this.festivalsPerWord = new HashMap<String, List>();
	}


	public Map<String, Integer> getRank() {
		return rank;
	}


	public Map<String, List> getFestivalsPerWord() {
		return festivalsPerWord;
	}
	
	
	
	

}
