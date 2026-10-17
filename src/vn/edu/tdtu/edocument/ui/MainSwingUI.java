package vn.edu.tdtu.edocument.ui;

import vn.edu.tdtu.edocument.app.AppContext;
import vn.edu.tdtu.edocument.model.Document;
import vn.edu.tdtu.edocument.model.DocumentStatus;
import vn.edu.tdtu.edocument.repository.StorageType;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Giao diện chính v2.0: danh sách hồ sơ, chọn nơi lưu trữ, tạo mới / tiếp tục bản nháp. */
public class MainSwingUI extends JFrame {
    private final JTextArea consoleArea = new JTextArea();
    private final DefaultTableModel tableModel =
            new DefaultTableModel(new String[]{"Mã hồ sơ", "Người nộp", "Loại hồ sơ", "Trạng thái", "Tập tin"}, 0);
    private final JTable documentTable = new JTable(tableModel);
    private final JComboBox<StorageType> cbStorage = new JComboBox<>(StorageType.values());
    private AppContext context = AppContext.create(StorageType.LOCAL_JSON);
    private List<Document> documents = new ArrayList<>();

    public MainSwingUI() {
        setTitle("Hệ thống Quản lý Hồ sơ Điện tử - v2.0");
        setSize(950, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JScrollPane tablePane = new JScrollPane(documentTable);
        tablePane.setBorder(BorderFactory.createTitledBorder("Danh sách hồ sơ hệ thống"));
        consoleArea.setEditable(false);
        consoleArea.setBackground(new Color(30, 30, 30));
        consoleArea.setForeground(Color.GREEN);
        consoleArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane logPane = new JScrollPane(consoleArea);
        logPane.setBorder(BorderFactory.createTitledBorder("Hệ thống Log/Thông báo"));
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tablePane, logPane);
        split.setDividerLocation(300);
        add(split, BorderLayout.CENTER);

        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAdd = new JButton("Tạo hồ sơ mới");
        JButton btnEdit = new JButton("Mở / tiếp tục hồ sơ nháp");
        JButton btnClear = new JButton("Xóa Log");
        toolBar.add(btnAdd);
        toolBar.add(btnEdit);
        toolBar.add(btnClear);
        toolBar.add(new JLabel("   Nơi lưu trữ:"));
        toolBar.add(cbStorage);
        add(toolBar, BorderLayout.NORTH);

        redirectSystemStreams();
        refreshTable();

        btnAdd.addActionListener(e -> openDialog(null));
        btnEdit.addActionListener(e -> {
            Document selected = selectedDocument();
            if (selected == null || selected.getStatus() != DocumentStatus.DRAFT) {
                JOptionPane.showMessageDialog(this, "Hãy chọn một hồ sơ ở trạng thái Bản nháp.");
            } else {
                openDialog(selected);
            }
        });
        btnClear.addActionListener(e -> consoleArea.setText(""));
        cbStorage.addActionListener(e -> {
            context = AppContext.create((StorageType) cbStorage.getSelectedItem());
            System.out.println("[CẤU HÌNH] Chuyển nơi lưu trữ sang " + cbStorage.getSelectedItem());
            refreshTable();
        });
        documentTable.getSelectionModel().addListSelectionListener(e -> {
            Document d = selectedDocument();
            if (!e.getValueIsAdjusting() && d != null) printDetail(d);
        });
    }

    private void openDialog(Document draft) {
        new AddDocumentDialog(this, context, draft).setVisible(true);
        refreshTable();
    }

    private Document selectedDocument() {
        int row = documentTable.getSelectedRow();
        if (row < 0) return null;
        String id = tableModel.getValueAt(row, 0).toString();
        for (Document d : documents) if (d.getId().equals(id)) return d;
        return null;
    }

    private void printDetail(Document d) {
        System.out.println("\n--- CHI TIẾT HỒ SƠ: " + d.getId() + " ---");
        System.out.println("Người nộp: " + d.getApplicantName() + " | " + d.getApplicantEmail() + " | " + d.getApplicantPhone());
        System.out.println("Cán bộ: " + d.getOfficerName() + " | " + d.getOfficerEmail() + " | " + d.getOfficerPhone());
        System.out.println("Loại: " + d.getDocumentType() + " | Tệp: " + d.getFilePath() + " (" + d.getFileSizeKB() + " KB)");
        System.out.println("Trạng thái: " + d.getStatus().getLabel());
        if (d.getExtractedContent() != null) System.out.println("Nội dung trích xuất: " + d.getExtractedContent());
    }

    private void refreshTable() {
        documents = context.repository().findAll();
        tableModel.setRowCount(0);
        for (Document d : documents) {
            tableModel.addRow(new Object[]{d.getId(), d.getApplicantName(), d.getDocumentType(),
                    d.getStatus().getLabel(), d.getFileExtension()});
        }
    }

    private void redirectSystemStreams() {
        OutputStream out = new OutputStream() {
            @Override public void write(int b) { append(new String(new byte[]{(byte) b}, StandardCharsets.UTF_8)); }
            @Override public void write(byte[] b, int off, int len) { append(new String(b, off, len, StandardCharsets.UTF_8)); }
        };
        System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(out, true, StandardCharsets.UTF_8));
    }

    private void append(String text) {
        SwingUtilities.invokeLater(() -> {
            consoleArea.append(text);
            consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) { }
        SwingUtilities.invokeLater(() -> new MainSwingUI().setVisible(true));
    }
}
