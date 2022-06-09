package coop.model;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@Entity
@Table(name = "task_history")
public class TaskHistory {
@Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "task_history_id")
  private Integer id;

  @Column(name = "date")
  private Date date;
  
  private String formattedDate;
  
  @ManyToOne
  @JoinColumn(name = "user_id")
  @LazyCollection(LazyCollectionOption.FALSE)
  private User changedByUser;
  
  @OneToMany
  @Fetch(value = FetchMode.SUBSELECT)
  @Cascade(CascadeType.ALL)
  @LazyCollection(LazyCollectionOption.FALSE)
  private List<TaskChange> changedValues = new ArrayList<TaskChange>();
  
  @ManyToOne
  @JoinColumn(name = "task_id")
  private Task task;

  public TaskHistory() {
    super();
    this.date = new Date();
    this.changedValues = new ArrayList<TaskChange>();
  }

  /**
   * @return the id
   */
  public Integer getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(Integer id) {
    this.id = id;
  }

  /**
   * @return the date
   */
  public Date getDate() {
    return date;
  }

  /**
   * @param date the date to set
   */
  public void setDate(Date date) {
    this.date = date;
  }
  
  public String getFormattedDate() {
	  SimpleDateFormat sdf = new SimpleDateFormat("MMM d, y 'at' h:mm a");
	  this.formattedDate = sdf.format(this.date);
	  return this.formattedDate;
  }

  /**
   * @return the changedField
   */
  public List<TaskChange> getChangedValueList() {
    return changedValues;
  }

  /**
   * @return the task
   */
  @JsonIgnore
  public Task getTask() {
    return task;
  }

  /**
   * @param task the task to set
   */
  public void setTask(Task task) {
    this.task = task;
  }
  
  public User getChangedByUser() {
		return changedByUser;
	}

	public void setChangedByUser(User changedByUser) {
		this.changedByUser = changedByUser;
	}

	public boolean isStatusChange() {
      for(TaskChange tc : changedValues) {
        if(tc.getChangedField().equals("Status")) {
          return true;
        }
      }
      return false;
    }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((changedValues == null) ? 0 : changedValues.hashCode());
    result = prime * result + ((date == null) ? 0 : date.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((task == null) ? 0 : task.hashCode());
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
    TaskHistory other = (TaskHistory) obj;
    if (changedValues == null) {
      if (other.changedValues != null)
        return false;
    } else if (!changedValues.equals(other.changedValues))
      return false;
    if (date == null) {
      if (other.date != null)
        return false;
    } else if (!date.equals(other.date))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (task == null) {
      if (other.task != null)
        return false;
    } else if (!task.equals(other.task))
      return false;
    return true;
  }

@Override
public String toString() {
	StringBuilder str = new StringBuilder();
	str.append(date + "= ");
	for (int i=0; i<changedValues.size(); i++) {
		TaskChange cv = changedValues.get(i);
		str.append(cv.getChangedField() + ": " + cv.getOldValue() + " -> " + cv.getNewValue());
		if(i < changedValues.size() - 1) {
			str.append(", ");
		}
	}
	return str.toString();
}

}
