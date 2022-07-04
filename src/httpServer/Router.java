package httpServer;

import java.io.IOException;

public class Router {

    Service service = new Service();

    public Router() {
    }

    public boolean routingExists(MyHttpServer.Request request) throws IOException {
        switch (request.path) {

            case "idcode":
                if (request.parameters.containsKey("id") && request.parameters.get("id") != null) {
                    service.validateIdCode(request.parameters.get("id"));
                    return true;
                }
                return false;

            case "salary":
                service.calculateSalary(request.parameters.get("grossSalary"));
                return true;

            case "json":
                String json = service.sendJson();


                return true;

            default:
                return false;
        }
    }
}
