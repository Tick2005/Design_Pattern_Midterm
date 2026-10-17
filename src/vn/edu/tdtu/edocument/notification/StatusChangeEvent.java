package vn.edu.tdtu.edocument.notification;

import vn.edu.tdtu.edocument.model.Document;
import vn.edu.tdtu.edocument.model.DocumentStatus;

/** Sự kiện "trạng thái hồ sơ vừa thay đổi". */
public class StatusChangeEvent {
    private final Document document;
    private final DocumentStatus oldStatus, newStatus;
    private final String detail;

    public StatusChangeEvent(Document document, DocumentStatus oldStatus, DocumentStatus newStatus, String detail) {
        this.document = document;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.detail = detail;
    }

    public Document getDocument() { return document; }
    public DocumentStatus getOldStatus() { return oldStatus; }
    public DocumentStatus getNewStatus() { return newStatus; }
    public String getDetail() { return detail; }

    public String toMessage() {
        String base = "Hồ sơ " + document.getId() + " chuyển sang trạng thái " + newStatus.getLabel();
        return detail == null || detail.isEmpty() ? base : base + " (" + detail + ")";
    }
}
