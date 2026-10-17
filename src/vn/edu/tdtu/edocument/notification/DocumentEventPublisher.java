package vn.edu.tdtu.edocument.notification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** [Observer - Subject] Phát sự kiện đến mọi observer đã đăng ký theo dõi hồ sơ đó. */
public class DocumentEventPublisher {
    private final Map<String, List<DocumentObserver>> observers = new HashMap<>();

    public void subscribe(String documentId, DocumentObserver observer) {
        observers.computeIfAbsent(documentId, k -> new ArrayList<>()).add(observer);
    }

    public void unsubscribe(String documentId, DocumentObserver observer) {
        List<DocumentObserver> list = observers.get(documentId);
        if (list != null) list.remove(observer);
    }

    public void publish(StatusChangeEvent event) {
        List<DocumentObserver> list = observers.get(event.getDocument().getId());
        if (list == null) return;
        for (DocumentObserver o : new ArrayList<>(list)) {
            try {
                o.onStatusChanged(event);
            } catch (RuntimeException e) {
                System.out.println("  [LỖI THÔNG BÁO] " + e.getMessage());
            }
        }
    }
}
