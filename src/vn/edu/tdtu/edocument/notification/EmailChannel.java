package vn.edu.tdtu.edocument.notification;

public class EmailChannel implements NotificationChannel {
    @Override public ChannelType type() { return ChannelType.EMAIL; }

    @Override
    public void send(Account to, String message) {
        System.out.println("  [GỬI EMAIL] -> " + to.getName() + " (" + to.getEmail() + "): " + message);
    }
}
