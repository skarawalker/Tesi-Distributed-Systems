import os
import numpy as np
from scipy.io import loadmat
from pyod.models.hbos import HBOS
from pysad.models.integrations import ReferenceWindowModel
from pysad.utils import ArrayStreamer
from pysad.transform.postprocessing import RunningAveragePostprocessor
from pysad.transform.preprocessing import InstanceUnitNormScaler
from pysad.transform.probability_calibration import GaussianTailProbabilityCalibrator
from tqdm.notebook import tqdm_notebook
import matplotlib.pyplot as plt
import pandas as pd
import csv
from matplotlib import pyplot as plt
from matplotlib.colors import ListedColormap, BoundaryNorm
from itertools import count
from matplotlib.animation import FuncAnimation
import pickle
import requests
import json

#graph variables
cpu_x = []
cpu_y = []
mem_x = []
mem_y = []
net_x = []
rx_y = []
tx_y = []
index = count()
interval = 500

fig, (ax1, ax2, ax3, ax4) = plt.subplots(nrows=1, ncols=4)
fig.suptitle("Docker stats cont1")

model = pickle.load(open("./models/HBOS/model.sav", "rb"))
preprocessor = pickle.load(open("./models/HBOS/preprocessor.sav", "rb"))
postprocessor = pickle.load(open("./models/HBOS/postprocessor.sav", "rb"))
calibrator = pickle.load(open("./models/HBOS/calibrator.sav", "rb"))
window_size = model.window_size

preds = []
preds_x = []

def usage_animation(i):
    plot_i = next(index)*interval/1000

    res = requests.get("http://127.0.0.1:5000/contStats", data="{\"contName\":\"restexamples-kafka-worker-1-1\"}", headers={"content-type":"application/json"})
    res = json.loads(res.text)

    #compute cpu delta
    usage = res["cpu_stats"]["cpu_usage"]["total_usage"]
    pre_usage = res["precpu_stats"]["cpu_usage"]["total_usage"]
    cpu_delta = abs(usage-pre_usage)
    n_cpus = len(res["cpu_stats"]["cpu_usage"]["percpu_usage"])

    #compute system delta
    sys_usage = res["cpu_stats"]["system_cpu_usage"]
    pre_sys_usage = res["precpu_stats"]["system_cpu_usage"]
    sys_delta = abs(sys_usage-pre_sys_usage)

    #compute usage percentual
    if(sys_delta!=0):
        cpu_perc=cpu_delta/sys_delta*100*n_cpus

    #append cpu stats
    cpu_x.append(plot_i)
    cpu_y.append(cpu_perc)

    #get memory stats
    mem_usage = res["memory_stats"]["usage"]/1000000

    #append memory stats
    mem_x.append(plot_i)
    mem_y.append(mem_usage)

    #get network stats
    net_rx = res["networks"]["eth0"]["rx_bytes"]/1000 
    net_tx = res["networks"]["eth0"]["tx_bytes"]/1000

    #append network stats
    net_x.append(plot_i)
    rx_y.append(net_rx)
    tx_y.append(net_tx)

    X = [cpu_perc, mem_usage, net_rx, net_tx]
    X = preprocessor.fit_transform_partial(X)
    anomaly_score = model.fit_score_partial(X)
    anomaly_score = postprocessor.fit_transform_partial(anomaly_score)
    calibrated_score = calibrator.fit_transform_partial(anomaly_score)
    preds.append(calibrated_score)
    preds_x.append(plot_i)

    #print CPU graph
    ax1.cla()
    ax1.set_ylabel("CPU usage (%%)")
    ax1.set_xlabel("Time")
    ax1.plot(cpu_x, cpu_y)

    #print memory graph
    ax2.cla()
    ax2.set_ylabel("Memory usage (MB)")
    ax2.set_xlabel("Time")
    ax2.plot(mem_x, mem_y)

    #print network graph
    ax3.cla()
    ax3.set_ylabel("Network I/O (KB)")
    ax3.set_xlabel("Time")
    ax3.plot(net_x, rx_y, label="input")
    ax3.plot(net_x, tx_y, label="output")
    ax3.legend(loc='upper left')

    #print memory graph
    ax4.cla()
    ax4.set_ylabel("Anomaly score")
    ax4.set_xlabel("Time")
    ax4.plot(preds_x, preds)

a1 = FuncAnimation(plt.gcf(), usage_animation, interval=interval)

plt.tight_layout()
plt.show()