package vn.edu.tdtu.edocument.service;

import vn.edu.tdtu.edocument.model.Document;
import vn.edu.tdtu.edocument.model.DocumentStatus;

/** Kết quả nộp hồ sơ trả về cho UI/log. */
public class SubmissionResult {
    private final boolean success;
    private final DocumentStatus status;
    private final String message;
    private final Document document;

    public SubmissionResult(boolean success, DocumentStatus status, String message, Document document) {
        this.success = success;
        this.status = status;
        this.message = message;
        this.document = document;
    }

    public boolean isSuccess() { return success; }
    public DocumentStatus getStatus() { return status; }
    public String getMessage() { return message; }
    public Document getDocument() { return document; }
}
