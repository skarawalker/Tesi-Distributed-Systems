package com.administrator.clustermanager;

import com.administrator.clustermanager.controllers.TopicsController;
import com.administrator.clustermanager.models.repositories.TopicsRepository;
import com.administrator.clustermanager.models.services.TopicService;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.util.Properties;

@SpringBootApplication
public class ClustermanagerApplication {

	public static MyKafkaAdmin kafkaAdmin;
	static String kafkaHosts = "172.26.0.5:9092";

	public static void main(String[] args) {
		//Kafka API
		Properties props = new Properties();
		props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHosts);
		kafkaAdmin = new MyKafkaAdmin(props);
		// Boot application
		SpringApplication.run(ClustermanagerApplication.class, args);

		CheckpointManager checkpoints = new CheckpointManager("C:\\Users\\scara\\Desktop\\checkpoints.txt", "9991");
		checkpoints.importCheckpoints();
		checkpoints.start();
	}

}
