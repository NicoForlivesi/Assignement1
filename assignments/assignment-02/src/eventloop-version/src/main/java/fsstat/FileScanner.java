package fsstat;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class FileScanner {

    public static FSReport scan(Path root, long maxFS, int nb) {
        long[] distribution = new long[nb + 1];
        long bandSize = maxFS / nb;

        try {
            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (attrs.isRegularFile()) {
                        long size = attrs.size();
                        int band = computeBand(size, maxFS, bandSize, nb);
                        distribution[band]++;
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Error scanning directory", e);
        }

        long totalFiles = 0;
        for (long c : distribution) totalFiles += c;

        return new FSReport(totalFiles, distribution);
    }

    private static int computeBand(long size, long maxFS, long bandSize, int nb) {
        if (size > maxFS) return nb;
        int band = (int)(size / bandSize);
        return Math.min(band, nb - 1);
    }
}
