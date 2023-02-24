package com.administrator.clustermanager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class CheckpointManager extends Thread {
    private String path;
    private File checkpoints;
    private String servicePort;
    private final HttpClient client = HttpClient.newBuilder().build();

    public CheckpointManager(String path, String servicePort){
        this.path = path;
        this.servicePort = servicePort;
        this.checkpoints = new File(path);
        if(!checkpoints.exists()) {
            try {
                checkpoints.createNewFile();
            } catch (IOException e) {
                close(e.getMessage());
            }
        }
    }

    public void importCheckpoints(){
        BufferedReader fReader = null;
        try {
            fReader = new BufferedReader(new FileReader(checkpoints));
            String line = fReader.readLine();
            if (line != null && line.charAt(0)=='[' && line.charAt(1)!=']') {
                List<String> topics = parse(line.substring(1));
                for (String t : topics) {
                    System.out.println(t);
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:"+servicePort+"/topic/recoverTopic"))
                            .header("Content-Type", "application/json")
                            .method("POST", HttpRequest.BodyPublishers.ofString("{"+t+"}"))
                            .build();
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    String topicName = getTopicName(t);
                    if(response.statusCode()==200) {
                        request = HttpRequest.newBuilder()
                                .uri(URI.create("http://localhost:"+servicePort+"/topic/getTopicStatus"))
                                .method("GET", HttpRequest.BodyPublishers.ofString(topicName))
                                .build();
                        response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        if(response.statusCode()==200 && response.body().equals("online"))
                            System.out.println("Topic " + topicName + " recovered");
                        else
                            System.out.println("Topic " + topicName + " not recovered");
                    }
                }
            }
        } catch(IOException | InterruptedException e){
            close(e.getMessage());
        }
    }

    public void run(){
        System.out.println("Start checkpointing");
        PrintWriter fWriter = null;
        while (true) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + servicePort + "/topic/"))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = null;
            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (Exception e) {
                close(e.getMessage());
            }
            if(response.statusCode()==200) {
                try {
                    fWriter = new PrintWriter(checkpoints);
                    fWriter.print(response.body());
                    fWriter.close();
                    Thread.sleep(5000);
                } catch (InterruptedException | FileNotFoundException e) {
                    close(e.getMessage());
                }
            }
        }
    }
    private void close(String error){
        System.err.println("Error: "+error);
        System.exit(1);
    }

    private String getTopicName(String topicJSON){
        String first = topicJSON.substring(topicJSON.indexOf("\"name\":\"")+8);
        return first.substring(0, first.indexOf("\""));
    }

    private List<String> parse(String line){
        List<String> list = new ArrayList<>();
        while(line.length()!=0) {
            JSONObject obj = new JSONObject(line);
            line = line.substring(obj.toString().length()+1);
            list.add(obj.toString().substring(1, obj.toString().length()-1));
        }
        return list;
    }

}
