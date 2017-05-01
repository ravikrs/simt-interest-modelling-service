package de.rwth.i9.cimt.ke.algorithm.kpextraction.unsupervised.topicclustering;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.rwth.i9.cimt.ke.model.Keyword;

public class KeyCluster {
	private static Logger log = LoggerFactory.getLogger(KeyCluster.class);

	public static List<Keyword> performKeyClusterKE(String textContent) {
		List<Keyword> returnedKeywords = new ArrayList<Keyword>();
		// 1. Candidate term selection - filter out stop words and select all
		// single terms as candidates.
		// 2. Calculate term relatedness - eg co-occurrence,wikipedia based term
		// relatedness pmi
		// 3. Term Clustering - group terms similar as clusters. - hierarchical,
		// spectral, affinity propagation
		// 4. use exemplar terms to get keyphrases from the document - pos tags
		// noun groups with zero or more adjectives followed by one or more
		// nouns.

		return returnedKeywords;

	}
}
