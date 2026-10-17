package vn.edu.tdtu.edocument.notification;

public class SmsChannel implements NotificationChannel {
    @Override public ChannelType type() { return ChannelType.SMS; }

    @Override
    public void send(Account to, String message) {
        System.out.println("  [GỬI SMS]   -> " + to.getName() + " (" + to.getPhone() + "): " + message);
    }
}
