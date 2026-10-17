package vn.edu.tdtu.edocument;

import vn.edu.tdtu.edocument.model.Document;
import vn.edu.tdtu.edocument.service.DocumentProcessor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class MainSwingUI extends JFrame {
    private JTextArea consoleArea;
    private JTable documentTable;
    private DefaultTableModel tableModel;
    private DocumentProcessor processor;
    private List<Document> documentList;

    public MainSwingUI() {
        processor = new DocumentProcessor();
        documentList = new ArrayList<>();

        setTitle("Hệ thống Quản lý Hồ sơ Điện tử - v1.0 (Home)");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        String[] columnNames = {"Mã hồ sơ", "Người nộp", "Loại hồ sơ", "Trạng thái", "Tập tin"};
        tableModel = new DefaultTableModel(columnNames, 0);
        documentTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(documentTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Danh sách hồ sơ hệ thống"));

        consoleArea = new JTextArea();
        consoleArea.setEditable(false);
        consoleArea.setBackground(new Color(30, 30, 30));
        consoleArea.setForeground(Color.GREEN);
        consoleArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        JScrollPane logScrollPane = new JScrollPane(consoleArea);
        logScrollPane.setBorder(BorderFactory.createTitledBorder("Hệ thống Log/Thông báo"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScrollPane, logScrollPane);
        splitPane.setDividerLocation(300);
        add(splitPane, BorderLayout.CENTER);

        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAdd = new JButton("Thêm mới hồ sơ");
        JButton btnClear = new JButton("Xóa Log");
        toolBar.add(btnAdd);
        toolBar.add(btnClear);
        add(toolBar, BorderLayout.NORTH);

        redirectSystemStreams();
        loadExistingDocuments();
        refreshTable();

        btnAdd.addActionListener(e -> {
            AddDocumentDialog dialog = new AddDocumentDialog(this, processor);
            dialog.setVisible(true);
            refreshTable();
        });

        btnClear.addActionListener(e -> consoleArea.setText(""));

        documentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && documentTable.getSelectedRow() != -1) {
                int selectedRow = documentTable.getSelectedRow();
                String docId = tableModel.getValueAt(selectedRow, 0).toString();
                
                for (Document doc : documentList) {
                    if (doc.id.equals(docId)) {
                        System.out.println("\n--- CHI TIẾT HỒ SƠ: " + doc.id + " ---");
                        System.out.println("Người nộp: " + doc.applicantName + " | Email: " + doc.applicantEmail + " | SĐT: " + doc.applicantPhone);
                        System.out.println("Cán bộ tiếp nhận: " + doc.officerName + " | Email: " + doc.officerEmail + " | SĐT: " + doc.officerPhone);
                        System.out.println("Loại hồ sơ: " + doc.documentType);
                        System.out.println("Đường dẫn tệp: " + doc.filePath + " (" + doc.fileSizeKB + " KB)");
                        System.out.println("Chữ ký số: " + doc.digitalSignature);
                        System.out.println("Trạng thái hiện tại: " + doc.status);
                        System.out.println("----------------------------------------\n");
                        break;
                    }
                }
            }
        });
    }

    private void loadExistingDocuments() {
        File storageDir = new File("server_storage");
        if (storageDir.exists() && storageDir.isDirectory()) {
            File[] files = storageDir.listFiles((dir, name) -> name.endsWith("_data.json"));
            if (files != null) {
                for (File file : files) {
                    try {
                        String content = new String(Files.readAllBytes(file.toPath()));
                        Document doc = parseJsonToDocument(content);
                        if (doc != null) {
                            documentList.add(doc);
                        }
                    } catch (Exception e) {
                        System.out.println("[LỖI LOAD] Không thể nạp hồ sơ: " + file.getName());
                    }
                }
            }
        }
    }

    private Document parseJsonToDocument(String json) {
        try {
            String id = extractValue(json, "id");
            String applicantName = extractValue(json, "applicantName");
            String applicantEmail = extractValue(json, "applicantEmail");
            String applicantPhone = extractValue(json, "applicantPhone");
            String officerName = extractValue(json, "officerName");
            String officerEmail = extractValue(json, "officerEmail");
            String officerPhone = extractValue(json, "officerPhone");
            String documentType = extractValue(json, "documentType");
            String filePath = extractValue(json, "filePath");
            String fileExtension = extractValue(json, "fileExtension");
            long fileSizeKB = Long.parseLong(extractValue(json, "fileSizeKB"));
            String digitalSignature = extractValue(json, "digitalSignature");
            String status = extractValue(json, "status");

            return new Document(id, applicantName, applicantEmail, applicantPhone,
                    officerName, officerEmail, officerPhone, documentType,
                    filePath, fileExtension, fileSizeKB, digitalSignature, null, status);
        } catch (Exception e) {
            return null;
        }
    }

    private String extractValue(String json, String key) {
        String pattern = "\"" + key + "\": ";
        int start = json.indexOf(pattern) + pattern.length();
        if (json.charAt(start) == '\"') {
            start++;
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        } else {
            int end = json.indexOf(",", start);
            if (end == -1) end = json.indexOf("\n", start);
            return json.substring(start, end).trim();
        }
    }

    public void addDocumentToList(Document doc) {
        documentList.add(doc);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Document doc : documentList) {
            tableModel.addRow(new Object[]{
                doc.id, doc.applicantName, doc.documentType, doc.status, doc.fileExtension
            });
        }
    }

    private void redirectSystemStreams() {
        OutputStream out = new OutputStream() {
            @Override public void write(int b) { updateTextArea(String.valueOf((char) b)); }
            @Override public void write(byte[] b, int off, int len) { updateTextArea(new String(b, off, len)); }
        };
        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }

    private void updateTextArea(final String text) {
        SwingUtilities.invokeLater(() -> {
            consoleArea.append(text);
            consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new MainSwingUI().setVisible(true));
    }
}