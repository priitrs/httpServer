import org.w3c.dom.ls.LSOutput;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;

public class MyHttpServer implements Runnable {

    public Socket socket;

    private static final int LOCALPORT = 8080;
    private static final String SUCCESSFUL = " 200 OK";
    private static final String NOTFOUND = " 404 FILE NOT FOUND";

    File fileNotFound = new File("test1/404.html");

    public MyHttpServer(Socket c) {
        this.socket = c;
    }

    public static void main(String[] args) {

        try {
            ServerSocket serverSocket = new ServerSocket(LOCALPORT);
            System.out.println("Server started.\n" + serverSocket);

            MyHttpServer myHttpServer = new MyHttpServer(serverSocket.accept());
            System.out.println("Incoming connection.\n" + myHttpServer.socket);
            serverSocket.close();

            Thread thread = new Thread(myHttpServer);
            thread.start();


        } catch (IOException e) {
            System.out.println("This error occured:\n");
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        System.out.println("Thread run() started");
        BufferedReader in = null;
        boolean status = true;

        while (status) {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String inputLine = in.readLine();
                String[] request = inputLine.split(" ");
                System.out.println();
                System.out.println(inputLine);

                if (request[0].equals("GET")) {
                    String pathname = "";
                    if (request[1].equals("/")) {
                        pathname = "index.html";
                    } else if (!request[1].isEmpty()) {
                        pathname = request[1].replaceFirst("/", "");
                    } else {
                        throw new NullPointerException("Connection is closed");

                    }

                    File file = new File(pathname);

                    if (file.exists()) {
                        sendResponseHeader(request, SUCCESSFUL, file.length());
                        sendResponseFile(file);
                    } else {
                        sendResponseHeader(request, NOTFOUND, fileNotFound.length());
                        sendResponseFile(fileNotFound);
//
//                        PrintWriter out = new PrintWriter(socket.getOutputStream());
//                        out.println(request[2] + " 404 FILE NOT FOUND");
//                        out.println("PRIIT Server 1.0!");
//                        out.println("Date: " + new Date());
//
//                        out.println("Content-length: " + (int) fileNotFound.length());
//                        out.println("");
//                        out.flush();
//
//
//                        BufferedOutputStream dataOut = new BufferedOutputStream(socket.getOutputStream());
//                        byte[] fileData = readFileData(fileNotFound);
//                        dataOut.write(fileData, 0, (int) fileNotFound.length());
//                        dataOut.flush();
                    }
                }


            } catch (NullPointerException | IOException e) {
                status = false;
                e.printStackTrace();
            }
        }
        System.out.println("Socket disconnected");
    }

    private void sendResponseFile(File file) throws IOException {
        BufferedOutputStream dataOut = new BufferedOutputStream(socket.getOutputStream());
        dataOut.write(readFileData(file), 0, (int) file.length());
        dataOut.flush();
    }

    private void sendResponseHeader(String[] request, String response, long filelength) throws IOException {
        PrintWriter out = new PrintWriter(socket.getOutputStream());

        out.println(request[2] + response);
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
}
