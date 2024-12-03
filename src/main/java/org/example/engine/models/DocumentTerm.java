package org.example.engine.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "document_terms")
@AllArgsConstructor
@NoArgsConstructor
public class DocumentTerm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "doc_id")
    private String docId;

    @Column(name = "term")
    private String term;

    @Column(name = "tf_idf")
    private Double tfIdf;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public Double getTfIdf() {
        return tfIdf;
    }

    public void setTfIdf(Double tfIdf) {
        this.tfIdf = tfIdf;
    }
}