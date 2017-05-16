package de.rwth.i9.cimt.ke.repository.wikipedia;

import java.util.List;

import de.rwth.i9.cimt.ke.model.wikipedia.WikiCategoryInlinks;
import de.rwth.i9.cimt.ke.model.wikipedia.WikiCategoryInlinksId;

public interface WikiCategoryInlinksRespository extends WikipediaBaseRepository<WikiCategoryInlinks, WikiCategoryInlinksId> {
	List<WikiCategoryInlinks> findById(WikiCategoryInlinksId id);
}
