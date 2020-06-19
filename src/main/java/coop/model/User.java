package coop.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import coop.model.repository.RepositoryHost;
import org.hibernate.annotations.*;

//import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnore;

import coop.dao.ProjectDao;
import coop.dao.UserDao;

@Entity
@Table(name = "coop_user")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "user_id")
  private Integer id;

  @Column(name = "username", nullable = false)
  private String username;
  
  @Column(name = "firstName")
  private String firstName;
  
  @Column(name = "lastName")
  private String lastName;

  @Column(name = "hash", nullable = false)
  private String hash;

  @Column(name = "salt")
  private String salt;

  @Column(name = "isActive")
  private int isActive;

  @ManyToMany(mappedBy = "users")
  @Cascade(CascadeType.SAVE_UPDATE)
  private List<Project> projects;
  
  @ManyToMany(mappedBy = "users")
  @Cascade(CascadeType.SAVE_UPDATE)
  private List<Project> ownedProjects;

  @ManyToMany( mappedBy = "owners")
  private List<Task> ownedTasks;
  
  @ManyToMany( mappedBy = "helpers")
  private List<Task> helpedTasks;
  
  @ManyToMany( mappedBy = "reviewers")
  private List<Task> reviewedTasks;
  
  @OneToMany(mappedBy = "user")
  private List<Work> work;

  @OneToMany
  @Cascade(CascadeType.DELETE)
  private List<RepositoryHost> repositoryHosts;

  public User() {
	  this.projects = new ArrayList<Project>();
	  this.ownedProjects = new ArrayList<Project>();
	  this.ownedTasks = new ArrayList<Task>();
	  this.helpedTasks = new ArrayList<Task>();
	  this.reviewedTasks = new ArrayList<Task>();
	  this.repositoryHosts = new ArrayList<RepositoryHost>();
  }

  public Integer getId() {
    return this.id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getUsername() {
    return this.username;
  }

  public void setUsername(String username) {
    this.username = username;
  }
  
  public String getFirstName() {
    return this.firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }
  
  public String getLastName() {
    return this.lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  @JsonIgnore
  public String getHash() {
    return this.hash;
  }

  @JsonIgnore
  public void setHash(String hash) {
    this.hash = hash;
  }

  @JsonIgnore
  public String getSalt() {
    return this.salt;
  }

  @JsonIgnore
  public void setSalt(String salt) {
    this.salt = salt;
  }

  public void setIsActive(int active) {
	this.isActive = active;
  }

  public int getIsActive() {
	 return this.isActive;
  }

  @JsonIgnore
  public List<Project> getProjects() {
    return this.projects;
  }
  
  @JsonIgnore
  public List<Project> getOwnedProjects() {
    return this.ownedProjects;
  }

  @JsonIgnore
  public List<Task> getOwnedTasks() {
    return this.ownedTasks;
  }
  
  @JsonIgnore
  public List<Task> getHelpedTasks() {
    return this.helpedTasks;
  }
  
  @JsonIgnore
  public List<Task> getReviewedTasks() {
    return this.reviewedTasks;
  }

  public List<RepositoryHost> getRepositoryHosts() {
      return repositoryHosts;
  }

  public void setRepositoryHosts(List<RepositoryHost> repositoryHosts) {
      this.repositoryHosts = repositoryHosts;
  }

  public void addRepositoryHost(RepositoryHost repositoryHost) {
      this.repositoryHosts.add(repositoryHost);
  }

  @JsonIgnore
  public List<Task> getAllTasks() {
	  List<Task> allTasks = new ArrayList<Task>();
	  allTasks.addAll(this.ownedTasks);
	  allTasks.addAll(this.helpedTasks);
	  allTasks.addAll(this.reviewedTasks);
	  return allTasks;
  }

  public String toString() {
    return this.firstName + " " + this.lastName;
  }

  public void deactivate() {
	  this.isActive = 0;
	  ProjectDao projectDao = new ProjectDao();
	  for (Project p : projects) {
		  p.deleteUser(this);
		  p.deleteOwner(this);
		  projectDao.updateProject(p);
	  }
	  UserDao u = new UserDao();
	  u.updateUser(this);
  }

  public void activate() {
	  this.isActive = 1;
	  ProjectDao projectDao = new ProjectDao();
	  for (Project p : projects) {
		  p.addUser(this);
		  projectDao.updateProject(p);
	  }
	  UserDao u = new UserDao();
	  u.updateUser(this);
  }

  @JsonIgnore
  public List<Task> getCommonTasks(User u){
	  List<Task> result = new ArrayList<Task>();
	  for(Project p : getCommonProjects(u)) {
		  result.addAll(p.getTasksForUser(u));
	  }	  
	  return result;
  }
  
  @JsonIgnore
  public List<Project> getCommonProjects(User u){
	  List<Project> commonProjects = new ArrayList<Project>();
	  List<Project> uProjects = u.getProjects();
	  for (Project p : this.getProjects()) {
		  if(uProjects.contains(p)) {
			  commonProjects.add(p);
		  }
	  }
	  return commonProjects;
  }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hash == null) ? 0 : hash.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
		result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (hash == null) {
			if (other.hash != null)
				return false;
		} else if (!hash.equals(other.hash))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		if (firstName == null) {
			if (other.firstName != null)
				return false;
		} else if (!firstName.equals(other.firstName))
			return false;
		if (lastName == null) {
			if (other.lastName != null)
				return false;
		} else if (!lastName.equals(other.lastName))
			return false;
		return true;
	}
	
	public static class UserStatsSorter implements Comparator<User> { 
	    public int compare(User a, User b) { 
	    	return a.getId().compareTo(b.getId());
	    } 
	} 
}