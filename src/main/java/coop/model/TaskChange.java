package coop.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;



@Entity
@Table(name = "changedvalues")
public class TaskChange {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "changed_value_id")
	private Integer id;
	
	@Column(name = "changed_field")
	private String changedField;
	
	@Column(name = "old_value")
	private String oldValue;
	
	@Column(name = "new_value")
	private String newValue;
	
	public TaskChange() {
		super();
	}

	public TaskChange(String changedField, String oldValue, String newValue) {
		super();
		this.changedField = changedField;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public String getChangedField() {
		return changedField;
	}

	public void setChangedField(String changedField) {
		this.changedField = changedField;
	}

	public String getOldValue() {
		return oldValue;
	}

	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}

	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((changedField == null) ? 0 : changedField.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((newValue == null) ? 0 : newValue.hashCode());
		result = prime * result + ((oldValue == null) ? 0 : oldValue.hashCode());
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
		TaskChange other = (TaskChange) obj;
		if (changedField == null) {
			if (other.changedField != null)
				return false;
		} else if (!changedField.equals(other.changedField))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (newValue == null) {
			if (other.newValue != null)
				return false;
		} else if (!newValue.equals(other.newValue))
			return false;
		if (oldValue == null) {
			if (other.oldValue != null)
				return false;
		} else if (!oldValue.equals(other.oldValue))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TaskChange [id=" + id + ", changedField=" + changedField + ", oldValue=" + oldValue + ", newValue="
				+ newValue + ", taskHistory=" + "]";
	}
}
