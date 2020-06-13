package com.georgiev.library.entities;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "authors")
public class Author {
    private int id;
    private String name;

    public Author() {}

    public Author(String name) {
        this.name = name;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;

        if (obj == null || obj.getClass() != this.getClass())
            return false;

        Author author = (Author) obj;
        return (author.id == this.id && author.getName().equals(this.name));
    }

    @Override
    public int hashCode() {
        return this.id * 670 + this.name.length();
    }
}