package de.rwth.i9.cimt.ke.service.semantic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import de.rwth.i9.cimt.ke.lib.util.WordCount;
import de.rwth.i9.cimt.ke.model.wikipedia.WikiPagemapline;
import de.rwth.i9.cimt.ke.repository.wikipedia.WikiPagemaplineRepository;
import de.rwth.i9.cimt.ke.util.MapSortUtil;
import de.rwth.i9.cimt.ke.util.WikipediaUtil;
import de.tudarmstadt.ukp.wikipedia.api.Category;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;

public class DummyClass {
	private static final Logger log = LoggerFactory.getLogger(WikipediaBasedIM.class);

	@Autowired
	private Wikipedia simpleWikiDb;
	@Autowired
	WikiPagemaplineRepository wikiPagemaplineRepository;
	private static final int CATEGORY_COUNT_40 = 40;
	private static final int CATEGORY_COUNT_20 = 20;
	private static final int CATEGORY_COUNT_10 = 10;

	public JSONObject getConceptMapJsonForLatentParentCategories(List<WordCount> wordCounts)
			throws WikiApiException, JSONException {
		JSONObject conceptMapJsonData = new JSONObject();
		Map<Integer, JSONObject> pageIdDItemMap = new HashMap<>();
		Map<Integer, JSONObject> pageIdParentThemeMap = new HashMap<>();
		Map<Integer, Set<String>> pageCategoryMap = new HashMap<>();
		JSONArray themes = new JSONArray();
		JSONArray perspectives = new JSONArray();
		JSONArray ditems = new JSONArray();
		Map<Integer, Category> categoryMap = new HashMap<>();
		Map<Integer, Page> pageMap = new HashMap<>();
		Map<Integer, Double> pageScore = new HashMap<>();
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
		for (Map.Entry<Integer, Double> entry : pageScore.entrySet()) {
			Page p = simpleWikiDb.getPage(entry.getKey());
			if (p.isDisambiguation()) {
				continue;
			}

			int pageId = p.getPageId();
			if (!pageIdDItemMap.containsKey(p.getPageId())) {
				JSONObject dItemJson = new JSONObject();
				JSONArray ditemsLinks = new JSONArray();
				ditemId++;
				dItemJson.put("type", "ditem");
				dItemJson.put("name", p.getTitle().getEntity());
				dItemJson.put("description", "Interest: " + p.getTitle().getEntity());
				dItemJson.put("ditem", ditemId);
				dItemJson.put("slug", "Page-" + p.getTitle().getEntity().replaceAll("\\s+", "-"));
				dItemJson.put("pageId", pageId);
				pageIdDItemMap.put(pageId, dItemJson);
				pageCategoryMap.put(pageId, new HashSet<>());

				//get parent categories for the wikipedia page and create themes for d3 concept map json
				for (Category parentCategory : p.getCategories()) {
					int parentCategoryPageId = parentCategory.getPageId();
					if (WikipediaUtil.isGenericWikipediaCategory(parentCategoryPageId)) {
						continue;
					}

					if (!pageIdParentThemeMap.containsKey(parentCategoryPageId)) {
						JSONObject themeJson = new JSONObject();
						themeJson.put("type", "theme");
						themeJson.put("name", "Ctg: " + parentCategory.getTitle().getEntity());
						themeJson.put("description", "Parent Category : " + parentCategory.getTitle().getEntity());
						themeJson.put("slug", "Ctg-" + parentCategory.getTitle().getEntity().replaceAll("\\s+", "-"));
						ditemsLinks.put(themeJson.getString("name"));
						pageIdParentThemeMap.put(parentCategoryPageId, themeJson);
					} else {
						ditemsLinks.put(pageIdParentThemeMap.get(parentCategoryPageId).getString("name"));
					}

				}
				dItemJson.put("links", ditemsLinks);
				ditems.put(dItemJson);

			}

		}
		//categoryScore = MapSortUtil.sortByValueDesc(categoryScore);
		//pageScore = MapSortUtil.sortByValueDesc(pageScore);
		// create themes json array for d3 concept map (wikipedia categories)
		for (Map.Entry<Integer, JSONObject> theme : pageIdParentThemeMap.entrySet()) {
			themes.put(theme.getValue());
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
