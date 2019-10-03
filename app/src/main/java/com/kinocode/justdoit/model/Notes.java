package com.kinocode.justdoit.model;

import java.io.Serializable;

public class Notes implements Serializable {

    private static final long serialVersionUID = 1L;
    String content;
    String title;
    String priority;
    String category;
    String deadline_date;
    String writing_date;


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDeadline_date() {
        return deadline_date;
    }

    public void setDeadline_date(String deadline_date) {
        this.deadline_date = deadline_date;
    }

    public String getWriting_date() {
        return writing_date;
    }

    public void setWriting_date(String writing_date) {
        this.writing_date = writing_date;
    }

}
