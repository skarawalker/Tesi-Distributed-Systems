import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ApplicationThread extends Thread{
    public static TopicList topicList = new TopicList();
    public static List<ApplicationThread> connectedApps = new ArrayList<>();
    private Socket socket;
    private Scanner scanner;
    private PrintWriter serverPrintOut;
    private String applicationName;
    private String topicName;
    private String task;
    private MyKafkaAdmin admin;

    public ApplicationThread(Socket socket, MyKafkaAdmin admin) {
        this.admin = admin;
        this.socket = socket;
        InputStream inputToServer = null;
        OutputStream outputFromServer = null;
        try {
            inputToServer = socket.getInputStream();
            outputFromServer = socket.getOutputStream();
            scanner = new Scanner(inputToServer, "UTF-8");
            serverPrintOut = new PrintWriter(new OutputStreamWriter(outputFromServer, "UTF-8"), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void run(){
        String[] input = scanner.nextLine().split(" ");
        if(input.length<3){
            closeError("not enough arguments");
            return;
        }
        applicationName = input[0];
        task = input[1];
        topicName = input[2];
        String note = Administrator.appNote.get(applicationName);
        if(note!=null){
            serverPrintOut.println("Notes: "+note);
            if (note.contains("No partitions") || note.contains("cannot be recovered")) {
                closeApp();
                return;
            }
        }
        for(ApplicationThread app : connectedApps){
            if(app.getAppName().equals(applicationName)){
                closeError("application with this name already connected");
                return;
            }
        }
        System.out.println("Application "+applicationName+" connected");
        if(!task.equals("produce") && !task.equals("consume")) {
            closeError("task not allowed");
            return;
        }
        try {
            initializeTopic();
            initializeApp();
        } catch (NoSuchElementException e){
            closeApp();
            return;
        }
        connectedApps.add(this);
        boolean end = false;
        String req = null;
        while(!end) {
            try {
                req = scanner.nextLine();
            } catch (NoSuchElementException e) {
                closeApp();
                end = true;
                continue;
            }
            if (req.equals("get offset")) {
                try {
                    serverPrintOut.println(topicList.getNextOffset(topicName, applicationName));
                } catch (Exception e) {
                    closeError(e.getMessage());
                    end = true;
                }
            } else if (req.split(" ")[0].equals("update") && req.split(" ")[1].equals("offset")) {
                try {
                    topicList.updateOffset(topicName, applicationName, Long.parseLong(req.split(" ")[2]));
                    serverPrintOut.println("ok");
                } catch (Exception e) {
                    closeError(e.getMessage());
                    end = true;
                }
            } else if(req.equals("end")){
                end = true;
                closeApp();
            } else if(req.equals("ok")){
                serverPrintOut.println("ok");
            } else {
                serverPrintOut.println("Request not recognized");
            }
        }
    }
    public String getAppName(){
        return applicationName;
    }
    public void closeError(String e){
        serverPrintOut.println("Error: "+e);
        System.out.println("Error application "+applicationName+": "+e);
        try {
            socket.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        if(connectedApps.contains(this))
            connectedApps.remove(this);
    }
    public void initializeTopic(){
        if(!topicList.checkTopic(topicName)) {
            if (admin.topicStatus(topicName)) {
                admin.deleteTopic(topicName);
            }
            serverPrintOut.println("Need to create topic, specify partitions and replication factor");
            String[] res = scanner.nextLine().split(" ");
            if (res.length < 2) {
                closeError("not enough arguments");
                return;
            }
            int partitions = Integer.parseInt(res[0]);
            short replicationFactor = Short.parseShort(res[1]);
            admin.createTopic(topicName, partitions, replicationFactor);
            System.out.println("Topic created: "+topicName);
            serverPrintOut.println("ok");
            try {
                topicList.addTopic(topicName);
            } catch (Exception e) {
                closeError(e.getMessage());
                return;
            }

        }
    }
    public void initializeApp(){
        if(!topicList.appList(topicName).contains(applicationName)) {
            try {
                topicList.addApplication(topicName, applicationName);
                System.out.println("Application "+applicationName+" added to topic "+topicName);
            } catch (Exception e) {
                closeError(e.getMessage());
                return;
            }
        }
        serverPrintOut.println("ok");
    }

    public void closeApp(){
        connectedApps.remove(this);
        System.out.println("Application "+applicationName+" disconnected");
        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}