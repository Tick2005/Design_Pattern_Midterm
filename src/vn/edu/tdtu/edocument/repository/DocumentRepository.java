package vn.edu.tdtu.edocument.repository;

import vn.edu.tdtu.edocument.model.Document;

import java.util.List;
import java.util.Optional;

/** [Repository] Hợp đồng lưu trữ duy nhất mà tầng nghiệp vụ được phép biết. */
public interface DocumentRepository {
    void save(Document doc);

    Optional<Document> findById(String id);

    List<Document> findAll();

    /** Có hồ sơ khác (đã xử lý) mang cùng mã băm tệp không? Dùng để chống nộp trùng. */
    boolean existsProcessedByFileHash(String fileHash, String excludeDocumentId);
}
