package coop.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "board")
public class Board {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "board_id", length = 20)
	private Integer boardId;

	@OneToOne
	@JoinColumn(name = "project_id")
	private Project project;

	@OneToOne
	@JoinColumn(name = "cycle_id")
	private Cycle cycle;

	@ManyToMany
	@Cascade(CascadeType.DELETE)
	@LazyCollection(LazyCollectionOption.FALSE)
	@JoinTable(
			name = "task_board",
			joinColumns = { @JoinColumn(name = "board_id") },
			inverseJoinColumns = { @JoinColumn(name = "task_id") })
	private List<Task> tasks;

	public Board() {
		tasks = new ArrayList<Task>();
	}
	
	public Board(Project project) {
		this();
		this.project = project;
	}

	public String toString() {
		return "Board (" + boardId + ")";
	}

	public Integer getBoardId() {
		return boardId;
	}

	public void setBoardId(Integer boardId) {
		this.boardId = boardId;
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	public void addTask(Task task) {
		tasks.add(task);
	}

	public void removeTask(Task task) {
		tasks.remove(task);
	}

	public Project getProject() {
		return project;
	}
	
	public void setProject(Project project) {
		this.project = project;
	}
	
	public Cycle getCycle() {
		return cycle;
	}
	
	public void setCycle(Cycle cycle) {
		this.cycle = cycle;
	}
	
	public boolean isActive() {
		if(cycle == null) {
			return true;
		} else {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			Date today = new Date();
			String todayStr = sdf.format(today);
			String cycleStartStr = sdf.format(cycle.getStartDate());
			String cycleEndStr = sdf.format(cycle.getEndDate());
			return todayStr.compareTo(cycleStartStr) >= 0 && todayStr.compareTo(cycleEndStr) <= 0;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((boardId == null) ? 0 : boardId.hashCode());
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
		Board other = (Board) obj;
		if (boardId == null) {
			if (other.boardId != null)
				return false;
		} else if (!boardId.equals(other.boardId))
			return false;
		return true;
	}
}
