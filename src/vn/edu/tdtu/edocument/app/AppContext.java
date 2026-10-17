package vn.edu.tdtu.edocument.app;

import vn.edu.tdtu.edocument.extraction.ExtractorFactory;
import vn.edu.tdtu.edocument.extraction.MockOcrService;
import vn.edu.tdtu.edocument.notification.DocumentEventPublisher;
import vn.edu.tdtu.edocument.notification.EmailChannel;
import vn.edu.tdtu.edocument.notification.NotificationManager;
import vn.edu.tdtu.edocument.notification.PushChannel;
import vn.edu.tdtu.edocument.notification.SmsChannel;
import vn.edu.tdtu.edocument.repository.DocumentRepository;
import vn.edu.tdtu.edocument.repository.RepositoryFactory;
import vn.edu.tdtu.edocument.repository.StorageType;
import vn.edu.tdtu.edocument.service.DocumentService;
import vn.edu.tdtu.edocument.validation.AntivirusHandler;
import vn.edu.tdtu.edocument.validation.BasicValidationHandler;
import vn.edu.tdtu.edocument.validation.DuplicateCheckHandler;
import vn.edu.tdtu.edocument.validation.SignatureVirusScanner;
import vn.edu.tdtu.edocument.validation.ValidationChain;

import java.util.Arrays;

/** Composition root: nơi DUY NHẤT biết lớp cụ thể nào được ghép với lớp nào. */
public class AppContext {
    public static final long MAX_SIZE_KB = 5 * 1024;

    private final DocumentRepository repository;
    private final ExtractorFactory extractorFactory;
    private final DocumentEventPublisher publisher;
    private final NotificationManager notificationManager;
    private final DocumentService service;

    public AppContext(DocumentRepository repository) {
        this.repository = repository;
        this.extractorFactory = ExtractorFactory.withDefaults(new MockOcrService());
        this.publisher = new DocumentEventPublisher();
        this.notificationManager = new NotificationManager(publisher,
                Arrays.asList(new EmailChannel(), new SmsChannel(), new PushChannel()));
        ValidationChain chain = ValidationChain.of(
                new BasicValidationHandler(extractorFactory.supportedExtensions(), MAX_SIZE_KB),
                new AntivirusHandler(new SignatureVirusScanner()),
                new DuplicateCheckHandler(repository));
        this.service = new DocumentService(repository, chain, extractorFactory, publisher);
    }

    public static AppContext create(StorageType type) {
        return new AppContext(RepositoryFactory.create(type));
    }

    /** Dựng service với chuỗi kiểm duyệt do quản trị viên tự sắp xếp. */
    public DocumentService serviceWith(ValidationChain chain) {
        return new DocumentService(repository, chain, extractorFactory, publisher);
    }

    public DocumentRepository repository() { return repository; }
    public ExtractorFactory extractorFactory() { return extractorFactory; }
    public NotificationManager notifications() { return notificationManager; }
    public DocumentService service() { return service; }
}
