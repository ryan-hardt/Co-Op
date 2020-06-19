package coop.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "coop_work")
public class Work {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "work_id", length = 20)
	private Integer workId;
	
	@ManyToOne
	@JoinColumn(name = "task_id")
	private Task task;
	
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;
	
	@Column(name = "description")
	private String description;
	
	@Column(name = "num_minutes", length = 10)
	private Integer numMinutes;
	
	@Column(name = "date", length = 10)
	private Date date;
	
	private String formattedDate;
	
	public Work() {
		this.numMinutes = 0;
		this.date = new Date();
	}
	
	public Work(Task task, User user, String description, int numMinutes, Date date) {
		this.task = task;
		this.user = user;
		this.description = description;
		this.numMinutes = numMinutes;
		this.date = date;
	}

	public Integer getWorkId() {
		return workId;
	}
	public void setWorkId(Integer workId) {
		this.workId = workId;
	}
	@JsonIgnore
	public Task getTask() {
		return task;
	}
	public void setTask(Task task) {
		this.task = task;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Integer getNumMinutes() {
		return numMinutes;
	}
	public void setNumMinutes(Integer numMinutes) {
		this.numMinutes = numMinutes;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getFormattedDate() {
	  SimpleDateFormat sdf = new SimpleDateFormat("M/d/y");
	  this.formattedDate = sdf.format(this.date);
	  return this.formattedDate;
  }
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((numMinutes == null) ? 0 : numMinutes.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((task == null) ? 0 : task.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		result = prime * result + ((workId == null) ? 0 : workId.hashCode());
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
		Work other = (Work) obj;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (numMinutes == null) {
			if (other.numMinutes != null)
				return false;
		} else if (!numMinutes.equals(other.numMinutes))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (task == null) {
			if (other.task != null)
				return false;
		} else if (!task.equals(other.task))
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		if (workId == null) {
			if (other.workId != null)
				return false;
		} else if (!workId.equals(other.workId))
			return false;
		return true;
	}
}
