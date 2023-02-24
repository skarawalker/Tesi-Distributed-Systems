package com.administrator.clustermanager;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

@SpringBootApplication
public class ClustermanagerApplication {

	public static MyKafkaAdmin kafkaAdmin;
	static String kafkaHosts = null;

	public static void main(String[] args) throws InterruptedException {
		while(kafkaHosts==null){
			try {
				kafkaHosts = getBootstraps();
			} catch (Exception e) {
				kafkaHosts = null;
			}
			Thread.sleep(2000);
		}
		//Kafka API
		Properties props = new Properties();
		props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHosts);
		kafkaAdmin = new MyKafkaAdmin(props);
		// Boot application
		SpringApplication.run(ClustermanagerApplication.class, args);

		CheckpointManager checkpoints = new CheckpointManager("/opt/admin-data/checkpoints.txt", "9991");
		checkpoints.importCheckpoints();
		checkpoints.start();
	}

	private static String getBootstraps() throws IOException {
		ServerSocket s = new ServerSocket(5000);
		Socket socket = s.accept();
		PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out.println("getBootstraps");
		String res = in.readLine();
		s.close();
		System.out.println(res);
		return res;
	}

}
