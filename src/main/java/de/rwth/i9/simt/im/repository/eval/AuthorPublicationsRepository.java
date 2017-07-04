package de.rwth.i9.simt.im.repository.eval;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

import de.rwth.i9.simt.im.model.eval.AuthorPublications;

public interface AuthorPublicationsRepository extends PagingAndSortingRepository<AuthorPublications, Integer> {
	List<AuthorPublications> findByAuthorId(Integer authorid);

	List<AuthorPublications> findByPublicationId(Integer publicationid);

	List<AuthorPublications> findByAuthorIdAndAuthorPosition(Integer authorId, Integer authorPosition);

}
