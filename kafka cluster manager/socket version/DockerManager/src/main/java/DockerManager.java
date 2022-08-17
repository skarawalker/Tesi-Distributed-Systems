import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;

import javax.print.Doc;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class DockerManager {
    static int serverPort=9992;
    static Socket socket;
    static Scanner scannerServer;
    static PrintWriter serverPrintOut;
    static DockerAdmin docker = null;
    public static void main(String args[]) {
        String adminContainerName = null;
        String adminNetworkName = null;
        int i=0;
        for(; i<args.length; i++){
            if(args[i].equals("-adminCont")) {
                adminContainerName = args[i+1];
                i++;
            } else if(args[i].equals("-adminNet")) {
                adminNetworkName = args[i+1];
                i++;
            }
        }
        if(adminContainerName==null && adminNetworkName==null) {
            exit("Usage: \n -adminCont 'name of the admin container' \n -adminNet 'name of the admin network \ninsert at least one of these 2 parameters'");
        } else {
            try {
                docker = new DockerAdmin(adminContainerName, adminNetworkName);
                docker.boot();
            } catch (Exception e) {
                exit(e.getMessage());
            }
        }
        Integer adminPort = null;
        try {
            adminPort = docker.getAdminPorts().get(1);
            System.out.println("Administrator port for the connection: "+adminPort);
        } catch (Exception e) {
            exit(e.getMessage());
        }
        adminConnection(adminPort);
        boolean end = false;
        while (!end) {
            String req = null;
            try {
                req = scannerServer.nextLine();
            } catch (NoSuchElementException e) {
                System.err.println("Administrator disconnected");
                end = true;
                continue;
            }
            System.out.println(req);
            if(req.equals("get bootstraps")) {
                try {
                    serverPrintOut.println(docker.getBootstraps());
                } catch (Exception e) {
                    serverPrintOut.println("Error: " + e.getMessage());
                    exit(e.getMessage());
                }
            } else if(req.equals("end")){
                end = true;
            } else {
                serverPrintOut.println("Error: cannot recognize the request");
            }
        }
        docker.close();
    }

    public static void exit(String e){
        if(docker!=null) {
            docker.close();
        }
        System.err.println(e);
        System.exit(1);
    }
    public static void adminConnection(int port){
        boolean connected = false;
        while (!connected) {
            try {
                Socket socket = new Socket("localhost", port);
                InputStream inputStream = socket.getInputStream();
                scannerServer = new Scanner(inputStream, "UTF-8");
                OutputStream outputStream = socket.getOutputStream();
                serverPrintOut = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);
                connected = true;
            } catch (IOException e) {
                System.err.println("Cannot connect, retry after 5s...");
            }
            try{
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
        }
        System.out.println("Connected to the administrator program");
    }
}
