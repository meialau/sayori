package me.meiallu.sayori;

import lombok.Getter;
import lombok.Setter;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class WebServer {

    private final List<Object> mappings;
    private boolean running;

    public void prepareStaticFiles(String path, String filePath) {
        File folder = new File(filePath);

        if (folder.exists() && !folder.isFile()) {
            File[] listFiles = folder.listFiles();

            if (listFiles != null) {
                for (File file : listFiles) {
                    String pathWithSlash = path + (path.endsWith("/") ? "" : "/");

                    if (!file.isFile()) {
                        prepareStaticFiles(pathWithSlash + file.getName(), file.getAbsolutePath());
                        continue;
                    }

                    getMapping(pathWithSlash + file.getName(), (request, response) -> response.writeFile(file.getAbsolutePath()));
                }
            }
        }
    }

    public void addMiddleware(MiddlewareFunction responseFunction) {
        mappings.add(responseFunction);
    }

    public Mapping addMapping(String path, Request.Type type, Action responseFunction) {
        Mapping mapping = new Mapping(path, type, responseFunction);
        mappings.add(mapping);
        return mapping;
    }

    public Mapping getMapping(String path, Action responseFunction) {
        return addMapping(path, Request.Type.GET, responseFunction);
    }

    public Mapping headMapping(String path, Action responseFunction) {
        return addMapping(path, Request.Type.HEAD, responseFunction);
    }

    public Mapping postMapping(String path, Action responseFunction) {
        return addMapping(path, Request.Type.POST, responseFunction);
    }

    public Mapping putMapping(String path, Action responseFunction) {
        return addMapping(path, Request.Type.PUT, responseFunction);
    }

    public Mapping deleteMapping(String path, Action responseFunction) {
        return addMapping(path, Request.Type.DELETE, responseFunction);
    }

    public Mapping connectMapping(String path, Action responseFunction) {
        return addMapping(path, Request.Type.CONNECT, responseFunction);
    }

    public Mapping optionsMapping(String path, Action responseFunction) {
        return addMapping(path, Request.Type.OPTIONS, responseFunction);
    }

    public Mapping traceMapping(String path, Action responseFunction) {
        return addMapping(path, Request.Type.TRACE, responseFunction);
    }

    public Mapping patchMapping(String path, Action responseFunction) {
        return addMapping(path, Request.Type.PATCH, responseFunction);
    }

    public Mapping getMapping(String path, Request.Type type) {
        for (Object object : mappings) {
            if (object instanceof Mapping mapping) {
                String regexPattern = "^" + mapping.getPath().replaceAll(":[^/]+", "([^/]+)") + "$";
                Pattern regex = Pattern.compile(regexPattern);
                Matcher matcher = regex.matcher(path);

                if (matcher.matches() && mapping.getAllowedTypes().contains(type))
                    return mapping;
            }
        }

        return null;
    }

    private void setup(ServerSocket serverSocket) {
        new Thread(() -> {
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();

                    new Thread(() -> {
                        try (BufferedInputStream inputStream = new BufferedInputStream(clientSocket.getInputStream()); BufferedReader in = new BufferedReader(new InputStreamReader(inputStream)); OutputStream out = clientSocket.getOutputStream()) {
                            String requestLine = in.readLine();

                            if (requestLine == null)
                                return;

                            String[] requestParts = requestLine.split(" ");
                            Request request = new Request(requestParts[0], requestParts[1], requestParts[2], this);

                            for (int i = 0; i < 50; i++) {
                                String headerLine = in.readLine();

                                if (headerLine.isEmpty())
                                    break;

                                String[] headerParts = headerLine.split(": ", 2);
                                request.setHeader(headerParts[0], headerParts[1]);
                            }

                            String lengthString = request.getHeader("Content-Length");

                            if (lengthString != null) {
                                try {
                                    int length = Integer.parseInt(lengthString);

                                    if (length > 0) {
                                        byte[] body = new byte[length];
                                        int bytesReadTotal = 0;

                                        while (bytesReadTotal < length) {
                                            int bytesRead = inputStream.read(body, bytesReadTotal, length - bytesReadTotal);

                                            if (bytesRead == -1)
                                                break;

                                            bytesReadTotal += bytesRead;
                                        }

                                        request.setRawBody(body);
                                    }
                                } catch (NumberFormatException ignored) {
                                }
                            }

                            Response response = new Response();

                            for (Object object : mappings) {
                                if (object instanceof MiddlewareFunction function) {
                                    if (!function.execute(request, response))
                                        break;
                                } else if (object instanceof Mapping value) {
                                    String regexPattern = "^" + value.getPath().replaceAll(":[^/]+", "([^/]+)") + "$";
                                    Pattern regex = Pattern.compile(regexPattern);
                                    Matcher matcher = regex.matcher(request.getPath());
                                    boolean matches = matcher.matches();

                                    if (matches && value.getAllowedTypes().contains(request.getType())) {
                                        String[] pathParts = value.getPath().split("/");
                                        List<String> paramNames = new ArrayList<>();

                                        for (String part : pathParts)
                                            if (part.startsWith(":"))
                                                paramNames.add(part.substring(1));

                                        for (int i = 0; i < paramNames.size(); i++)
                                            request.setParameter(paramNames.get(i), matcher.group(i + 1));

                                        request.setMapping(value);
                                        value.execute(request, response);
                                        break;
                                    }
                                }
                            }

                            response.send(request.getVersion(), out);
                        } catch (IOException exception) {
                            try {
                                clientSocket.close();
                            } catch (IOException exception1) {
                                throw new RuntimeException("Exception closing connection: " + exception1.getMessage());
                            }
                        }
                    }).start();
                } catch (IOException exception) {
                    throw new RuntimeException("Failed to start web server: " + exception.getMessage());
                }
            }
        }).start();
    }

    public WebServer(int port) {
        try {
            this.mappings = new ArrayList<>();
            this.running = true;

            ServerSocket serverSocket = new ServerSocket(port);
            setup(serverSocket);
        } catch (IOException exception) {
            throw new RuntimeException("Failed to start web server: " + exception.getMessage());
        }
    }

    public WebServer(int port, String filePath, String password) {
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");

            FileInputStream keyStoreStream = new FileInputStream(filePath);
            keyStore.load(keyStoreStream, password.toCharArray());

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, password.toCharArray());

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
            ServerSocket server = sslServerSocketFactory.createServerSocket(port);

            this.mappings = new ArrayList<>();
            this.running = true;

            setup(server);
        } catch (IOException | KeyStoreException | UnrecoverableKeyException | CertificateException |
                 NoSuchAlgorithmException | KeyManagementException exception) {
            throw new RuntimeException("Failed to run web server: " + exception.getMessage());
        }
    }
}
