import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

class ApplicationManager {
    private Socket socket;
    private String applicationName;
    private String topicName;
    private String task;
    private Scanner scannerServer;
    private PrintWriter serverPrintOut;
    private Long offset;
    private int partition=-1;
    public ApplicationManager(String ipAddress, int port) throws IOException {
        socket = new Socket(ipAddress, port);
        InputStream fromServer = socket.getInputStream();
        OutputStream toServer = socket.getOutputStream();
        scannerServer = new Scanner(fromServer, "UTF-8");
        serverPrintOut = new PrintWriter(new OutputStreamWriter(toServer, "UTF-8"), true);
    }
    public void initApp(String applicationName, String topicName, String task){
        this.applicationName = applicationName;
        this.topicName = topicName;
        this.task = task;
    }
    public void connect() throws Exception {
        //System.out.println("Send request");
        serverPrintOut.println(applicationName+" "+task+" "+topicName);
        String res = null;
        //System.out.println("Wait response");
        try {
            res = scannerServer.nextLine();
        } catch (NoSuchElementException e){
            System.out.println("Cannot connect");
            throw new Exception("Cannot connect to server");
        }
        System.out.println(res);
        if(res.split(" ")[0].equals("Notes:")) {
            if (res.contains("Subscribe to")) {
                String first = res.substring(res.lastIndexOf("n.") + 2);
                partition = Integer.parseInt(first.substring(0, first.indexOf(". ")));
            } else if (res.contains("No partitions") || res.contains("cannot be recovered")) {
                closeError(res);
            }
            res = scannerServer.nextLine();
        }
        if(res.split(" ")[0].equals("Error:")){
            closeError(res);
        } else if(!res.equals("ok") && !res.split(" ")[0].equals("Error:")){
            serverPrintOut.println(1+" "+1);
            String rescreate = scannerServer.nextLine();
            if(!rescreate.equals("ok"))
                closeError(rescreate);
            res = scannerServer.nextLine();
            if(!res.equals("ok"))
                closeError(res);
        }
        System.out.println("Connected to server");
    }
    public void updateOffset(Long newOffset) throws Exception {
        serverPrintOut.println("update offset "+newOffset);
        String res = null;
        try {
            res = scannerServer.nextLine();
            if(!res.equals("ok")){
                closeError(res);
            }
        } catch (NoSuchElementException e){
            //closeError("Cannot connect to server");
            reconnect();
        }
    }
    public Long getOffset() throws Exception {
        serverPrintOut.println("get offset");
        String res = null;
        try {
            res = scannerServer.nextLine();
            /*if(!res.equals("ok")){
                closeError(res);
            }*/
        } catch (NoSuchElementException e){
            //closeError("Cannot connect to server");
            reconnect();
        }
        offset = Long.parseLong(res);
        return offset;
    }
    public void disconnect(){
        serverPrintOut.println("end");
    }
    public void closeError(String e){
        System.err.println(e);
        System.exit(1);
    }
    public void imhere() {
        serverPrintOut.println("ok");
        try {
            String res = scannerServer.nextLine();
            if(!res.equals("ok")){
                closeError(res);
            }
        } catch (NoSuchElementException e){
            //closeError("Cannot connect to server");
            reconnect();
        }
    }
    public void reconnect(){
        try {
            closeStreams();
        } catch (IOException ex) {
            closeError("Error: "+ex.getMessage());
        }
        boolean connected = false;
        int tries = 10;
        for(int i=0; !connected && i<tries; i++){
            System.out.println("Disconnected from server, trying to reconnect.... (try n. "+(i+1)+")");
            System.out.flush();
            try {
                socket = new Socket("localhost", 9991);
                InputStream fromServer = socket.getInputStream();
                OutputStream toServer = socket.getOutputStream();
                scannerServer = new Scanner(fromServer, "UTF-8");
                serverPrintOut = new PrintWriter(new OutputStreamWriter(toServer, "UTF-8"), true);
                connect();
                connected = true;
            } catch (IOException e) {
                closeError("socket new error: "+e.getMessage());
            } catch (Exception e) {
                try {
                    closeStreams();
                } catch (IOException ex) {
                    closeError("Error: "+ex.getMessage());
                }
                connected = false;
            }
            try {
                Thread.sleep(1000*(i+1));
            } catch (InterruptedException e) {
                closeError("Error: "+e.getMessage());
            }
            /*try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                closeError("Error: "+e.getMessage());
            }*/
        }
        if(!connected)
            closeError("Cannot connect to server");
    }
    public void closeStreams() throws IOException {
        socket.close();
        scannerServer.close();
        serverPrintOut.close();
    }
    public int getPartition(){
        return partition;
    }
}