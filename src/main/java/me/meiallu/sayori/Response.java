package me.meiallu.sayori;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Getter
@SuppressWarnings("unused")
public class Response {

    private final HashMap<String, String> headers;
    private Status status;
    private byte[] data;

    public void send(Request.Version version, OutputStream mainOutput) {
        try {
            Util.writeLine(version.getName() + " " + status.getId() + " " + status.getMessage(), mainOutput);

            for (String header : headers.keySet())
                Util.writeLine(header + ": " + headers.get(header), mainOutput);

            if (data != null) {
                Util.writeLine("", mainOutput);
                mainOutput.write(data);
            }

            mainOutput.flush();
        } catch (IOException exception) {
            throw new RuntimeException("Error sending data: " + exception.getMessage());
        }
    }

    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    public void redirect(String path) {
        setHeader("Content-Length", "0");
        setHeader("Connection", "close");
        setHeader("Location", path);

        sendStatus(Status.MOVED_PERMANENTLY);
        data = ("Location: " + path).getBytes(StandardCharsets.UTF_8);
    }

    public void sendStatus(Status status) {
        this.status = status;
    }

    public void writeFile(String path) {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(path));
            writeContent(bytes, ContentType.getByExtension(Util.getExtension(path)).name);
        } catch (IOException exception) {
            throw new RuntimeException("Exception trying to write file: " + exception.getMessage());
        }
    }

    public void writeContent(String string, ContentType contentType) {
        writeContent(string, contentType, StandardCharsets.UTF_8);
    }

    public void writeContent(String string, ContentType contentType, Charset charset) {
        byte[] bytes = string.getBytes(charset);
        setHeader("Content-Type", contentType.name + "; charset=" + charset.name());
        setHeader("Content-Length", Integer.toString(bytes.length));
        data = bytes;
    }

    public void writeContent(byte[] bytes, String contentType) {
        setHeader("Content-Type", contentType);
        setHeader("Content-Length", Integer.toString(bytes.length));
        data = bytes;
    }

    @Getter
    public enum ContentType {
        HTML("text/html", "html"),
        CSS("text/css", "css"),
        JAVASCRIPT("application/javascript", "js"),
        JSON("application/json", "json"),
        TEXT_JSON("text/json", "json"),
        XML("application/xml", "xml"),
        TEXT("text/plain", "txt"),
        CSV("text/csv", "csv"),
        JPEG("image/jpeg", "jpeg", "jpg"),
        PNG("image/png", "png"),
        GIF("image/gif", "gif"),
        WEBP("image/webp", "webp"),
        SVG("image/svg+xml", "svg"),
        MP3("audio/mpeg", "mpeg"),
        OGG_AUDIO("audio/ogg", "ogg"),
        WAV("audio/wav", "wav"),
        MP4("video/mp4", "mp4"),
        WEBM("video/webm", "webm"),
        OGG_VIDEO("video/ogg", "ogg"),
        PDF("application/pdf", "pdf"),
        ZIP("application/zip", "zip"),
        OCTET_STREAM("application/octet-stream");

        private final String name;
        private final List<String> extensions;

        public static ContentType getByName(@NotNull String name) {
            for (ContentType contentType : values())
                if (contentType.name().equalsIgnoreCase(name) || contentType.getName().equalsIgnoreCase(name))
                    return contentType;

            return TEXT;
        }

        public static ContentType getByExtension(@NotNull String extension) {
            String extensionLowercase = extension.toLowerCase();

            for (ContentType contentType : values())
                if (contentType.extensions.contains(extensionLowercase))
                    return contentType;

            return TEXT;
        }

        ContentType(String name, String... extensions) {
            this.name = name;
            this.extensions = Arrays.asList(extensions);
        }
    }

    @Getter
    public enum Status {

        CONTINUE(100, "Continue"),
        SWITCHING_PROTOCOL(101, "Switching Protocol"),
        CHECKPOINTS(103, "Checkpoints"),
        OK(200, "OK"),
        CREATED(201, "Created"),
        ACCEPTED(202, "Accepted"),
        RESET_CONTENT(205, "Reset Content"),
        PARTIAL_CONTENT(206, "Partial Content"),
        MOVED_PERMANENTLY(301, "Moved Permanently"),
        FOUND(302, "Found"),
        NOT_MODIFIED(304, "Not Modified"),
        USE_PROXY(305, "Use Proxy"),
        TEMPORARY_REDIRECT(307, "Temporary Redirect"),
        BAD_REQUEST(400, "Bad Request"),
        UNAUTHORIZED(401, "Unauthorized"),
        FORBIDDEN(403, "Forbidden"),
        NOT_FOUND(404, "Not Found"),
        REQUEST_TIMEOUT(408, "Request Timeout"),
        GONE(410, "Gone"),
        TOO_MANY_REQUESTS(429, "Too Many Requests"),
        INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
        BAD_GATEWAY(502, "Bad Gateway"),
        SERVICE_UNAVAILABLE(503, "Service Unavailable"),
        GATEWAY_TIMEOUT(504, "Gateway Timeout");

        private final int id;
        private final String message;

        Status(int id, String message) {
            this.id = id;
            this.message = message;
        }
    }

    public Response() {
        status = Status.OK;
        headers = new HashMap<>();
    }
}
