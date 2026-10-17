package vn.edu.tdtu.edocument.repository;

import vn.edu.tdtu.edocument.model.Document;
import vn.edu.tdtu.edocument.model.DocumentStatus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Nền tảng dùng chung cho các kho giả lập (MySQL / S3) và cho unit test. */
public class InMemoryDocumentRepository implements DocumentRepository {
    private final Map<String, Document> store = new LinkedHashMap<>();

    @Override
    public void save(Document doc) { store.put(doc.getId(), doc); }

    @Override
    public Optional<Document> findById(String id) { return Optional.ofNullable(store.get(id)); }

    @Override
    public List<Document> findAll() { return new ArrayList<>(store.values()); }

    @Override
    public boolean existsProcessedByFileHash(String fileHash, String excludeDocumentId) {
        if (fileHash == null) return false;
        for (Document d : store.values()) {
            if (!d.getId().equals(excludeDocumentId)
                    && d.getStatus() == DocumentStatus.PROCESSED
                    && fileHash.equals(d.getFileHash())) {
                return true;
            }
        }
        return false;
    }
}
