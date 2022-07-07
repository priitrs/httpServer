package httpServer;

import java.io.IOException;
import java.net.Socket;

public class Router {

    private final Service service = new Service();

    public Router() {
    }

    public String routingExists(Request request, Socket socket) throws IOException {
        switch (request.getPath()) {

            case "idcode":
                if (request.getParameters().containsKey("id") && request.getParameters().get("id") != null) {
                    return service.validateIdCode(request.getParameters().get("id"));
                }
                return "";

            case "salary":
                return service.calculateSalary(request.getParameters().get("salary"), request.getParameters().get("type"));

            case "validateCalculate":
                return "[" + service.validateIdCode(request.getJsonMap().get("idcode")) + "," + service.calculateSalary(request.getJsonMap().get("salary"), request.getParameters().get("type")) + "]";

            default:
                return "";
        }
    }
}
