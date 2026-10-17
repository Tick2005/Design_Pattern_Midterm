package vn.edu.tdtu.edocument.ui;

import vn.edu.tdtu.edocument.app.AppContext;
import vn.edu.tdtu.edocument.model.Document;
import vn.edu.tdtu.edocument.model.DocumentBuilder;
import vn.edu.tdtu.edocument.notification.Account;
import vn.edu.tdtu.edocument.notification.ChannelType;
import vn.edu.tdtu.edocument.service.SubmissionResult;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/** Biểu mẫu 3 bước: Thông tin -> Tài liệu -> Xác nhận & chọn kênh thông báo. Có Lưu nháp / Nộp. */
public class AddDocumentDialog extends JDialog {
    private final AppContext context;
    private final DocumentBuilder builder;
    private final String documentId;

    private final JTextField txtApplicantName = new JTextField(), txtApplicantEmail = new JTextField(),
            txtApplicantPhone = new JTextField(),
            txtOfficerName = new JTextField("Cán bộ trực ban"), txtOfficerEmail = new JTextField("officer@tdtu.edu.vn"),
            txtOfficerPhone = new JTextField("0123456789"), txtSignature = new JTextField();
    private final JComboBox<String> cbType = new JComboBox<>(new String[]{"DON_XIN_PHEP", "BAO_CAO", "HO_SO_THUE"});
    private final JLabel lblFile = new JLabel("Chưa chọn");
    private final JCheckBox[] applicantChannels = channelBoxes();
    private final JCheckBox[] officerChannels = channelBoxes();
    private File selectedFile;

    public AddDocumentDialog(JFrame parent, AppContext context, Document draft) {
        super(parent, draft == null ? "Tiếp nhận hồ sơ mới" : "Tiếp tục bản nháp " + draft.getId(), true);
        this.context = context;
        this.builder = draft == null ? new DocumentBuilder() : DocumentBuilder.from(draft);
        this.documentId = draft == null ? builder.buildDraft().getId() : draft.getId();
        if (draft != null) prefill(draft);

        setSize(520, 480);
        setLocationRelativeTo(parent);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("1. Thông tin", step1());
        tabs.addTab("2. Tài liệu", step2());
        tabs.addTab("3. Xác nhận & thông báo", step3());
        add(tabs, BorderLayout.CENTER);

        JButton btnDraft = new JButton("Lưu nháp"), btnSubmit = new JButton("Nộp hồ sơ"), btnClose = new JButton("Đóng");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(btnDraft); buttons.add(btnSubmit); buttons.add(btnClose);
        add(buttons, BorderLayout.SOUTH);

        btnDraft.addActionListener(e -> saveDraft());
        btnSubmit.addActionListener(e -> submit());
        btnClose.addActionListener(e -> dispose());
    }

    private JPanel step1() {
        JPanel p = grid(8);
        row(p, "Tên người nộp:", txtApplicantName);
        row(p, "Email người nộp:", txtApplicantEmail);
        row(p, "SĐT người nộp:", txtApplicantPhone);
        row(p, "Tên cán bộ:", txtOfficerName);
        row(p, "Email cán bộ:", txtOfficerEmail);
        row(p, "SĐT cán bộ:", txtOfficerPhone);
        row(p, "Loại hồ sơ:", cbType);
        return p;
    }

    private JPanel step2() {
        JPanel p = grid(4);
        JButton btnFile = new JButton("Chọn...");
        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        filePanel.add(btnFile); filePanel.add(lblFile);
        row(p, "Tập tin đính kèm:", filePanel);
        row(p, "Chữ ký số:", txtSignature);
        btnFile.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedFile = fc.getSelectedFile();
                lblFile.setText(selectedFile.getName());
            }
        });
        return p;
    }

    private JPanel step3() {
        JPanel p = new JPanel(new GridLayout(2, 1, 5, 5));
        p.add(channelPanel("Người nộp nhận thông báo qua", applicantChannels));
        p.add(channelPanel("Cán bộ nhận thông báo qua", officerChannels));
        return p;
    }

    // ---- hành động ----
    private void saveDraft() {
        collect();
        subscribe();
        context.service().saveDraft(builder);
        JOptionPane.showMessageDialog(this, "Đã lưu bản nháp " + documentId);
    }

    private void submit() {
        collect();
        subscribe();
        SubmissionResult result = context.service().submit(builder);
        if (result.isSuccess()) {
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Không thể nộp hồ sơ", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Đẩy dữ liệu trên form vào Builder (mọi bước dùng chung một Builder). */
    private void collect() {
        builder.id(documentId)
                .applicant(txtApplicantName.getText().trim(), txtApplicantEmail.getText().trim(), txtApplicantPhone.getText().trim())
                .officer(txtOfficerName.getText().trim(), txtOfficerEmail.getText().trim(), txtOfficerPhone.getText().trim())
                .documentType(cbType.getSelectedItem().toString())
                .digitalSignature(txtSignature.getText().trim());
        if (selectedFile != null) builder.attachment(selectedFile.getAbsolutePath());
    }

    private void subscribe() {
        apply(new Account(txtApplicantName.getText(), txtApplicantEmail.getText(), txtApplicantPhone.getText(),
                "push-" + txtApplicantEmail.getText()), applicantChannels);
        apply(new Account(txtOfficerName.getText(), txtOfficerEmail.getText(), txtOfficerPhone.getText(),
                "push-" + txtOfficerEmail.getText()), officerChannels);
    }

    private void apply(Account account, JCheckBox[] boxes) {
        for (int i = 0; i < boxes.length; i++) {
            ChannelType type = ChannelType.values()[i];
            if (boxes[i].isSelected()) context.notifications().subscribe(documentId, account, type);
            else context.notifications().unsubscribe(documentId, account, type);
        }
    }

    private void prefill(Document d) {
        txtApplicantName.setText(nz(d.getApplicantName())); txtApplicantEmail.setText(nz(d.getApplicantEmail()));
        txtApplicantPhone.setText(nz(d.getApplicantPhone())); txtOfficerName.setText(nz(d.getOfficerName()));
        txtOfficerEmail.setText(nz(d.getOfficerEmail())); txtOfficerPhone.setText(nz(d.getOfficerPhone()));
        txtSignature.setText(nz(d.getDigitalSignature()));
        if (d.getDocumentType() != null) cbType.setSelectedItem(d.getDocumentType());
        if (d.getFilePath() != null) { selectedFile = new File(d.getFilePath()); lblFile.setText(selectedFile.getName()); }
    }

    // ---- tiện ích dựng form ----
    private static String nz(String s) { return s == null ? "" : s; }

    private static JCheckBox[] channelBoxes() {
        ChannelType[] types = ChannelType.values();
        JCheckBox[] boxes = new JCheckBox[types.length];
        for (int i = 0; i < types.length; i++) boxes[i] = new JCheckBox(types[i].name());
        return boxes;
    }

    private static JPanel channelPanel(String title, JCheckBox[] boxes) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setBorder(BorderFactory.createTitledBorder(title));
        for (JCheckBox b : boxes) p.add(b);
        return p;
    }

    private static JPanel grid(int rows) {
        JPanel p = new JPanel(new GridLayout(rows, 2, 5, 10));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        return p;
    }

    private static void row(JPanel p, String label, JComponent field) {
        p.add(new JLabel(label));
        p.add(field);
    }
}
