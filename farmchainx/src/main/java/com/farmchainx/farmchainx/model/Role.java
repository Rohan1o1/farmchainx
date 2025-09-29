package com.farmchainx.farmchainx.model;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name="role")
public class Role {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(nullable = false)
	private String roleName;
	
 @ManyToMany(mappedBy = "roles")
 private Set<User> users;

 public long getId() {
	return id;
 }

 public void setId(long id) {
	this.id = id;
 }

 public String getRoleName() {
	return roleName;
 }

 public void setRoleName(String roleName) {
	this.roleName = roleName;
 }

 public Set<User> getUsers() {
	return users;
 }

 public void setUsers(Set<User> users) {
	this.users = users;
 }
	
 
	
	
	
}


