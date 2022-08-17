package com.administrator.clustermanager.models.repositories;

import com.administrator.clustermanager.models.entities.Topic;

import java.util.List;

public interface TopicsRepository {
    void addTopic(Topic topic);
    Topic getTopic(String name);
    List<Topic> getAllTopics();
    Topic removeTopic(String name);
}
