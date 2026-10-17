package vn.edu.tdtu.edocument.notification;

/** Observer cụ thể: gửi thông báo cho một tài khoản qua đúng một kênh. */
public class ChannelSubscriber implements DocumentObserver {
    private final Account account;
    private final NotificationChannel channel;

    public ChannelSubscriber(Account account, NotificationChannel channel) {
        this.account = account;
        this.channel = channel;
    }

    @Override
    public void onStatusChanged(StatusChangeEvent event) {
        channel.send(account, event.toMessage());
    }
}
