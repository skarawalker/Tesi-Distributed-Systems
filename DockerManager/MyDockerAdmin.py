from time import sleep
import docker

class MyDockerAdmin:
    client = docker.from_env()

    def __init__(self, netName=None, adminName=None):
        self.netName = netName
        self.adminName = adminName
        if(self.adminName==None and self.netName==None):
            raise Exception("At least one between netName and adminName must be defined")
        if(self.adminName==None):
            containers = self.client.containers.list(all=True, filters={"network":self.netName})
            for cont in containers:
                if("administrator-admin" in cont.name):
                    self.adminName = cont.name
        elif(self.netName==None):
            self.netName=list(self.client.containers.get(self.adminName).attrs["NetworkSettings"]["Networks"].keys())[0]

    def getAllContainers(self):
        return self.client.containers.list(all=True)

    def getContainersInNet(self):
        return self.client.containers.list(all=True, filters={"network":self.netName})
    
    def getAllIp(self):
        ipList = {}
        containers = self.client.containers.list(all=True, filters={"network":self.netName})
        for cont in containers:
            name = cont.name
            ipAddress = cont.attrs["NetworkSettings"]["Networks"][self.netName]['IPAddress']
            ipList[name] = ipAddress
        return ipList

    def getIp(self, contName):
        cont = self.client.containers.get(contName)
        return cont.attrs["NetworkSettings"]["Networks"][self.netName]['IPAddress']

    def getPorts(self, contName):
        ports = []
        for p in self.client.containers.get(contName).attrs["NetworkSettings"]["Ports"]:
            ports.append(p[0:p.index("/")])
        return ports

    def getAdminIp(self):
        cont = self.client.containers.get(self.adminName)
        return cont.attrs["NetworkSettings"]["Networks"][self.netName]['IPAddress']

    def getAdminPorts(self):
        ports = []
        for p in self.client.containers.get(self.adminName).attrs["NetworkSettings"]["Ports"]:
            ports.append(p[0:p.index("/")])
        return ports
    
    def getBootstraps(self):
        bootstraps = ""
        workers = [cont for cont in self.client.containers.list(all=True, filters={"network":self.netName}) if "kafka-worker" in cont.name]
        for cont in workers:
            name = cont.name
            workerIp = self.getIp(cont.name)
            workerPort = self.getPorts(cont.name)[1]
            bootstraps = bootstraps+workerIp+":"+workerPort+","
        return bootstraps
    
    def boot(self):
        zookeepers = [cont for cont in self.client.containers.list(all=True, filters={"network":self.netName}) if "zookeeper" in cont.name]
        others = [cont for cont in self.client.containers.list(all=True, filters={"network":self.netName}) if not "zookeeper" in cont.name]
        for zoo in zookeepers:
            if(zoo.status!="running"):
                zoo.start()
                print("Container "+zoo.name+" started")
        for other in others:
            if(other.status!="running"):
                other.start()
                print("Container "+other.name+" started")

    def createWorker(self):
        #Take the zookeepers
        zookeepers = [cont for cont in self.client.containers.list(all=True, filters={"network":self.netName}) if "zookeeper" in cont.name]
        zooAddresses = ""
        for zoo in zookeepers:
            zooIp = self.getIp(zoo.name)
            zooPort = self.getPorts(zoo.name)[0]
            zooAddresses = zooAddresses+zooIp+":"+zooPort+","
        #Take new worker name and id
        workers = [cont for cont in self.client.containers.list(all=True, filters={"network":self.netName}) if "kafka-worker" in cont.name]
        newWorkerId = len(workers)+1
        newWorkerName = "kafka-worker-"+str(newWorkerId)
        #Take new worker port
        workerPorts = [self.getPorts(worker.name)[0] for worker in workers]
        newWorkerPort = int(max(workerPorts))+1
        #Set the new worker environment
        env = [
            "KAFKA_BROKER_ID="+str(newWorkerId),
            "KAFKA_ZOOKEEPER_CONNECT="+zooAddresses,
            "KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://"+newWorkerName+":9092,PLAINTEXT_HOST://localhost:"+str(newWorkerPort),
            "KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT",
            "KAFKA_INTER_BROKER_LISTENER_NAME=PLAINTEXT"
        ]
        #Set exposed port
        exposedPorts = {str(newWorkerPort)+"/tcp": [{"HostIp": "0.0.0.0", "HostPort": str(newWorkerPort)}]}
        #Create container
        newWorker = self.client.containers.create(image="confluentinc/cp-kafka:latest", name=newWorkerName, environment=env, network=self.netName, ports=exposedPorts)
        newWorker.start()
        return newWorker in self.client.networks.get(self.netName).containers and self.client.containers.get(newWorker.name).status=="running"
        
    def startContainer(self, contName):
        cont = self.client.containers.get(contName)
        cont.start()

    def getContStats(self, contName):
        cont = self.client.containers.get(contName)
        return cont.stats(stream=False)

    def getContStatsStream(self, contName):
        cont = self.client.containers.get(contName)
        return cont.stats(stream=True)

    def close(self):
        self.client.close()
