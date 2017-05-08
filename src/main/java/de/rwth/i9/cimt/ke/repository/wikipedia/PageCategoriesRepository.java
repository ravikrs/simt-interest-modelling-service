package de.rwth.i9.cimt.ke.repository.wikipedia;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import de.rwth.i9.cimt.ke.model.wikipedia.PageCategories;
import de.rwth.i9.cimt.ke.model.wikipedia.PageCategoriesId;

public interface PageCategoriesRepository extends CrudRepository<PageCategories, PageCategoriesId> {
	List<PageCategories> findById(PageCategoriesId id);

}
