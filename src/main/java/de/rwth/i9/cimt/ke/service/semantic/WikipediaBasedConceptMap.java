package de.rwth.i9.cimt.ke.service.semantic;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tika.utils.ExceptionUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.rwth.i9.cimt.ke.util.WikipediaUtil;
import de.tudarmstadt.ukp.wikipedia.api.Category;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;

@Service("wbConceptMap")
public class WikipediaBasedConceptMap {

	private static final Logger log = LoggerFactory.getLogger(WikipediaBasedConceptMap.class);

	@Autowired
	Wikipedia simpleWikiDb;

	@SuppressWarnings("unchecked")
	public JSONObject getConceptMapJsonForInterests(List<String> interests) throws JSONException {
		JSONObject conceptMapJsonData = new JSONObject();
		Set<String> uniqueInterests = new HashSet<>(interests);
		Map<Integer, JSONObject> pageIdDItemMap = new HashMap<>();
		Map<Integer, JSONObject> pageIdParentThemeMap = new HashMap<>();
		Map<Integer, JSONObject> pageIdSiblingThemeMap = new HashMap<>();
		Map<Integer, Integer> pageIdSiblingCountMap = new HashMap<>();
		Map<Integer, Set<String>> pageCategoryMap = new HashMap<>();
		JSONArray themes = new JSONArray();
		JSONArray perspectives = new JSONArray();
		JSONArray ditems = new JSONArray();
		JSONArray ditemsLinks = new JSONArray();
		int ditemId = 0;

		for (String interest : uniqueInterests) {
			try {
				//get wikipedia page for interest and create an item in json format required by d3 concept map
				Page p = simpleWikiDb.getPage(interest);
				int pageId = p.getPageId();
				if (!pageIdDItemMap.containsKey(p.getPageId())) {
					JSONObject dItemJson = new JSONObject();
					ditemId++;
					dItemJson.put("type", "ditem");
					dItemJson.put("name", p.getTitle().getEntity());
					dItemJson.put("description", "Interest: " + p.getTitle().getEntity());
					dItemJson.put("ditem", ditemId);
					dItemJson.put("slug", "page-" + p.getTitle().getEntity().replaceAll("\\s+", "-"));
					dItemJson.put("pageId", pageId);
					pageIdDItemMap.put(pageId, dItemJson);
					pageCategoryMap.put(pageId, new HashSet<>());

					//get parent categories for the wikipedia page and create themes for d3 concept map json
					for (Category parentCategory : p.getCategories()) {
						int parentCategoryPageId = parentCategory.getPageId();
						if (WikipediaUtil.isGenericWikipediaCategory(parentCategoryPageId)) {
							continue;
						}
						if (pageIdParentThemeMap.containsKey(parentCategoryPageId)) {
							pageIdSiblingCountMap.remove(parentCategoryPageId);
							pageIdSiblingThemeMap.remove(parentCategoryPageId);
						}

						if (!pageIdParentThemeMap.containsKey(parentCategoryPageId)) {
							JSONObject themeJson = new JSONObject();
							themeJson.put("type", "theme");
							themeJson.put("name", "Ctg: " + parentCategory.getTitle().getEntity());
							themeJson.put("description", "Parent Category : " + parentCategory.getTitle().getEntity());
							themeJson.put("slug",
									"category-" + parentCategory.getTitle().getEntity().replaceAll("\\s+", "-"));
							pageIdParentThemeMap.put(parentCategoryPageId, themeJson);
						}
						Set<String> links = pageCategoryMap.get(pageId);
						links.add("Ctg: " + parentCategory.getTitle().getEntity());

						//for each category get siblings to be used for creating perspective for d3 concept map json
						// these siblings will be descendents of parent category... so at the same level as the interests provided by user
						for (Category descendentCategory : parentCategory.getChildren()) {
							int descendentPageId = descendentCategory.getPageId();
							if (WikipediaUtil.isGenericWikipediaCategory(parentCategoryPageId)) {
								continue;
							}
							if (!pageIdSiblingThemeMap.containsKey(descendentPageId)) {
								JSONObject themeJson = new JSONObject();
								themeJson.put("type", "theme");
								themeJson.put("name", "Ctg: " + parentCategory.getTitle().getEntity());
								themeJson.put("description",
										"Parent Category : " + parentCategory.getTitle().getEntity());
								themeJson.put("slug",
										"category-" + parentCategory.getTitle().getEntity().replaceAll("\\s+", "-"));
								pageIdSiblingThemeMap.put(parentCategoryPageId, themeJson);

								themeJson.put("type", "perspective");
								themeJson.put("name", "Sblng: " + descendentCategory.getTitle().getEntity());
								themeJson.put("description",
										"Sibling Category : " + descendentCategory.getTitle().getEntity());
								themeJson.put("slug", "sibling-category-"
										+ descendentCategory.getTitle().getEntity().replaceAll("\\s+", "-"));
								pageIdSiblingThemeMap.put(descendentPageId, themeJson);
								pageIdSiblingCountMap.put(descendentPageId, 1);
							} else if (pageIdSiblingCountMap.containsKey(descendentPageId)) {
								pageIdSiblingCountMap.put(descendentPageId,
										pageIdSiblingCountMap.get(descendentPageId) + 1);
							}
							links.add("Sblng: " + descendentCategory.getTitle().getEntity());

						}
					}

				}

			} catch (WikiApiException ex) {
				log.debug(ExceptionUtils.getStackTrace(ex));

			}

		}

		// create themes json array for d3 concept map (wikipedia categories)
		Set<String> selectedCategoriesString = new HashSet<>();
		for (Map.Entry<Integer, JSONObject> theme : pageIdParentThemeMap.entrySet()) {
			themes.put(theme.getValue());
			selectedCategoriesString.add(theme.getValue().getString("name"));
		}

		//create sibling json array for d3 concept map
		int siblingCategorySizeLimit = pageIdParentThemeMap.size();
		Object[] a = pageIdSiblingCountMap.entrySet().toArray();
		Arrays.sort(a, new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
				return ((Map.Entry<Integer, Integer>) o2).getValue()
						.compareTo(((Map.Entry<Integer, Integer>) o1).getValue());
			}
		});

		// add only perspectiveJsonSizeLimit perspectives which are common accross other categories

		for (int iter = 0; iter < a.length && iter < siblingCategorySizeLimit; iter++) {
			Map.Entry<Integer, Integer> entry = (Map.Entry<Integer, Integer>) a[iter];
			JSONObject themeJson = pageIdSiblingThemeMap.get(entry.getKey());
			themes.put(themeJson);
			selectedCategoriesString.add(themeJson.getString("name"));
		}

		//create ditems json array for d3 concept map with links to sibling/parent categories
		for (Map.Entry<Integer, JSONObject> ditem : pageIdDItemMap.entrySet()) {
			JSONObject ditemJsonObject = ditem.getValue();
			Set<String> refinedLinks = new HashSet<>(pageCategoryMap.get(ditemJsonObject.getInt("pageId")));
			refinedLinks.retainAll(selectedCategoriesString);
			// add links to ditem
			ditemsLinks = new JSONArray(refinedLinks);
			ditemJsonObject.put("links", ditemsLinks);
			ditemJsonObject.remove("pageId");
			ditems.put(ditem.getValue());
		}
		//categories
		conceptMapJsonData.put("ditems", ditems);
		//Interests
		conceptMapJsonData.put("themes", themes);
		//siblings
		conceptMapJsonData.put("perspectives", perspectives);
		return conceptMapJsonData;
	}

	public JSONObject getConceptMapJsonForInterestsOld(List<String> interests) throws JSONException {
		JSONObject conceptMapJsonData = new JSONObject();
		Set<String> uniqueInterests = new HashSet<>(interests);
		Map<Integer, JSONObject> pageIdDItemMap = new HashMap<>();
		Map<Integer, JSONObject> pageIdThemeMap = new HashMap<>();
		Map<Integer, JSONObject> pageIdPerspectiveMap = new HashMap<>();
		Map<Integer, Integer> pageIdPerspectiveCountMap = new HashMap<>();
		Map<Integer, Set<String>> pageCategoryMap = new HashMap<>();
		JSONArray themes = new JSONArray();
		JSONArray perspectives = new JSONArray();
		JSONArray ditems = new JSONArray();
		JSONArray ditemsLinks = new JSONArray();
		int ditemId = 0;
		int group = interests.size();

		for (String interest : uniqueInterests) {
			try {
				//get wikipedia page for interest and create an item in json format required by d3 concept map
				Page p = simpleWikiDb.getPage(interest);
				int pageId = p.getPageId();
				if (!pageIdDItemMap.containsKey(p.getPageId())) {
					JSONObject dItemJson = new JSONObject();
					ditemId++;
					group++;
					dItemJson.put("type", "ditem");
					dItemJson.put("name", p.getTitle().getEntity());
					dItemJson.put("description", "Interest: " + p.getTitle().getEntity());
					dItemJson.put("ditem", ditemId);
					dItemJson.put("slug", "page-" + p.getTitle().getEntity().replaceAll("\\s+", "-"));
					dItemJson.put("pageId", pageId);
					pageIdDItemMap.put(pageId, dItemJson);
					pageCategoryMap.put(pageId, new HashSet<>());

					//get parent categories for the wikipedia page and create themes for d3 concept map json
					for (Category parentCategory : p.getCategories()) {
						int parentCategoryPageId = parentCategory.getPageId();
						if (WikipediaUtil.isGenericWikipediaCategory(parentCategoryPageId)) {
							continue;
						}

						if (!pageIdThemeMap.containsKey(parentCategoryPageId)) {
							JSONObject themeJson = new JSONObject();
							themeJson.put("type", "theme");
							themeJson.put("name", "Ctg: " + parentCategory.getTitle().getEntity());
							themeJson.put("description", "Parent Category : " + parentCategory.getTitle().getEntity());
							themeJson.put("slug",
									"category-" + parentCategory.getTitle().getEntity().replaceAll("\\s+", "-"));
							pageIdThemeMap.put(parentCategoryPageId, themeJson);
						}
						Set<String> links = pageCategoryMap.get(pageId);
						links.add("Ctg: " + parentCategory.getTitle().getEntity());

						//for each category get siblings to be used for creating perspective for d3 concept map json
						// these siblings will be descendents of parent category... so at the same level as the interests provided by user
						for (Category descendentCategory : parentCategory.getChildren()) {
							int descendentPageId = descendentCategory.getPageId();
							if (WikipediaUtil.isGenericWikipediaCategory(parentCategoryPageId)) {
								continue;
							}

							JSONObject perspectiveJson = new JSONObject();
							if (!pageIdPerspectiveMap.containsKey(descendentPageId)) {
								perspectiveJson.put("type", "perspective");
								perspectiveJson.put("name", "Sblng: " + descendentCategory.getTitle().getEntity());
								perspectiveJson.put("description",
										"Sibling Category : " + descendentCategory.getTitle().getEntity());
								perspectiveJson.put("slug", "sibling-category-"
										+ descendentCategory.getTitle().getEntity().replaceAll("\\s+", "-"));
								perspectiveJson.put("group", group);
								perspectiveJson.put("count", 1);
								pageIdPerspectiveMap.put(descendentPageId, perspectiveJson);
								pageIdPerspectiveCountMap.put(descendentPageId, 1);
							} else {
								perspectiveJson = pageIdPerspectiveMap.get(descendentPageId);
								int count = perspectiveJson.getInt("count");
								perspectiveJson.put("count", count + 1);
								pageIdPerspectiveMap.put(descendentPageId, perspectiveJson);
								pageIdPerspectiveCountMap.put(descendentPageId,
										pageIdPerspectiveCountMap.get(descendentPageId) + 1);
							}
							links.add("Sblng: " + descendentCategory.getTitle().getEntity());
						}
					}

				}

			} catch (WikiApiException ex) {
				log.debug(ExceptionUtils.getStackTrace(ex));

			}

		}

		// create themes json array for d3 concept map (wikipedia categories)
		Set<String> selectedProspectiveString = new HashSet<>();
		for (Map.Entry<Integer, JSONObject> theme : pageIdThemeMap.entrySet()) {
			themes.put(theme.getValue());
			selectedProspectiveString.add(theme.getValue().getString("name"));
		}

		//create perspectives json array for d3 concept map
		int perspectiveJsonSizeLimit = pageIdThemeMap.size();
		Object[] a = pageIdPerspectiveCountMap.entrySet().toArray();
		Arrays.sort(a, new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
				return ((Map.Entry<Integer, Integer>) o2).getValue()
						.compareTo(((Map.Entry<Integer, Integer>) o1).getValue());
			}
		});

		// add only perspectiveJsonSizeLimit perspectives which are common accross other categories

		for (int iter = 0; iter < a.length && iter < perspectiveJsonSizeLimit; iter++) {
			Map.Entry<Integer, Integer> entry = (Map.Entry<Integer, Integer>) a[iter];
			JSONObject prospectiveJson = pageIdPerspectiveMap.get(entry.getKey());
			perspectives.put(prospectiveJson);
			selectedProspectiveString.add(prospectiveJson.getString("name"));
		}

		//create ditems json array for d3 concept map 
		for (Map.Entry<Integer, JSONObject> ditem : pageIdDItemMap.entrySet()) {
			JSONObject ditemJsonObject = ditem.getValue();
			Set<String> refinedLinks = new HashSet<>(pageCategoryMap.get(ditemJsonObject.getInt("pageId")));
			refinedLinks.retainAll(selectedProspectiveString);
			// add links to ditem
			ditemsLinks = new JSONArray(refinedLinks);
			ditemJsonObject.put("links", ditemsLinks);
			ditemJsonObject.remove("pageId");
			ditems.put(ditem.getValue());
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
