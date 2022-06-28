import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class MyHttpServer implements Runnable {

    public Socket socket;

    private static final int LOCALPORT = 8080;
    private static final String SUCCESSFUL = " 200 OK";
    private static final String NOTFOUND = " 404 FILE NOT FOUND";
    private static boolean serverListening = true;

    File fileNotFound = new File("test1/404.html");

    public MyHttpServer(Socket c) {
        this.socket = c;
    }

    public static void main(String[] args) {

            try {
                ServerSocket serverSocket = new ServerSocket(LOCALPORT);
                System.out.println("Server started.\n" + serverSocket);

                while (serverListening) {
                    try {
                        MyHttpServer myHttpServer = new MyHttpServer(serverSocket.accept());
                        System.out.println("Incoming connection.\n" + myHttpServer.socket);
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
        System.out.println("Thread run() started");
        boolean connected = true;

        while (connected) {
            try {
                String httpRequest = recieveHttpRequest();
                Request request = new Request(httpRequest.split(" "));
                System.out.println();
                System.out.println(httpRequest);

                if (request.type.equals("GET")) {
                    if (request.path.equals("/")) {
                        request.path = "index.html";
                    } else  {
                        request.path = request.path.replaceFirst("/", "");
                    }

                    File file = new File(request.path);

                    if (file.exists() || file.isDirectory()) {
                        sendResponseHeader(request.httpVersion, SUCCESSFUL, file.length());
                        sendResponseFile(file);
                    } else {
                        sendResponseHeader(request.httpVersion, NOTFOUND, fileNotFound.length());
                        sendResponseFile(fileNotFound);
                    }
                }
            } catch (NullPointerException | IOException e) {
                connected = false;
                e.printStackTrace();
            }
        }
        System.out.println("Socket " + socket + " disconnected");
    }

    private String recieveHttpRequest() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String httpRequest = in.readLine();
        return httpRequest;
    }

    private void sendResponseFile(File file) throws IOException {
        BufferedOutputStream dataOut = new BufferedOutputStream(socket.getOutputStream());
        dataOut.write(readFileData(file), 0, (int) file.length());
        dataOut.flush();
    }

    private void sendResponseHeader(String httpVersion, String responseMessage, long filelength) throws IOException {
        PrintWriter out = new PrintWriter(socket.getOutputStream());
        out.println(httpVersion + responseMessage);
        out.println("PRIIT Server 1.0!");
        out.println("Date: " + new Date());
//                out.println("Content-type: " + "text/html");
        out.println("Content-length: " + filelength);
        out.println("");
        out.flush();
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

    public static class Request {
        String type;
        String path;
        String httpVersion;

        public Request(String[] request) {
            this.type = request[0];
            this.path = request[1];
            this.httpVersion = request[2];
        }
    }
}
