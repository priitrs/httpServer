package httpServer;

import idNumber.EEIdNumber;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Service {

    public Service() {
    }

    public String validateIdCode(String id) {
        EEIdNumber eeIdNumber = new EEIdNumber(id);
        return "{\"Validation\":\""+ eeIdNumber.validateIdNumber() + "\",\"BirthDate\":\"" + eeIdNumber.birthDate + "\"}";
    }

    public void calculateSalary(String grossSalary) throws IOException {
        writeToFile( "some result from " + grossSalary);
    }

    private void writeToFile(String routingResult) throws IOException {
        FileOutputStream out = new FileOutputStream("webroot/testing.html");

        String beginning = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Testing</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div>\n";
        out.write(beginning.getBytes(StandardCharsets.UTF_8));
        out.write(routingResult.getBytes(StandardCharsets.UTF_8));
        String ending = "\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>";
        out.write(ending.getBytes(StandardCharsets.UTF_8));
        out.close();
    }

    public String sendJson() {
        return "{\"Brutopalk\":\"1500\",\"Netopalk\":\"1200\"}";
    }
}

