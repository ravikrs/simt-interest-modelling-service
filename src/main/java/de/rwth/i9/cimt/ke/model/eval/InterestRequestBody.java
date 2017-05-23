package de.rwth.i9.cimt.ke.model.eval;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InterestRequestBody implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8584676277104657249L;
	/**
	 * 
	 */
	private int authorId;
	private String algorithmName;
	private String latentInterestType;
	private int numKeywords;

	public int getNumKeywords() {
		return numKeywords;
	}

	public void setNumKeywords(int numKeywords) {
		this.numKeywords = numKeywords;
	}

	public String getLatentInterestType() {
		return latentInterestType;
	}

	public void setLatentInterestType(String latentInterestType) {
		this.latentInterestType = latentInterestType;
	}

	public int getAuthorId() {
		return authorId;
	}

	public void setAuthorId(int authorId) {
		this.authorId = authorId;
	}

	public String getAlgorithmName() {
		return algorithmName;
	}

	public void setAlgorithmName(String algorithmName) {
		this.algorithmName = algorithmName;
	}

}
