package vn.edu.tdtu.edocument.notification;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/** Quản lý đăng ký: tài khoản chọn kênh nào thì tạo đúng ChannelSubscriber đó và gắn vào publisher. */
public class NotificationManager {
    private final DocumentEventPublisher publisher;
    private final Map<ChannelType, NotificationChannel> channels = new EnumMap<>(ChannelType.class);
    private final Map<String, ChannelSubscriber> subscriptions = new HashMap<>();

    public NotificationManager(DocumentEventPublisher publisher, Collection<NotificationChannel> available) {
        this.publisher = publisher;
        for (NotificationChannel c : available) channels.put(c.type(), c);
    }

    public void subscribe(String documentId, Account account, ChannelType... types) {
        for (ChannelType type : types) {
            String key = key(documentId, account, type);
            if (subscriptions.containsKey(key)) continue;
            ChannelSubscriber s = new ChannelSubscriber(account, channels.get(type));
            subscriptions.put(key, s);
            publisher.subscribe(documentId, s);
        }
    }

    public void unsubscribe(String documentId, Account account, ChannelType... types) {
        for (ChannelType type : types) {
            ChannelSubscriber s = subscriptions.remove(key(documentId, account, type));
            if (s != null) publisher.unsubscribe(documentId, s);
        }
    }

    private String key(String documentId, Account a, ChannelType t) {
        return documentId + "|" + a.getEmail() + "|" + t;
    }
}
