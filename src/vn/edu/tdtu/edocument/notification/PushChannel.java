package vn.edu.tdtu.edocument.notification;

public class PushChannel implements NotificationChannel {
    @Override public ChannelType type() { return ChannelType.PUSH; }

    @Override
    public void send(Account to, String message) {
        System.out.println("  [APP PUSH]  -> " + to.getName() + " (" + to.getPushToken() + "): " + message);
    }
}
