import threading
import time
from MyDockerAdmin import MyDockerAdmin

class ContainerChecker:
    
    def thread_function(self):
        while(True):
            contList = self.admin.getContainersInNet()
            offlineCont = [cont for cont in contList if cont.status!="running"]
            for cont in offlineCont:
                self.admin.startContainer(cont.name)
            time.sleep(5)


    def __init__(self, admin):
        self.admin = admin
        x = threading.Thread(target=self.thread_function)
        x.start()