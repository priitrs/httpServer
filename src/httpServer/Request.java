package httpServer;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Data
public class Request {
    private final String type;
    private String path;
    private final String httpVersion;
    private String body;
    private String contentType;
    private String authorization;
    private int contentLength;
    private final Map<String, String> parameters = new HashMap<>();
    private final Map<String, String> jsonMap = new HashMap<>();

    public Request(List<String> rawRequest) {
        String[] splitRequest = rawRequest.get(0).split(" ");
        this.type = splitRequest[0];
        this.path = splitRequest[1].replaceFirst("/", "");
        this.httpVersion = splitRequest[2];
        for (String requestLine : rawRequest) {
            if (requestLine.contains("Content-Length: ")) {
                this.contentLength = Integer.parseInt(requestLine.replaceFirst("Content-Length: ", ""));
            }
        }
        for (String requestLine : rawRequest) {
            if (requestLine.contains("Content-Type: ")) {
                this.contentType = requestLine.replaceFirst("Content-Type: ", "");
            }
        }
        for (String requestLine : rawRequest) {
            if (requestLine.contains("Authorization: Basic ")) {
                this.authorization = requestLine.replaceFirst("Authorization: Basic ", "");
            }
        }
        extractParametersFromPath();
        ifEmptySetPathToIndex();
    }

    private void extractParametersFromPath() {
        if (this.path.contains("?")) {
            String[] splitPath = this.path.split("\\?");
            this.path = splitPath[0];
            putParametersToMap(splitPath[1]);
        }
    }

    private void putParametersToMap(String parametersFromPath) {
        String[] allKeysValuesArray = parametersFromPath.split("&");
        for (String keyValue : allKeysValuesArray) {
            String[] keyValueArray = keyValue.split("=");
            String key = keyValueArray[0];
            String value = keyValueArray[1];
            this.parameters.put(key, value);
        }
    }

    private void ifEmptySetPathToIndex() {
        if (this.path.equals("")) {
            this.path = "index.html";
        }
    }

}
