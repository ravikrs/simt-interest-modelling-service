package de.rwth.i9.cimt.ke.repository.wikipedia;

import java.util.List;

import de.rwth.i9.cimt.ke.model.wikipedia.WikiPageCategories;
import de.rwth.i9.cimt.ke.model.wikipedia.WikiPageCategoriesId;

public interface WikiPageCategoriesRepository extends WikipediaBaseRepository<WikiPageCategories, WikiPageCategoriesId> {
	List<WikiPageCategories> findById(WikiPageCategoriesId id);

}
