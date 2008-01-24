package com.apress.progwt.client.domain.dto;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.apress.progwt.client.domain.ForumPost;
import com.apress.progwt.client.domain.School;
import com.apress.progwt.client.domain.User;

public class SearchResult implements Serializable {

    private List<User> users = new LinkedList<User>();
    private List<ForumPost> forumPosts = new LinkedList<ForumPost>();
    private List<School> schools = new LinkedList<School>();

    public SearchResult() {
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<ForumPost> getForumPosts() {
        return forumPosts;
    }

    public void setForumPosts(List<ForumPost> forumPosts) {
        this.forumPosts = forumPosts;
    }

    public List<School> getSchools() {
        return schools;
    }

    public void setSchools(List<School> schools) {
        this.schools = schools;
    }

}
