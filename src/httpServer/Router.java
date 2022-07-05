package httpServer;

import salary.GrossSalary;
import salary.SalaryCalculator;

import java.io.IOException;
import java.math.BigDecimal;

public class Router {

    Service service = new Service();

    public Router() {
    }

    public String routingExists(Request request) throws IOException {
        switch (request.path) {

            case "idcode":
                if (request.parameters.containsKey("id") && request.parameters.get("id") != null) {
                    service.validateIdCode(request.parameters.get("id"));
                    return "valideeritud";
                }
                return "vigane sisend";

            case "json":
                return service.sendJson();

            case "salary":
                SalaryCalculator calculation = new SalaryCalculator(new GrossSalary(BigDecimal.valueOf(1000)), false, true, true, true, true);

                return calculation.toString();

            default:
                return "";
        }
    }
}
