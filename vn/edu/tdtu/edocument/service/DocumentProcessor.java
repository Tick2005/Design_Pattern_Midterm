package vn.edu.tdtu.edocument.service;

import vn.edu.tdtu.edocument.model.Document;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class DocumentProcessor {

    public void process(Document doc) {
        System.out.println("\n=======================================================");
        System.out.println("BẮT ĐẦU XỬ LÝ HỒ SƠ ID: " + doc.id);

        if (doc.id == null || doc.id.isEmpty() ||
            doc.applicantName == null || doc.applicantName.isEmpty() ||
            doc.applicantEmail == null || doc.applicantEmail.isEmpty() ||
            doc.applicantPhone == null || doc.applicantPhone.isEmpty() ||
            doc.officerName == null || doc.officerName.isEmpty() ||
            doc.officerEmail == null || doc.officerEmail.isEmpty() ||
            doc.officerPhone == null || doc.officerPhone.isEmpty() ||
            doc.documentType == null || doc.documentType.isEmpty() ||
            doc.filePath == null || doc.filePath.isEmpty() ||
            doc.fileExtension == null || doc.fileExtension.isEmpty() ||
            doc.digitalSignature == null || doc.digitalSignature.isEmpty()) {
            
            System.out.println("[LỖI TIẾP NHẬN] Thiếu trường thông tin bắt buộc. Hủy tạo hồ sơ.");
            return;
        }

        doc.status = "DA_TIEP_NHAN";
        sendNotifications(doc);

        System.out.println("[KIỂM DUYỆT] Đang kiểm tra dung lượng và định dạng...");
        if (doc.fileSizeKB > 5120) {
            System.out.println("[TỪ CHỐI] Dung lượng file " + doc.fileSizeKB + "KB vượt quá 5MB.");
            doc.status = "TU_CHOI";
            sendNotifications(doc);
            return;
        }

        if (!doc.fileExtension.equalsIgnoreCase("txt")) {
            System.out.println("[TỪ CHỐI] Định dạng " + doc.fileExtension + " không được hỗ trợ ở v1.0.");
            doc.status = "TU_CHOI";
            sendNotifications(doc);
            return;
        }

        System.out.println("[TRÍCH XUẤT] Đang đọc nội dung tệp đính kèm...");
        try {
            String content = new String(Files.readAllBytes(Paths.get(doc.filePath)));
            doc.extractedContent = content;
        } catch (IOException e) {
            System.out.println("[LỖI] Không thể đọc nội dung file: " + e.getMessage());
            doc.status = "TU_CHOI";
            sendNotifications(doc);
            return;
        }

        System.out.println("[LƯU TRỮ] Đang sao chép file và xuất dữ liệu JSON...");
        saveToStorage(doc);

        System.out.println("[HOÀN TẤT] Hồ sơ hợp lệ và đã được lưu trữ thành công.");
        doc.status = "DANG_XET_DUYET";
        sendNotifications(doc);
    }

    private void saveToStorage(Document doc) {
        String storageDirPath = "server_storage";
        File storageDir = new File(storageDirPath);
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }

        try {
            Path sourcePath = Paths.get(doc.filePath);
            Path targetPath = Paths.get(storageDirPath + File.separator + doc.id + "_" + sourcePath.getFileName().toString());
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

            String json = "{\n" +
                    "  \"id\": \"" + doc.id + "\",\n" +
                    "  \"applicantName\": \"" + doc.applicantName + "\",\n" +
                    "  \"applicantEmail\": \"" + doc.applicantEmail + "\",\n" +
                    "  \"applicantPhone\": \"" + doc.applicantPhone + "\",\n" +
                    "  \"officerName\": \"" + doc.officerName + "\",\n" +
                    "  \"officerEmail\": \"" + doc.officerEmail + "\",\n" +
                    "  \"officerPhone\": \"" + doc.officerPhone + "\",\n" +
                    "  \"documentType\": \"" + doc.documentType + "\",\n" +
                    "  \"filePath\": \"" + targetPath.toString().replace("\\", "\\\\") + "\",\n" +
                    "  \"fileExtension\": \"" + doc.fileExtension + "\",\n" +
                    "  \"fileSizeKB\": " + doc.fileSizeKB + ",\n" +
                    "  \"digitalSignature\": \"" + doc.digitalSignature + "\",\n" +
                    "  \"status\": \"" + doc.status + "\"\n" +
                    "}";

            File dataFile = new File(storageDirPath + File.separator + doc.id + "_data.json");
            FileWriter writer = new FileWriter(dataFile);
            writer.write(json);
            writer.close();

        } catch (IOException e) {
            System.out.println("[LỖI HỆ THỐNG] Lỗi khi lưu trữ vật lý: " + e.getMessage());
        }
    }

    private void sendNotifications(Document doc) {
        System.out.println("  [GỬI EMAIL] -> Người nộp (" + doc.applicantEmail + "): Hồ sơ chuyển sang trạng thái " + doc.status);
        System.out.println("  [GỬI SMS]   -> Người nộp (" + doc.applicantPhone + "): Hồ sơ chuyển sang trạng thái " + doc.status);
        System.out.println("  [GỬI EMAIL] -> Cán bộ xử lý (" + doc.officerEmail + "): Hồ sơ chuyển sang trạng thái " + doc.status);
        System.out.println("  [GỬI SMS]   -> Cán bộ xử lý (" + doc.officerPhone + "): Hồ sơ chuyển sang trạng thái " + doc.status);
    }
}