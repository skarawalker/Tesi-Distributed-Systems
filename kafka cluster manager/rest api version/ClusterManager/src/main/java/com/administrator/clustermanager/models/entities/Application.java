package com.administrator.clustermanager.models.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Application {
    private String name;
    private long offset;
    private int partion;
    private List<String> notes;

    public void addNote(String note){
        notes.add(note);
    }
}
