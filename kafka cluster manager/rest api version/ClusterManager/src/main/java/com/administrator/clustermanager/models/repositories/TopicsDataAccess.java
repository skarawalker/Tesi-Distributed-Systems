package com.administrator.clustermanager.models.repositories;

import com.administrator.clustermanager.models.entities.Topic;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Repository("topicDataAccess")
public class TopicsDataAccess implements TopicsRepository{
    private List<Topic> topics = new ArrayList<>();

    @Override
    public void addTopic(Topic topic) {
        topics.add(topic);
    }

    @Override
    public Topic getTopic(String name) {
        return topics.stream().filter(t -> t.getName().equals(name)).findFirst().orElse(null);
    }

    @Override
    public List<Topic> getAllTopics() {
        return topics;
    }

    @Override
    public Topic removeTopic(String name) {
        Topic t = getTopic(name);
        if(t!=null){
            topics.remove(t);
        }
        return t;
    }
}
