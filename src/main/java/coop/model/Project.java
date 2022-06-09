package coop.model;

import java.util.ArrayList;
import java.util.List;

import coop.model.repository.RepositoryHost;
import coop.model.repository.RepositoryProject;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Project model object
 * 
 */
@Entity
@Table(name = "project")
public class Project {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "project_id", length = 20)
  private Integer id;

  @Column(name = "project_name", length = 30)
  private String name;

  @OneToOne
  @JoinColumn(name = "repository_project_id")
  private RepositoryProject repositoryProject;

  @OneToOne
  @JoinColumn(name = "board_id")
  @Cascade(CascadeType.ALL)
  private Board board;
  
  @OneToMany(mappedBy = "project")
  @Cascade(CascadeType.ALL)
  @LazyCollection(LazyCollectionOption.FALSE)
  private List<Cycle> cycles;
  
  @ManyToMany
  @Cascade(CascadeType.SAVE_UPDATE)
  @LazyCollection(LazyCollectionOption.FALSE)
  @JoinTable(
    name = "user_project",
	joinColumns = { @JoinColumn(name = "project_id") },
	inverseJoinColumns = { @JoinColumn(name = "user_id") }
  )
  private List<User> users;
  
  @ManyToMany
  @Cascade(CascadeType.REFRESH)
  @LazyCollection(LazyCollectionOption.FALSE)
  @JoinTable(
    name = "user_project_owner",
	joinColumns = { @JoinColumn(name = "project_id") },
	inverseJoinColumns = { @JoinColumn(name = "user_id") }
  )
  private List<User> owners;

  @OneToOne
  @Cascade(CascadeType.SAVE_UPDATE)
  @JoinColumn(name = "slack_workspace_id")
  private SlackWorkspace slackWorkspace;

  public Project() {
	  users = new ArrayList<User>();
	  owners = new ArrayList<User>();
	  cycles = new ArrayList<Cycle>();
	  board = new Board(this);
  }
  
  public void setId(Integer id) {
    this.id=id;
  }

/**
   * @return the id
   */
  public Integer getId() {
    return id;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  public RepositoryProject getRepositoryProject() {
      return repositoryProject;
  }

  public void setRepositoryProject(RepositoryProject repositoryProject) {
      this.repositoryProject = repositoryProject;
  }

    /**
   * @return the board
   */
  public Board getBoard() {
    return board;
  }

/**
   * @return the cycles
   */
  public List<Cycle> getCycles() {
    return cycles;
  }

  /**
   * @return the owners
   */
  public List<User> getOwners() {
    return owners;
  }
  
  public void setOwners(List<User> owners) {
	  this.owners = owners;
  }
  
  /**
   * @param owner you want to delete from project 
   */
  public boolean deleteOwner(User owner) {
      return owners.remove(owner);
  }
  
  /**
   * @param add owner to projects list 
   */
  
  public boolean addOwner(User owner) {
	  boolean returnSuccess = false;
	  if(!owners.contains(owner)){
		  owners.add(owner);
		  returnSuccess = true;
	  }
	  return returnSuccess;
  }
  
  /**
   * @return the users
   */
  public List<User> getUsers() {
    return users;
  }
  
  public void setUsers(List<User> users) {
	  this.users = users;
  }
  
  /**
   * @param user you want to delete from project 
   */
  public boolean deleteUser(User user) {
      return users.remove(user);
  }
  
  /**
   * @param add User to projects LIst 
   */
  
  public boolean addUser(User user) {
	  boolean returnSuccess = false;
	  if(!users.contains(user)){
		  users.add(user);
		  returnSuccess = true;
	  }
	  return returnSuccess;
  }
  
  /**
   * @param delete cycle from cycles list
   */
  public boolean deleteCycle(Cycle cycle) {
      return cycles.remove(cycle);
  }
  
  /**
   * @param add cycle to project if not already there
   */
  public boolean addCycle(Cycle cycle) {
    boolean returnSuccess = false;
    if(!cycles.contains(cycle)) {
    	cycles.add(cycle);
    	returnSuccess = true;
    }
      return returnSuccess;
  }
  
  public List<Board> getCycleBoards() {
	  List<Board> boards = new ArrayList<Board>();
	  for(Cycle s : cycles) {
		  boards.add(s.getBoard());
	  }
	  return boards;
  }
  
  public List<Task> getTasksForUser(User u) {
	  List<Task> userTasks = new ArrayList<Task>();
	  for(Cycle s: this.cycles) {
		  for(Task t: s.getBoard().getTasks()) {
			  if(t.getUserType(u) != null) {
				  userTasks.add(t);
			  }
		  }
	  }
	  return userTasks;
  }

  public RepositoryHost getRepository() {
      if(this.repositoryProject != null) {
          return this.repositoryProject.getRepositoryHost();
      }
      return null;
  }

  public SlackWorkspace getSlackWorkspace() {
      return slackWorkspace;
  }

  public void setSlackWorkspace(SlackWorkspace slackWorkspace) {
      this.slackWorkspace = slackWorkspace;
  }

  //used to get the Co-Op user that corresponds to a git user
  public User getProjectUserWithName(String name) {
      for(User projectUser: this.users) {
          if(projectUser.matchesName(name)) {
              return projectUser;
          }
      }
      return null;
  }

    @Override
  public String toString() {
	  return name;
  }

  @Override
  public int hashCode() {
	  final int prime = 31;
	  int result = 1;
	  result = prime * result + ((board == null) ? 0 : board.hashCode());
	  result = prime * result + ((id == null) ? 0 : id.hashCode());
	  result = prime * result + ((name == null) ? 0 : name.hashCode());
	  result = prime * result + ((cycles == null) ? 0 : cycles.hashCode());
	  result = prime * result + ((users == null) ? 0 : users.hashCode());
	  result = prime * result + ((owners == null) ? 0 : owners.hashCode());
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
	  Project other = (Project) obj;
	  if (board == null) {
		  if (other.board != null)
			  return false;
	  } else if (!board.equals(other.board))
		  return false;
	  if (id == null) {
		  if (other.id != null)
			  return false;
	  } else if (!id.equals(other.id))
		  return false;
	  if (name == null) {
		  if (other.name != null)
			  return false;
	  } else if (!name.equals(other.name))
		  return false;
	  if(users == null) {
		  if(other.users != null)
			  return false;
	  } else if (!compareUsers(this.users, other.users))
		  return false;
	  if(owners == null) {
		  if(other.owners != null)
			  return false;
	  } else if (!compareUsers(this.owners, other.owners))
		  return false;
	  if (cycles == null) {
		  if (other.cycles != null)
			  return false;
	  } else if (!compareCycles(this.cycles, other.cycles))
		  return false;
	  
	  return true;
  }
  
  private boolean compareCycles(List<Cycle> s1, List<Cycle> s2) {
      ArrayList<Cycle> cycles = new ArrayList<>(s1);
      for (Object s : s2) {
          if (!cycles.remove(s)) {
              return false;
          }
      }
      return cycles.isEmpty();
  }
  private boolean compareUsers(List<User> u1, List<User> u2) {
      for(User u : u1) {
    	  boolean found = false;
    	  for(User uu : u2) {
    		  if(u.getId().equals(uu.getId())) {
    			  found = true;
    			  break;
    		  }
    	  }
    	  if(!found) {
    		  return false;
    	  }
      }
      return true;
  }
}