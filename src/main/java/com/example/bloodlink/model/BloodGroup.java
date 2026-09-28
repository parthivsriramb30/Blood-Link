package com.example.bloodlink.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "blood_group")
public class BloodGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_name", nullable = false, unique = true, length = 10)
    private String groupName;

    public BloodGroup() {
    }

    public BloodGroup(String groupName) {
        this.groupName = groupName;
    }

    public BloodGroup(Long id, String groupName) {
        this.id = id;
        this.groupName = groupName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BloodGroup that = (BloodGroup) o;
        return Objects.equals(id, that.id) || Objects.equals(groupName, that.groupName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupName);
    }

    @Override
    public String toString() {
        return groupName;
    }
}
