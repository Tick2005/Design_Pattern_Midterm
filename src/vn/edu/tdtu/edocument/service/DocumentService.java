package vn.edu.tdtu.edocument.service;

import vn.edu.tdtu.edocument.extraction.ExtractionException;
import vn.edu.tdtu.edocument.extraction.ExtractorFactory;
import vn.edu.tdtu.edocument.model.Document;
import vn.edu.tdtu.edocument.model.DocumentBuilder;
import vn.edu.tdtu.edocument.model.DocumentStatus;
import vn.edu.tdtu.edocument.model.IncompleteDocumentException;
import vn.edu.tdtu.edocument.notification.DocumentEventPublisher;
import vn.edu.tdtu.edocument.notification.StatusChangeEvent;
import vn.edu.tdtu.edocument.repository.DocumentRepository;
import vn.edu.tdtu.edocument.validation.ValidationChain;
import vn.edu.tdtu.edocument.validation.ValidationException;

/**
 * Điều phối quy trình nghiệp vụ (thay cho DocumentProcessor "God class" của v1.0).
 * Chỉ làm việc với các abstraction được tiêm vào: không biết JSON/MySQL/S3, không biết Email/SMS,
 * không có if-else theo định dạng tệp hay theo từng bước kiểm duyệt.
 */
public class DocumentService {
    private final DocumentRepository repository;
    private final ValidationChain validationChain;
    private final ExtractorFactory extractorFactory;
    private final DocumentEventPublisher publisher;

    public DocumentService(DocumentRepository repository, ValidationChain validationChain,
                           ExtractorFactory extractorFactory, DocumentEventPublisher publisher) {
        this.repository = repository;
        this.validationChain = validationChain;
        this.extractorFactory = extractorFactory;
        this.publisher = publisher;
    }

    /** Yêu cầu 1: lưu nháp, không cần đủ trường. */
    public Document saveDraft(DocumentBuilder builder) {
        Document draft = builder.buildDraft();
        draft.setStatus(DocumentStatus.DRAFT);
        repository.save(draft);
        System.out.println("[NHÁP] Đã lưu bản nháp " + draft.getId());
        return draft;
    }

    /** Nộp hồ sơ: dựng đối tượng -> kiểm duyệt -> trích xuất -> lưu trữ; mỗi lần đổi trạng thái đều phát sự kiện. */
    public SubmissionResult submit(DocumentBuilder builder) {
        Document doc;
        try {
            doc = builder.build();
        } catch (IncompleteDocumentException e) {
            System.out.println("[LỖI TIẾP NHẬN] " + e.getMessage());
            return new SubmissionResult(false, DocumentStatus.DRAFT, e.getMessage(), null);
        }
        System.out.println("\n=== XỬ LÝ HỒ SƠ " + doc.getId() + " ===");
        changeStatus(doc, DocumentStatus.RECEIVED, null);

        try {
            validationChain.validate(doc);
        } catch (ValidationException e) {
            return reject(doc, e.getMessage());
        }

        try {
            doc.setExtractedContent(extractorFactory.get(doc.getFileExtension()).extract(doc.getFilePath()));
        } catch (ExtractionException e) {
            return reject(doc, e.getMessage());
        }

        doc.setStatus(DocumentStatus.PROCESSED);
        repository.save(doc);
        publisher.publish(new StatusChangeEvent(doc, DocumentStatus.RECEIVED, DocumentStatus.PROCESSED, null));
        System.out.println("[HOÀN TẤT] Hồ sơ " + doc.getId() + " đã xử lý và lưu trữ.");
        return new SubmissionResult(true, DocumentStatus.PROCESSED, "Hồ sơ đã được xử lý", doc);
    }

    private SubmissionResult reject(Document doc, String reason) {
        System.out.println("[TỪ CHỐI] " + reason);
        doc.setStatus(DocumentStatus.REJECTED);
        repository.save(doc);
        publisher.publish(new StatusChangeEvent(doc, DocumentStatus.RECEIVED, DocumentStatus.REJECTED, reason));
        return new SubmissionResult(false, DocumentStatus.REJECTED, reason, doc);
    }

    private void changeStatus(Document doc, DocumentStatus next, String detail) {
        DocumentStatus old = doc.getStatus();
        doc.setStatus(next);
        publisher.publish(new StatusChangeEvent(doc, old, next, detail));
    }
}
