package coop.model.repository;

import coop.util.JasyptUtil;
import org.eclipse.egit.github.core.*;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.ContentsService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.swing.text.AbstractDocument;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.*;

@Entity
public class GitHubRepositoryHost extends RepositoryHost {

    @Transient
    private GitHubClient client;

    @Transient
    private RepositoryService repositoryService;

    @Transient
    private CommitService commitService;

    @Transient
    private ContentsService contentsService;

    public GitHubRepositoryHost() {

    }

    public GitHubRepositoryHost(String name, String url) {
        this.name = name;
        this.repositoryHostUrl = url;
    }

    @Override
    public RepositoryProject retrieveProjectFromRepository(String repositoryProjectUrl) {
        List<RepositoryProject> repositoryProjects = retrieveProjectsFromRepository();
        establishConnection();
        for(RepositoryProject repositoryProject : repositoryProjects) {
            if(repositoryProject.getRepositoryProjectUrl().equals(repositoryProjectUrl)) {
                return repositoryProject;
            }
        }
        return null;
    }

    @Override
    public List<RepositoryProject> retrieveProjectsFromRepository() {
        List<RepositoryProject> gitHubProjects = new ArrayList<RepositoryProject>();
        establishConnection();
        try {
            for(Repository repository: repositoryService.getRepositories()) {
                String repositoryName = repository.getName();
                String repositoryProjectUrl = repository.getHtmlUrl();
                RepositoryProject project = new RepositoryProject(repositoryName, repositoryProjectUrl, this);
                gitHubProjects.add(project);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return gitHubProjects;
    }

    @Override
    public List<String> retrieveProjectBranchesFromRepository(RepositoryProject repositoryProject) {
        List<String> projectBranches = new ArrayList<String>();
        establishConnection();
        try {
            Repository gitHubRepository = getGitHubRepository(repositoryService.getRepositories(), repositoryProject);
            List<RepositoryBranch> branches = repositoryService.getBranches(gitHubRepository);
            for(RepositoryBranch branch: branches) {
                projectBranches.add(branch.getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return projectBranches;
    }

    @Override
    public List<RepositoryProjectBranchCommit> retrieveProjectBranchCommitsFromRepository(RepositoryProject repositoryProject, String branchName) {
        List<RepositoryProjectBranchCommit> commits = new ArrayList<RepositoryProjectBranchCommit>();
        establishConnection();
        try {
            Repository gitHubRepository = getGitHubRepository(repositoryService.getRepositories(), repositoryProject);
            for(RepositoryCommit repositoryCommit: commitService.getCommits(gitHubRepository, branchName, null)) {
                String commitId = repositoryCommit.getSha();
                String commitMessage = repositoryCommit.getCommit().getMessage();
                String committerName = repositoryCommit.getCommitter().getName();
                RepositoryProjectBranchCommit commit = new RepositoryProjectBranchCommit(commitId, commitMessage, committerName);
                commits.add(commit);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return commits;
    }

    @Override
    public Map<String, Set<String>> retrieveProjectFilesFromRepository(RepositoryProject repositoryProject, String branchName) {
        Map<String, Set<String>> projectFiles = new TreeMap<String, Set<String>>();
        establishConnection();
        try {
            Repository gitHubRepository = getGitHubRepository(repositoryService.getRepositories(), repositoryProject);
            processGitHubRepositoryTree(projectFiles, repositoryProject.getName(), repositoryProject.getName(), gitHubRepository, "");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return projectFiles;
    }

    @Override
    public List<String> retrieveModifiedFilesFromRepositoryCommit(RepositoryProject repositoryProject, String commitId) {
        List<String> modifiedFiles = new ArrayList<String>();
        establishConnection();
        try {
            Repository gitHubRepository = getGitHubRepository(repositoryService.getRepositories(), repositoryProject);
            RepositoryCommit repositoryCommit = commitService.getCommit(gitHubRepository, commitId);
            List<CommitFile> commitFiles = repositoryCommit.getFiles();
            processGitHubCommitDiffs(modifiedFiles, commitFiles, repositoryProject.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return modifiedFiles;
    }

    private void establishConnection() {
        if(this.client == null) {
            this.client = new GitHubClient();
            this.client.setOAuth2Token(JasyptUtil.decrypt(accessToken));
            this.repositoryService = new RepositoryService(this.client);
            this.commitService = new CommitService(this.client);
            this.contentsService = new ContentsService(this.client);
        }
    }

    private Repository getGitHubRepository(List<Repository> repositories, RepositoryProject repositoryProject) {
        String url = repositoryProject.getRepositoryProjectUrl();
        for(Repository repository: repositories) {
            if(url.equals(repository.getHtmlUrl())) {
                return repository;
            }
        }
        return null;
    }

    private void processGitHubRepositoryTree(Map<String, Set<String>> projectFiles, String projectName, String parentKey, Repository gitHubRepository, String path) {
        HashSet<String> files = new HashSet<String>();
        projectFiles.put(parentKey, files);
        String dirKey;

        try {
            for(RepositoryContents repositoryContent : contentsService.getContents(gitHubRepository, path)) {
                if(RepositoryContents.TYPE_DIR.equals(repositoryContent.getType())) {
                    dirKey = projectName + "/" + repositoryContent.getPath();
                    path = repositoryContent.getPath();
                    files.add(dirKey);
                    processGitHubRepositoryTree(projectFiles, projectName, dirKey, gitHubRepository, path);
                } else if(RepositoryContents.TYPE_FILE.equals(repositoryContent.getType())) {
                    files.add(repositoryContent.getName());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processGitHubCommitDiffs(List<String> modifiedFiles, List<CommitFile> commitFiles, String projectName) {
        for(CommitFile commitFile : commitFiles) {
            if(!commitFile.getStatus().equals("added") && !commitFile.getStatus().equals("renamed")) {
                modifiedFiles.add(projectName + "/" + commitFile.getFilename());
            }
        }
    }
}
