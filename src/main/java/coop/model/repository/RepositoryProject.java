package coop.model.repository;

import com.fasterxml.jackson.annotation.JsonIgnore;
import coop.model.Project;
import org.hibernate.annotations.Cascade;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "repository_project")
public class RepositoryProject {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "repository_project_id", nullable = false, length = 20)
    protected Integer id;

    @Column(name = "repository_project_name", nullable = false, length = 30)
    protected String name;

    @Column(name = "repository_project_url", nullable = false)
    protected String repositoryProjectUrl;

    @OneToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(optional = false)
    @JoinColumn(name="repository_host_id")
    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    private RepositoryHost repositoryHost;

    public RepositoryProject() {}

    public RepositoryProject(String name, String repositoryProjectUrl, RepositoryHost repositoryHost) {
        this.name = name;
        this.repositoryProjectUrl = repositoryProjectUrl;
        this.repositoryHost = repositoryHost;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRepositoryProjectUrl() {
        return repositoryProjectUrl;
    }

    public void setRepositoryProjectUrl(String repositoryProjectUrl) {
        this.repositoryProjectUrl = repositoryProjectUrl;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    @JsonIgnore
    public RepositoryHost getRepositoryHost() {
        return repositoryHost;
    }

    public void setRepositoryHost(RepositoryHost repositoryHost) {
        this.repositoryHost = repositoryHost;
    }

    public String getCommitBaseUrl() {
        return repositoryProjectUrl + repositoryHost.getCommitUrlPath();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RepositoryProject)) return false;
        RepositoryProject that = (RepositoryProject) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, repositoryProjectUrl);
    }
}
