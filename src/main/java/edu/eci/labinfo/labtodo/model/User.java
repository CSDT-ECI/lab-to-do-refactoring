package edu.eci.labinfo.labtodo.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.time.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    private String fullName;
    @Column(unique = true)
    private String userName;
    private String role;
    private String accountType;
    private String password;
    private LocalDateTime creationDate;
    private LocalDateTime updateDate;
    private LocalDateTime lastLoginDate;
    private boolean connect;

    @ManyToMany(mappedBy = "users")
    @ToString.Exclude
    List<Task> tasks;

    @OneToMany(mappedBy = "creatorUser")
    @ToString.Exclude
    List<Comment> comments = new ArrayList<>();

    public void addTask(Task taskToAdd) {
        tasks.add(taskToAdd);
    }

<<<<<<< HEAD
    public boolean getConnet() {
=======
    public Boolean getConnect() {
>>>>>>> d191e794201f6b0a1da0de6e239ecb4172c6f53d
        return this.connect;
    }

}
