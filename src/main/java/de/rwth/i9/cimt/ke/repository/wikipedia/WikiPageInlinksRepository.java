package de.rwth.i9.cimt.ke.repository.wikipedia;

import java.util.List;

import de.rwth.i9.cimt.ke.model.wikipedia.WikiPageInlinks;
import de.rwth.i9.cimt.ke.model.wikipedia.WikiPageInlinksId;

public interface WikiPageInlinksRepository extends WikipediaBaseRepository<WikiPageInlinks, WikiPageInlinksId> {
	List<WikiPageInlinks> findById(WikiPageInlinksId id);

}
