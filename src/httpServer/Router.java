package httpServer;

public class Router {

    Service service = new Service();

    public Router() {
    }

    public String routing(MyHttpServer.Request request) {
        switch (request.path) {
            case "idcode":
               return service.validateIdCode(request.parameters.get("id"));

            case "salary":
               return service.calculateSalary(request.parameters.get("grossSalary"));

            default:
                return "400";
        }
    }
}
