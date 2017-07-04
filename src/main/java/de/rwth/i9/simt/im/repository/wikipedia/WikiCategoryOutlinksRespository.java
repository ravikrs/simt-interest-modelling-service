package de.rwth.i9.simt.im.repository.wikipedia;

import java.util.List;

import de.rwth.i9.simt.im.model.wikipedia.WikiCategoryOutlinks;
import de.rwth.i9.simt.im.model.wikipedia.WikiCategoryOutlinksId;

public interface WikiCategoryOutlinksRespository extends WikipediaBaseRepository<WikiCategoryOutlinks, WikiCategoryOutlinksId> {
	List<WikiCategoryOutlinks> findById(WikiCategoryOutlinksId id);
}
