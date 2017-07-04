package de.rwth.i9.simt.im.repository.wikipedia;

import java.util.List;

import de.rwth.i9.simt.im.model.wikipedia.WikiPageCategories;
import de.rwth.i9.simt.im.model.wikipedia.WikiPageCategoriesId;

public interface WikiPageCategoriesRepository extends WikipediaBaseRepository<WikiPageCategories, WikiPageCategoriesId> {
	List<WikiPageCategories> findById(WikiPageCategoriesId id);

}
