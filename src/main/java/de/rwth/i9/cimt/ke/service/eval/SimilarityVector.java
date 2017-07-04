package de.rwth.i9.cimt.ke.service.eval;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SimilarityVector {
	private List<String> vector1;
	private List<String> vector2;
	private String similarityAlgorithm;

	public List<String> getVector1() {
		return vector1;
	}

	public void setVector1(List<String> vector1) {
		this.vector1 = vector1;
	}

	public List<String> getVector2() {
		return vector2;
	}

	public void setVector2(List<String> vector2) {
		this.vector2 = vector2;
	}

	public String getSimilarityAlgorithm() {
		return similarityAlgorithm;
	}

	public void setSimilarityAlgorithm(String similarityAlgorithm) {
		this.similarityAlgorithm = similarityAlgorithm;
	}

}
