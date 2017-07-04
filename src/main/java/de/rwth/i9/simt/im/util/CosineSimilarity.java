package de.rwth.i9.simt.im.util;

import java.util.HashSet;
import java.util.Set;

public class CosineSimilarity {

	public static double computeSimilarity(Set<String> leftVector, Set<String> rightVector) {
		double simScore = 0.0;
		if (leftVector == null || rightVector == null) {
			return simScore;
		}
		Set<String> intersectionSet = new HashSet<>(leftVector);
		intersectionSet.retainAll(rightVector);

		double[] vectorA = new double[leftVector.size()];
		double[] vectorB = new double[rightVector.size()];
		int i = 0;
		for (String a : leftVector) {
			if (rightVector.contains(a)) {
				vectorA[i] = 1;
			}
			i++;
		}
		i = 0;
		for (String b : rightVector) {
			if (leftVector.contains(b)) {
				vectorB[i] = 1;
			}
			i++;
		}
		return cosineSimilarity(vectorA, vectorB);
	}

	public static double cosineSimilarity(double[] vectorA, double[] vectorB) {
		double dotProduct = 0.0;
		double normA = 0.0;
		double normB = 0.0;
		for (int i = 0; i < vectorA.length; i++) {
			dotProduct += vectorA[i] * vectorB[i];
			normA += Math.pow(vectorA[i], 2);
			normB += Math.pow(vectorB[i], 2);
		}
		if (normA == 0 || normB == 0) {
			return 0.0;
		}
		return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
	}

	public static void main(String[] args) {
		//		Set<String> leftVector = new HashSet<>(
		//				Arrays.asList(new String[] { "personal learning environment", "technology-enhanced learning",
		//						"peer assessment", "knowledge management", "moocs", "educational data mining" }));
		//		Set<String> rightVector = new HashSet<>(Arrays
		//				.asList(new String[] { "educational technology", "massive open online courses", "personalized learning",
		//						"personal learning environments", "network learning", "learning analytics" }));
		//		System.out.println("Normal similarity score" + computeSimilarity(leftVector, rightVector));
		//		Set<String> leftVector1 = new HashSet<>(
		//				Arrays.asList(new String[] { "personal learning environment", "technology-enhanced learning",
		//						"peer assessment", "knowledge management", "moocs", "educational data mining" }));
		//		Set<String> rightVector1 = new HashSet<>(Arrays
		//				.asList(new String[] { "educational technology", "massive open online courses", "personalized learning",
		//						"personal learning environment", "network learning", "learning analytics" }));
		//		System.out.println("Lemmatized similarity score" + computeSimilarity(leftVector1, rightVector1));

		double[] vectorA = new double[] { 1, 1, 1, 1, 1, 0.19, 0.28 };
		double[] vectorB = new double[] { 1, 1, 1, 0.43, 0.238, 1, 1 };

		System.out.println("WIKIPEDIALINKMEASURE score" + cosineSimilarity(vectorA, vectorB));
	}
}
