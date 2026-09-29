package com.campusloop.util;

import com.campusloop.config.AppConfig;
import com.campusloop.service.ValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.servlet.http.Part;

/**
 * Safe local image uploads (no paid hosting).
 *
 * Validation never trusts the client: the file type is detected from the file's
 * own magic bytes (JPEG, PNG, WebP), the size limit is enforced server-side, and
 * JPEG/PNG headers are decoded to check the pixel dimensions without decoding
 * the whole image (prevents "decompression bomb" uploads).
 *
 * Files are written under {upload.dir}/{listings|profiles}/ with a random
 * 32-hex-character name; the original filename is never used. They are served
 * by MediaServlet at /media/{folder}/{name}. The value stored in the database is
 * the context-relative path "media/{folder}/{name}".
 */
public final class ImageStorage {

    private static final Logger LOG = Logger.getLogger(ImageStorage.class.getName());

    public enum Kind {
        LISTINGS("listings", 300),
        PROFILES("profiles", 100);

        private final String folder;
        private final int minDimension;

        Kind(String folder, int minDimension) {
            this.folder = folder;
            this.minDimension = minDimension;
        }

        public String getFolder() {
            return folder;
        }
    }

    private static final int MAX_DIMENSION = 8000;
    private static final Pattern STORED_PATH =
            Pattern.compile("^media/(listings|profiles)/[a-f0-9]{32}\\.(jpg|png|webp)$");

    private ImageStorage() {
    }

    /** True when the multipart part actually carries a chosen file. */
    public static boolean hasFile(Part part) {
        if (part == null || part.getSize() <= 0) {
            return false;
        }
        String submitted = part.getSubmittedFileName();
        return submitted != null && !submitted.trim().isEmpty();
    }

    /**
     * Validates and stores an image. Returns the context-relative path to save in
     * the database, e.g. "media/listings/3f2a...c9.jpg".
     *
     * @param field the form field name used for the error message
     */
    public static String store(Part part, Kind kind, String field) throws ValidationException, IOException {
        long max = AppConfig.get().getMaxUploadBytes();
        long maxMb = max / (1024L * 1024L);

        if (!hasFile(part)) {
            throw new ValidationException(field, "Please choose a photo (JPG, PNG or WebP, up to " + maxMb + " MB).");
        }
        if (part.getSize() > max) {
            throw new ValidationException(field, "That image is " + String.format(java.util.Locale.ROOT, "%.1f",
                    part.getSize() / (1024.0 * 1024.0)) + " MB. The limit is " + maxMb + " MB.");
        }

        byte[] head = new byte[12];
        int read;
        try (InputStream in = part.getInputStream()) {
            read = readFully(in, head);
        }
        String ext = detectExtension(head, read);
        if (ext == null) {
            throw new ValidationException(field,
                    "That file isn't a supported image. Please upload a JPG, PNG or WebP photo.");
        }

        Path dir = AppConfig.get().getUploadDir().resolve(kind.getFolder());
        Files.createDirectories(dir);
        String name = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path tmp = Files.createTempFile(dir, "upload-", ".part");
        try {
            try (InputStream in = part.getInputStream()) {
                Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
            }
            if (Files.size(tmp) > max) {
                throw new ValidationException(field, "That image is larger than " + maxMb + " MB.");
            }
            if (!"webp".equals(ext)) {
                checkDimensions(tmp, kind, field);
            }
            Files.move(tmp, dir.resolve(name), StandardCopyOption.ATOMIC_MOVE);
        } finally {
            Files.deleteIfExists(tmp);
        }
        return "media/" + kind.getFolder() + "/" + name;
    }

    /**
     * Deletes a previously uploaded image. Paths that are not CampusLoop uploads
     * (for example the bundled demo illustrations) are never touched.
     */
    public static void deleteQuietly(String storedPath) {
        if (storedPath == null || !STORED_PATH.matcher(storedPath).matches()) {
            return;
        }
        Path root = AppConfig.get().getUploadDir();
        Path file = root.resolve(storedPath.substring("media/".length())).normalize();
        if (!file.startsWith(root)) {
            return;
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Could not delete old image " + file, e);
        }
    }

    /** jpg / png / webp from the file signature, or null. */
    static String detectExtension(byte[] b, int n) {
        if (n >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        if (n >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A) {
            return "png";
        }
        if (n >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return "webp";
        }
        return null;
    }

    private static void checkDimensions(Path file, Kind kind, String field) throws ValidationException, IOException {
        try (ImageInputStream iis = ImageIO.createImageInputStream(file.toFile())) {
            Iterator<ImageReader> readers = iis == null ? null : ImageIO.getImageReaders(iis);
            if (readers == null || !readers.hasNext()) {
                throw new ValidationException(field, "That image appears to be damaged. Please choose another photo.");
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(iis, true, true);
                int w = reader.getWidth(0);
                int h = reader.getHeight(0);
                if (w > MAX_DIMENSION || h > MAX_DIMENSION) {
                    throw new ValidationException(field, "That image is too large (" + w + "\u00D7" + h
                            + " px). Please use a photo up to " + MAX_DIMENSION + " px on each side.");
                }
                if (w < kind.minDimension || h < kind.minDimension) {
                    throw new ValidationException(field, "That image is too small (" + w + "\u00D7" + h
                            + " px). Please use a photo at least " + kind.minDimension + " px on each side.");
                }
            } catch (IOException | RuntimeException e) {
                throw new ValidationException(field, "That image appears to be damaged. Please choose another photo.");
            } finally {
                reader.dispose();
            }
        }
    }

    private static int readFully(InputStream in, byte[] buf) throws IOException {
        int total = 0;
        while (total < buf.length) {
            int n = in.read(buf, total, buf.length - total);
            if (n < 0) {
                break;
            }
            total += n;
        }
        return total;
    }
}
