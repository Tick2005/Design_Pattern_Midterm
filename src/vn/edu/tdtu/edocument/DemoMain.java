package vn.edu.tdtu.edocument;

import vn.edu.tdtu.edocument.app.AppContext;
import vn.edu.tdtu.edocument.model.Document;
import vn.edu.tdtu.edocument.model.DocumentBuilder;
import vn.edu.tdtu.edocument.notification.Account;
import vn.edu.tdtu.edocument.notification.ChannelType;
import vn.edu.tdtu.edocument.repository.LocalJsonDocumentRepository;
import vn.edu.tdtu.edocument.repository.StorageType;
import vn.edu.tdtu.edocument.service.DocumentService;
import vn.edu.tdtu.edocument.service.SubmissionResult;
import vn.edu.tdtu.edocument.validation.AntivirusHandler;
import vn.edu.tdtu.edocument.validation.BasicValidationHandler;
import vn.edu.tdtu.edocument.validation.DuplicateCheckHandler;
import vn.edu.tdtu.edocument.validation.SignatureVirusScanner;
import vn.edu.tdtu.edocument.validation.ValidationChain;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Kịch bản demo (console) chứng minh 5 yêu cầu của v2.0. Chạy: java vn.edu.tdtu.edocument.DemoMain */
public class DemoMain {
    private static final Account APPLICANT = new Account("Nguyễn Văn A", "a@gmail.com", "0901111111", "push-a");
    private static final Account OFFICER = new Account("Cán bộ trực ban", "officer@tdtu.edu.vn", "0123456789", "push-officer");

    private static Path dir;

    public static void main(String[] args) throws IOException {
        System.setOut(new PrintStream(System.out, true, "UTF-8"));
        dir = Files.createTempDirectory("edoc_demo");
        AppContext ctx = new AppContext(new LocalJsonDocumentRepository(dir.resolve("storage")));

        header("YÊU CẦU 1 - Lưu nháp & nộp hồ sơ theo từng bước (Builder)");
        scenario1(ctx);
        header("YÊU CẦU 2 - Đọc nhiều định dạng (Strategy + Factory)");
        scenario2(ctx);
        header("YÊU CẦU 3 - Chuỗi kiểm duyệt (Chain of Responsibility)");
        scenario3(ctx);
        header("YÊU CẦU 4 - Đăng ký nhận thông báo theo kênh (Observer)");
        scenario4(ctx);
        header("YÊU CẦU 5 - Đổi nơi lưu trữ không đổi nghiệp vụ (Repository)");
        scenario5();
    }

    // ---- Yêu cầu 1 ----
    private static void scenario1(AppContext ctx) throws IOException {
        DocumentService service = ctx.service();
        DocumentBuilder b = new DocumentBuilder();
        System.out.println("Bước 1: điền thông tin cá nhân -> Lưu nháp");
        b.applicant("Nguyễn Văn A", "a@gmail.com", "0901111111").officer("Cán bộ trực ban", "officer@tdtu.edu.vn", "0123456789").documentType("DON_XIN_PHEP");
        Document draft = service.saveDraft(b);

        System.out.println("Thử nộp khi chưa có tài liệu (thiếu trường):");
        service.submit(b);

        System.out.println("Bước 2: tải tài liệu -> Lưu nháp (tiếp tục từ bản nháp đã lưu)");
        b = DocumentBuilder.from(ctx.repository().findById(draft.getId()).get())
                .attachment(file("don_xin_phep.txt", "Kính gửi... Em xin phép nghỉ học.")).digitalSignature("SIG-001");
        service.saveDraft(b);

        System.out.println("Bước 3: xác nhận -> Nộp hồ sơ (đăng ký nhận Email cho người nộp)");
        ctx.notifications().subscribe(draft.getId(), APPLICANT, ChannelType.EMAIL);
        service.submit(b);
    }

    // ---- Yêu cầu 2 ----
    private static void scenario2(AppContext ctx) throws IOException {
        for (String name : new String[]{"bao_cao.txt", "scan.pdf", "anh_chup.png", "bang_tinh.xlsx"}) {
            System.out.println("\n>> Tệp đính kèm: " + name);
            SubmissionResult r = ctx.service().submit(newBuilder(name, "Nội dung của " + name));
            if (r.isSuccess()) System.out.println("   Nội dung trích xuất: " + r.getDocument().getExtractedContent());
        }
    }

    // ---- Yêu cầu 3 ----
    private static void scenario3(AppContext ctx) throws IOException {
        DocumentService service = ctx.service();
        System.out.println("Chuỗi mặc định: " + "Kiểm tra cơ bản -> Quét Antivirus -> Kiểm tra trùng lặp");
        System.out.println("\n>> (a) Quá dung lượng 5MB");
        service.submit(newBuilder("lon.txt", "x").attachment(dir.resolve("lon.txt").toString(), "txt", 6000));
        System.out.println("\n>> (b) Nhiễm virus");
        service.submit(newBuilder("virus.txt", "EICAR-STANDARD-ANTIVIRUS-TEST-FILE"));
        System.out.println("\n>> (c) Nộp trùng nội dung với hồ sơ đã xử lý (bao_cao.txt)");
        service.submit(newBuilder("bao_cao_copy.txt", "Nội dung của bao_cao.txt"));

        System.out.println("\n>> (d) Quản trị viên đổi thứ tự: Trùng lặp -> Antivirus (bỏ trạm cơ bản)");
        ValidationChain custom = ValidationChain.of(
                new DuplicateCheckHandler(ctx.repository()), new AntivirusHandler(new SignatureVirusScanner()));
        System.out.println("   Chuỗi mới: " + custom.stationNames());
        ctx.serviceWith(custom).submit(newBuilder("virus2.txt", "EICAR-STANDARD-ANTIVIRUS-TEST-FILE"));
    }

    // ---- Yêu cầu 4 ----
    private static void scenario4(AppContext ctx) throws IOException {
        DocumentBuilder b = newBuilder("thong_bao.txt", "Hồ sơ thử thông báo");
        String id = b.buildDraft().getId();
        System.out.println("Người nộp chọn: EMAIL + SMS | Cán bộ chọn: chỉ APP PUSH");
        ctx.notifications().subscribe(id, APPLICANT, ChannelType.EMAIL, ChannelType.SMS);
        ctx.notifications().subscribe(id, OFFICER, ChannelType.PUSH);
        ctx.service().submit(b);

        System.out.println("\nHồ sơ không ai đăng ký -> không gửi thông báo nào:");
        ctx.service().submit(newBuilder("khong_dang_ky.txt", "Không ai theo dõi"));
    }

    // ---- Yêu cầu 5 ----
    private static void scenario5() throws IOException {
        for (StorageType type : new StorageType[]{StorageType.MYSQL, StorageType.S3}) {
            System.out.println("\n>> Nơi lưu trữ: " + type + " (cùng một đoạn mã nghiệp vụ)");
            AppContext ctx = AppContext.create(type);
            ctx.service().submit(newBuilder("luu_tru_" + type + ".txt", "Dữ liệu " + type));
            System.out.println("   Số hồ sơ trong kho: " + ctx.repository().findAll().size());
        }
        System.out.println("\n>> Nơi lưu trữ: LOCAL_JSON -> các tệp đã ghi ở " + dir.resolve("storage"));
        try (java.util.stream.Stream<Path> s = Files.list(dir.resolve("storage"))) {
            System.out.println("   Số tệp trong thư mục lưu trữ: " + s.count());
        }
    }

    // ---- tiện ích ----
    private static DocumentBuilder newBuilder(String fileName, String content) throws IOException {
        return new DocumentBuilder()
                .applicant(APPLICANT.getName(), APPLICANT.getEmail(), APPLICANT.getPhone())
                .officer(OFFICER.getName(), OFFICER.getEmail(), OFFICER.getPhone())
                .documentType("BAO_CAO").digitalSignature("SIG-DEMO")
                .attachment(file(fileName, content));
    }

    private static String file(String name, String content) throws IOException {
        Path p = dir.resolve(name);
        Files.write(p, content.getBytes(StandardCharsets.UTF_8));
        return p.toString();
    }

    private static void header(String title) {
        System.out.println("\n############################################################");
        System.out.println("# " + title);
        System.out.println("############################################################");
    }
}
