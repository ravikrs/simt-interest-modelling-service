package de.rwth.i9.cimt.ke.repository.eval;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

import de.rwth.i9.cimt.ke.model.eval.Author;

public interface AuthorRepository extends PagingAndSortingRepository<Author, Integer> {
	List<Author> findByAuthorName(String authorname);

}
