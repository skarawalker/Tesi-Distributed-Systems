package com.administrator.clustermanager.models.services;

import com.administrator.clustermanager.models.entities.Application;
import com.administrator.clustermanager.models.entities.Topic;
import com.administrator.clustermanager.models.repositories.TopicsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static com.administrator.clustermanager.ClustermanagerApplication.kafkaAdmin;

@Service
public class TopicService {
    private final TopicsRepository topicList;

    @Autowired
    public TopicService(@Qualifier("topicDataAccess") TopicsRepository topicList) {
        this.topicList = topicList;
    }

    /**
     * Methods for topic management
     */
    public Topic addTopic(String name){
        if(getTopic(name)!=null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Topic already present");
        if(kafkaAdmin.topicStatus(name))
            kafkaAdmin.deleteTopic(name);
        kafkaAdmin.createTopic(name, 1, (short) 1);
        Topic topic = Topic.builder()
                .name(name)
                .status("online")
                .apps(new ArrayList<>())
                .notes(new ArrayList<>())
                .build();
        topicList.addTopic(topic);
        return topic;
    }

    public void recoverTopic(Topic t){
        t.setNotes(new ArrayList<>());
        if(getTopic(t.getName())==null) {
            if (!kafkaAdmin.topicStatus(t.getName())) {
                t.setStatus("offline");
                t.addNote("Cannot recover topic");
            } else {
                t.setStatus("online");
            }
            Topic topic = Topic.builder()
                    .name(t.getName())
                    .status(t.getStatus())
                    .apps(new ArrayList<>())
                    .notes(t.getNotes())
                    .build();
            topicList.addTopic(topic);
            Map<Integer, Long> maxOffsets = new HashMap<>();
            if(topic.getStatus().equals("online")){
            long numPart = kafkaAdmin.getPartitionNumber(topic.getName());
                for (int i = 0; i<numPart; i++) {
                    try {
                        long o = kafkaAdmin.offsetStatus(t.getName(), i);
                        maxOffsets.put(i, o);
                    } catch (Exception e) {
                        break;
                    }
                }
            }
            if (t.getApps() != null) {
                for (Application app : t.getApps()) {
                    app.setNotes(new ArrayList<>());
                    int part = app.getPartion();
                    if (topic.getStatus().equals("online") && part != -1 && maxOffsets.get(part) != null) {
                        app.addNote("Previous offset: " + app.getOffset() + ", Current offset: " + maxOffsets.get(part));
                        if (maxOffsets.get(part) < app.getOffset()) {
                            app.addNote("Problems to recover messages in the topic");
                        }
                    } else if (topic.getStatus().equals("online") && part != -1 && maxOffsets.get(part) == null) {
                        app.addNote("No partition found");
                    } else if (topic.getStatus().equals("offline") && part != -1) {
                        app.addNote("Cannot recover previous saved partition");
                    }
                    addAppToTopic(t.getName(), app);
                }
            }
        }
    }

    public List<Topic> getAllTopics(){
        return topicList.getAllTopics();
    }

    public Topic getTopic(String name){
        return topicList.getTopic(name);
    }

    public Topic removeTopic(String name){
        return topicList.removeTopic(name);
    }

    public String getTopicStatus(String name){
        Topic t = getTopic(name);
        if(t==null){
            return null;
        } else {
            return t.getStatus();
        }
    }

    public List<String> getTopicNotes(String name){
        Topic t = getTopic(name);
        if(t==null){
            return null;
        } else {
            return t.getNotes();
        }
    }

    public String getMaxOffset(String name){
        if(getTopic(name)!=null) {
            if (!kafkaAdmin.topicStatus(name)) {
                return null;
            }
            long maxOffsets = 0;
            int part = 0;
            long numPart = 0;
            try {
                maxOffsets = kafkaAdmin.offsetStatus(name, 0);
                numPart = kafkaAdmin.getPartitionNumber(name);
            } catch (Exception e) {
                return null;
            }
            for (int i = 0; i<numPart; i++) {
                try {
                    long o = kafkaAdmin.offsetStatus(name, i);
                    if(maxOffsets<o) {
                        maxOffsets = o;
                        part = i;
                    }
                } catch (Exception e) {
                    break;
                }
            }
            return part+":"+maxOffsets;
        } else {
            return null;
        }
    }

    public boolean increaseTopicPartition(String name, int n){
        if(getTopic(name)==null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found");
        }
        if(getPartitionNumber(name)>n){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New partition number must be greater than the actual one");
        }
        if(getTopic(name).getStatus().equals("offline")){
            return false;
        }
        try {
            return kafkaAdmin.increasePartiotions(name, n);
        } catch (Exception e) {
            return false;
        }
    }
    public Long getPartitionNumber(String name){
        if(getTopic(name)==null || getTopic(name).getStatus().equals("offline")){
            return null;
        } else {
            return kafkaAdmin.getPartitionNumber(name);
        }
    }

    /**
     * Methods for application management
     */
    public List<Application> appsInTopic(String tName){
        Topic t = getTopic(tName);
        if(t==null){
            return null;
        } else {
            return t.getApps();
        }
    }

    public Application getApp(String tName, String aName){
        Topic t = getTopic(tName);
        if(t==null){
            return null;
        } else {
            return t.getApp(aName);
        }
    }

    public boolean addAppToTopic(String tName, Application a){
        if(getTopic(tName)==null || getApp(tName, a.getName())!=null){
            return false;
        } else {
            return topicList.getTopic(tName).push(a);
        }
    }

    public Application removeAppFromTopic(String tName, String aName){
        if(getTopic(tName)==null){
            return null;
        } else {
            return topicList.getTopic(tName).pop(aName);
        }
    }
    public Long getOffset(String tName, String aName){
        Application app = getApp(tName, aName);
        if(app==null){
            return null;
        } else {
            return  app.getOffset();
        }
    }

    public Application updateOffset(String tName, String aName, long offset){
        if(getTopic(tName)==null){
            return null;
        } else {
            Application app = getApp(tName, aName);
            if (app != null) {
                topicList.getTopic(tName).pop(aName);
                app.setOffset(offset);
                topicList.getTopic(tName).push(app);
            }
            return app;
        }
    }

    public Integer getPartition(String tName, String aName){
        Application app = getApp(tName, aName);
        if(app==null){
            return null;
        } else {
            return  app.getPartion();
        }
    }

    public Application updatePartition(String tName, String aName, int partition){
        if(getTopic(tName)==null){
            return null;
        } else {
            Application app = getApp(tName, aName);
            if (app != null) {
                topicList.getTopic(tName).pop(aName);
                app.setPartion(partition);
                topicList.getTopic(tName).push(app);
            }
            return app;
        }
    }

    public List<String> getAppNotes(String tName, String aName){
        Application app = getApp(tName, aName);
        if(app==null){
            return null;
        } else {
            return  app.getNotes();
        }
    }
}
