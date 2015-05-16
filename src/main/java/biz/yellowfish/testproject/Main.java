package biz.yellowfish.testproject;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicReference;

public class Main {
    public static void main(String[] args) throws Exception {
        System.setProperty("webdriver.chrome.driver", "/Users/traveler/projects/chromedriver");
        Main main = new Main();
        main.findFirstPdf(Paths.get(".", "downloads"));
    }

    private Path findFirstPdf(Path directory) throws Exception {
        final AtomicReference<Path> pdf = new AtomicReference<>();
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                System.out.println("FileName "+file.getFileName());
                if (file.getFileName().toString().endsWith(".pdf")) {
                    pdf.set(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        System.out.println("FOUND: "+pdf.get()+" size "+ Files.size(pdf.get()));
        return pdf.get();
    }

}
