package com.administrator.clustermanager.models.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Topic {
    private String name;
    private String status;
    private List<Application> apps;
    private List<String> notes;

    public boolean push(Application a){
        if(apps.contains(a)){
            return false;
        } else {
            apps.add(a);
            return true;
        }
    }

    public Application getApp(String name){
        return apps.stream().filter(app->app.getName().equals(name)).findFirst().orElse(null);
    }

    public Application pop(String name){
        Application a = getApp(name);
        if(a!=null){
            apps.remove(a);
        }
        return a;
    }

    public void addNote(String note){
        notes.add(note);
    }
}
