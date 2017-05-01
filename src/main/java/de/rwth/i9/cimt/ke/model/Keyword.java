package de.rwth.i9.cimt.ke.model;

import java.util.Comparator;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Keyword implements Comparable<Keyword> {
	private String keyword;
	private double score;

	public Keyword(String keyword, double score) {
		this.keyword = keyword;
		this.score = score;
	}

	public Keyword() {
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public static Comparator<Keyword> KeywordComparatorAsc = new Comparator<Keyword>() {

		public int compare(Keyword keyword1, Keyword keyword2) {
			return keyword1.compareTo(keyword2);
		}

	};
	public static Comparator<Keyword> KeywordComparatorDesc = new Comparator<Keyword>() {

		public int compare(Keyword keyword1, Keyword keyword2) {
			// return this.quantity - compareQuantity;
			return keyword2.compareTo(keyword1);
		}

	};

	@Override
	public int compareTo(Keyword o) {
		// TODO Auto-generated method stub
		if (this.score < o.getScore())
			return -1;
		else if (this.score > o.getScore())
			return 1;
		else
			return this.keyword.compareTo(o.getKeyword());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Keyword) {
			Keyword that = (Keyword) obj;
			if ((this.score == that.getScore()) && this.getKeyword().equals(that.getKeyword()))
				return true;
		}
		return false;
	}
}
