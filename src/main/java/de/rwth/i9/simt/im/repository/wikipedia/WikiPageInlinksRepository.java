package de.rwth.i9.simt.im.repository.wikipedia;

import java.util.List;

import de.rwth.i9.simt.im.model.wikipedia.WikiPageInlinks;
import de.rwth.i9.simt.im.model.wikipedia.WikiPageInlinksId;

public interface WikiPageInlinksRepository extends WikipediaBaseRepository<WikiPageInlinks, WikiPageInlinksId> {
	List<WikiPageInlinks> findById(WikiPageInlinksId id);

}
