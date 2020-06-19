package coop.model;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "impacted_project_file")
public class ImpactedProjectFile {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "impacted_project_file_id", length = 20)
    private Integer impactedProjectFileId;

    @Column(name = "path")
    private String path;

    @Column(name = "branch")
    private String branch;

    public ImpactedProjectFile() {}

    public ImpactedProjectFile(String path, String branch) {
        this.path = path;
        this.branch = branch;
    }

    public Integer getImpactedProjectFileId() {
        return impactedProjectFileId;
    }

    public void setImpactedProjectFileId(Integer impactedProjectFileId) {
        this.impactedProjectFileId = impactedProjectFileId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    @Override
    public String toString() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImpactedProjectFile)) return false;
        ImpactedProjectFile that = (ImpactedProjectFile) o;
        return Objects.equals(impactedProjectFileId, that.impactedProjectFileId) &&
                Objects.equals(path, that.path) &&
                Objects.equals(branch, that.branch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(impactedProjectFileId, path, branch);
    }
}
