package httpServer;

import java.io.IOException;
import java.net.Socket;

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

            case "salary":
                return service.calculateSalary(request.parameters.get("salary"), request.parameters.get("type"));

            case "validateCalculate":
                return "[" + service.validateIdCode(request.jsonMap.get("idcode")) + "," + service.calculateSalary(request.jsonMap.get("salary"), request.parameters.get("type")) + "]";

            default:
                return "";
        }
    }
}
