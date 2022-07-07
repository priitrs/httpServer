package httpServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

public class MyHttpServer implements Runnable {

    public Socket socket;
    private final BufferedReader in;
    public Router router = new Router();
    boolean authorized = false;
    private static final int LOCALPORT = 8080;
    private static final String SUCCESSFUL = " 200 OK";
    private static final String BADREQUEST = " 401 NOT AUTHORIZED";
    private static final String INCORRECT_USER_PASS = " 401 NOT AUTHORIZED";
    private static final String NOTFOUND = " 404 FILE NOT FOUND";
    private static final String WEBROOT = "webroot/";
    File badRequest = new File(WEBROOT + "errors/400.html");
    File incorrectUserPass = new File(WEBROOT + "errors/401.html");
    File fileNotFound = new File(WEBROOT + "errors/404.html");


    public MyHttpServer(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(LOCALPORT);
            while (true) {
                try {
                    MyHttpServer myHttpServer = new MyHttpServer(serverSocket.accept());
                    System.out.println("Incoming connection.  " + myHttpServer.socket);
                    new Thread(myHttpServer).start();
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                Request request = new Request(receiveHttpRequest());
                if (authorized) {
                    chooseResponseAction(request);
                } else {
                    sendUnauthorizedHeader(request);
                    Request authRequest = new Request(receiveHttpRequest());
                    if (getDecodedAuthorization(authRequest).equals("test:test")) {
                        authorized = true;
                        chooseResponseAction(authRequest);
                    } else {
                        sendResponse(authRequest, INCORRECT_USER_PASS, readFileData(incorrectUserPass));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
        System.out.println("Socket " + socket + " disconnected");
    }

    private List<String> receiveHttpRequest() throws IOException {
        List<String> rawRequest = new ArrayList<>();
        while (true) {
            try {
                String lastLine = in.readLine();
                rawRequest.add(lastLine);
                if (lastLine.equals("")) {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println(rawRequest.get(0));
        return rawRequest;
    }

    private void sendUnauthorizedHeader(Request unauthorizedRequest) throws IOException {
        PrintWriter out = new PrintWriter(socket.getOutputStream());
        out.println(unauthorizedRequest.httpVersion + " 401" + " Authorization Required");
        out.println("Date: " + new Date());
        out.println("Content-length: 0");
        out.println("WWW-Authenticate: Basic realm=\"protected\"");
        out.println("");
        out.flush();
    }

    private String getDecodedAuthorization(Request authorizationRequest) {
        return new String(Base64.getDecoder().decode(authorizationRequest.authorization));
    }

    private void chooseResponseAction(Request request) throws IOException {
        if (request.type.equals("POST")) {
            receiveRequestBody(request);
        }
        if (request.type.equals("GET") || request.type.equals("POST")) {
            chooseResponseType(request);
        } else {
            sendResponse(request, BADREQUEST, readFileData(badRequest));
        }
    }

    private void chooseResponseType(Request request) throws IOException {
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
        String routingResult = router.routingExists(request, socket);
        if (routingResult.equals("")) {
            sendResponse(request, BADREQUEST, readFileData(badRequest));
        } else {
            request.contentType = "application/json";
            sendResponse(request, SUCCESSFUL, routingResult.getBytes(StandardCharsets.UTF_8));
        }
    }

    private void sendResponse(Request request, String status, byte[] file) throws IOException {
        sendHeader(request, status, file.length);
        sendFile(file);
    }

    private void sendHeader(Request request, String responseMessage, long filelength) throws IOException {
        PrintWriter out = new PrintWriter(socket.getOutputStream());
        out.println(request.httpVersion + responseMessage);
        out.println("PRIIT Server 1.0!");
        out.println("Date: " + new Date());
        out.println("Content-length: " + filelength);
        out.println("Content-Type: " + request.contentType);
        out.println("");
        out.flush();
    }

    private void sendFile(byte[] file) throws IOException {
        OutputStream dataOut = socket.getOutputStream();
        dataOut.write(file);
        dataOut.flush();
    }

    private void receiveRequestBody(Request request) {
        if (request.contentLength > 0) {
            try {
                StringBuilder stringBuilder = new StringBuilder();
                char[] arr = new char[request.contentLength];
                in.read(arr);
                stringBuilder.append(arr, 0, request.contentLength);
                request.body = stringBuilder.toString();
                System.out.println(request.body);
            } catch (NullPointerException | IOException e) {
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

    private byte[] readFileData(File file) throws IOException {
        return Files.readAllBytes(file.toPath());
    }
}
