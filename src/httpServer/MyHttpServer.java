package httpServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.*;

public class MyHttpServer implements Runnable {

    public Socket socket;
    private final BufferedReader in;
    public Router router = new Router();
    private static final int LOCALPORT = 8080;
    private static final String SUCCESSFUL = " 200 OK";
    private static final String BADREQUEST = " 400 BAD REQUEST";
    private static final String NOTFOUND = " 404 FILE NOT FOUND";
    private static final String WEBROOT = "webroot/";
    File badRequest = new File(WEBROOT + "errors/400.html");
    File fileNotFound = new File(WEBROOT + "errors/404.html");
    private static boolean serverListening = true;

    public MyHttpServer(Socket c) throws IOException {
        this.socket = c;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public static void main(String[] args) {

        try {
            ServerSocket serverSocket = new ServerSocket(LOCALPORT);
            while (serverListening) {
                try {
                    MyHttpServer myHttpServer = new MyHttpServer(serverSocket.accept());
                    System.out.println("Incoming connection.  " + myHttpServer.socket);
                    new Thread(myHttpServer).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            serverListening = false;
        }
    }

    @Override
    public void run() {
        boolean connected = true;

        while (connected) {
            try {
                List<String> rawRequest = receiveHttpRequest();
                if (rawRequest.get(0) != null) {
                    Request request = new Request(rawRequest);
                    if (request.type.equals("GET")) {
                        chooseResponseForGet(request);
                    } else if (request.type.equals("POST")) {
                        getRequestBody(request);
                        sendPostHeaderResponse(request);
                        PrintWriter out = new PrintWriter(socket.getOutputStream());
                        out.println("{\"Brutopalk\":\"1500\",\"Netopalk\":\"1200\"}");;
                        out.flush();

                    } else {
                        sendResponse(request, BADREQUEST, readFileData(badRequest));
                    }
                }
            } catch (IOException e) {
                connected = false;
            }
        }
        System.out.println("Socket " + socket + " disconnected");
    }

    private void getRequestBody(Request request) {
        if (request.contentLength > 0) {
            try {
                StringBuilder stringBuilder = new StringBuilder();
                char[] arr = new char[request.contentLength];
                in.read(arr);
                stringBuilder.append(arr, 0, request.contentLength);
                request.body = stringBuilder.toString();
                System.out.println(request.body);
            } catch (IOException e) {
                e.printStackTrace();
            }
            extractJson(request);
        }
    }

    private void extractJson(Request request) {
        String json = request.body.replace("{", "");
        json = json.replace("}", "");
        json = json.replace("\"", "");
        String[] keyValuePairs = json.split(",");
        for (String keyValuePair : keyValuePairs) {
            String[] keyValue = keyValuePair.split(":");
            request.jsonMap.put(keyValue[0], keyValue[1]);
        }
    }

    private void sendPostHeaderResponse(Request request) throws IOException {
        PrintWriter out = new PrintWriter(socket.getOutputStream());
        out.println(request.httpVersion + SUCCESSFUL);
        out.println("Content-Type: application/json");
        int length = "{\"Brutopalk\":\"1500\",\"Netopalk\":\"1200\"}".length();
        out.println("Content-Length: " + length);
        out.println("");
        out.flush();
    }

    private List<String> receiveHttpRequest() throws IOException {
        List<String> rawRequest = new ArrayList<>();
        while (true) {
            String lastLine = in.readLine();
            rawRequest.add(lastLine);
            if (lastLine.equals("")) {
                break;
            }
        }
        System.out.println(rawRequest.get(0));
        return rawRequest;
    }

    private void chooseResponseForGet(Request request) throws IOException {
        if (request.path.contains(".")) {
            fileResponse(request);
        } else {
            routingResponse(request);
        }
    }

    private void fileResponse(Request request) throws IOException {
        File file = new File(WEBROOT + request.path);
        if (file.exists()) {
            sendResponse(request, SUCCESSFUL, readFileData(file));
        } else {
            sendResponse(request, NOTFOUND, readFileData(fileNotFound));
        }
    }

    private void routingResponse(Request request) throws IOException {
        if (router.routingExists(request)) {
            sendResponse(request, SUCCESSFUL, readFileData(new File(WEBROOT + "testing.html")));
        } else {
            sendResponse(request, BADREQUEST, readFileData(badRequest));
        }
    }

    private void sendResponse(Request request, String status, byte[] file) throws IOException {
        sendHeader(request.httpVersion, status, file.length);
        sendFile(file);
    }

    private void sendHeader(String httpVersion, String responseMessage, long filelength) throws IOException {
        PrintWriter out = new PrintWriter(socket.getOutputStream());
        out.println(httpVersion + responseMessage);
        out.println("PRIIT Server 1.0!");
        out.println("Date: " + new Date());
        out.println("Content-length: " + filelength);
        out.println("");
        out.flush();
    }

    private void sendFile(byte[] file) throws IOException {
        OutputStream dataOut = socket.getOutputStream();
        dataOut.write(file);
        dataOut.flush();
    }

    private byte[] readFileData(File file) throws IOException {
        return Files.readAllBytes(file.toPath());
    }

    public static class Request {
        List<String> rawRequest;
        String type;
        String path;
        String httpVersion;
        int contentLength;
        Map<String, String> parameters = new HashMap<>();
        Map<String, String> jsonMap = new HashMap<>();
        String body;

        public Request(List<String> rawRequest) {
            String[] splitRequest = rawRequest.get(0).split(" ");
            this.type = splitRequest[0];
            this.path = splitRequest[1].replaceFirst("/", "");
            this.httpVersion = splitRequest[2];
            this.rawRequest = rawRequest;
            for (String requestLine : rawRequest) {
                if (requestLine.contains("Content-Length: ")) {
                    this.contentLength = Integer.parseInt(requestLine.replaceFirst("Content-Length: ", ""));
                }
            }

            extractParametersFromPath();
            ifEmptySetPathToIndex();
        }

        public void setJsonMap(Map<String, String> jsonMap) {
            this.jsonMap = jsonMap;
        }

        private void extractParametersFromPath() {
            if (this.path.contains("?")) {
                String[] splitPath = this.path.split("\\?");
                this.path = splitPath[0];
                getParameters(splitPath[1]);
            }
        }

        private void getParameters(String allKeysValues) {
            String[] allKeysValuesArray = allKeysValues.split("&");
            for (String keyValue : allKeysValuesArray) {
                String[] keyValueArray = keyValue.split("=");
                String key = keyValueArray[0];
                String value = keyValueArray[1];
                this.parameters.put(key, value);
            }
        }

        private void ifEmptySetPathToIndex() {
            if (this.path.equals("")) {
                this.path = "index.html";
            }
        }

        public String getType() {
            return type;
        }

        public String getPath() {
            return path;
        }

        public String getHttpVersion() {
            return httpVersion;
        }

        public Map<String, String> getParameters() {
            return parameters;
        }
    }
}
