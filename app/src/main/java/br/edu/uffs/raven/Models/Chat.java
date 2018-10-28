package br.edu.uffs.raven.Models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Chat {
    private String id;
    private List<String> usersName;
    private List<String> usersIds;
    private String groupId;
    @ServerTimestamp
    private Date createdAt;
    private List<String> typersList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getUsersName() {
        return usersName;
    }

    public void setUsersName(List<String> usersName) {
        this.usersName = usersName;
    }

    public List<String> getUsersIds() {
        return usersIds;
    }

    public void setUsersIds(List<String> usersIds) {
        this.usersIds = usersIds;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public List<String> getTypersList() {
        return typersList;
    }

    public void setTypersList(List<String> typersList) {
        this.typersList = typersList;
    }
}
