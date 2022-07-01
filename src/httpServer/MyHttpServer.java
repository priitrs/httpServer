package httpServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;

public class MyHttpServer implements Runnable {

    public Socket socket;
    public Router router = new Router();
    private static final int LOCALPORT = 8080;
    private static final String SUCCESSFUL = " 200 OK";
    private static final String NOTFOUND = " 404 FILE NOT FOUND";
    private static final String BADREQUEST = " 400 BAD REQUEST";
    private static boolean serverListening = true;
    File fileNotFound = new File("test1/404.html");
    File badRequest = new File("test1/400.html");

    public MyHttpServer(Socket c) {
        this.socket = c;
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
                Request request = new Request(receiveHttpRequest());
                if (request.type.equals("GET")) {
                    sendHttpResponse(request);
                }
            } catch (IOException e) {
                connected = false;
            }
        }
        System.out.println("Socket " + socket + " disconnected");
    }

    private String receiveHttpRequest() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        return in.readLine();
    }

    private void sendHttpResponse(Request request) throws IOException {
        if (request.path.contains(".")) {
            sendResponseFile(request);
        } else{
            sendRoutingResponse(request);
        }
    }

    private void sendResponseFile(Request request) throws IOException {
        File file = new File(request.path);
        if (file.exists()) {
            sendResponse(request, SUCCESSFUL, file);
        }else{
            sendResponse(request, NOTFOUND, fileNotFound);
        }
    }

    private void sendRoutingResponse(Request request) throws IOException {
        if (!router.routingExists(request)) {
            sendResponse(request, BADREQUEST, badRequest);
        } else {
            sendResponse(request, SUCCESSFUL, new File("testing.html"));
        }
    }

    private void sendResponse(Request request, String status, File file) throws IOException {
        sendResponseHeader(request.httpVersion, status, file.length());
        sendResponseFile(file);
    }

    private void sendResponseHeader(String httpVersion, String responseMessage, long filelength) throws IOException {
        PrintWriter out = new PrintWriter(socket.getOutputStream());
        out.println(httpVersion + responseMessage);
        out.println("PRIIT Server 1.0!");
        out.println("Date: " + new Date());
        out.println("Content-length: " + filelength);
        out.println("");
        out.flush();
    }

    private void sendResponseFile(File file) throws IOException {
        BufferedOutputStream dataOut = new BufferedOutputStream(socket.getOutputStream());
        dataOut.write(readFileData(file), 0, (int) file.length());
        dataOut.flush();
    }

    private byte[] readFileData(File file) throws IOException {
        FileInputStream fileInputStream = null;
        byte[] filedata = new byte[(int) file.length()];
        try {
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(filedata);
        } finally {
            if (fileInputStream != null)
                fileInputStream.close();
        }
        return filedata;
    }

    protected static class Request {
        String type;
        String path;
        String httpVersion;
        HashMap<String, String> parameters = new HashMap<>();

        public Request(String request) {
            String[] splitRequest = request.split(" ");
            this.type = splitRequest[0];
            this.path = splitRequest[1].replaceFirst("/", "");
            this.httpVersion = splitRequest[2];

            extractParametersFromPath();
            ifEmptySetPathToIndex();

            System.out.println(this.type + " " + this.path);
        }

        private void extractParametersFromPath() {
            if (this.path.contains("?")) {
                String[] splitPath = this.path.split("\\?");
                this.path = splitPath[0];
                getParameters(splitPath[1]);
            }
        }

        private void ifEmptySetPathToIndex() {
            if (this.path.equals("")) {
                this.path = "index.html";
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
    }
}
