package com.github.ubaifadhli.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileHelper {
    private static final int CHARACTER_LIMIT = 512;

    private static BufferedImage toImage(byte[] data) throws IOException {
        InputStream inputStream = new ByteArrayInputStream(data);
        return ImageIO.read(inputStream);
    }

    public static void saveImage(byte[] data, String imageFilePath) throws IOException {
        File imagePath = Paths.get(imageFilePath).toFile();
        imagePath.mkdirs();

        ImageIO.write(toImage(data), "png", imagePath);
    }

    public static void saveText(byte[] data, String textFilePath) throws IOException {
        Path textPath = Paths.get(textFilePath);
        textPath.toFile().getParentFile().mkdirs();

        Files.write(textPath, data);
    }

    public static String limitStringLength(byte[] data) {
        String dataText = new String(data);

        if (dataText.length() > CHARACTER_LIMIT) {
            int remainingCharacter = dataText.length() - CHARACTER_LIMIT;
            dataText = dataText.substring(0, CHARACTER_LIMIT) + "\\\r\\\n... and " + remainingCharacter + " more characters.";
        }

        return dataText;
    }
}
