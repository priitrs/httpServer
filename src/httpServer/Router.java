package httpServer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

public class Router {

    Service service = new Service();

    public Router() {
    }

    public String routingExists(Request request, Socket socket) throws IOException {
        switch (request.path) {

            case "idcode":
                if (request.parameters.containsKey("id") && request.parameters.get("id") != null) {
                    return service.validateIdCode(request.parameters.get("id"));

                }
                return "";

            case "validateCalculate":
                return "[" + service.validateIdCode(request.jsonMap.get("idcode")) + "," + service.calculateSalary(request.jsonMap.get("salary"), request.parameters.get("type")) + "]";

            case "salary":
                return service.calculateSalary(request.parameters.get("salary"), request.parameters.get("type"));

            case "protected":
                PrintWriter out = new PrintWriter(socket.getOutputStream());
                out.println(request.httpVersion + " 401" + " Authorization Required");
                out.println("Date: " + new Date());
                out.println("Content-length: 0");
                out.println("WWW-Authenticate: Basic realm=\"protected\"");
                out.println("");
                out.flush();


            default:
                return "";
        }
    }
}
