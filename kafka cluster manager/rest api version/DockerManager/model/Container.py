class Container:
    def __init__(self, id=None, name=None, status=None, network=None, ip=None, ports=None):
        self.id = id
        self.name = name
        self.status = status
        self.network = network
        self.ip = ip
        self.ports = ports

    def toJSON(self):
        json = {
            "id": self.id,
            "name" : self.name,
            "status" : self.status,
            "network" : self.network,
            "ip" : self.ip,
            "port" : self.ports
        }
        return json

    def fromContainer(self, cont):
        self.id = cont.id
        self.name = cont.name
        self.status = cont.status
        self.network = list(cont.attrs["NetworkSettings"]["Networks"])[0]
        self.ip = cont.attrs["NetworkSettings"]["Networks"][self.network]['IPAddress']
        self.ports = cont.attrs["NetworkSettings"]["Ports"]
        return self
