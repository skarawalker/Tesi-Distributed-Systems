import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AdminConnection {
    private final HttpClient client = HttpClient.newBuilder().build();
    private String adminAddress;

    public AdminConnection() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:5000/adminPorts"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String res = responseHandler(response);
        if(res.charAt(0)=='[' && res.charAt(res.length()-2)==']' ){
            res = res.substring(1,res.length()-2);
            String adminPort = null;
            for(String port: res.split(",")){
                if(!port.contains("5000") || !port.contains("8080"))
                    adminPort = port.substring(1, port.length()-1);
            }
            adminAddress = "localhost:"+adminPort;
        }
    }

    /**
     * Methods for applications
     */
    public String addAppToTopic(String appName, String topicName, String startingOffset) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://"+adminAddress+"/app/addApp"))
                .header("Content-Type", "application/json")
                .method("POST", HttpRequest.BodyPublishers.ofString("{\r\n" +
                        "\"appName\":\""+appName+"\",\r\n" +
                        "\"topicName\":\""+topicName+"\",\r\n" +
                        "\"offset\":\""+startingOffset+"\"\n" +
                        "}"))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return responseHandler(response);
    }

    public String getAppInTopic(String topicName) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://"+adminAddress+"/app/getApp"))
                .method("GET", HttpRequest.BodyPublishers.ofString(topicName))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return responseHandler(response);
    }

    public String getOffset(String appName, String topicName) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://"+adminAddress+"/app/getOffset"))
                .header("Content-Type", "application/json")
                .method("GET", HttpRequest.BodyPublishers.ofString("{\r\n" +
                        "\"appName\":\""+appName+"\",\r\n" +
                        "\"topicName\":\""+topicName+"\"\r\n" +
                        "}"))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return responseHandler(response);
    }

    public String updateOffset(String appName, String topicName, long newOffset) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://"+adminAddress+"/app/updateOffset"))
                .header("Content-Type", "application/json")
                .method("POST", HttpRequest.BodyPublishers.ofString("{\r\n" +
                        "\"appName\":\""+appName+"\",\r\n" +
                        "\"topicName\":\""+topicName+"\",\r\n" +
                        "\"offset\":\""+newOffset+"\"\n" +
                        "}"))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return responseHandler(response);
    }

    public String getPartition(String appName, String topicName) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://"+adminAddress+"/app/getPartition"))
                .header("Content-Type", "application/json")
                .method("GET", HttpRequest.BodyPublishers.ofString("{\r\n" +
                        "\"appName\":\""+appName+"\",\r\n" +
                        "\"topicName\":\""+topicName+"\"\r\n" +
                        "}"))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return responseHandler(response);
    }

    public String updatePartition(String appName, String topicName, int newPartition) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://"+adminAddress+"/app/updatePartition"))
                .header("Content-Type", "application/json")
                .method("POST", HttpRequest.BodyPublishers.ofString("{\r\n" +
                        "\"appName\":\""+appName+"\",\r\n" +
                        "\"topicName\":\""+topicName+"\",\r\n" +
                        "\"partition\":\""+newPartition+"\"\n" +
                        "}"))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return responseHandler(response);
    }
    public String getAppNotes(String appName, String topicName) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://"+adminAddress+"/app/getNotes"))
                .header("Content-Type", "application/json")
                .method("GET", HttpRequest.BodyPublishers.ofString("{\r\n" +
                        "\"appName\":\""+appName+"\",\r\n" +
                        "\"topicName\":\""+topicName+"\"\r\n" +
                        "}"))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return responseHandler(response);
    }

    /**
     * Methods for topics
     */
    public String getTopicStatus(String topicName) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://"+adminAddress+"/topic/getTopicStatus"))
                .method("GET", HttpRequest.BodyPublishers.ofString(topicName))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return responseHandler(response);
    }
    public String createTopic(String topicName) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://"+adminAddress+"/topic/addTopic"))
                .method("POST", HttpRequest.BodyPublishers.ofString(topicName))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return responseHandler(response);
    }
    public String getTopicNotes(String topicName) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://"+adminAddress+"/topic/getNotes"))
                .method("GET", HttpRequest.BodyPublishers.ofString(topicName))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return responseHandler(response);
    }

    /**
     * Common methods
     */
    private String responseHandler(HttpResponse<String> response){
        if(response.statusCode()==200)
            return response.body();
        else if(response.uri().toString().contains(":5000")) {
            return "Error: "+response.body();
        }else{
            String res = response.body().substring(response.body().indexOf("\"error"));
            return res.substring(9, res.indexOf(",")-1);
        }
    }
}
