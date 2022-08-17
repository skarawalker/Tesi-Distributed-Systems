import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.*;

import java.util.*;


public class MyDockerAdmin {
    private DockerClient docker = null;
    private String adminContainerName;
    private String adminNetworkName;
    public MyDockerAdmin(String adminContainerName, String adminNetworkName) throws Exception {
        this.docker = DefaultDockerClient.fromEnv().build();
        this.adminContainerName = adminContainerName;
        this.adminNetworkName = adminNetworkName;
        if(this.adminContainerName==null && this.adminNetworkName==null) {
            throw new Exception("At least one of the parameters must be not null");
        }
    }
    public void boot() throws DockerException, InterruptedException {
         if(this.adminNetworkName==null) {
            ContainerInfo adminCont = docker.inspectContainer(this.adminContainerName);
            for(String name: adminCont.networkSettings().networks().keySet()){
                if(name.contains("administrator")){
                    this.adminNetworkName = name;
                    break;
                }
            }
            System.out.println("Retrieved admin network: "+this.adminNetworkName);
        }
        if(docker.inspectNetwork(this.adminNetworkName).containers().isEmpty() || this.adminContainerName==null){
            List<Container> zooConts = docker.listContainers(DockerClient.ListContainersParam.allContainers()).stream().filter(cont -> cont.names().get(0).contains("zookeeper")).toList();
            List<Container> otherConts = docker.listContainers(DockerClient.ListContainersParam.allContainers()).stream().filter(cont -> !cont.names().get(0).contains("zookeeper")).toList();
            for(Container zoo: zooConts){
                List<String> contNets = zoo.networkSettings().networks().keySet().asList();
                String name = zoo.names().get(0);
                if (contNets.contains(this.adminNetworkName)){
                    if(!zoo.state().equals("running")){
                        docker.startContainer(name);
                        System.out.println("Starting container "+name);
                    }
                }
            }
            for(Container cont: otherConts){
                List<String> contNets = cont.networkSettings().networks().keySet().asList();
                String name = cont.names().get(0);
                if (contNets.contains(this.adminNetworkName)){
                    if(this.adminContainerName == null && name.contains("administrator-administrator")) {
                        this.adminContainerName = name;
                        System.out.println("Retrieved admin container: "+this.adminContainerName);
                    }
                    if(!cont.state().equals("running")){
                        docker.startContainer(name);
                        System.out.println("Starting container "+name);
                    }
                }
            }
        }
    }
    public Map<String, String> getIp() throws DockerException, InterruptedException {
        Map<String, String> ipMap = new HashMap<>();
        for(String id: docker.inspectNetwork(adminNetworkName).containers().keySet()){
            ContainerInfo container = docker.inspectContainer(id);
            ipMap.put(container.name(), container.networkSettings().networks().get(adminNetworkName).ipAddress());
        }
        return ipMap;
    }
    public String getIp(String contName) throws DockerException, InterruptedException {
        for(String id: docker.inspectNetwork(adminNetworkName).containers().keySet()){
            ContainerInfo container = docker.inspectContainer(id);
            if(container.name().substring(1).equals(contName))
                return container.networkSettings().networks().get(adminNetworkName).ipAddress();
        }
        return null;
    }
    public String getBootstraps() throws DockerException, InterruptedException {
        String bootstrapAddresses = "";
        for(String id: docker.inspectNetwork(adminNetworkName).containers().keySet()){
            ContainerInfo container = docker.inspectContainer(id);
            if(container.name().contains("kafka-worker")) {
                String ip = container.networkSettings().networks().get(adminNetworkName).ipAddress();
                String port = container.networkSettings().ports().keySet().asList().get(1).substring(0,4);
                bootstrapAddresses = bootstrapAddresses+ip+":"+port+",";
            }
        }
        return bootstrapAddresses;
    }
    public List<Integer> getAdminPorts() throws DockerException, InterruptedException {
        List<Integer> ports = new ArrayList<>();
        ContainerInfo cont = docker.inspectContainer(adminContainerName);
        for(String port : cont.networkSettings().ports().keySet()){
            ports.add(Integer.parseInt(port.substring(0,4)));
        }
        return ports;
    }
    public void close(){
        docker.close();
    }
    public void createWorker() throws DockerException, InterruptedException {
        List<Container> zooConts = docker.listContainers(DockerClient.ListContainersParam.allContainers()).stream().filter(cont -> cont.names().get(0).contains("zookeeper")).toList();
        String zooPorts = "";
        for(Container zoo: zooConts){
            ContainerInfo zooInfo = docker.inspectContainer(zoo.id());
            if(zooPorts.length()!=0)
                zooPorts = zooPorts+",";
            String zooName = zooInfo.name();
            String zooPort = zooInfo.networkSettings().ports().keySet().asList().get(0).substring(0,4);
            System.out.println(getIp(zoo.names().get(0).substring(1)));
            zooPorts = zooPorts+getIp(zoo.names().get(0).substring(1))+":"+zooPort;
        }
        List<Container> workerConts = docker.listContainers(DockerClient.ListContainersParam.allContainers()).stream().filter(cont -> cont.names().get(0).contains("worker")).toList();
        List<Integer> workerPorts = new ArrayList<>();
        workerConts.stream().forEach(cont -> {
            try {
                List<String> ports = docker.inspectContainer(cont.id()).networkSettings().ports().keySet().asList();
                if(!ports.isEmpty()) {
                    String port = ports.get(0);
                    System.out.println(port.substring(0, port.indexOf("/")));
                    workerPorts.add(Integer.parseInt(port.substring(0, port.indexOf("/"))));
                }
            } catch (DockerException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        Integer nextPort = workerPorts.stream().max(Integer::compare).get()+1000;
        List<String> env = Arrays.asList("KAFKA_BROKER_ID="+(workerConts.toArray().length+1),
                "KAFKA_ZOOKEEPER_CONNECT="+zooPorts,
                "KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://kafka-worker-"+(workerConts.toArray().length+1)+":9092,PLAINTEXT_HOST://localhost:"+nextPort,
                "KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT",
                "KAFKA_INTER_BROKER_LISTENER_NAME=PLAINTEXT");
        System.out.println(env);

        ContainerConfig containerConfig = ContainerConfig.builder()
                .image("confluentinc/cp-kafka:latest")
                .env(env)
                .build();

        ContainerCreation creation = docker.createContainer(containerConfig, "kafka-worker-"+(workerConts.toArray().length+1));
        String id = creation.id();
        Network net = docker.inspectNetwork(adminNetworkName);
        docker.connectToNetwork(id, net.id());
        docker.startContainer(id);
        net = docker.inspectNetwork(adminNetworkName);
        System.out.println(net.containers());
    }
}
