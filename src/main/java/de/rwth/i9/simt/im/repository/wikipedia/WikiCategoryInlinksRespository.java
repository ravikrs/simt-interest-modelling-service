package de.rwth.i9.simt.im.repository.wikipedia;

import java.util.List;

import de.rwth.i9.simt.im.model.wikipedia.WikiCategoryInlinks;
import de.rwth.i9.simt.im.model.wikipedia.WikiCategoryInlinksId;

public interface WikiCategoryInlinksRespository extends WikipediaBaseRepository<WikiCategoryInlinks, WikiCategoryInlinksId> {
	List<WikiCategoryInlinks> findById(WikiCategoryInlinksId id);
}
