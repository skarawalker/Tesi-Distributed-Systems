from MyDockerAdmin import MyDockerAdmin
import json

admin = MyDockerAdmin(netName="restexamples_spark_kafka", adminName=None)

out = admin.getContStats("restexamples-kafka-worker-1-1")

tot = out["cpu_stats"]["cpu_usage"]["total_usage"]
print(out)
print(sum([o for o in out["cpu_stats"]["cpu_usage"]["percpu_usage"]]))
print(tot)

