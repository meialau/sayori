package me.meiallu.sayori;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

@Getter
@SuppressWarnings("unused")
public class Request {

    private final Request.Type type;
    private final Version version;
    private final HashMap<String, String> headers;
    private final HashMap<String, String> parameters;
    @Setter
    private Mapping mapping;
    @Setter
    private byte[] rawBody;
    @Setter
    private String path;

    public String getBody() {
        return rawBody == null ? "" : new String(rawBody, StandardCharsets.UTF_8);
    }

    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public void setParameter(String name, String value) {
        parameters.put(name, value);
    }

    public String getParameter(String name) {
        return parameters.get(name);
    }

    @Getter
    public enum Version {

        HTTP09("HTTP/0.9"),
        HTTP10("HTTP/1.0"),
        HTTP11("HTTP/1.1"),
        HTTP2("HTTP/2"),
        HTTP3("HTTP/3");

        private final String name;

        public static Version getByName(@NotNull String name) {
            for (Version version : values())
                if (version.name.equalsIgnoreCase(name) || version.name().equalsIgnoreCase(name))
                    return version;

            return HTTP11;
        }

        Version(String name) {
            this.name = name;
        }
    }

    public enum Type {

        GET,
        HEAD,
        POST,
        PUT,
        DELETE,
        CONNECT,
        OPTIONS,
        TRACE,
        PATCH;

        public static Type getByName(@NotNull String name) {
            for (Type type : values())
                if (type.name().equalsIgnoreCase(name))
                    return type;

            return GET;
        }
    }

    public Request(String type, String path, String version, WebServer webServer) {
        this.type = Request.Type.getByName(type);
        this.version = Version.getByName(version);
        this.path = path;
        this.headers = new HashMap<>();
        this.parameters = new HashMap<>();
    }
}
