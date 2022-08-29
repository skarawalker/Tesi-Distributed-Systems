import csv

class ContainerData:

    def __init__(self, contName):
        self.contName = contName
        self.cpu_stats = []
        self.mem_stats = []
        self.net_in_stats = []
        self.net_out_stats = []
        self.time = []

    def import_csv(self, path):
        f = open(path, 'r', newline='')
        reader = csv.reader(f, delimiter=',')
        row = reader.__next__()
        for i in range(0, len(row)):
            if(row[i]=='time'):
                time_index = i
            elif(row[i]=='CPU_usage'):
                cpu_index = i
            elif(row[i]=='MEM_usage'):
                mem_index = i
            elif(row[i]=='Net_rx'):
                net_in_index = i
            elif(row[i]=='Net_tx'):
                net_out_index = i
            else:
                print("Header not recognized")
        if time_index==-1 or cpu_index==-1 or mem_index==-1 or net_in_index==-1 or net_out_index==-1:
            print("Missing headers")
            return
        for row in reader:
            self.time.append(float(row[time_index]))
            self.cpu_stats.append(float(row[cpu_index]))
            self.mem_stats.append(float(row[mem_index]))
            self.net_in_stats.append(float(row[net_in_index]))
            self.net_out_stats.append(float(row[net_out_index]))
        