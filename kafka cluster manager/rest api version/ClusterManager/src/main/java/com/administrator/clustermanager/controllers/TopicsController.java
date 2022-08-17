package com.administrator.clustermanager.controllers;

import com.administrator.clustermanager.models.entities.Topic;
import com.administrator.clustermanager.models.services.TopicService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("topic")
public class TopicsController {
    private final TopicService topicService;

    @Autowired
    public TopicsController(TopicService topicService) {
        this.topicService = topicService;
    }

    @PostMapping("/addTopic")
    public Topic addTopic(@RequestBody String name){
        Topic topic = topicService.addTopic(name);
        if(topic==null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad request");
        }
        return topic;
    }

    @PostMapping("/recoverTopic")
    public void recoverTopic(@RequestBody Topic t){
        topicService.recoverTopic(t);
    }

    @GetMapping("/")
    public List<Topic> getAllTopics(){
        return  topicService.getAllTopics();
    }

    @DeleteMapping("deleteTopic")
    public Topic removeTopic(@RequestBody String name){
        Topic t = topicService.removeTopic(name);
        if(t==null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found");
        } else {
            return t;
        }
    }

    @GetMapping("/getTopicStatus")
    public String getTopicStatus(@RequestBody String name){
        String res = topicService.getTopicStatus(name);
        if(res==null)
            res = "Not exists";
        return res;
    }

    @GetMapping("/getNotes")
    public List<String> getTopicNotes(@RequestBody String name){
        List<String> notes = topicService.getTopicNotes(name);
        if(notes==null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found");
        } else {
            return notes;
        }
    }

    @PostMapping("/increasePartitionNumber")
    public void increasePartitionNumber(@RequestBody ObjectNode json){
        String name = json.get("name").asText();
        Integer nPart = json.get("nPart").asInt();
        if(name==null || nPart==null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required input in request body");
        }
        boolean result = topicService.increaseTopicPartition(name, nPart);
        if(!result){
            throw new ResponseStatusException(HttpStatus.NOT_MODIFIED, "Cannot increase the number of partitions");
        }
    }

    @GetMapping("/getPartitionNumber")
    public long getPartitionNumber(@RequestBody String name){
        Long p = topicService.getPartitionNumber(name);
        if(p==null || p==-1){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found");
        } else {
            return p;
        }
    }
}
