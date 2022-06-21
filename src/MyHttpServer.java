import org.w3c.dom.ls.LSOutput;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class MyHttpServer implements Runnable {

    public Socket socket;

    public MyHttpServer(Socket c) {
        this.socket = c;
    }

    public static void main(String[] args) {

        try {
            ServerSocket serverSocket = new ServerSocket(8080);
            System.out.println("Server started. Listening on port: 8080");

            MyHttpServer myHttpServer = new MyHttpServer(serverSocket.accept());
            System.out.println("Incoming connection, InetAddress: " + myHttpServer.socket.getInetAddress());
            System.out.println("Incoming connection, port: " + myHttpServer.socket.getPort());
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
        while (true) {

            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String inputLine = in.readLine();
                String[] request = inputLine.split(" ");
                System.out.println();
                System.out.println(inputLine);

                String pathname = "";
                if (request[1].equals("/")) {
                    pathname = "index.html";
                } else {
                    pathname = request[1].replaceFirst("/", "");
                }

                File file = new File(pathname);
                byte[] fileData = readFileData(file);

                PrintWriter out = new PrintWriter(socket.getOutputStream());
                out.println(request[2] + " 200 OK");
                out.println("PRIIT Server 1.0!");
                out.println("Date: " + new Date());
//                out.println("Content-type: " + "text/html");
                out.println("Content-length: " + (int) file.length());
                out.println("");
                out.flush();

                BufferedOutputStream dataOut = new BufferedOutputStream(socket.getOutputStream());
                dataOut.write(fileData, 0, (int) file.length());
                dataOut.flush();




            } catch (NullPointerException| IOException e) {
                System.out.println("This error occured:\n");
                e.printStackTrace();
                try {
                    PrintWriter out = new PrintWriter(socket.getOutputStream());
                    out.println("HTTP/1.1 404 FILE NOT FOUND");
                    out.println("PRIIT Server 1.0!");
                    out.println("Date: " + new Date());
                    File file = new File("test1/404.html");
                    out.println("Content-length: " + (int) file.length());
                    out.println("");
                    out.flush();


                    BufferedOutputStream dataOut = new BufferedOutputStream(socket.getOutputStream());
                    byte[] fileData = readFileData(file);
                    dataOut.write(fileData, 0, (int) file.length());
                    dataOut.flush();


                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

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
