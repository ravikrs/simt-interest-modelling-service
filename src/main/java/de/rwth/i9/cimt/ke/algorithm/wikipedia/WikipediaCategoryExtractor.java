package de.rwth.i9.cimt.ke.algorithm.wikipedia;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.tika.utils.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.rwth.i9.cimt.ke.util.WikipediaUtil;
import de.tudarmstadt.ukp.wikipedia.api.Category;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;

public class WikipediaCategoryExtractor {
	private static Logger log = LoggerFactory.getLogger(WikipediaCategoryExtractor.class);

	/**
	 * returns the parent categories along with number of times these categories
	 * repeat for the set of input pages
	 * 
	 * @param pages
	 * @param wiki
	 * @return
	 */
	public static Map<Category, Integer> extractParentCategory(Set<String> pages, Wikipedia wiki) {

		Map<Category, Integer> parentCategoriesCount = new HashMap<>();
		for (String pageString : pages) {
			try {
				//get wikipedia page for interest and create an item in json format required by d3 concept map
				Page p = wiki.getPage(pageString);
				for (Category parentCategory : p.getCategories()) {
					int parentCategoryPageId = parentCategory.getPageId();
					if (WikipediaUtil.isGenericWikipediaCategory(parentCategoryPageId)) {
						continue;
					}
					if (!parentCategoriesCount.containsKey(parentCategory)) {
						parentCategoriesCount.put(parentCategory, 1);
					} else {
						parentCategoriesCount.put(parentCategory, parentCategoriesCount.get(parentCategory) + 1);
					}
				}
			} catch (WikiApiException ex) {
				log.debug(ExceptionUtils.getStackTrace(ex));
			}
		}

		return parentCategoriesCount;

	}

	/**
	 * returns the map of categories,count that are descendent to parent
	 * categories of input pages
	 * 
	 * @param pages
	 * @param wiki
	 * @param numDescendents
	 * @return
	 */
	public static Map<Category, Integer> extractDescendentCategory(Set<String> pages, Wikipedia wiki,
			int numDescendents) {

		Map<Category, Integer> descendentCategoriesCount = new HashMap<>();
		for (String pageString : pages) {
			try {
				//get wikipedia page for interest and create an item in json format required by d3 concept map
				Page p = wiki.getPage(pageString);
				for (Category parentCategory : p.getCategories()) {
					if (WikipediaUtil.isGenericWikipediaCategory(parentCategory.getPageId())) {
						continue;
					}
					for (Category descendentCategory : parentCategory.getChildren()) {
						if (WikipediaUtil.isGenericWikipediaCategory(descendentCategory.getPageId())) {
							continue;
						}
						if (!descendentCategoriesCount.containsKey(descendentCategory)) {
							descendentCategoriesCount.put(descendentCategory, 1);
						} else {
							descendentCategoriesCount.put(descendentCategory,
									descendentCategoriesCount.get(descendentCategory) + 1);
						}
					}
				}
			} catch (WikiApiException ex) {
				log.debug(ExceptionUtils.getStackTrace(ex));
			}
		}

		return descendentCategoriesCount;

	}

	/**
	 * returns set of categories that are sibling to parent categories of the
	 * pages
	 * 
	 * @param pages
	 * @param wiki
	 * @param numSiblings
	 * @return
	 */

	public static Map<Category, Integer> extractSiblingCategory(Set<String> pages, Wikipedia wiki, int numSiblings) {

		Map<Category, Integer> siblingCategoriesCount = new HashMap<>();
		for (String pageString : pages) {
			try {
				//get wikipedia page for interest and create an item in json format required by d3 concept map
				Page p = wiki.getPage(pageString);
				for (Category parentCategory : p.getCategories()) {
					if (WikipediaUtil.isGenericWikipediaCategory(parentCategory.getPageId())) {
						continue;
					}
					for (Category siblingCategory : parentCategory.getChildren()) {
						if (WikipediaUtil.isGenericWikipediaCategory(siblingCategory.getPageId())) {
							continue;
						}
						if (!siblingCategoriesCount.containsKey(siblingCategory)) {
							siblingCategoriesCount.put(siblingCategory, 1);
						} else {
							siblingCategoriesCount.put(siblingCategory,
									siblingCategoriesCount.get(siblingCategory) + 1);
						}
					}
				}
			} catch (WikiApiException ex) {
				log.debug(ExceptionUtils.getStackTrace(ex));
			}
		}

		return siblingCategoriesCount;

	}

}
