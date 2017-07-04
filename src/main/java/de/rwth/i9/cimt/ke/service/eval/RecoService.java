package de.rwth.i9.cimt.ke.service.eval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.rwth.i9.cimt.ke.util.CosineSimilarity;
import de.rwth.i9.cimt.ke.util.MapSortUtil;
import de.rwth.i9.cimt.ke.util.PearsonSimilarity;
import de.rwth.i9.cimt.ke.util.WikipediaUtil;
import de.tudarmstadt.ukp.wikipedia.api.Category;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;

@Service("recoService")
public class RecoService {

	@Autowired
	private Wikipedia simpleWikiDb;

	public double computeDefaultorReduced(Set<String> setA, Set<String> setB, boolean isCB) {
		double score = 0.0;
		Set<String> totalSet = new HashSet<>(setA);
		totalSet.addAll(setB);
		double[] vecA = new double[totalSet.size()];
		double[] vecB = new double[totalSet.size()];
		int i = 0;
		for (String term : totalSet) {
			if (setA.contains(term)) {
				vecA[i] = 1.0;
			}
			if (setB.contains(term)) {
				vecB[i] = 1.0;
			}
			i++;
		}
		if (isCB) {
			score = CosineSimilarity.cosineSimilarity(vecA, vecB);
		} else {
			score = PearsonSimilarity.Correlation(vecA, vecB);
		}
		return score;
	}

	public List<Double> computeParent(Set<String> setA, Set<String> setB, boolean isCB) throws WikiApiException {
		List<Double> scores = new ArrayList<>();
		Map<String, Integer> mapA = this.getParentCategories(setA);
		Map<String, Integer> mapB = this.getParentCategories(setB);
		return this.getScore(mapA, mapB, isCB);
	}

	public List<Double> computeSibling(Set<String> setA, Set<String> setB, boolean isCB) throws WikiApiException {
		List<Double> scores = new ArrayList<>();
		Map<String, Integer> mapA = this.getSiblingCategories(setA);
		Map<String, Integer> mapB = this.getSiblingCategories(setB);
		return this.getScore(mapA, mapB, isCB);
	}

	public List<Double> computeDescendent(Set<String> setA, Set<String> setB, boolean isCB) throws WikiApiException {
		List<Double> scores = new ArrayList<>();
		Map<String, Integer> mapA = this.getDescendentCategories(setA);
		Map<String, Integer> mapB = this.getDescendentCategories(setB);
		return this.getScore(mapA, mapB, isCB);
	}

	private Map<String, Integer> getParentCategories(Set<String> setA) throws WikiApiException {
		Map<String, Integer> mapA = new HashMap<>();
		for (String term : setA) {
			if (simpleWikiDb.existsPage(term)) {
				Page p = simpleWikiDb.getPage(term);
				if (p.isDisambiguation()) {
					continue;
				}
				for (Category c : p.getCategories()) {
					int parentCategoryId = c.getPageId();
					String categoryName = c.getTitle().getEntity();
					if (WikipediaUtil.isGenericWikipediaCategory(parentCategoryId)) {
						continue;
					}
					if (mapA.containsKey(categoryName)) {
						mapA.put(categoryName, mapA.get(categoryName) + 1);
					} else {
						mapA.put(categoryName, 1);
					}
				}

			}

		}
		mapA = MapSortUtil.sortByValueDesc(mapA);
		return mapA;

	}

	private Map<String, Integer> getDescendentCategories(Set<String> setA) throws WikiApiException {
		Map<String, Integer> mapA = new HashMap<>();
		for (String term : setA) {
			if (simpleWikiDb.existsPage(term)) {
				Page p = simpleWikiDb.getPage(term);
				if (p.isDisambiguation()) {
					continue;
				}
				for (Category c : p.getCategories()) {
					if (WikipediaUtil.isGenericWikipediaCategory(c.getPageId())) {
						continue;
					}
					for (Category d : c.getChildren()) {
						int descCategoryId = d.getPageId();
						String categoryName = d.getTitle().getEntity();
						if (WikipediaUtil.isGenericWikipediaCategory(descCategoryId)) {
							continue;
						}
						if (mapA.containsKey(categoryName)) {
							mapA.put(categoryName, mapA.get(categoryName) + 1);
						} else {
							mapA.put(categoryName, 1);
						}
					}

				}

			}

		}
		mapA = MapSortUtil.sortByValueDesc(mapA);
		return mapA;

	}

	private Map<String, Integer> getSiblingCategories(Set<String> setA) throws WikiApiException {
		Map<String, Integer> mapA = new HashMap<>();
		for (String term : setA) {
			if (simpleWikiDb.existsPage(term)) {
				Page p = simpleWikiDb.getPage(term);
				if (p.isDisambiguation()) {
					continue;
				}
				for (Category c : p.getCategories()) {
					if (WikipediaUtil.isGenericWikipediaCategory(c.getPageId())) {
						continue;
					}
					for (Category s : c.getSiblings()) {
						int siblingCategoryId = s.getPageId();
						String categoryName = s.getTitle().getEntity();
						if (WikipediaUtil.isGenericWikipediaCategory(siblingCategoryId)) {
							continue;
						}
						if (mapA.containsKey(categoryName)) {
							mapA.put(categoryName, mapA.get(categoryName) + 1);
						} else {
							mapA.put(categoryName, 1);
						}
					}

				}

			}

		}
		mapA = MapSortUtil.sortByValueDesc(mapA);
		return mapA;

	}

	private List<Double> getScore(Map<String, Integer> mapA, Map<String, Integer> mapB, boolean isCB) {
		Set<String> pSetA5 = new HashSet<>();
		Set<String> pSetA10 = new HashSet<>();
		Set<String> pSetA15 = new HashSet<>();
		Set<String> pSetAAll = mapA.keySet();
		Set<String> pSetB5 = new HashSet<>();
		Set<String> pSetB10 = new HashSet<>();
		Set<String> pSetB15 = new HashSet<>();
		Set<String> pSetBAll = mapB.keySet();
		List<Double> scores = new ArrayList<>();
		int count = 0;
		for (Map.Entry<String, Integer> entry : mapA.entrySet()) {
			if (count < 5) {
				pSetA5.add(entry.getKey());
			}
			if (count < 10) {
				pSetA10.add(entry.getKey());
			}
			if (count < 15) {
				pSetA15.add(entry.getKey());
			}
			count++;
		}
		count = 0;
		for (Map.Entry<String, Integer> entry : mapB.entrySet()) {
			if (count < 5) {
				pSetB5.add(entry.getKey());
			}
			if (count < 10) {
				pSetB10.add(entry.getKey());
			}
			if (count < 15) {
				pSetB15.add(entry.getKey());
			}
			count++;
		}
		scores.add(this.computeDefaultorReduced(pSetA5, pSetB5, isCB));
		scores.add(this.computeDefaultorReduced(pSetA10, pSetB10, isCB));
		scores.add(this.computeDefaultorReduced(pSetA15, pSetB15, isCB));
		scores.add(this.computeDefaultorReduced(pSetAAll, pSetBAll, isCB));
		return scores;
	}
}
