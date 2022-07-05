package httpServer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {
    List<String> rawRequest;
    String type;
    String path;
    String httpVersion;
    String body;
    int contentLength;
    String contentType;
    Map<String, String> parameters = new HashMap<>();
    Map<String, String> jsonMap = new HashMap<>();

    public Request(List<String> rawRequest) {
        String[] splitRequest = rawRequest.get(0).split(" ");
        this.type = splitRequest[0];
        this.path = splitRequest[1].replaceFirst("/", "");
        this.httpVersion = splitRequest[2];
        this.rawRequest = rawRequest;
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

        extractParametersFromPath();
        ifEmptySetPathToIndex();
    }

    private void extractParametersFromPath() {
        if (this.path.contains("?")) {
            String[] splitPath = this.path.split("\\?");
            this.path = splitPath[0];
            getParameters(splitPath[1]);
        }
    }

    private void getParameters(String allKeysValues) {
        String[] allKeysValuesArray = allKeysValues.split("&");
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
