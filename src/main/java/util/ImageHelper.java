package util;

import javax.imageio.ImageIO;
import javax.inject.Singleton;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

@Singleton
public class ImageHelper {
    private int imageCount = 0;

    private String getCurrentImageName() {
        imageCount++;
        return "screenshot-" + String.format("%03d", imageCount) + ".png";
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
}
