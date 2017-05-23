package de.rwth.i9.cimt.ke.model.eval;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * AuthorPublications generated by hbm2java
 */
@Entity
@Table(name = "author_publications", catalog = "enwikidb", uniqueConstraints = @UniqueConstraint(columnNames = {
		"author_id", "publication_id" }))
public class AuthorPublications implements java.io.Serializable {

	private Integer id;
	private Integer authorId;
	private Integer authorPosition;
	private Integer publicationId;

	public AuthorPublications() {
	}

	public AuthorPublications(Integer authorId, Integer authorPosition, Integer publicationId) {
		this.authorId = authorId;
		this.authorPosition = authorPosition;
		this.publicationId = publicationId;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)

	@Column(name = "id", unique = true, nullable = false)
	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Column(name = "author_id")
	public Integer getAuthorId() {
		return this.authorId;
	}

	public void setAuthorId(Integer authorId) {
		this.authorId = authorId;
	}

	@Column(name = "author_position")
	public Integer getAuthorPosition() {
		return this.authorPosition;
	}

	public void setAuthorPosition(Integer authorPosition) {
		this.authorPosition = authorPosition;
	}

	@Column(name = "publication_id")
	public Integer getPublicationId() {
		return this.publicationId;
	}

	public void setPublicationId(Integer publicationId) {
		this.publicationId = publicationId;
	}

}