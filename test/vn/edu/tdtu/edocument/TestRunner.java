package vn.edu.tdtu.edocument;

import vn.edu.tdtu.edocument.app.AppContext;
import vn.edu.tdtu.edocument.extraction.ContentExtractor;
import vn.edu.tdtu.edocument.extraction.ExtractorFactory;
import vn.edu.tdtu.edocument.extraction.MockOcrService;
import vn.edu.tdtu.edocument.model.Document;
import vn.edu.tdtu.edocument.model.DocumentBuilder;
import vn.edu.tdtu.edocument.model.DocumentStatus;
import vn.edu.tdtu.edocument.model.IncompleteDocumentException;
import vn.edu.tdtu.edocument.notification.Account;
import vn.edu.tdtu.edocument.notification.ChannelType;
import vn.edu.tdtu.edocument.notification.DocumentEventPublisher;
import vn.edu.tdtu.edocument.notification.NotificationChannel;
import vn.edu.tdtu.edocument.notification.NotificationManager;
import vn.edu.tdtu.edocument.repository.DocumentRepository;
import vn.edu.tdtu.edocument.repository.InMemoryDocumentRepository;
import vn.edu.tdtu.edocument.repository.LocalJsonDocumentRepository;
import vn.edu.tdtu.edocument.repository.MySqlDocumentRepository;
import vn.edu.tdtu.edocument.repository.S3DocumentRepository;
import vn.edu.tdtu.edocument.service.DocumentService;
import vn.edu.tdtu.edocument.service.SubmissionResult;
import vn.edu.tdtu.edocument.validation.AntivirusHandler;
import vn.edu.tdtu.edocument.validation.BasicValidationHandler;
import vn.edu.tdtu.edocument.validation.DuplicateCheckHandler;
import vn.edu.tdtu.edocument.validation.SignatureVirusScanner;
import vn.edu.tdtu.edocument.validation.ValidationChain;
import vn.edu.tdtu.edocument.validation.ValidationException;
import vn.edu.tdtu.edocument.validation.ValidationHandler;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Bộ kiểm thử không phụ thuộc thư viện ngoài. Chạy: java -cp out vn.edu.tdtu.edocument.TestRunner */
public class TestRunner {
    interface Case { void run() throws Exception; }

    private static int passed = 0, failed = 0;
    private static Path dir;
    private static PrintStream REAL;
    private static final Account A = new Account("A", "a@x.com", "0901", "push-a");
    private static final Account O = new Account("O", "o@x.com", "0123", "push-o");

    public static void main(String[] args) throws Exception {
        REAL = new PrintStream(System.out, true, "UTF-8");
        System.setOut(REAL);
        dir = Files.createTempDirectory("edoc_test");
        PrintStream quiet = new PrintStream(new java.io.ByteArrayOutputStream());  // ẩn log chương trình

        REAL.println("== YC1 Builder");
        run("Lưu nháp không cần đủ trường", TestRunner::draftNeedsNoFields, quiet);
        run("Nộp thiếu trường -> liệt kê đủ trường thiếu", TestRunner::strictBuildListsMissing, quiet);
        run("Email sai định dạng bị chặn", TestRunner::strictBuildRejectsBadEmail, quiet);
        run("Nháp -> bổ sung -> nộp, giữ nguyên mã hồ sơ", TestRunner::incrementalDraftThenSubmit, quiet);
        REAL.println("== YC2 Strategy + Factory");
        run("TXT đọc nguyên văn", TestRunner::txtIsRead, quiet);
        run("PDF/JPG/PNG đi qua OCR", TestRunner::pdfAndImagesUseOcr, quiet);
        run("Định dạng chưa hỗ trợ (.docx) bị từ chối", TestRunner::unsupportedFormatRejected, quiet);
        run("Thêm định dạng mới không sửa DocumentService (OCP)", TestRunner::newFormatNeedsNoServiceChange, quiet);
        REAL.println("== YC3 Chain of Responsibility");
        run("Quá 5MB bị chặn ở trạm 1", TestRunner::oversizeRejectedAtStation1, quiet);
        run("Có virus -> dừng, trạm sau không chạy", TestRunner::virusRejectedAndLaterStationSkipped, quiet);
        run("Nộp trùng bị chặn ở trạm 3", TestRunner::duplicateRejected, quiet);
        run("Đổi thứ tự trạm -> lỗi dừng ở trạm khác", TestRunner::chainOrderIsConfigurable, quiet);
        run("Hồ sơ sạch đi qua cả 3 trạm", TestRunner::cleanDocumentPassesAll, quiet);
        REAL.println("== YC4 Observer");
        run("Chỉ kênh đã đăng ký mới được gửi", TestRunner::onlyChosenChannelsFire, quiet);
        run("Không đăng ký -> không gửi gì", TestRunner::noSubscriptionNoNotification, quiet);
        run("Từ chối được báo kèm lý do", TestRunner::rejectionIsNotifiedWithReason, quiet);
        run("Hủy đăng ký thì ngừng nhận", TestRunner::unsubscribeStopsNotifications, quiet);
        REAL.println("== YC5 Repository");
        run("Cùng nghiệp vụ chạy trên JSON / MySQL / S3", TestRunner::sameFlowOnEveryStorage, quiet);
        run("JSON ghi rồi đọc lại đúng (escape ký tự đặc biệt)", TestRunner::localJsonRoundTrip, quiet);
        run("DocumentService chỉ phụ thuộc interface", TestRunner::serviceDependsOnlyOnInterface, quiet);

        REAL.println("\nKẾT QUẢ: " + passed + " đạt, " + failed + " lỗi");
        if (failed > 0) System.exit(1);
    }

    private static void run(String name, Case c, PrintStream quiet) {
        try {
            System.setOut(quiet);
            c.run();
            System.setOut(REAL);
            passed++;
            REAL.println("  [PASS] " + name);
        } catch (Throwable t) {
            System.setOut(REAL);
            failed++;
            REAL.println("  [FAIL] " + name + " -> " + t);
        }
    }

    // ====================== tiện ích ======================
    private static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); }

    private static String file(String name, String content) throws Exception {
        Path p = dir.resolve(name);
        Files.write(p, content.getBytes(StandardCharsets.UTF_8));
        return p.toString();
    }

    private static DocumentBuilder full(String name, String content) throws Exception {
        return new DocumentBuilder().applicant("A", "a@x.com", "0901").officer("O", "o@x.com", "0123")
                .documentType("BAO_CAO").digitalSignature("SIG").attachment(file(name, content));
    }

    private static AppContext ctx() { return new AppContext(new InMemoryDocumentRepository()); }

    /** Kênh ghi nhận tin nhắn để kiểm tra. */
    static class RecordingChannel implements NotificationChannel {
        final ChannelType type;
        final List<String> received = new ArrayList<>();
        RecordingChannel(ChannelType t) { type = t; }
        public ChannelType type() { return type; }
        public void send(Account to, String m) { received.add(to.getName() + ": " + m); }
    }

    /** Trạm đếm số lần được gọi, để chứng minh chuỗi dừng đúng chỗ. */
    static class CountingStation extends ValidationHandler {
        int calls = 0;
        protected void check(Document d) { calls++; }
        public String stationName() { return "Đếm"; }
    }

    // ====================== YC1 ======================
    static void draftNeedsNoFields() {
        Document d = new DocumentBuilder().applicant("A", null, null).buildDraft();
        check(d.getStatus() == DocumentStatus.DRAFT, "phải là DRAFT");
        check(d.getApplicantEmail() == null, "trường chưa nhập phải null");
    }

    static void strictBuildListsMissing() {
        try {
            new DocumentBuilder().applicant("A", "a@x.com", "0901").build();
            check(false, "phải ném IncompleteDocumentException");
        } catch (IncompleteDocumentException e) {
            check(e.getProblems().size() >= 5, "phải liệt kê các trường thiếu: " + e.getProblems());
        }
    }

    static void strictBuildRejectsBadEmail() throws Exception {
        try {
            full("e.txt", "x").applicant("A", "khong-co-a-cong", "0901").build();
            check(false, "email sai phải bị chặn");
        } catch (IncompleteDocumentException e) {
            check(e.getMessage().contains("Email"), "thông báo phải nói về email");
        }
    }

    static void incrementalDraftThenSubmit() throws Exception {
        AppContext c = ctx();
        DocumentBuilder b = new DocumentBuilder().applicant("A", "a@x.com", "0901").officer("O", "o@x.com", "0123").documentType("BAO_CAO");
        Document draft = c.service().saveDraft(b);
        DocumentBuilder b2 = DocumentBuilder.from(c.repository().findById(draft.getId()).get())
                .digitalSignature("SIG").attachment(file("inc.txt", "noi dung"));
        SubmissionResult r = c.service().submit(b2);
        check(r.isSuccess(), "nộp sau khi bổ sung phải thành công: " + r.getMessage());
        check(r.getDocument().getId().equals(draft.getId()), "phải giữ nguyên mã hồ sơ của bản nháp");
        check(c.repository().findById(draft.getId()).get().getStatus() == DocumentStatus.PROCESSED, "kho phải cập nhật PROCESSED");
    }

    // ====================== YC2 ======================
    static void txtIsRead() throws Exception {
        SubmissionResult r = ctx().service().submit(full("t.txt", "xin chao"));
        check("xin chao".equals(r.getDocument().getExtractedContent()), "TXT phải đọc nguyên văn");
    }

    static void pdfAndImagesUseOcr() throws Exception {
        for (String name : Arrays.asList("a.pdf", "b.jpg", "c.png")) {
            SubmissionResult r = ctx().service().submit(full(name, "bin"));
            check(r.isSuccess(), name + " phải được chấp nhận");
            check(r.getDocument().getExtractedContent().startsWith("[OCR"), name + " phải đi qua OCR");
        }
    }

    static void unsupportedFormatRejected() throws Exception {
        SubmissionResult r = ctx().service().submit(full("d.docx", "x"));
        check(r.getStatus() == DocumentStatus.REJECTED, "docx chưa hỗ trợ phải bị từ chối");
    }

    static void newFormatNeedsNoServiceChange() throws Exception {
        InMemoryDocumentRepository repo = new InMemoryDocumentRepository();
        ExtractorFactory f = ExtractorFactory.withDefaults(new MockOcrService());
        f.register("docx", (ContentExtractor) path -> "[DOCX] đọc được");   // chỉ thêm 1 strategy
        DocumentService s = new DocumentService(repo,
                ValidationChain.of(new BasicValidationHandler(f.supportedExtensions(), 5120)), f, new DocumentEventPublisher());
        SubmissionResult r = s.submit(full("ok.docx", "x"));
        check(r.isSuccess() && "[DOCX] đọc được".equals(r.getDocument().getExtractedContent()), "định dạng mới phải chạy được");
    }

    // ====================== YC3 ======================
    static void oversizeRejectedAtStation1() throws Exception {
        SubmissionResult r = ctx().service().submit(full("big.txt", "x").attachment(file("big.txt", "x"), "txt", 6000));
        check(r.getStatus() == DocumentStatus.REJECTED && r.getMessage().contains("Kiểm tra cơ bản"), r.getMessage());
    }

    static void virusRejectedAndLaterStationSkipped() throws Exception {
        CountingStation after = new CountingStation();
        InMemoryDocumentRepository repo = new InMemoryDocumentRepository();
        ExtractorFactory f = ExtractorFactory.withDefaults(new MockOcrService());
        ValidationChain chain = ValidationChain.of(new AntivirusHandler(new SignatureVirusScanner()), after);
        SubmissionResult r = new DocumentService(repo, chain, f, new DocumentEventPublisher())
                .submit(full("v.txt", "EICAR-STANDARD-ANTIVIRUS-TEST-FILE"));
        check(r.getMessage().contains("Antivirus"), "lỗi phải nêu đúng trạm: " + r.getMessage());
        check(after.calls == 0, "trạm sau không được chạy khi trạm trước đã lỗi");
    }

    static void duplicateRejected() throws Exception {
        AppContext c = ctx();
        check(c.service().submit(full("o1.txt", "giong nhau")).isSuccess(), "bản đầu phải qua");
        SubmissionResult r = c.service().submit(full("o2.txt", "giong nhau"));
        check(r.getStatus() == DocumentStatus.REJECTED && r.getMessage().contains("trùng"), r.getMessage());
    }

    static void chainOrderIsConfigurable() throws Exception {
        InMemoryDocumentRepository repo = new InMemoryDocumentRepository();
        Document d = full("ord.txt", "EICAR-STANDARD-ANTIVIRUS-TEST-FILE").attachment(file("ord.txt", "EICAR-STANDARD-ANTIVIRUS-TEST-FILE"), "txt", 6000).buildDraft();
        // Thứ tự mặc định: cơ bản trước -> lỗi dung lượng
        ValidationChain c1 = ValidationChain.of(new BasicValidationHandler(new java.util.HashSet<>(Arrays.asList("txt")), 5120),
                new AntivirusHandler(new SignatureVirusScanner()));
        // Đảo thứ tự: antivirus trước -> lỗi virus
        ValidationChain c2 = ValidationChain.of(new AntivirusHandler(new SignatureVirusScanner()),
                new BasicValidationHandler(new java.util.HashSet<>(Arrays.asList("txt")), 5120));
        String first = "", second = "";
        try { c1.validate(d); } catch (ValidationException e) { first = e.getStation(); }
        try { c2.validate(d); } catch (ValidationException e) { second = e.getStation(); }
        check(first.equals("Kiểm tra cơ bản"), "chuỗi 1 phải dừng ở trạm cơ bản, thực tế: " + first);
        check(second.equals("Quét Antivirus"), "chuỗi 2 phải dừng ở antivirus, thực tế: " + second);
        check(c2.stationNames().size() == 2, "danh sách trạm phải đúng");
    }

    static void cleanDocumentPassesAll() throws Exception {
        SubmissionResult r = ctx().service().submit(full("clean.txt", "sach"));
        check(r.isSuccess() && r.getDocument().getFileHash() != null, "hồ sơ sạch phải qua và có mã băm");
    }

    // ====================== YC4 ======================
    private static Object[] notifyWiring(InMemoryDocumentRepository repo, RecordingChannel e, RecordingChannel s, RecordingChannel p) {
        DocumentEventPublisher pub = new DocumentEventPublisher();
        NotificationManager m = new NotificationManager(pub, Arrays.asList(e, s, p));
        ExtractorFactory f = ExtractorFactory.withDefaults(new MockOcrService());
        ValidationChain chain = ValidationChain.of(new BasicValidationHandler(f.supportedExtensions(), 5120),
                new AntivirusHandler(new SignatureVirusScanner()), new DuplicateCheckHandler(repo));
        return new Object[]{new DocumentService(repo, chain, f, pub), m};
    }

    static void onlyChosenChannelsFire() throws Exception {
        RecordingChannel e = new RecordingChannel(ChannelType.EMAIL), s = new RecordingChannel(ChannelType.SMS), p = new RecordingChannel(ChannelType.PUSH);
        Object[] w = notifyWiring(new InMemoryDocumentRepository(), e, s, p);
        DocumentBuilder b = full("n1.txt", "n1");
        String id = b.buildDraft().getId();
        ((NotificationManager) w[1]).subscribe(id, A, ChannelType.EMAIL);
        ((NotificationManager) w[1]).subscribe(id, O, ChannelType.PUSH);
        ((DocumentService) w[0]).submit(b);
        check(e.received.size() == 2 && e.received.get(0).startsWith("A"), "Email chỉ cho người nộp, 2 lần đổi trạng thái");
        check(p.received.size() == 2 && p.received.get(0).startsWith("O"), "Push chỉ cho cán bộ");
        check(s.received.isEmpty(), "SMS không ai đăng ký -> không được gửi");
    }

    static void noSubscriptionNoNotification() throws Exception {
        RecordingChannel e = new RecordingChannel(ChannelType.EMAIL), s = new RecordingChannel(ChannelType.SMS), p = new RecordingChannel(ChannelType.PUSH);
        Object[] w = notifyWiring(new InMemoryDocumentRepository(), e, s, p);
        ((DocumentService) w[0]).submit(full("n2.txt", "n2"));
        check(e.received.isEmpty() && s.received.isEmpty() && p.received.isEmpty(), "không ai đăng ký -> không gửi gì");
    }

    static void rejectionIsNotifiedWithReason() throws Exception {
        RecordingChannel e = new RecordingChannel(ChannelType.EMAIL), s = new RecordingChannel(ChannelType.SMS), p = new RecordingChannel(ChannelType.PUSH);
        Object[] w = notifyWiring(new InMemoryDocumentRepository(), e, s, p);
        DocumentBuilder b = full("n3.txt", "EICAR-STANDARD-ANTIVIRUS-TEST-FILE");
        ((NotificationManager) w[1]).subscribe(b.buildDraft().getId(), A, ChannelType.SMS);
        ((DocumentService) w[0]).submit(b);
        check(s.received.size() == 2 && s.received.get(1).contains("Từ chối") && s.received.get(1).contains("Antivirus"), "SMS phải báo từ chối kèm lý do: " + s.received);
    }

    static void unsubscribeStopsNotifications() throws Exception {
        RecordingChannel e = new RecordingChannel(ChannelType.EMAIL), s = new RecordingChannel(ChannelType.SMS), p = new RecordingChannel(ChannelType.PUSH);
        Object[] w = notifyWiring(new InMemoryDocumentRepository(), e, s, p);
        DocumentBuilder b = full("n4.txt", "n4");
        String id = b.buildDraft().getId();
        NotificationManager m = (NotificationManager) w[1];
        m.subscribe(id, A, ChannelType.EMAIL, ChannelType.SMS);
        m.unsubscribe(id, A, ChannelType.EMAIL);
        ((DocumentService) w[0]).submit(b);
        check(e.received.isEmpty() && s.received.size() == 2, "chỉ SMS còn được gửi");
    }

    // ====================== YC5 ======================
    static void sameFlowOnEveryStorage() throws Exception {
        List<DocumentRepository> repos = Arrays.asList(
                new LocalJsonDocumentRepository(dir.resolve("json")),
                new MySqlDocumentRepository(m -> { }), new S3DocumentRepository("b", m -> { }));
        int i = 0;
        for (DocumentRepository repo : repos) {
            AppContext c = new AppContext(repo);
            SubmissionResult r = c.service().submit(full("st" + (i++) + ".txt", "noi dung rieng " + i));
            check(r.isSuccess(), repo.getClass().getSimpleName() + " phải chạy được cùng nghiệp vụ");
            check(repo.findById(r.getDocument().getId()).get().getStatus() == DocumentStatus.PROCESSED, "đọc lại phải thấy PROCESSED");
        }
    }

    static void localJsonRoundTrip() throws Exception {
        LocalJsonDocumentRepository repo = new LocalJsonDocumentRepository(dir.resolve("rt"));
        Document d = full("rt.txt", "x").buildDraft();
        d.setExtractedContent("dòng 1\n\"trích dẫn\" và \\ gạch chéo");
        d.setStatus(DocumentStatus.PROCESSED);
        repo.save(d);
        Document back = repo.findById(d.getId()).get();
        check(d.getExtractedContent().equals(back.getExtractedContent()), "ký tự đặc biệt phải được escape đúng");
        check(back.getApplicantEmail().equals("a@x.com") && back.getStatus() == DocumentStatus.PROCESSED, "các trường phải khớp");
        check(Files.exists(dir.resolve("rt").resolve(d.getId() + "_data.json")), "phải có tệp .json");
    }

    static void serviceDependsOnlyOnInterface() throws Exception {
        for (java.lang.reflect.Constructor<?> k : DocumentService.class.getConstructors()) {
            for (Class<?> t : k.getParameterTypes()) {
                check(!t.getName().contains("Local") && !t.getName().contains("MySql") && !t.getName().contains("S3"),
                        "DocumentService không được phụ thuộc lớp lưu trữ cụ thể: " + t);
            }
        }
        for (java.lang.reflect.Field f : DocumentService.class.getDeclaredFields()) {
            check(!f.getType().getSimpleName().matches(".*(Local|MySql|S3).*"), "field phụ thuộc lớp cụ thể: " + f);
        }
    }
}
