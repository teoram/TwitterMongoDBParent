package be.ordina.twimon.dto;

import java.util.Comparator;

public class FestivalWordCountDTO implements Comparable<FestivalWordCountDTO>{
	
	private String word;
	private String festival;
	private Integer count;
	
	public FestivalWordCountDTO(String word, String festival, Integer count) {
		this.word = word;
		this.festival = festival;
		this.count = count;
	}

	public String getWord() {
		return word;
	}

	public String getFestival() {
		return festival;
	}

	public Integer getCount() {
		return count;
	}

	@Override
	public int compareTo(FestivalWordCountDTO compareTo) {
		return compareTo.getCount() - this.count;
	}
	
	public static Comparator<FestivalWordCountDTO> festivalWordCountComparator  = new Comparator<FestivalWordCountDTO>() {
		public int compare(FestivalWordCountDTO festivalWord1, FestivalWordCountDTO festivalWord2) {
			//ascending order
			return festivalWord2.getCount().compareTo(festivalWord1.getCount());
		}
	};
		
	

}
