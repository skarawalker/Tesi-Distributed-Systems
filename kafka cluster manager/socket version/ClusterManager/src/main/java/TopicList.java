import java.util.*;

public class TopicList {
    private Map<String, Map<String, Long>> list;

    public TopicList(){
        this.list = new HashMap<>();
    }

    public void addTopic(String topicName) throws Exception {
        if(list.get(topicName)!=null){
            throw new Exception("Topic already present");
        } else {
            list.put(topicName, new HashMap<>());
        }
    }
    public void add(String topicName, Map<String, Long> appList) throws Exception {
        if(list.get(topicName)!=null){
            throw new Exception("Topic already present");
        } else {
            list.put(topicName, appList);
        }
    }

    public boolean checkTopic(String topicName){
        return list.get(topicName)!=null;
    }

    public void addApplication(String topicName, String appName) throws Exception {
        if(list.get(topicName)!=null){
            list.get(topicName).put(appName, -1L);
        } else {
            throw new Exception("Topic not present");
        }
    }

    public void updateOffset(String topicName, String appName, Long newOffset) throws Exception {
        if(list.get(topicName)!=null && list.get(topicName).get(appName)!=null){
            list.get(topicName).put(appName, newOffset);
        } else {
            throw new Exception("Cannot update offset");
        }
    }

    public Long getOffset(String topicName, String appName) throws Exception {
        if(list.get(topicName)!=null && list.get(topicName).get(appName)!=null){
            return list.get(topicName).get(appName);
        } else {
            throw new Exception("Cannot get offset");
        }
    }
    public Long getNextOffset(String topicName, String appName) throws Exception {
        if(list.get(topicName)!=null && list.get(topicName).get(appName)!=null){
            return list.get(topicName).get(appName)+1;
        } else {
            throw new Exception("Cannot get offset");
        }
    }

    public List<String> appList(){
        List<String> apps = new ArrayList<>();
        for (String topic: list.keySet()) {
            for(String app: list.get(topic).keySet()){
                apps.add(app);
            }
        }
        return apps;
    }

    public List<String> appList(String topicName){
        List<String> apps = new ArrayList<>();
        for(String app: list.get(topicName).keySet()){
            apps.add(app);
        }
        return apps;
    }
    public String toString(){
        String json = "{\n";
        for (String topicName: list.keySet()) {
            json = json+"  \""+topicName+"\": {\n";
            for(String appName: list.get(topicName).keySet()){
                json = json+"    \""+appName+"\": "+list.get(topicName).get(appName)+"\n";
            }
            json = json+"  }\n";
        }
        json = json+"}";
        return json;
    }

}
