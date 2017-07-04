package de.rwth.i9.simt.im.service.semantic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.rwth.i9.simt.im.model.wikipedia.WikiPagemapline;
import de.rwth.i9.simt.im.repository.wikipedia.WikiPagemaplineRepository;
import de.rwth.i9.simt.im.util.MapSortUtil;
import de.rwth.i9.simt.im.util.WikipediaUtil;
import de.rwth.i9.simt.ke.lib.util.WordCount;
import de.tudarmstadt.ukp.wikipedia.api.Category;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;

@Service("wikipediaBasedIM")
public class WikipediaBasedIM {
	private static final Logger log = LoggerFactory.getLogger(WikipediaBasedIM.class);

	@Autowired
	private Wikipedia simpleWikiDb;
	@Autowired
	WikiPagemaplineRepository wikiPagemaplineRepository;
	private static final int COUNT_40 = 40;

	public JSONObject getConceptMapJsonForLatentParentCategories(List<WordCount> wordCounts, int categoryCount)
			throws WikiApiException, JSONException {
		JSONObject conceptMapJsonData = new JSONObject();
		JSONArray themes = new JSONArray();
		JSONArray perspectives = new JSONArray();
		JSONArray ditems = new JSONArray();
		Map<Integer, Double> pageScore = new HashMap<>();
		Map<Integer, String> pageName = new HashMap<>();
		Map<Integer, Double> categoryScore = new HashMap<>();
		Map<Integer, String> categoryName = new HashMap<>();
		Map<Integer, Set<Integer>> pageIdCategoryId = new HashMap<>();
		int ditemId = 0;
		double sum = 0.0;
		for (WordCount wc : wordCounts) {
			sum += wc.getY();
		}
		for (WordCount wc : wordCounts) {
			if (simpleWikiDb.existsPage(wc.getX())) {
				Page p = simpleWikiDb.getPage(wc.getX());
				if (pageScore.containsKey(p.getPageId())) {
					pageScore.put(p.getPageId(), pageScore.get(p.getPageId()) + wc.getY() / sum);
				} else {
					pageScore.put(p.getPageId(), wc.getY() / sum);
				}
			} else {
				Set<Integer> wpmPageIds = new HashSet<>();
				List<WikiPagemapline> wpms = wikiPagemaplineRepository
						.findByName(WikipediaUtil.toWikipediaArticleName(wc.getX()));
				if (wpms.isEmpty()) {
					wpms = wikiPagemaplineRepository.findByStem(WikipediaUtil.toWikipediaArticleStem(wc.getX()));
				}
				for (WikiPagemapline wpm : wpms) {
					if (wpmPageIds.contains(wpm.getPageId())) {
						continue;
					}
					wpmPageIds.add(wpm.getPageId());
					if (pageScore.containsKey(wpm.getPageId())) {
						pageScore.put(wpm.getPageId(), pageScore.get(wpm.getPageId()) + wc.getY() / sum);
					} else {
						pageScore.put(wpm.getPageId(), wc.getY() / sum);
					}
				}
			}
		}
		pageScore = MapSortUtil.sortByValueDesc(pageScore);
		int pageCounter = 0;
		for (Map.Entry<Integer, Double> entry : pageScore.entrySet()) {
			if (pageCounter >= COUNT_40) {
				break;
			}
			pageCounter++;
			Page p = simpleWikiDb.getPage(entry.getKey());
			if (p.isDisambiguation()) {
				continue;
			}
			int pageId = entry.getKey();
			if (!pageName.containsKey(pageId)) {
				pageName.put(pageId, p.getTitle().getEntity());
			}
			if (!pageIdCategoryId.containsKey(pageId)) {
				pageIdCategoryId.put(pageId, new HashSet<>());
			}
			for (Category parentCategory : p.getCategories()) {
				int parentCategoryId = parentCategory.getPageId();
				if (WikipediaUtil.isGenericWikipediaCategory(parentCategoryId)) {
					continue;
				}
				if (!categoryName.containsKey(parentCategoryId)) {
					categoryName.put(parentCategoryId, parentCategory.getTitle().getEntity());
				}
				if (categoryScore.containsKey(parentCategoryId)) {
					categoryScore.put(parentCategoryId, categoryScore.get(parentCategoryId) + pageScore.get(pageId));
				} else {
					categoryScore.put(parentCategoryId, pageScore.get(pageId));
				}
				pageIdCategoryId.get(pageId).add(parentCategoryId);
			}

		}
		categoryScore = MapSortUtil.sortByValueDesc(categoryScore);
		Map<Integer, Double> topCategories = new LinkedHashMap<>();
		int categoryCounter = 0;
		for (Map.Entry<Integer, Double> entry : categoryScore.entrySet()) {
			if (++categoryCounter > categoryCount) {
				break;
			}
			String catgoryName = categoryName.get(entry.getKey());
			JSONObject themeJson = new JSONObject();
			themeJson.put("type", "theme");
			themeJson.put("name", "Ctg: " + catgoryName);
			themeJson.put("description", "Parent Category : " + catgoryName);
			themeJson.put("slug", "Ctg-" + catgoryName.replaceAll("\\s+", "-"));
			themes.put(themeJson);
			topCategories.put(entry.getKey(), entry.getValue());
		}

		for (Map.Entry<Integer, String> entry : pageName.entrySet()) {
			//package ditems
			ditemId++;
			JSONObject dItemJson = new JSONObject();
			JSONArray ditemsLinks = new JSONArray();
			dItemJson.put("type", "ditem");
			dItemJson.put("name", entry.getValue());
			dItemJson.put("description", "Interest: " + entry.getValue());
			dItemJson.put("ditem", ditemId);
			dItemJson.put("slug", "Page-" + entry.getValue().replaceAll("\\s+", "-"));
			for (int categoryId : pageIdCategoryId.get(entry.getKey())) {
				if (!topCategories.containsKey(categoryId)) {
					continue;
				}
				String catgoryName = categoryName.get(categoryId);
				ditemsLinks.put("Ctg: " + catgoryName);
			}
			dItemJson.put("links", ditemsLinks);
			ditems.put(dItemJson);
		}

		//categories
		conceptMapJsonData.put("ditems", ditems);
		//Interests
		conceptMapJsonData.put("themes", themes);
		//siblings
		conceptMapJsonData.put("perspectives", perspectives);
		return conceptMapJsonData;
	}

	public JSONObject getConceptMapJsonForLatentParentDescendentCategories(List<WordCount> wordCounts,
			int categoryCount, boolean filterCommonCategories) throws WikiApiException, JSONException {
		JSONObject conceptMapJsonData = new JSONObject();
		JSONArray themes = new JSONArray();
		JSONArray perspectives = new JSONArray();
		JSONArray ditems = new JSONArray();
		Map<Integer, Double> pageScore = new HashMap<>();
		Map<Integer, String> pageName = new HashMap<>();
		Map<Integer, Double> categoryScore = new HashMap<>();
		Map<Integer, Double> descCategoryScore = new HashMap<>();
		Map<Integer, String> categoryName = new HashMap<>();
		Map<Integer, Set<Integer>> pageIdCategoryId = new HashMap<>();
		Map<Integer, Set<Integer>> pageIdDescCategoryId = new HashMap<>();
		int ditemId = 0;
		double sum = 0.0;
		for (WordCount wc : wordCounts) {
			sum += wc.getY();
		}
		for (WordCount wc : wordCounts) {
			if (simpleWikiDb.existsPage(wc.getX())) {
				Page p = simpleWikiDb.getPage(wc.getX());
				if (pageScore.containsKey(p.getPageId())) {
					pageScore.put(p.getPageId(), pageScore.get(p.getPageId()) + wc.getY() / sum);
				} else {
					pageScore.put(p.getPageId(), wc.getY() / sum);
				}
			} else {
				Set<Integer> wpmPageIds = new HashSet<>();
				List<WikiPagemapline> wpms = wikiPagemaplineRepository
						.findByName(WikipediaUtil.toWikipediaArticleName(wc.getX()));
				if (wpms.isEmpty()) {
					wpms = wikiPagemaplineRepository.findByStem(WikipediaUtil.toWikipediaArticleStem(wc.getX()));
				}
				for (WikiPagemapline wpm : wpms) {
					if (wpmPageIds.contains(wpm.getPageId())) {
						continue;
					}
					wpmPageIds.add(wpm.getPageId());
					if (pageScore.containsKey(wpm.getPageId())) {
						pageScore.put(wpm.getPageId(), pageScore.get(wpm.getPageId()) + wc.getY() / sum);
					} else {
						pageScore.put(wpm.getPageId(), wc.getY() / sum);
					}
				}
			}
		}
		pageScore = MapSortUtil.sortByValueDesc(pageScore);
		int pageCounter = 0;
		for (Map.Entry<Integer, Double> entry : pageScore.entrySet()) {
			if (pageCounter >= COUNT_40) {
				break;
			}
			pageCounter++;
			Page p = simpleWikiDb.getPage(entry.getKey());
			if (p.isDisambiguation()) {
				continue;
			}
			int pageId = entry.getKey();
			if (!pageName.containsKey(pageId)) {
				pageName.put(pageId, p.getTitle().getEntity());
			}
			if (!pageIdCategoryId.containsKey(pageId)) {
				pageIdCategoryId.put(pageId, new HashSet<>());
			}
			if (!pageIdDescCategoryId.containsKey(pageId)) {
				pageIdDescCategoryId.put(pageId, new HashSet<>());
			}
			for (Category parentCategory : p.getCategories()) {
				int parentCategoryId = parentCategory.getPageId();
				if (WikipediaUtil.isGenericWikipediaCategory(parentCategoryId)) {
					continue;
				}
				if (!categoryName.containsKey(parentCategoryId)) {
					categoryName.put(parentCategoryId, parentCategory.getTitle().getEntity());
				}
				if (categoryScore.containsKey(parentCategoryId)) {
					categoryScore.put(parentCategoryId, categoryScore.get(parentCategoryId) + pageScore.get(pageId));
				} else {
					categoryScore.put(parentCategoryId, pageScore.get(pageId));
				}
				pageIdCategoryId.get(pageId).add(parentCategoryId);

				//get descendent categories
				for (Category descedentCategory : parentCategory.getChildren()) {
					int descendentCategoryId = parentCategory.getPageId();
					if (WikipediaUtil.isGenericWikipediaCategory(descendentCategoryId)) {
						continue;
					}
					if (!categoryName.containsKey(descendentCategoryId)) {
						categoryName.put(descendentCategoryId, descedentCategory.getTitle().getEntity());
					}
					if (descCategoryScore.containsKey(descendentCategoryId)) {
						descCategoryScore.put(descendentCategoryId,
								descCategoryScore.get(descendentCategoryId) + pageScore.get(pageId));
					} else {
						descCategoryScore.put(descendentCategoryId, pageScore.get(pageId));
					}
					pageIdDescCategoryId.get(pageId).add(descendentCategoryId);
				}
				descCategoryScore.remove(parentCategoryId);
			}

		}
		categoryScore = MapSortUtil.sortByValueDesc(categoryScore);
		descCategoryScore = MapSortUtil.sortByValueDesc(descCategoryScore);
		Set<Integer> topCategories = new HashSet<>();
		Set<Integer> parentCategories = new HashSet<>();
		int categoryCounter = 0;
		for (Map.Entry<Integer, Double> entry : categoryScore.entrySet()) {
			if (++categoryCounter > categoryCount) {
				break;
			}
			String catgoryName = categoryName.get(entry.getKey());
			JSONObject themeJson = new JSONObject();
			themeJson.put("type", "theme");
			themeJson.put("name", "Ctg: " + catgoryName);
			themeJson.put("description", "Parent Category : " + catgoryName);
			themeJson.put("slug", "Ctg-" + catgoryName.replaceAll("\\s+", "-"));
			themes.put(themeJson);
			topCategories.add(entry.getKey());
			parentCategories.add(entry.getKey());
		}
		categoryCounter = 0;
		for (Map.Entry<Integer, Double> entry : descCategoryScore.entrySet()) {
			//filter parent categories
			if (filterCommonCategories && parentCategories.contains(entry.getKey())) {
				continue;
			}
			if (++categoryCounter > categoryCount / 2) {
				break;
			}
			String catgoryName = categoryName.get(entry.getKey());
			JSONObject themeJson = new JSONObject();
			themeJson.put("type", "theme");
			themeJson.put("name", "DCtg: " + catgoryName);
			themeJson.put("description", "Descendent Category : " + catgoryName);
			themeJson.put("slug", "DCtg-" + catgoryName.replaceAll("\\s+", "-"));
			themes.put(themeJson);
			topCategories.add(entry.getKey());
		}

		for (Map.Entry<Integer, String> entry : pageName.entrySet()) {
			//package ditems
			ditemId++;
			JSONObject dItemJson = new JSONObject();
			JSONArray ditemsLinks = new JSONArray();
			dItemJson.put("type", "ditem");
			dItemJson.put("name", entry.getValue());
			dItemJson.put("description", "Interest: " + entry.getValue());
			dItemJson.put("ditem", ditemId);
			dItemJson.put("slug", "Page-" + entry.getValue().replaceAll("\\s+", "-"));
			for (int categoryId : pageIdCategoryId.get(entry.getKey())) {
				if (!topCategories.contains(categoryId)) {
					continue;
				}
				String catgoryName = categoryName.get(categoryId);
				ditemsLinks.put("Ctg: " + catgoryName);
			}
			for (int categoryId : pageIdDescCategoryId.get(entry.getKey())) {
				if (!topCategories.contains(categoryId)) {
					continue;
				}
				String catgoryName = categoryName.get(categoryId);
				ditemsLinks.put("DCtg: " + catgoryName);
			}
			dItemJson.put("links", ditemsLinks);
			ditems.put(dItemJson);
		}

		//categories
		conceptMapJsonData.put("ditems", ditems);
		//Interests
		conceptMapJsonData.put("themes", themes);
		//siblings
		conceptMapJsonData.put("perspectives", perspectives);
		return conceptMapJsonData;
	}

	public JSONObject getConceptMapJsonForLatentParentSiblingCategories(List<WordCount> wordCounts, int categoryCount,
			boolean filterCommonCategories) throws WikiApiException, JSONException {
		JSONObject conceptMapJsonData = new JSONObject();
		JSONArray themes = new JSONArray();
		JSONArray perspectives = new JSONArray();
		JSONArray ditems = new JSONArray();
		Map<Integer, Double> pageScore = new HashMap<>();
		Map<Integer, String> pageName = new HashMap<>();
		Map<Integer, Double> categoryScore = new HashMap<>();
		Map<Integer, Double> siblingCategoryScore = new HashMap<>();
		Map<Integer, String> categoryName = new HashMap<>();
		Map<Integer, Set<Integer>> pageIdCategoryId = new HashMap<>();
		Map<Integer, Set<Integer>> pageIdSibCategoryId = new HashMap<>();
		int ditemId = 0;
		double sum = 0.0;
		for (WordCount wc : wordCounts) {
			sum += wc.getY();
		}
		for (WordCount wc : wordCounts) {
			if (simpleWikiDb.existsPage(wc.getX())) {
				Page p = simpleWikiDb.getPage(wc.getX());
				if (pageScore.containsKey(p.getPageId())) {
					pageScore.put(p.getPageId(), pageScore.get(p.getPageId()) + wc.getY() / sum);
				} else {
					pageScore.put(p.getPageId(), wc.getY() / sum);
				}
			} else {
				Set<Integer> wpmPageIds = new HashSet<>();
				List<WikiPagemapline> wpms = wikiPagemaplineRepository
						.findByName(WikipediaUtil.toWikipediaArticleName(wc.getX()));
				if (wpms.isEmpty()) {
					wpms = wikiPagemaplineRepository.findByStem(WikipediaUtil.toWikipediaArticleStem(wc.getX()));
				}
				for (WikiPagemapline wpm : wpms) {
					if (wpmPageIds.contains(wpm.getPageId())) {
						continue;
					}
					wpmPageIds.add(wpm.getPageId());
					if (pageScore.containsKey(wpm.getPageId())) {
						pageScore.put(wpm.getPageId(), pageScore.get(wpm.getPageId()) + wc.getY() / sum);
					} else {
						pageScore.put(wpm.getPageId(), wc.getY() / sum);
					}
				}
			}
		}
		pageScore = MapSortUtil.sortByValueDesc(pageScore);
		int pageCounter = 0;
		for (Map.Entry<Integer, Double> entry : pageScore.entrySet()) {
			if (pageCounter >= COUNT_40) {
				break;
			}
			pageCounter++;
			Page p = simpleWikiDb.getPage(entry.getKey());
			if (p.isDisambiguation()) {
				continue;
			}
			int pageId = entry.getKey();
			if (!pageName.containsKey(pageId)) {
				pageName.put(pageId, p.getTitle().getEntity());
			}
			if (!pageIdCategoryId.containsKey(pageId)) {
				pageIdCategoryId.put(pageId, new HashSet<>());
			}
			if (!pageIdSibCategoryId.containsKey(pageId)) {
				pageIdSibCategoryId.put(pageId, new HashSet<>());
			}
			for (Category parentCategory : p.getCategories()) {
				int parentCategoryId = parentCategory.getPageId();
				if (WikipediaUtil.isGenericWikipediaCategory(parentCategoryId)) {
					continue;
				}
				if (!categoryName.containsKey(parentCategoryId)) {
					categoryName.put(parentCategoryId, parentCategory.getTitle().getEntity());
				}
				if (categoryScore.containsKey(parentCategoryId)) {
					categoryScore.put(parentCategoryId, categoryScore.get(parentCategoryId) + pageScore.get(pageId));
				} else {
					categoryScore.put(parentCategoryId, pageScore.get(pageId));
				}
				pageIdCategoryId.get(pageId).add(parentCategoryId);
				//get sibling categories
				for (Category siblingCategory : parentCategory.getSiblings()) {
					int siblingCategoryId = parentCategory.getPageId();
					if (WikipediaUtil.isGenericWikipediaCategory(siblingCategoryId)) {
						continue;
					}
					if (!categoryName.containsKey(siblingCategoryId)) {
						categoryName.put(siblingCategoryId, siblingCategory.getTitle().getEntity());
					}
					if (siblingCategoryScore.containsKey(siblingCategoryId)) {
						siblingCategoryScore.put(siblingCategoryId,
								siblingCategoryScore.get(siblingCategoryId) + pageScore.get(pageId));
					} else {
						siblingCategoryScore.put(siblingCategoryId, pageScore.get(pageId));
					}
					pageIdSibCategoryId.get(pageId).add(siblingCategoryId);
				}

			}

		}
		categoryScore = MapSortUtil.sortByValueDesc(categoryScore);
		siblingCategoryScore = MapSortUtil.sortByValueDesc(siblingCategoryScore);
		Set<Integer> topCategories = new HashSet<>();
		Set<Integer> parentCategories = new HashSet<>();
		int categoryCounter = 0;
		for (Map.Entry<Integer, Double> entry : categoryScore.entrySet()) {
			if (++categoryCounter > categoryCount) {
				break;
			}
			String catgoryName = categoryName.get(entry.getKey());
			JSONObject themeJson = new JSONObject();
			themeJson.put("type", "theme");
			themeJson.put("name", "Ctg: " + catgoryName);
			themeJson.put("description", "Parent Category : " + catgoryName);
			themeJson.put("slug", "Ctg-" + catgoryName.replaceAll("\\s+", "-"));
			themes.put(themeJson);
			topCategories.add(entry.getKey());
			parentCategories.add(entry.getKey());
		}
		categoryCounter = 0;
		for (Map.Entry<Integer, Double> entry : siblingCategoryScore.entrySet()) {
			//filter parent categories
			if (filterCommonCategories && parentCategories.contains(entry.getKey())) {
				continue;
			}
			if (++categoryCounter > categoryCount / 2) {
				break;
			}
			String catgoryName = categoryName.get(entry.getKey());
			JSONObject themeJson = new JSONObject();
			themeJson.put("type", "theme");
			themeJson.put("name", "SCtg: " + catgoryName);
			themeJson.put("description", "Sibling Category : " + catgoryName);
			themeJson.put("slug", "SCtg-" + catgoryName.replaceAll("\\s+", "-"));
			themes.put(themeJson);
			topCategories.add(entry.getKey());
		}

		for (Map.Entry<Integer, String> entry : pageName.entrySet()) {
			//package ditems
			ditemId++;
			JSONObject dItemJson = new JSONObject();
			JSONArray ditemsLinks = new JSONArray();
			dItemJson.put("type", "ditem");
			dItemJson.put("name", entry.getValue());
			dItemJson.put("description", "Interest: " + entry.getValue());
			dItemJson.put("ditem", ditemId);
			dItemJson.put("slug", "Page-" + entry.getValue().replaceAll("\\s+", "-"));
			for (int categoryId : pageIdCategoryId.get(entry.getKey())) {
				if (!topCategories.contains(categoryId)) {
					continue;
				}
				String catgoryName = categoryName.get(categoryId);
				ditemsLinks.put("Ctg: " + catgoryName);
			}
			for (int categoryId : pageIdSibCategoryId.get(entry.getKey())) {
				if (!topCategories.contains(categoryId)) {
					continue;
				}
				String catgoryName = categoryName.get(categoryId);
				ditemsLinks.put("SCtg: " + catgoryName);
			}

			dItemJson.put("links", ditemsLinks);
			ditems.put(dItemJson);
		}

		//categories
		conceptMapJsonData.put("ditems", ditems);
		//Interests
		conceptMapJsonData.put("themes", themes);
		//siblings
		conceptMapJsonData.put("perspectives", perspectives);
		return conceptMapJsonData;
	}

	public JSONObject getConceptMapJsonForLatentParentSiblingDescendentCategories(List<WordCount> wordCounts,
			int categoryCount, boolean filterCommonCategories) throws WikiApiException, JSONException {
		JSONObject conceptMapJsonData = new JSONObject();
		JSONArray themes = new JSONArray();
		JSONArray perspectives = new JSONArray();
		JSONArray ditems = new JSONArray();
		Map<Integer, Double> pageScore = new HashMap<>();
		Map<Integer, String> pageName = new HashMap<>();
		Map<Integer, Double> categoryScore = new HashMap<>();
		Map<Integer, Double> siblingCategoryScore = new HashMap<>();
		Map<Integer, Double> descendentCategoryScore = new HashMap<>();
		Map<Integer, String> categoryName = new HashMap<>();
		Map<Integer, Set<Integer>> pageIdCategoryId = new HashMap<>();
		Map<Integer, Set<Integer>> pageIdDescCategoryId = new HashMap<>();
		Map<Integer, Set<Integer>> pageIdSibCategoryId = new HashMap<>();
		int ditemId = 0;
		double sum = 0.0;
		for (WordCount wc : wordCounts) {
			sum += wc.getY();
		}
		for (WordCount wc : wordCounts) {
			if (simpleWikiDb.existsPage(wc.getX())) {
				Page p = simpleWikiDb.getPage(wc.getX());
				if (pageScore.containsKey(p.getPageId())) {
					pageScore.put(p.getPageId(), pageScore.get(p.getPageId()) + wc.getY() / sum);
				} else {
					pageScore.put(p.getPageId(), wc.getY() / sum);
				}
			} else {
				Set<Integer> wpmPageIds = new HashSet<>();
				List<WikiPagemapline> wpms = wikiPagemaplineRepository
						.findByName(WikipediaUtil.toWikipediaArticleName(wc.getX()));
				if (wpms.isEmpty()) {
					wpms = wikiPagemaplineRepository.findByStem(WikipediaUtil.toWikipediaArticleStem(wc.getX()));
				}
				for (WikiPagemapline wpm : wpms) {
					if (wpmPageIds.contains(wpm.getPageId())) {
						continue;
					}
					wpmPageIds.add(wpm.getPageId());
					if (pageScore.containsKey(wpm.getPageId())) {
						pageScore.put(wpm.getPageId(), pageScore.get(wpm.getPageId()) + wc.getY() / sum);
					} else {
						pageScore.put(wpm.getPageId(), wc.getY() / sum);
					}
				}
			}
		}
		pageScore = MapSortUtil.sortByValueDesc(pageScore);
		int pageCounter = 0;
		for (Map.Entry<Integer, Double> entry : pageScore.entrySet()) {
			if (pageCounter >= COUNT_40) {
				break;
			}
			pageCounter++;
			Page p = simpleWikiDb.getPage(entry.getKey());
			if (p.isDisambiguation()) {
				continue;
			}
			int pageId = entry.getKey();
			if (!pageName.containsKey(pageId)) {
				pageName.put(pageId, p.getTitle().getEntity());
			}
			if (!pageIdCategoryId.containsKey(pageId)) {
				pageIdCategoryId.put(pageId, new HashSet<>());
			}
			if (!pageIdSibCategoryId.containsKey(pageId)) {
				pageIdSibCategoryId.put(pageId, new HashSet<>());
			}
			if (!pageIdDescCategoryId.containsKey(pageId)) {
				pageIdDescCategoryId.put(pageId, new HashSet<>());
			}
			for (Category parentCategory : p.getCategories()) {
				int parentCategoryId = parentCategory.getPageId();
				if (WikipediaUtil.isGenericWikipediaCategory(parentCategoryId)) {
					continue;
				}
				if (!categoryName.containsKey(parentCategoryId)) {
					categoryName.put(parentCategoryId, parentCategory.getTitle().getEntity());
				}
				if (categoryScore.containsKey(parentCategoryId)) {
					categoryScore.put(parentCategoryId, categoryScore.get(parentCategoryId) + pageScore.get(pageId));
				} else {
					categoryScore.put(parentCategoryId, pageScore.get(pageId));
				}
				pageIdCategoryId.get(pageId).add(parentCategoryId);
				//get sibling categories
				for (Category siblingCategory : parentCategory.getSiblings()) {
					int siblingCategoryId = parentCategory.getPageId();
					if (WikipediaUtil.isGenericWikipediaCategory(siblingCategoryId)) {
						continue;
					}
					if (!categoryName.containsKey(siblingCategoryId)) {
						categoryName.put(siblingCategoryId, siblingCategory.getTitle().getEntity());
					}
					if (siblingCategoryScore.containsKey(siblingCategoryId)) {
						siblingCategoryScore.put(siblingCategoryId,
								siblingCategoryScore.get(siblingCategoryId) + pageScore.get(pageId));
					} else {
						siblingCategoryScore.put(siblingCategoryId, pageScore.get(pageId));
					}
					pageIdSibCategoryId.get(pageId).add(siblingCategoryId);
				}
				//get descendent categories
				for (Category descendentCategory : parentCategory.getChildren()) {
					int descendentCategoryId = parentCategory.getPageId();
					if (WikipediaUtil.isGenericWikipediaCategory(descendentCategoryId)) {
						continue;
					}
					if (!categoryName.containsKey(descendentCategoryId)) {
						categoryName.put(descendentCategoryId, descendentCategory.getTitle().getEntity());
					}
					if (descendentCategoryScore.containsKey(descendentCategoryId)) {
						descendentCategoryScore.put(descendentCategoryId,
								descendentCategoryScore.get(descendentCategoryId) + pageScore.get(pageId));
					} else {
						descendentCategoryScore.put(descendentCategoryId, pageScore.get(pageId));
					}
					pageIdDescCategoryId.get(pageId).add(descendentCategoryId);
				}
			}

		}
		categoryScore = MapSortUtil.sortByValueDesc(categoryScore);
		siblingCategoryScore = MapSortUtil.sortByValueDesc(siblingCategoryScore);
		descendentCategoryScore = MapSortUtil.sortByValueDesc(descendentCategoryScore);
		Set<Integer> topCategories = new HashSet<>();
		Set<Integer> parentCategories = new HashSet<>();
		int categoryCounter = 0;
		for (Map.Entry<Integer, Double> entry : categoryScore.entrySet()) {

			if (++categoryCounter > categoryCount) {
				break;
			}
			String catgoryName = categoryName.get(entry.getKey());
			JSONObject themeJson = new JSONObject();
			themeJson.put("type", "theme");
			themeJson.put("name", "Ctg: " + catgoryName);
			themeJson.put("description", "Parent Category : " + catgoryName);
			themeJson.put("slug", "Ctg-" + catgoryName.replaceAll("\\s+", "-"));
			themes.put(themeJson);
			topCategories.add(entry.getKey());
			parentCategories.add(entry.getKey());
		}
		categoryCounter = 0;
		for (Map.Entry<Integer, Double> entry : siblingCategoryScore.entrySet()) {
			//filter parent categories
			if (filterCommonCategories && parentCategories.contains(entry.getKey())) {
				continue;
			}
			if (++categoryCounter > categoryCount / 2) {
				break;
			}
			String catgoryName = categoryName.get(entry.getKey());
			JSONObject themeJson = new JSONObject();
			themeJson.put("type", "theme");
			themeJson.put("name", "SCtg: " + catgoryName);
			themeJson.put("description", "Sibling Category : " + catgoryName);
			themeJson.put("slug", "SCtg-" + catgoryName.replaceAll("\\s+", "-"));
			themes.put(themeJson);
			topCategories.add(entry.getKey());
		}
		categoryCounter = 0;
		for (Map.Entry<Integer, Double> entry : descendentCategoryScore.entrySet()) {
			if (filterCommonCategories && parentCategories.contains(entry.getKey())) {
				continue;
			}
			if (++categoryCounter > categoryCount / 2) {
				break;
			}
			String catgoryName = categoryName.get(entry.getKey());
			JSONObject themeJson = new JSONObject();
			themeJson.put("type", "theme");
			themeJson.put("name", "DCtg: " + catgoryName);
			themeJson.put("description", "Descendent Category : " + catgoryName);
			themeJson.put("slug", "DCtg-" + catgoryName.replaceAll("\\s+", "-"));
			themes.put(themeJson);
			topCategories.add(entry.getKey());
		}

		for (Map.Entry<Integer, String> entry : pageName.entrySet()) {
			//package ditems
			ditemId++;
			JSONObject dItemJson = new JSONObject();
			JSONArray ditemsLinks = new JSONArray();
			dItemJson.put("type", "ditem");
			dItemJson.put("name", entry.getValue());
			dItemJson.put("description", "Interest: " + entry.getValue());
			dItemJson.put("ditem", ditemId);
			dItemJson.put("slug", "Page-" + entry.getValue().replaceAll("\\s+", "-"));
			for (int categoryId : pageIdCategoryId.get(entry.getKey())) {
				if (!topCategories.contains(categoryId)) {
					continue;
				}
				String catgoryName = categoryName.get(categoryId);
				ditemsLinks.put("Ctg: " + catgoryName);
			}
			for (int categoryId : pageIdSibCategoryId.get(entry.getKey())) {
				if (!topCategories.contains(categoryId)) {
					continue;
				}
				String catgoryName = categoryName.get(categoryId);
				ditemsLinks.put("SCtg: " + catgoryName);
			}
			for (int categoryId : pageIdDescCategoryId.get(entry.getKey())) {
				if (!topCategories.contains(categoryId)) {
					continue;
				}
				String catgoryName = categoryName.get(categoryId);
				ditemsLinks.put("DCtg: " + catgoryName);
			}
			dItemJson.put("links", ditemsLinks);
			ditems.put(dItemJson);
		}

		//categories
		conceptMapJsonData.put("ditems", ditems);
		//Interests
		conceptMapJsonData.put("themes", themes);
		//siblings
		conceptMapJsonData.put("perspectives", perspectives);
		return conceptMapJsonData;
	}

}
