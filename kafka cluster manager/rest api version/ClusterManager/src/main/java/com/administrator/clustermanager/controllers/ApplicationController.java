package com.administrator.clustermanager.controllers;

import com.administrator.clustermanager.models.entities.Application;
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
@RequestMapping("app")
public class ApplicationController {
    private final TopicService topicService;

    @Autowired
    public ApplicationController(TopicService topicService) {
        this.topicService = topicService;
    }

    @PostMapping("/addApp")
    public Application addApplication(@RequestBody ObjectNode json){
        String tName = json.get("topicName").asText();
        String aName = json.get("appName").asText();
        if(aName==null || tName==null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required input in request body");
        }
        Topic t = topicService.getTopic(tName);
        if(t == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic Not Found");
        }
        long offset = 0;
        int partition = -1;
        if(json.get("offset")!=null){
            offset = json.get("offset").asLong();
        } else if(json.get("offset").equals("start")){
            offset = 0;
        } else if(json.get("offset").equals("end")){
            String partOffset = topicService.getMaxOffset(tName);
            if(partOffset!=null) {
                partition = Integer.parseInt(partOffset.split(":")[0]);
                offset = Long.parseLong(partOffset.split(":")[1]);
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot recover max offset and relative partition of this topic");
            }
        }
        if(json.get("partition")!=null && partition==-1){
            partition = json.get("partition").asInt();
        }
        Application app = Application.builder()
                .name(aName)
                .offset(offset)
                .partion(partition)
                .notes(new ArrayList<>())
                .build();
        if(!topicService.addAppToTopic(tName, app)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Application already added to the topic");
        }
        return app;
    }

    @GetMapping("/getApp")
    public List<Application> getAllApps(@RequestBody String name){
        List<Application> res = topicService.appsInTopic(name);
        if(res==null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not Found");
        } else {
            return res;
        }
    }

    @DeleteMapping("/deleteApp")
    public Application deleteApp(@RequestBody ObjectNode json){
        String aName = json.get("appName").asText();
        String tName = json.get("topicName").asText();
        if(aName==null || tName==null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required input in request body");
        }
        Application app = topicService.removeAppFromTopic(tName, aName);
        if(app==null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Application Not Found");
        } else {
            return app;
        }
    }

    @GetMapping("/getOffset")
    public long getOffset(@RequestBody ObjectNode json) {
        String aName = json.get("appName").asText();
        String tName = json.get("topicName").asText();
        if(aName==null || tName==null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required input in request body");
        }
        Long offset = topicService.getOffset(tName, aName);
        if(offset==null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Application Not Found");
        } else {
            return offset;
        }
    }

    @PostMapping("/updateOffset")
    public Application updateOffset(@RequestBody ObjectNode json){
        String aName = json.get("appName").asText();
        String tName = json.get("topicName").asText();
        Long offset = json.get("offset").asLong();
        if(aName==null || tName==null || offset==null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required input in request body");
        }
        Application app = topicService.updateOffset(tName, aName, offset);
        if(app==null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Application Not Found");
        } else {
            return app;
        }
    }

    @GetMapping("/getPartition")
    public int getPartition(@RequestBody ObjectNode json){
        String aName = json.get("appName").asText();
        String tName = json.get("topicName").asText();
        if(aName==null || tName==null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required input in request body");
        }
        Integer partition = topicService.getPartition(tName, aName);
        if(partition==null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Application Not Found");
        } else {
            return partition;
        }
    }

    @PostMapping("/updatePartition")
    public Application updatePartition(@RequestBody ObjectNode json){
        String aName = json.get("appName").asText();
        String tName = json.get("topicName").asText();
        Integer partition = json.get("partition").asInt();
        if(aName==null || tName==null || partition==null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required input in request body");
        }
        Application app = topicService.updatePartition(tName, aName, partition);
        if(app==null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Application Not Found");
        } else {
            return app;
        }
    }

    @GetMapping("/getNotes")
    public List<String> getAppNotes(@RequestBody ObjectNode json){
        String aName = json.get("appName").asText();
        String tName = json.get("topicName").asText();
        if(aName==null || tName==null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required input in request body");
        }
        List<String> notes = topicService.getAppNotes(tName, aName);
        if(notes==null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Application Not Found");
        } else {
            return notes;
        }
    }
}
