package util;

import javax.imageio.ImageIO;
import javax.inject.Singleton;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Singleton
public class FileHelper {
    private final int CHARACTER_LIMIT = 256;
    private int imageCount = 0;
    private int textCount = 0;

    private String getCurrentImageName() {
        imageCount++;
        return "screenshot-" + String.format("%03d", imageCount) + ".png";
    }

    private String getCurrentTextName() {
        textCount++;
        return "text-" + String.format("%03d", textCount) + ".txt";
    }

    private BufferedImage toImage(byte[] data) throws IOException {
        InputStream inputStream = new ByteArrayInputStream(data);
        return ImageIO.read(inputStream);
    }

    public String saveImage(byte[] data) throws IOException {
        String currentImageName = getCurrentImageName();

        File imagePath = Paths.get(FilePath.DEFAULT_REPORT_ATTACHMENTS_PATH, currentImageName).toFile();

        imagePath.mkdirs();

        ImageIO.write(toImage(data), "png", imagePath);

        return currentImageName;
    }

    public String saveText(byte[] data) throws IOException {
        String currentTextName = getCurrentTextName();

        Path textPath = Paths.get(FilePath.DEFAULT_REPORT_ATTACHMENTS_PATH, currentTextName);

        Files.write(textPath, data);

        return currentTextName;
    }

    public String limitStringLength(byte[] data) {
        String dataText = new String(data);

        if (dataText.length() > CHARACTER_LIMIT) {
            int remainingCharacter = dataText.length() - CHARACTER_LIMIT;
            dataText = dataText.substring(0, CHARACTER_LIMIT) + "\\\r\\\n... and " + remainingCharacter + " more characters.";
        }

        return dataText;
    }
}
