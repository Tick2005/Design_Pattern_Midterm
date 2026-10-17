package vn.edu.tdtu.edocument.notification;

/** Tài khoản nhận thông báo (người nộp hoặc cán bộ). */
public class Account {
    private final String name, email, phone, pushToken;

    public Account(String name, String email, String phone, String pushToken) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.pushToken = pushToken;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getPushToken() { return pushToken; }
}
