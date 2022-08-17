from operator import imod
import flask
import sys
from MyDockerAdmin import MyDockerAdmin
from model.Container import Container
import docker.errors
from ContainerChecker import ContainerChecker

args = sys.argv[1:]
if (not("-networkName" in args) and not("-containerName" in args)):
    sys.exit("Required arguments \n  -networkName    name of the Docker network \n  -containerName  name of the Docker container")
elif not("-networkName" in args):
    adminName = args[args.index("-containerName")+1]
    netName = None
else:
    adminName = None
    netName = args[args.index("-networkName")+1]
try:
    admin = MyDockerAdmin(netName=netName, adminName=adminName)
    admin.boot()
except:
    print("Docker is offline")
    exit(1)
checker = ContainerChecker(admin)

app = flask.Flask(__name__)

@app.get("/")
def allFunctions():
    return ["GET /listAllContainers",
    "GET /listContainersInNet",
    "GET /listAllIp",
    "GET /listContainerIp BODY: contName",
    "GET /listContainerPorts BODY: contName",
    "GET /adminIp",
    "GET /adminPorts",
    "GET /bootstrapsIp",
    "POST /createWorker"]


@app.get("/listAllContainers")
def getAllContainers():
    contList = admin.getAllContainers()
    res = []
    for c in contList:
        res.append(Container().fromContainer(c).toJSON())
    return res

@app.get("/listContainersInNet")
def getContainerInNet():
    contList = admin.getContainersInNet()
    res = []
    for c in contList:
        res.append(Container().fromContainer(c).toJSON())
    return res

@app.get("/listAllIp")
def getAllIp():
    return admin.getAllIp()

@app.get("/listContainerIp")
def getIp():
    contName = flask.request.json["contName"]
    return admin.getIp(contName)

@app.get("/listContainerPorts")
def getPorts():
    contName = flask.request.json["contName"]
    return admin.getPorts(contName)

@app.get("/adminIp")
def getAdminIp():
    return admin.getAdminIp()

@app.get("/adminPorts")
def getAdminPorts():
    return admin.getAdminPorts()

@app.get("/bootstrapsIp")
def getBootstraps():
    return admin.getBootstraps()

@app.post("/createWorker")
def createWorker():
    res = admin.createWorker()
    if(res):
        return "ok", 200
    else:
        return "Not created", 500

@app.get("/contStats")
def getContStats():
    contName = flask.request.json["contName"]
    return admin.getContStats(contName)

@app.errorhandler(docker.errors.NotFound)
def return404(e):
    if("No such container" in str(e)):
        return 'Container not found', 404
    else:
        return 'Not found', 404

@app.errorhandler(docker.errors.NullResource)
def nullResourceHandler(e):
    return return404(e)

app.run()