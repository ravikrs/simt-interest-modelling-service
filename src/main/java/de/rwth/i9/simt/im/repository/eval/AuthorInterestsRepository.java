package de.rwth.i9.simt.im.repository.eval;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

import de.rwth.i9.simt.im.model.eval.AuthorInterests;

public interface AuthorInterestsRepository extends PagingAndSortingRepository<AuthorInterests, Integer> {
	List<AuthorInterests> findByAuthorId(Integer authorid);

	List<AuthorInterests> findById(Integer id);

	List<AuthorInterests> findByAuthorIdAndKeAlgorithmAndInterestType(Integer authorId, String keAlgorithm,
			String interestType);
}
