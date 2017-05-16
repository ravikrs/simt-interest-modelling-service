package de.rwth.i9.cimt.ke.repository.wikipedia;

import java.util.List;

import de.rwth.i9.cimt.ke.model.wikipedia.WikiCategoryOutlinks;
import de.rwth.i9.cimt.ke.model.wikipedia.WikiCategoryOutlinksId;

public interface WikiCategoryOutlinksRespository extends WikipediaBaseRepository<WikiCategoryOutlinks, WikiCategoryOutlinksId> {
	List<WikiCategoryOutlinks> findById(WikiCategoryOutlinksId id);
}
