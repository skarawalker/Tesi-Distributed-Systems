import java.io.IOException;
import java.util.List;

public class ApplicationManager {
    private final String appName;
    private final String topicName;
    private AdminConnection connection;
    public ApplicationManager(String appName, String topicName, String adminAddress){
        this.appName = appName;
        this.topicName = topicName;
        try {
            this.connection = new AdminConnection();
        } catch (Exception e) {
            close(e.getMessage());
        }
    }

    public void connect(String startingOffset){
        try {
            if (connection.getTopicStatus(topicName).equals("Not exists")) {
                String res = connection.createTopic(topicName);
                if(res.equals("Bad Request") || res.equals("Not Found")){
                    close("Error during creation of the topic "+topicName);
                }
            }
            if (connection.getTopicStatus(topicName).equals("online")) {
                String res = connection.addAppToTopic(appName, topicName, startingOffset);
                if(res.equals("Not Found")){
                    close("Topic "+topicName+" not found");
                } else if(res.equals("Bad Request")){
                    String appNotes = connection.getAppNotes(appName, topicName);
                    if(appNotes.contains("No partition found")){
                        connection.updatePartition(appName, topicName, -1);
                    } else if(appNotes.contains("Problems to recover messages in the topic")){
                        connection.updateOffset(appName, topicName, 0);
                    }
                }
            } else {
                close("Topic offline");
            }
        } catch (Exception e) {
            close(e.getMessage());
        }
    }

    public long getOffset(){
        Long offset = null;
        try {
            String res = connection.getOffset(appName, topicName);
            if(res.equals("Not Found"))
                close("Application or topic not found");
            else
                offset = Long.parseLong(res);
        } catch (Exception e) {
            close(e.getMessage());
        }
        return offset;
    }

    public void updateOffset(long newOffset){
        try{
            connection.updateOffset(appName, topicName, newOffset);
        } catch (Exception e){
            close(e.getMessage());
        }
    }

    public int getPartition(){
        Integer partition = null;
        try {
            String res = connection.getPartition(appName, topicName);
            if(res.equals("Not Found"))
                close("Application or topic not found");
            else
                partition = Integer.parseInt(res);
        } catch (Exception e) {
            close(e.getMessage());
        }
        return partition;
    }

    public void updatePartition(int newPartition){
        try{
            connection.updatePartition(appName, topicName, newPartition);
        } catch (Exception e){
            close(e.getMessage());
        }
    }

    private void close(String error){
        System.err.println("Error: "+error);
        System.exit(1);
    }
}
