HỆ THỐNG TIẾP NHẬN VÀ XỬ LÝ HỒ SƠ ĐIỆN TỬ - PHIÊN BẢN 2.0
Môn: Design Pattern - Tiểu luận giữa kỳ
Nhóm / thành viên: <điền họ tên + MSSV các thành viên>
Video thuyết trình: <dán link YouTube / Google Drive công khai>

1. YÊU CẦU MÔI TRƯỜNG
   - JDK 11 trở lên (đã kiểm tra với JDK 21). Không cần thư viện ngoài, không cần Maven/Gradle.
   - Không cần MySQL / AWS thật: MySQL, S3, OCR, Antivirus đều được giả lập bằng log.

2. CẤU TRÚC THƯ MỤC
   src/vn/edu/tdtu/edocument/
     model/         Document, DocumentBuilder (Builder), DocumentStatus, IncompleteDocumentException
     extraction/    ContentExtractor (Strategy), ExtractorFactory (Factory + Registry), Txt/Pdf/Image extractor, OcrService
     validation/    ValidationHandler (Chain of Responsibility), BasicValidation / Antivirus / DuplicateCheck, ValidationChain
     notification/  DocumentObserver, DocumentEventPublisher (Observer), ChannelSubscriber, NotificationManager, Email/Sms/Push channel
     repository/    DocumentRepository (Repository), LocalJson / MySql / S3 / InMemory, RepositoryFactory
     service/       DocumentService (điều phối nghiệp vụ), SubmissionResult
     app/           AppContext (nơi duy nhất ghép các lớp cụ thể lại với nhau)
     ui/            MainSwingUI, AddDocumentDialog (giao diện Swing)
     DemoMain.java  kịch bản demo trên console cho 5 yêu cầu
   test/            TestRunner.java (20 test case, không cần JUnit)
   docs/images/     sơ đồ dùng trong báo cáo
   report.md        báo cáo phân tích & thiết kế

3. BIÊN DỊCH (chạy tại thư mục gốc của project)
   Linux / macOS:
     mkdir -p out
     javac -encoding UTF-8 -d out $(find src test -name '*.java')
   Windows (cmd):
     mkdir out
     dir /s /b src\*.java test\*.java > sources.txt
     javac -encoding UTF-8 -d out @sources.txt

4. CHẠY
   a) Kiểm thử tự động (20 test, in PASS/FAIL, thoát mã khác 0 nếu có lỗi):
        java -Dfile.encoding=UTF-8 -cp out vn.edu.tdtu.edocument.TestRunner
   b) Demo console cho 5 yêu cầu (tạo tệp mẫu trong thư mục tạm, không để lại rác):
        java -Dfile.encoding=UTF-8 -cp out vn.edu.tdtu.edocument.DemoMain
   c) Giao diện Swing:
        java -Dfile.encoding=UTF-8 -cp out vn.edu.tdtu.edocument.ui.MainSwingUI

5. HƯỚNG DẪN DÙNG GIAO DIỆN
   - "Tạo hồ sơ mới": biểu mẫu 3 tab (1. Thông tin, 2. Tài liệu, 3. Xác nhận & thông báo).
       * "Lưu nháp": lưu bất cứ lúc nào, không cần đủ trường.
       * "Nộp hồ sơ": kiểm tra đủ trường -> kiểm duyệt 3 trạm -> trích xuất -> lưu trữ.
       * Tab 3: tick kênh Email / SMS / PUSH cho người nộp và cán bộ. Không tick = không nhận thông báo.
   - "Mở / tiếp tục hồ sơ nháp": chọn một dòng có trạng thái "Bản nháp" rồi bấm nút để điền tiếp.
   - Ô "Nơi lưu trữ": đổi giữa LOCAL_JSON (thư mục server_storage), MYSQL, S3 (giả lập, xem log).
   - Click một dòng trong bảng để xem chi tiết trong khung log.

6. TỆP KIỂM THỬ GỢI Ý
   - Hợp lệ: tệp .txt / .pdf / .jpg / .png dưới 5MB.
   - Nhiễm virus (giả lập): tệp chứa chuỗi EICAR-STANDARD-ANTIVIRUS-TEST-FILE.
   - Trùng lặp: nộp lại đúng nội dung tệp của hồ sơ đã xử lý.
   - Sai định dạng: .docx / .xlsx.

7. LƯU Ý
   - Thêm định dạng tệp mới: viết 1 lớp implements ContentExtractor và gọi ExtractorFactory.register("ext", ...) trong AppContext.
   - Đổi thứ tự/thêm/bớt trạm kiểm duyệt: sửa danh sách trong ValidationChain.of(...) ở AppContext.
   - Thêm nơi lưu trữ: viết 1 lớp implements DocumentRepository và thêm vào RepositoryFactory.
