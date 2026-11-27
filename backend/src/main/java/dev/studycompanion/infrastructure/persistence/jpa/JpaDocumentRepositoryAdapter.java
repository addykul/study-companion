package dev.studycompanion.infrastructure.persistence.jpa;

import dev.studycompanion.domain.model.DocumentStatus;
import dev.studycompanion.domain.model.StudyDocument;
import dev.studycompanion.domain.repository.DocumentRepository;
import dev.studycompanion.infrastructure.persistence.jpa.entity.DocumentJpaEntity;
import dev.studycompanion.infrastructure.persistence.jpa.repository.DocumentJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaDocumentRepositoryAdapter implements DocumentRepository {
    private final DocumentJpaRepository documentJpaRepository;

    public JpaDocumentRepositoryAdapter(DocumentJpaRepository documentJpaRepository) {
        this.documentJpaRepository = documentJpaRepository;
    }

    @Override
    @Transactional
    public StudyDocument saveProcessing(StudyDocument document) {
        DocumentJpaEntity savedDocument = documentJpaRepository.saveAndFlush(
                DocumentJpaEntity.fromDomain(document));
        return savedDocument.toDomain();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudyDocument> findByCourseId(UUID courseId) {
        return documentJpaRepository.findAllByCourseIdOrderByCreatedAtDesc(courseId).stream()
                .map(DocumentJpaEntity::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StudyDocument> findById(UUID id) {
        return documentJpaRepository.findById(id).map(DocumentJpaEntity::toDomain);
    }

    @Override
    @Transactional
    public void updateStatus(UUID id, DocumentStatus status, String errorMessage) {
        documentJpaRepository.updateStatus(id, status, errorMessage);
    }
}
