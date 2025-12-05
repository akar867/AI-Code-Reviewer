package com.aicode.reviewer.repository.mongo;

import com.aicode.reviewer.model.mongo.DiffDocument;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DiffDocumentRepository extends MongoRepository<DiffDocument, String> {
    Optional<DiffDocument> findByReviewId(Long reviewId);
}
