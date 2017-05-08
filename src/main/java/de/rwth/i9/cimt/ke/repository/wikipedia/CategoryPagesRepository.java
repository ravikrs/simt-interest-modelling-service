package de.rwth.i9.cimt.ke.repository.wikipedia;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import de.rwth.i9.cimt.ke.model.wikipedia.CategoryPages;
import de.rwth.i9.cimt.ke.model.wikipedia.CategoryPagesId;

public interface CategoryPagesRepository extends CrudRepository<CategoryPages, CategoryPagesId> {
	List<CategoryPages> findById(CategoryPagesId id);

}
