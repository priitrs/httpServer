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

    private final Socket socket;
    private final BufferedReader in;
    private final Router router = new Router();
    private boolean unAuthorized = true;
    private static final int LOCALPORT = 8080;
    private static final String SUCCESSFUL = " 200 OK";
    private static final String BADREQUEST = " 400 BAD REQUEST";
    private static final String AUTHORIZATION_NEEDED = " 401 AUTHORIZATION NEEDED";
    private static final String FORBIDDEN = " 403 FORBIDDEN";
    private static final String NOTFOUND = " 404 FILE NOT FOUND";
    private static final String WEBROOT = "webroot/";
    private final File badRequest = new File(WEBROOT + "errors/400.html");
    private final File incorrectUserPass = new File(WEBROOT + "errors/403.html");
    private final File fileNotFound = new File(WEBROOT + "errors/404.html");


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
                if (unAuthorized) {
                    sendHeader(request, AUTHORIZATION_NEEDED, 0);
                    request = new Request(receiveHttpRequest());
                    validateBasicAuthentication(request);
                }
                chooseResponseAction(request);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
        System.out.println("Socket " + socket + " disconnected");
    }

    private void validateBasicAuthentication(Request request) throws FileNotFoundException {
        BufferedReader userDB = new BufferedReader(new FileReader("passwords.txt"));
        while (true) {
            try {
                if (userDB.readLine().equals(getDecodedAuthorization(request))) {
                    unAuthorized = false;
                    break;
                }
            } catch (NullPointerException | IOException e) {
                break;
            }
        }
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

    private String getDecodedAuthorization(Request authorizationRequest) {
        return new String(Base64.getDecoder().decode(authorizationRequest.getAuthorization()));
    }

    private void chooseResponseAction(Request request) throws IOException {
        if (unAuthorized) {
            sendHeader(request, FORBIDDEN, 0);
        } else {
            if (request.getType().equals("POST")) {
                receiveRequestBody(request);
            }
            if (request.getType().equals("GET") || request.getType().equals("POST")) {
                chooseResponseType(request);
            } else {
                sendResponse(request, BADREQUEST, readFileData(badRequest));
            }
        }
    }

    private void chooseResponseType(Request request) throws IOException {
        if (request.getPath().contains(".")) {
            fileResponse(request);
        } else {
            routingResponse(request);
        }
    }

    private void fileResponse(Request request) throws IOException {
        File file = new File(WEBROOT + request.getPath());
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
            request.setContentType("application/json");
            sendResponse(request, SUCCESSFUL, routingResult.getBytes(StandardCharsets.UTF_8));
        }
    }

    private void sendResponse(Request request, String status, byte[] file) throws IOException {
        sendHeader(request, status, file.length);
        sendFile(file);
    }

    private void sendHeader(Request request, String responseMessage, long filelength) throws IOException {
        PrintWriter out = new PrintWriter(socket.getOutputStream());
        out.println(request.getHttpVersion() + responseMessage);
        out.println("PRIIT Server 1.0!");
        out.println("Date: " + new Date());
        out.println("Content-length: " + filelength);
        if (responseMessage.equals(AUTHORIZATION_NEEDED)) {
            out.println("WWW-Authenticate: Basic realm=\"protected\"");
        } else {
            out.println("Content-Type: " + request.getContentType());
        }
        out.println("");
        out.flush();
    }

    private void sendFile(byte[] file) throws IOException {
        OutputStream dataOut = socket.getOutputStream();
        dataOut.write(file);
        dataOut.flush();
    }

    private void receiveRequestBody(Request request) {
        int contentLength = request.getContentLength();
        if (contentLength > 0) {
            try {
                StringBuilder stringBuilder = new StringBuilder();
                char[] arr = new char[contentLength];
                in.read(arr);
                stringBuilder.append(arr, 0, contentLength);
                request.setBody(stringBuilder.toString());
                System.out.println(request.getBody());
            } catch (NullPointerException | IOException e) {
                e.printStackTrace();
            }
            extractJson(request);
        }
    }

    private void extractJson(Request request) {
        String json = request.getBody().replace("{", "");
        json = json.replace("}", "");
        json = json.replace("\"", "");
        String[] keyValuePairs = json.split(",");
        for (String keyValuePair : keyValuePairs) {
            String[] keyValue = keyValuePair.split(":");
            request.getJsonMap().put(keyValue[0], keyValue[1]);
        }
    }

    private byte[] readFileData(File file) throws IOException {
        return Files.readAllBytes(file.toPath());
    }
}
