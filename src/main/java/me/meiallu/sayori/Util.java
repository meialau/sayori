package me.meiallu.sayori;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class Util {

    public static void writeLine(String line, OutputStream outputStream) throws IOException {
        byte[] lineBytes = (line + System.lineSeparator()).getBytes(StandardCharsets.UTF_8);
        outputStream.write(lineBytes);
    }

    public static String getExtension(String filePath) {
        String extension = "";

        int i = filePath.lastIndexOf('.');
        int p = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));

        if (i > p)
            extension = filePath.substring(i + 1);

        return extension;
    }
}
