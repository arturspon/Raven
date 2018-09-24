package br.edu.uffs.raven.Models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Date;

public class Chat {
    private String id;
    private ArrayList<String> usersName;
    private ArrayList<String> usersIds;
    @ServerTimestamp
    private Date createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<String> getUsersName() {
        return usersName;
    }

    public void setUsersName(ArrayList<String> usersName) {
        this.usersName = usersName;
    }

    public ArrayList<String> getUsersIds() {
        return usersIds;
    }

    public void setUsersIds(ArrayList<String> usersIds) {
        this.usersIds = usersIds;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
