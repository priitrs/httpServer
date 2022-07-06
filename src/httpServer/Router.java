package httpServer;

import java.io.IOException;

public class Router {

    Service service = new Service();

    public Router() {
    }

    public String routingExists(Request request) throws IOException {
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


            default:
                return "";
        }
    }
}
