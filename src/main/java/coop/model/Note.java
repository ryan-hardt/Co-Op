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
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "note")
public class Note {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "note_id", length = 20)
	private Integer noteId;
	
	@ManyToOne
	private Task task;
	
	@OneToOne
	@JoinColumn(name = "user_id")	
	@LazyCollection(LazyCollectionOption.FALSE)
	private User user;
	
	@Column(name = "text")
	private String text;
	
	@Column(name = "date")
	private Date date;
	
	private String formattedDate;
	
	public Note() {
		this.date = new Date();
	}
	
	public Note(Task task, User user, String text) {
		this();
		this.task = task;
		this.user = user;
		this.text = text;
	}
	
	public Integer getNoteId() {
		return noteId;
	}
	public void setNoteId(Integer noteId) {
		this.noteId = noteId;
	}
	@JsonIgnore
	public Task getTask() {
		return task;
	}
	public void setTask(Task task) {
		this.task = task;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getFormattedDate() {
		  SimpleDateFormat sdf = new SimpleDateFormat("MMM d, y 'at' h:mm a");
		  this.formattedDate = sdf.format(this.date);
		  return this.formattedDate;
	  }
}
