	package coop.model;
	
	import java.text.SimpleDateFormat;
import java.util.ArrayList;
	import java.util.Date;
	import java.util.List;
	import org.hibernate.annotations.Cascade;
	import org.hibernate.annotations.CascadeType;
	import org.hibernate.annotations.LazyCollection;
	import org.hibernate.annotations.LazyCollectionOption;

	import javax.persistence.*;

	import com.fasterxml.jackson.annotation.JsonIgnore;
	
	/**
	 * This is a model class for Task objects.
	 */
	@Entity
	@Table(name = "task")
	public class Task {
	  //user types
	  public static final String OWNER = "Owner";
	  public static final String HELPER = "Helper";
	  public static final String REVIEWER = "Reviewer";
	  //tags
	  public static final String RESEARCH = "Research";
	  public static final String FEATURE = "Feature Implementation";
	  public static final String UNIT_TEST = "Unit Test";
	  public static final String BUG_FIX = "Bug Fix";
	  public static final String REFACTOR = "Refactor";
	  public static final String OTHER = "Other";
	  //statuses
	  public static final String NOT_STARTED = "Not Started";
	  public static final String IMPACT_ANALYSIS = "Impact Analysis";
	  public static final String IN_PROGRESS = "In Progress";
	  public static final String NEEDS_HELP = "Needs Help";
	  public static final String REVIEW = "Review";
	  //public static final String READY_FOR_COMMIT = "Ready for Commit";
	  public static final String COMPLETED = "Completed";
	  //These lists are repeated in board.js
	  public static final String[] TASK_TAGS = {RESEARCH, FEATURE, UNIT_TEST, BUG_FIX, REFACTOR, OTHER};
	  public static final String[] TASK_STATUSES = {NOT_STARTED, IMPACT_ANALYSIS, IN_PROGRESS, REVIEW, COMPLETED};
	  public static final String[] TASK_ROLES = {OWNER, HELPER, REVIEWER};

	  @Id
	  @GeneratedValue(strategy = GenerationType.AUTO)
	  @Column(name = "task_id", length = 20)
	  private Integer taskID;
	
	  @Column(name = "description")
	  private String description;
	
	  @Column(name = "priority")
	  private Integer priority;
	
	  @Column(name = "tag")
	  private String tag;
	
	  @Column(name = "status")
	  private String status;
	
	  @Column(name = "time_estimate")
	  private Double timeEstimate;
	  
	  @Column(name = "completion_date_est")
	  private Date completionDateEst;
	  
	  @Column(name = "repository_project_branch")
	  private String repositoryProjectBranch;
	  
	  @Transient
	  private String fmtCompletionDateEst;
	
	  @OneToMany(mappedBy = "task")
	  @Cascade(CascadeType.ALL)
	  @LazyCollection(LazyCollectionOption.FALSE)
	  private List<Note> notes;
	  
	  @ManyToOne
	  @Cascade(CascadeType.SAVE_UPDATE)
	  @LazyCollection(LazyCollectionOption.FALSE)
	  private Board board;
	
	  @OneToMany(mappedBy = "task")
	  @Cascade(CascadeType.ALL)
	  @LazyCollection(LazyCollectionOption.FALSE)
	  private List<TaskHistory> taskHistories;
	  
	  @OneToMany(mappedBy = "task")
	  @Cascade(CascadeType.ALL)
	  @LazyCollection(LazyCollectionOption.FALSE)
	  private List<Work> work;
	  
	  @ManyToMany
	  @Cascade(CascadeType.SAVE_UPDATE)
	  @JoinTable(
			  name = "task_owner", 
		  joinColumns = {@JoinColumn(name = "task_id")},
		  inverseJoinColumns = {@JoinColumn(name = "user_id")})
	  private List<User> owners;
	  
	  @ManyToMany
	  @Cascade(CascadeType.SAVE_UPDATE)
	  @JoinTable(
			  name = "task_helper", 
		  joinColumns = {@JoinColumn(name = "task_id")},
		  inverseJoinColumns = {@JoinColumn(name = "user_id")})
	  private List<User> helpers;
	  
	  @ManyToMany
	  @Cascade(CascadeType.SAVE_UPDATE)
	  @JoinTable(
			  name = "task_reviewer", 
		  joinColumns = {@JoinColumn(name = "task_id")},
		  inverseJoinColumns = {@JoinColumn(name = "user_id")})
	  private List<User> reviewers;

	  @ManyToMany
	  @Cascade(CascadeType.SAVE_UPDATE)
	  @JoinTable(
	  		name = "task_impacted_project_file",
			  joinColumns = {@JoinColumn(name = "task_id")},
			  inverseJoinColumns = {@JoinColumn(name = "impacted_project_file_id")})
	  private List<ImpactedProjectFile> impactedFiles;
	
	  public Task() {
		  this.description = "";
		  this.notes = new ArrayList<Note>();
		  this.work = new ArrayList<Work>();
		  this.owners = new ArrayList<User>();
		  this.helpers = new ArrayList<User>();
		  this.reviewers = new ArrayList<User>();
		  this.taskHistories = new ArrayList<TaskHistory>();
		  this.impactedFiles = new ArrayList<ImpactedProjectFile>();
		  this.timeEstimate = 0.0;
		  this.repositoryProjectBranch = "";
	  }

	  public Task(Task otherTask) {
		  this.description = otherTask.description;
		  this.notes = new ArrayList<>(otherTask.notes);
		  this.work = new ArrayList<>();			//work is wiped for copied tasks
		  this.owners = new ArrayList<>(otherTask.owners);
		  this.helpers = new ArrayList<>(otherTask.helpers);
		  this.reviewers = new ArrayList<>(otherTask.reviewers);
		  this.taskHistories = new ArrayList<>();	//history is wiped for copied tasks
		  this.impactedFiles = new ArrayList<>();	//impact analysis must be repeated for copied tasks
		  this.timeEstimate = 0.0;
		  this.repositoryProjectBranch = "";		//branch selection must be repeated for copied tasks
		  this.tag = otherTask.tag;
		  this.status = Task.NOT_STARTED;
	  }
	  
	  @Override
	  public String toString() {
	    return description;
	  }
	
	  // List mutators
	  public void addBoard(Board b) {
	    this.board = b;
	  }
	  
	  public void addWork(Work work) {
		  this.work.add(work);
	  }
	
	  public void addOwner(User newOwner) {
	    this.owners.add(newOwner);
	  }
	
	  public void removeOwner(User oldOwner) {
	    this.owners.remove(oldOwner);
	  }
	  
	  public void addHelper(User newHelper) {
	    this.helpers.add(newHelper);
	  }
	
	  public void removeHelper(User oldHelper) {
	    this.helpers.remove(oldHelper);
	  }
	  
	  public void addReviewer(User newReviewer) {
	    this.reviewers.add(newReviewer);
	  }
	
	  public void removeReviewer(User oldReviewer) {
	    this.reviewers.remove(oldReviewer);
	  }
	
	  // Getters and Setters
	  public Integer getTaskId() {
	    return taskID;
	  }
	
	  public void setTaskId(Integer task_id) {
	    this.taskID = task_id;
	  }
	
	  public String getDescription() {
	    return description;
	  }
	
	  public void setDescription(String description) {
	    this.description = description;
	  }
	
	  public Integer getPriority() {
	    return priority;
	  }
	
	  public void setPriority(Integer priority) {
	    this.priority = priority;
	  }
	
	  public String getTag() {
	    return tag;
	  }
	
	  public void setTag(String tag) {
	    this.tag = tag;
	  }
	
	  public String getStatus() {
	    return status;
	  }
	
	  public void setStatus(String status) {
	    this.status = status;
	  }
	
	  public Double getTimeEstimate() {
	    return timeEstimate;
	  }
	
	  public void setTimeEstimate(Double timeEstimate) {
	    this.timeEstimate = timeEstimate;
	  }
	
	  public String getRepositoryProjectBranch() {
		return repositoryProjectBranch;
	  }

	  public void setRepositoryProjectBranch(String gitLabProjectBranch) {
		this.repositoryProjectBranch = gitLabProjectBranch;
	  }

	  public List<Note> getNotes() {
	    return notes;
	  }
	  
	  public void addNote(Note note) {
		  this.notes.add(note);
	  }
	
	  public void setNotes(List<Note> notes) {
	    this.notes = notes;
	  }
	
	  @JsonIgnore
	  public Board getBoard() {
	    return board;
	  }
	  
	  public List<Work> getWork() {
		  return work;
	  }
	  
	  public void setWork(List<Work> work) {
		  this.work = work;
	  }
	  
	  public List<User> getOwners() {
	    return owners;
	  }
	
	  public void setOwners(List<User> owners) {
	    this.owners = owners;
	  }
	
	  public List<User> getHelpers() {
	    return helpers;
	  }
	
	  public void setHelpers(List<User> helpers) {
	    this.helpers = helpers;
	  }
	  
	  public List<User> getReviewers() {
	    return reviewers;
	  }
	
	  public void setReviewers(List<User> reviewers) {
	    this.reviewers = reviewers;
	  }

	  public List<ImpactedProjectFile> getImpactedFiles() {
	  	return impactedFiles;
	  }

	  public void setImpactedFiles(List<ImpactedProjectFile> impactedFiles) {
	  	this.impactedFiles = impactedFiles;
	  }

		public void clearUsers() {
		  this.owners.clear();
		  this.helpers.clear();
		  this.reviewers.clear();
	  }
	  
	  public void removeWork(Work w) {
		  this.work.remove(w);
	  }
	  
	  public void addUser(User user, String userType) {
		  if(Task.OWNER.equals(userType)) {
				this.addOwner(user);
			} else if(Task.HELPER.equals(userType)) {
				this.addHelper(user);
			} else if(Task.REVIEWER.equals(userType)) {
				this.addReviewer(user);
			}
	  }
	
	  public List<TaskHistory> getTaskHistories() {
		  return taskHistories;
	  }
	
	  public String printDescription(Task task) {
	    return task.getDescription();
	  }
	  
	  public Date getCompletionDateEst() {
			return completionDateEst;
	  }
		
	  public void setCompletionDateEst(Date completionDateEst) {
		  this.completionDateEst = completionDateEst;
	  }
		
	  public String getFmtCompletionDateEst() {
		  SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		  if(this.completionDateEst != null) {
			  this.fmtCompletionDateEst = sdf.format(this.completionDateEst);
		  } else {
			  this.fmtCompletionDateEst = "";
		  }
		  return this.fmtCompletionDateEst;
	  }
	  
	  public String getUserType(User u) {
		  if(this.owners.contains(u)) {
			  return Task.OWNER;
		  } else if(this.helpers.contains(u)) {
			  return Task.HELPER;
		  } else if(this.reviewers.contains(u)) {
			  return Task.REVIEWER;
		  }
		  return null;
	  }
	  
	  public boolean isCodingTask() {
		  return Task.FEATURE.equals(this.tag) || Task.BUG_FIX.equals(this.tag) || Task.UNIT_TEST.equals(this.tag) || Task.REFACTOR.equals(this.tag);
	  }
	  
	  @JsonIgnore
	  public Project getProject() {
		  Project p = null;
		  if(this.board != null) {
			  p = this.board.getProject();
		  }
		  return p;
	  }
	  
	@Override
	  public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((board == null) ? 0 : board.hashCode());
	    result = prime * result + ((description == null) ? 0 : description.hashCode());
	    result = prime * result + ((taskID == null) ? 0 : taskID.hashCode());
	    result = prime * result + ((notes == null) ? 0 : notes.hashCode());
	    result = prime * result + ((priority == null) ? 0 : priority.hashCode());
	    result = prime * result + ((status == null) ? 0 : status.hashCode());
	    result = prime * result + ((tag == null) ? 0 : tag.hashCode());
	    result = prime * result + ((timeEstimate == null) ? 0 : timeEstimate.hashCode());
	    result = prime * result + ((repositoryProjectBranch == null) ? 0 : repositoryProjectBranch.hashCode());
	    result = prime * result + ((owners == null) ? 0 : owners.hashCode());
	    result = prime * result + ((helpers == null) ? 0 : helpers.hashCode());
	    result = prime * result + ((reviewers == null) ? 0 : reviewers.hashCode());
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
	    Task other = (Task) obj;
	    if (description == null) {
	      if (other.description != null)
	        return false;
	    } else if (!description.equals(other.description))
	      return false;
	    if (taskID == null) {
	      if (other.taskID != null)
	        return false;
	    } else if (!taskID.equals(other.taskID))
	      return false;
	    if (notes == null) {
	      if (other.notes != null)
	        return false;
	    } else if (!notes.equals(other.notes))
	      return false;
	    if (priority == null) {
	      if (other.priority != null)
	        return false;
	    } else if (!priority.equals(other.priority))
	      return false;
	    if (status == null) {
	      if (other.status != null)
	        return false;
	    } else if (!status.equals(other.status))
	      return false;
	    if (tag == null) {
	      if (other.tag != null)
	        return false;
	    } else if (!tag.equals(other.tag))
	      return false;
	    if (timeEstimate == null) {
	      if (other.timeEstimate != null)
	        return false;
	    } else if (!timeEstimate.equals(other.timeEstimate))
	      return false;
	    if (repositoryProjectBranch == null) {
	      if (other.repositoryProjectBranch != null)
	        return false;
	    } else if (!repositoryProjectBranch.equals(other.repositoryProjectBranch))
	      return false;
		if (owners == null) {
	      if (other.owners != null)
	        return false;
	    } else if(other.getOwners() != null && owners.size() == other.getOwners().size()) {
	    		boolean areOwnersEqual = true;
	        for (int i = 0; i < owners.size(); i++) {
	        	areOwnersEqual = owners.get(i).equals(other.getOwners().get(i));
	        }
	        return areOwnersEqual;
	    } else {
	    		return false;
	    }
	    if (helpers == null) {
	        if (other.helpers != null)
	          return false;
	      } else if(other.helpers != null && helpers.size() == other.helpers.size()) {
	      		boolean areHelpersEqual = true;
	          for (int i = 0; i < helpers.size(); i++) {
	        	  areHelpersEqual = helpers.get(i).equals(other.helpers.get(i));
	          }
	          return areHelpersEqual;
	      } else {
	      		return false;
	      }
	      if (reviewers == null) {
	        if (other.reviewers != null)
	          return false;
	      } else if(other.reviewers != null && reviewers.size() == other.reviewers.size()) {
	      		boolean areReviewers = true;
	          for (int i = 0; i < reviewers.size(); i++) {
	        	  areReviewers = reviewers.get(i).equals(other.reviewers.get(i));
	          }
	          return areReviewers;
	      } else {
	      		return false;
	      }
	    return true;
	  }
	}