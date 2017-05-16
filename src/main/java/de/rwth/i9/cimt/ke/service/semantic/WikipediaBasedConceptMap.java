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

import de.rwth.i9.cimt.ke.lib.util.WikipediaUtil;
import de.tudarmstadt.ukp.wikipedia.api.Category;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;

@Service("wbConceptMap")
public class WikipediaBasedConceptMap {

	private static final Logger log = LoggerFactory.getLogger(WikipediaBasedConceptMap.class);

	@Autowired
	private Wikipedia simpleWikiDb;

	@SuppressWarnings("unchecked")
	public JSONObject getConceptMapJsonForLatentParentDescendentInterests(List<String> interests) throws JSONException {
		JSONObject conceptMapJsonData = new JSONObject();
		Set<String> uniqueInterests = new HashSet<>(interests);
		Map<Integer, JSONObject> pageIdDItemMap = new HashMap<>();
		Map<Integer, JSONObject> pageIdParentThemeMap = new HashMap<>();
		Map<Integer, JSONObject> pageIdDescendentThemeMap = new HashMap<>();
		Map<Integer, Integer> pageIdDescendentCountMap = new HashMap<>();
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
							pageIdDescendentCountMap.remove(parentCategoryPageId);
							pageIdDescendentThemeMap.remove(parentCategoryPageId);
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

						//for each category get descendent to be used for creating perspective for d3 concept map json
						// these will be descendents of parent category... so in an abstract way, at the same level as the interests provided by user
						for (Category descendentCategory : parentCategory.getChildren()) {
							int descendentPageId = descendentCategory.getPageId();
							if (WikipediaUtil.isGenericWikipediaCategory(parentCategoryPageId)) {
								continue;
							}
							if (!pageIdDescendentThemeMap.containsKey(descendentPageId)) {
								JSONObject themeJson = new JSONObject();
								themeJson.put("type", "theme");
								themeJson.put("name", "DCtg: " + descendentCategory.getTitle().getEntity());
								themeJson.put("description",
										"Descendent Category : " + descendentCategory.getTitle().getEntity());
								themeJson.put("slug", "descendent-category-"
										+ descendentCategory.getTitle().getEntity().replaceAll("\\s+", "-"));
								pageIdDescendentThemeMap.put(descendentPageId, themeJson);
								pageIdDescendentCountMap.put(descendentPageId, 1);
							} else if (pageIdDescendentCountMap.containsKey(descendentPageId)) {
								pageIdDescendentCountMap.put(descendentPageId,
										pageIdDescendentCountMap.get(descendentPageId) + 1);
							}
							links.add(pageIdDescendentThemeMap.get(descendentPageId).getString("name"));

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
		Object[] a = pageIdDescendentCountMap.entrySet().toArray();
		Arrays.sort(a, new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
				return ((Map.Entry<Integer, Integer>) o2).getValue()
						.compareTo(((Map.Entry<Integer, Integer>) o1).getValue());
			}
		});

		// add only perspectiveJsonSizeLimit perspectives which are common accross other categories

		for (int iter = 0; iter < a.length && iter < siblingCategorySizeLimit; iter++) {
			Map.Entry<Integer, Integer> entry = (Map.Entry<Integer, Integer>) a[iter];
			JSONObject themeJson = pageIdDescendentThemeMap.get(entry.getKey());
			themes.put(themeJson);
			selectedCategoriesString.add(themeJson.getString("name"));
		}

		//create ditems json array for d3 concept map with links to descendent/parent categories
		for (Map.Entry<Integer, JSONObject> ditem : pageIdDItemMap.entrySet()) {
			JSONObject ditemJsonObject = ditem.getValue();
			Set<String> allCategoryLinks = new HashSet<>(pageCategoryMap.get(ditemJsonObject.getInt("pageId")));
			Set<String> refinedLinks = new HashSet<>(selectedCategoriesString);
			refinedLinks.retainAll(allCategoryLinks);
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
		conceptMapJsonData.put("perspectives", perspectives);
		return conceptMapJsonData;
	}

	public JSONObject getConceptMapJsonForLatentParentSiblingInterests(List<String> interests) throws JSONException {
		JSONObject conceptMapJsonData = new JSONObject();
		Set<String> uniqueInterests = new HashSet<>(interests);
		Map<Integer, JSONObject> pageIdDItemMap = new HashMap<>();
		Map<Integer, JSONObject> pageIdParentThemeMap = new HashMap<>();
		Map<Integer, JSONObject> pageIdDescendentThemeMap = new HashMap<>();
		Map<Integer, Integer> pageIdDescendentCountMap = new HashMap<>();
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
							pageIdDescendentCountMap.remove(parentCategoryPageId);
							pageIdDescendentThemeMap.remove(parentCategoryPageId);
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

						//for each category get descendent to be used for creating perspective for d3 concept map json
						// these will be descendents of parent category... so in an abstract way, at the same level as the interests provided by user
						for (Category descendentCategory : parentCategory.getChildren()) {
							int descendentPageId = descendentCategory.getPageId();
							if (WikipediaUtil.isGenericWikipediaCategory(parentCategoryPageId)) {
								continue;
							}
							if (!pageIdDescendentThemeMap.containsKey(descendentPageId)) {
								JSONObject themeJson = new JSONObject();
								themeJson.put("type", "theme");
								themeJson.put("name", "SCtg: " + descendentCategory.getTitle().getEntity());
								themeJson.put("description",
										"Sibling Category : " + descendentCategory.getTitle().getEntity());
								themeJson.put("slug", "sibling-category-"
										+ descendentCategory.getTitle().getEntity().replaceAll("\\s+", "-"));
								pageIdDescendentThemeMap.put(descendentPageId, themeJson);
								pageIdDescendentCountMap.put(descendentPageId, 1);
							} else if (pageIdDescendentCountMap.containsKey(descendentPageId)) {
								pageIdDescendentCountMap.put(descendentPageId,
										pageIdDescendentCountMap.get(descendentPageId) + 1);
							}
							links.add("Sctg: " + descendentCategory.getTitle().getEntity());

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
		Object[] a = pageIdDescendentCountMap.entrySet().toArray();
		Arrays.sort(a, new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
				return ((Map.Entry<Integer, Integer>) o2).getValue()
						.compareTo(((Map.Entry<Integer, Integer>) o1).getValue());
			}
		});

		// add only perspectiveJsonSizeLimit perspectives which are common accross other categories

		for (int iter = 0; iter < a.length && iter < siblingCategorySizeLimit; iter++) {
			Map.Entry<Integer, Integer> entry = (Map.Entry<Integer, Integer>) a[iter];
			JSONObject themeJson = pageIdDescendentThemeMap.get(entry.getKey());
			themes.put(themeJson);
			selectedCategoriesString.add(themeJson.getString("name"));
		}

		//create ditems json array for d3 concept map with links to sibling/parent categories
		for (Map.Entry<Integer, JSONObject> ditem : pageIdDItemMap.entrySet()) {
			JSONObject ditemJsonObject = ditem.getValue();
			Set<String> allCategoryLinks = new HashSet<>(pageCategoryMap.get(ditemJsonObject.getInt("pageId")));
			Set<String> refinedLinks = new HashSet<>(selectedCategoriesString);
			refinedLinks.retainAll(allCategoryLinks);
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

	public JSONObject getConceptMapJsonForLatentParentInterests(List<String> interests) throws JSONException {
		JSONObject conceptMapJsonData = new JSONObject();
		Set<String> uniqueInterests = new HashSet<>(interests);
		Map<Integer, JSONObject> pageIdDItemMap = new HashMap<>();
		Map<Integer, JSONObject> pageIdParentThemeMap = new HashMap<>();
		Map<Integer, Set<String>> pageCategoryMap = new HashMap<>();
		JSONArray themes = new JSONArray();
		JSONArray perspectives = new JSONArray();
		JSONArray ditems = new JSONArray();
		int ditemId = 0;

		for (String interest : uniqueInterests) {
			try {
				//get wikipedia page for interest and create an item in json format required by d3 concept map
				Page p = simpleWikiDb.getPage(interest);
				int pageId = p.getPageId();
				if (!pageIdDItemMap.containsKey(p.getPageId())) {
					JSONObject dItemJson = new JSONObject();
					JSONArray ditemsLinks = new JSONArray();
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
						if (!pageIdParentThemeMap.containsKey(parentCategoryPageId)) {
							JSONObject themeJson = new JSONObject();
							themeJson.put("type", "theme");
							themeJson.put("name", "Ctg: " + parentCategory.getTitle().getEntity());
							themeJson.put("description", "Parent Category : " + parentCategory.getTitle().getEntity());
							themeJson.put("slug",
									"category-" + parentCategory.getTitle().getEntity().replaceAll("\\s+", "-"));
							ditemsLinks.put(themeJson.getString("name"));
							pageIdParentThemeMap.put(parentCategoryPageId, themeJson);
						} else {
							ditemsLinks.put("Ctg: " + pageIdParentThemeMap.get(parentCategoryPageId).getString("name"));
						}

					}
					dItemJson.put("links", ditemsLinks);
					ditems.put(dItemJson);

				}

			} catch (WikiApiException ex) {
				log.debug(ExceptionUtils.getStackTrace(ex));

			}

		}

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
