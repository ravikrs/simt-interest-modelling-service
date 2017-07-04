package de.rwth.i9.simt.im.repository.wikipedia;

import java.util.List;

import de.rwth.i9.simt.im.model.wikipedia.WikiMetadata;

public interface WikiMetadataRepository extends WikipediaBaseRepository<WikiMetadata, Long> {

	List<WikiMetadata> findById(Long id);

	List<WikiMetadata> findByLanguage(String language);
}
