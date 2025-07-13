# Lightweight Java Web Server

Sayori is an extremely lightweight Java HTTPS web server built with minimal dependencies. 
It is designed to serve static files, handle basic API endpoints and more.

## Features

* Native HTTPS support using `.p12` certificates.
* Static file serving with recursive directory handling.
* Supports all standard HTTP methods: GET, POST, PUT, DELETE, etc.
* Middleware support (before route handling).
* Dynamic route parameters with pattern matching (e.g. /user/:id).
* Thread-per-request execution.
* Minimal dependencies for fast startup and low resource usage.
* No frameworks — just plain Java and a few utility classes.

### HTTP Example

```java
WebServer server = new WebServer(8080);

server.getMapping("/", (req, res) -> {
    res.writeContent("Hello, World!", Response.ContentType.TEXT);
});
```

### HTTPS Example

```java
WebServer server = new WebServer(443, "path/to/certificate.p12", "password");

server.getMapping("/", (req, res) -> {
    res.writeContent("Hello, World!", Response.ContentType.TEXT);
});
```

### Static Files

```java
WebServer server = new WebServer(8080);

server.prepareStaticFiles("/", "/var/www");
```

This serves all files under `/var/www` as `/`-based URLs (e.g. `/index.html`).

### Route with Params

You can define dynamic segments in your route using a colon : followed by a name. These are automatically extracted and made available via request.getParameter.

```java
WebServer server = new WebServer(8080);

server.getMapping("/user/:id", (req, res) -> {
    String id = req.getParameter("id");
    res.writeContent("User ID: " + id, Response.ContentType.TEXT);
});
```

### Middleware

Middleware functions are executed before the route logic. 
They can inspect the request and response, perform validation, logging, or access control — and decide whether to allow the request to proceed.

```java
WebServer server = new WebServer(8080);

server.addMiddleware((req, res) -> {
    System.out.println("Request to: " + req.getPath());
    return true; // return false to block request
});
```

## Why?

This server was built for environments where minimalism, speed, and control matter more than scalability or full-stack features.
