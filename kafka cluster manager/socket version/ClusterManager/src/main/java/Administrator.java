import org.apache.kafka.clients.admin.AdminClientConfig;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Administrator {
    public static int serverPort = 9991;
    public static MyKafkaAdmin admin;
    public static Map<String, String> appNote = new HashMap<>();
    static Logger logger = LoggerFactory.getLogger(Administrator.class);

    public static void main(String args[]) {
        DockerManagerConnection dockerConnection = new DockerManagerConnection();
        String kafkaHosts = dockerConnection.getBootstrap();
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHosts);

        //Initialize the object for the topic management
        admin = new MyKafkaAdmin(props);
        try {
            new CheckpointAdmin("/opt/kafka-data/checkpoint.txt").start();
        } catch (IOException e){
            System.out.println(e.getMessage());
            System.exit(1);
        }
        System.out.println("Administrator ready.");
        //Initialize the connection
        while(true) {
            try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
                Socket socket = serverSocket.accept();
                new ApplicationThread(socket, admin).start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static class CheckpointAdmin extends Thread{
        private String checkpointPath;
        private File checkpointFile;
        private long period;
        public CheckpointAdmin(String checkpointPath) throws IOException {
            this.checkpointPath = checkpointPath;
            this.checkpointFile = new File(checkpointPath);
            if(!checkpointFile.exists()) {
                checkpointFile.createNewFile();
            } else {
                BufferedReader fReader = new BufferedReader(new FileReader(checkpointFile));
                String line = fReader.readLine();
                if(line==null)
                    return;
                if(!line.equals("{")){
                    throw new RuntimeException("Checkpoint file malformed");
                }
                line = fReader.readLine();
                while (!line.equals("}")){
                    if(line.length()>2 && line.charAt(2)=='\"'){
                        String topicName = line.substring(3,line.lastIndexOf("\""));
                        String application = fReader.readLine();
                        Map<String, Long> topicApplication = new HashMap<>();
                        if(checkTopic(topicName)) {
                            while (line.length() > 4 && !application.equals("  }") && application.charAt(4) == '\"') {
                                String appName = application.substring(5, application.lastIndexOf("\""));
                                long offset = Long.parseLong(application.substring(application.lastIndexOf("\"") + 3));
                                if(offset!=-1) {
                                    int part = checkOffset(topicName, offset);
                                    String note = appNote.get(appName);
                                    if (note == null)
                                        note = "";
                                    if (part == -1)
                                        note = note + "No partitions for " + topicName + " with requested offset. ";
                                    else
                                        note = note + "Subscribe to " + topicName + " part. n." + part + ". ";
                                    appNote.put(appName, note);
                                }
                                topicApplication.put(appName, offset);
                                application = fReader.readLine();
                            }
                            try {
                                ApplicationThread.topicList.add(topicName, topicApplication);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            while (line.length() > 4 && !application.equals("  }") && application.charAt(4) == '\"') {
                                String appName = application.substring(5, application.lastIndexOf("\""));
                                String note = appNote.get(appName);
                                if(note==null)
                                    note = "";
                                note = note+"Topic "+topicName+" cannot be recovered. ";
                                appNote.put(appName, note);
                                application = fReader.readLine();
                            }
                        }
                    }
                    line = fReader.readLine();
                }
                System.out.println(ApplicationThread.topicList);
                for (String app: appNote.keySet()) {
                    System.out.println("Note for "+app+": "+appNote.get(app));
                }

            }
        }
        public void run(){
            PrintWriter fWriter = null;
            while (true){
                try {
                    Thread.sleep(5000);
                    fWriter = new PrintWriter(checkpointFile);
                    fWriter.print(ApplicationThread.topicList.toString());
                    fWriter.close();
                } catch (InterruptedException | FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        public boolean checkTopic(String topicName){
            if (!admin.topicStatus(topicName)) {
                System.out.println("Topic "+topicName+" not present");
                return false;
            } else {
                return true;
            }
        }
        public int checkOffset(String topicName, long offset){
            String note = null;
            for(int i = 0; i<10; i++){
                try {
                    long currentOffset = admin.offsetStatus(topicName, i);
                    if(currentOffset>offset){
                        return i;
                    }
                } catch (Exception e) {
                    break;
                }
            }
            return -1;
        }

    }

    static class DockerManagerConnection{
        private Socket socket;
        private Scanner scanner;
        private PrintWriter serverPrintOut;
        public DockerManagerConnection(){
            try {
                ServerSocket server = new ServerSocket(9992);
                this.socket = server.accept();
                InputStream inputStream = this.socket.getInputStream();
                this.scanner = new Scanner(inputStream, "UTF-8");
                OutputStream outputStream = this.socket.getOutputStream();
                this.serverPrintOut = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);
            } catch (IOException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
            System.out.println("Connected to the Docker Manager");
        }
        public String getBootstrap(){
            serverPrintOut.println("get bootstraps");
            String res = null;
            try{
                res = scanner.nextLine();
            } catch (NoSuchElementException e) {
                System.err.println("Docker manager disconnected");
                System.exit(1);
            }
            if(res.contains("Error")){
                System.err.println(res);
                System.exit(1);
            }
            return res;
        }
    }


}
