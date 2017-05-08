package de.rwth.i9.cimt.ke.repository.wikipedia;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import de.rwth.i9.cimt.ke.model.wikipedia.CategoryInlinks;
import de.rwth.i9.cimt.ke.model.wikipedia.CategoryInlinksId;

public interface CategoryInlinksRespository extends CrudRepository<CategoryInlinks, CategoryInlinksId> {
	List<CategoryInlinks> findById(CategoryInlinksId id);
}
