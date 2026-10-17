package vn.edu.tdtu.edocument.notification;

/** Một kênh gửi thông báo (Email / SMS / Push). */
public interface NotificationChannel {
    ChannelType type();

    void send(Account to, String message);
}
