package coop.model;

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

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import coop.util.CoOpUtil;

@Entity
@Table(name = "cycle")
public class Cycle {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "cycle_id", length = 20)
	private Integer id;

	@Column(name = "start_date", length = 10)
	private Date startDate;

	@Column(name = "end_date", length = 10)
	private Date endDate;

	@Column(name = "team_branch_name", length = 35)
	private String cycleTeamBranchName;

	@ManyToOne(optional = false)
	@JoinColumn(name="project_id")
	@Cascade(CascadeType.SAVE_UPDATE)
	private Project project;

	@OneToOne(optional = false)
	@JoinColumn(name="board_id")
	@Cascade(CascadeType.ALL)
	private Board board;

	public Cycle() {
		this.board = new Board();
		this.board.setCycle(this);
		this.board.setProject(this.getProject());
	}
	
	public Cycle(Project project, Date startDate, Date endDate) {
		this();
		this.project = project;
		this.startDate = startDate;
		this.endDate = endDate;
	}
	
	/**
	 * Gets the unique ID for this Cycle
	 * @return The ID for this Cycle
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the cycle id for testing purposes
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Sets the Start Date for this Cycle
	 * @param startDate - a Date object for the starting date of this Cycle
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	/**
	 * Sets the End Date for this Cycle
	 * @param endDate - a Date object for the ending date of this Cycle
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getCycleTeamBranchName() {
		return cycleTeamBranchName;
	}

	public void setCycleTeamBranchName(String cycleTeamBranchName) {
		this.cycleTeamBranchName = cycleTeamBranchName;
	}

	/**
	 * Sets the project relationship to be the given Project object
	 * @param project - a Project object to be related to this Cycle
	 */
	public void setProject(Project project) {
		this.project = project;
	}

	/**
	 * Sets the board relationship to be the given Project object
	 * @param board - A Board object to be related to this Cycle
	 */
	public void setBoard(Board board) {
		this.board = board;
	}

	/**
	 * Gets the Start Date for this Cycle
	 * @return The starting date for this Cycle
	 */
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * Gets the End Date for this Cycle
	 * @return The ending date for this Cycle
	 */
	public Date getEndDate() {
		return endDate;
	}

	/**
	 * Gets the Project associated with this Cycle
	 * @return The Project object associated with this Cycle
	 */
	public Project getProject() {
		return project;
	}

	/**
	 * Gets the Board associated with this Cycle
	 * @return The Board object associated with this Cycle
	 */
	public Board getBoard() {
		return board;
	}

	/**
	 * Returns a string detailing the name of the cycle using it's ID
	 * @returns A string representation of the cycle
	 */

	@Override
	public String toString() {
		return "Cycle " + CoOpUtil.getCycleNumber(this);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((board == null) ? 0 : board.hashCode());
		result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
		result = prime * result + ((project == null) ? 0 : project.hashCode());
		result = prime * result + id;
		result = prime * result + ((startDate == null) ? 0 : startDate.hashCode());
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
		Cycle other = (Cycle) obj;
		if (board == null) {
			if (other.board != null)
				return false;
		} else if (!board.equals(other.board))
			return false;
		if (endDate == null) {
			if (other.endDate != null)
				return false;
		} else if (!endDate.equals(other.endDate))
			return false;
		if (cycleTeamBranchName == null) {
			if (other.cycleTeamBranchName != null)
				return false;
		} else if (!cycleTeamBranchName.equals(other.cycleTeamBranchName))
			return false;
		if (project == null) {
			if (other.project != null)
				return false;
		} else if (!project.getId().equals(other.project.getId()))
			return false;
		if (!id.equals(other.id))
			return false;
		if (startDate == null) {
			if (other.startDate != null)
				return false;
		} else if (!startDate.equals(other.startDate))
			return false;
		return true;
	}
}
