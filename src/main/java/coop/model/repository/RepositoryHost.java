package coop.model.repository;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "repository_host")
public abstract class RepositoryHost {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "repository_host_id", nullable = false, length = 20)
    protected Integer id;

    @Column(name = "repository_host_name", nullable = false, length = 50)
    protected String name;

    @Column(name = "repository_host_url", nullable = false)
    protected String repositoryHostUrl;

    @Column(name = "access_token", nullable = false)
    protected String accessToken;

    @Column(name = "namespace")
    protected String namespace;

    @OneToMany(mappedBy = "repositoryHost")
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<RepositoryProject> repositoryProjects;

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

    public String getRepositoryHostUrl() {
        return repositoryHostUrl;
    }

    public void setRepositoryHostUrl(String repositoryHostUrl) {
        this.repositoryHostUrl = repositoryHostUrl;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public List<RepositoryProject> getRepositoryProjects() {
        return repositoryProjects;
    }

    public void setRepositoryProjects(List<RepositoryProject> repositoryProjects) {
        this.repositoryProjects = repositoryProjects;
    }

    public RepositoryProject getProjectByUrl(String projectUrl) {
        for(RepositoryProject repositoryProject : repositoryProjects) {
            if(repositoryProject.getRepositoryProjectUrl().equals(projectUrl)) {
                return repositoryProject;
            }
        }
        return retrieveProjectFromRepository(projectUrl);
    }

    public abstract RepositoryProject retrieveProjectFromRepository(String repositoryProjectUrl);
    public abstract List<RepositoryProject> retrieveProjectsFromRepository();
    public abstract List<String> retrieveBranchesFromRepository(RepositoryProject repositoryProject);
    public abstract List<Commit> retrieveCommitsFromRepository(RepositoryProject repositoryProject, String branchName, Date startDate, Date endDate);

    public abstract Commit retrieveCommitFromRepository(RepositoryProject repositoryProject, String commitId);

    public abstract Map<String, Set<String>> retrieveProjectFilesFromRepository(RepositoryProject repositoryProject, String branchName);
    public abstract List<String> retrieveModifiedFilesFromRepositoryCommit(RepositoryProject repositoryProject, String commitId);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RepositoryHost)) return false;
        RepositoryHost that = (RepositoryHost) o;
        return id.equals(that.id) &&
                name.equals(that.name) &&
                repositoryHostUrl.equals(that.repositoryHostUrl) &&
                accessToken.equals(that.accessToken) &&
                Objects.equals(namespace, that.namespace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, repositoryHostUrl, accessToken, namespace);
    }
}
