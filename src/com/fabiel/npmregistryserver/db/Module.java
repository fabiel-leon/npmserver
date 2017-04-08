/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fabiel.npmregistryserver.db;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 *
 * @author user
 */
@Entity
public class Module implements Serializable {

//    private static final long serialVersionUID = 1L;
    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;

    @ElementCollection
    private final List<String> keywords;
    private String description;
    private String readme;

    public Module(String id, List<String> keywords, String description) {
        this.id = id;
        this.keywords = keywords;
        this.description = description;
    }

    public Module(List<String> keywords) {
        this.keywords = keywords;
    }

    public Module() {
        this.keywords = new LinkedList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReadme() {
        return readme;
    }

    public void setReadme(String readme) {
        this.readme = readme;
    }

    public int size() {
        return keywords.size();
    }

    public boolean isEmpty() {
        return keywords.isEmpty();
    }

    public boolean contains(String o) {
        return keywords.contains(o);
    }

    public Object[] toArray() {
        return keywords.toArray();
    }

    public boolean add(String e) {
        return keywords.add(e);
    }

    public boolean containsAll(Collection<?> c) {
        return keywords.containsAll(c);
    }

    public boolean addAll(Collection<? extends String> c) {
        return keywords.addAll(c);
    }

    public String get(int index) {
        return keywords.get(index);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Module)) {
            return false;
        }
        Module other = (Module) object;
        return !((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id)));
    }

//    @Override
//    public String toString() {
//        return "com.fabiel.npmregistryserver.db.Module[ id=" + id + " ]";
//    }
    @Override
    public String toString() {
        return "Module{" + "id=" + id + ", keywords=" + keywords + ", description=" + description + '}';
    }

}
