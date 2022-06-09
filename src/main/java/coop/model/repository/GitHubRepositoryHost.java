package coop.model.repository;

import coop.util.JasyptUtil;
import org.eclipse.egit.github.core.*;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.ContentsService;
import org.eclipse.egit.github.core.service.RepositoryService;

import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import java.io.IOException;
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
    public List<String> retrieveBranchesFromRepository(RepositoryProject repositoryProject) {
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
    public List<Commit> retrieveCommitsFromRepository(RepositoryProject repositoryProject, String branchName, Date startDate, Date endDate) {
        List<Commit> commits = new ArrayList<Commit>();
        establishConnection();
        try {
            Repository gitHubRepository = getGitHubRepository(repositoryService.getRepositories(), repositoryProject);
            for(RepositoryCommit repositoryCommit: commitService.getCommits(gitHubRepository, branchName, null)) {
                String commitId = repositoryCommit.getSha();
                String commitMessage = repositoryCommit.getCommit().getMessage();
                String committerName = repositoryCommit.getCommit().getAuthor().getName();
                Date commitDate = repositoryCommit.getCommit().getCommitter().getDate();

                if(commitDate != null && (startDate == null || commitDate.after(startDate)) && (endDate == null || commitDate.before(endDate))) {
                    //this service method retrieves modified files for the commit
                    repositoryCommit = commitService.getCommit(gitHubRepository, commitId);
                    int numAdditions = 0;
                    int numDeletions = 0;
                    for(CommitFile modifiedFile: repositoryCommit.getFiles()) {
                        numAdditions += modifiedFile.getAdditions();
                        numDeletions += modifiedFile.getDeletions();
                    }
                    Commit commit = new Commit(commitId, commitMessage, committerName, commitDate, numAdditions, numDeletions);
                    commits.add(commit);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return commits;
    }

    @Override
    public Map<String, Set<String>> retrieveProjectFilesFromRepository(RepositoryProject repositoryProject, String branchName, Date untilDate) {
        Map<String, Set<String>> projectFiles = new TreeMap<String, Set<String>>();
        establishConnection();
        try {
            Repository gitHubRepository = getGitHubRepository(repositoryService.getRepositories(), repositoryProject);
            String commitId = getLatestCommitIdBeforeDate(repositoryProject, branchName, untilDate);
            processGitHubRepositoryTree(projectFiles, repositoryProject.getName(), repositoryProject.getName(), gitHubRepository, "", commitId);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return projectFiles;
    }


    @Override
    public List<String> retrieveModifiedFilesFromRepositoryCommit(RepositoryProject repositoryProject, String commitId, Set<String> ignoredFiles) {
        List<String> modifiedFiles = new ArrayList<String>();
        establishConnection();
        try {
            Repository gitHubRepository = getGitHubRepository(repositoryService.getRepositories(), repositoryProject);
            RepositoryCommit repositoryCommit = commitService.getCommit(gitHubRepository, commitId);
            List<CommitFile> commitFiles = repositoryCommit.getFiles();
            processGitHubCommitDiffs(modifiedFiles, commitFiles, repositoryProject.getName(), ignoredFiles);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return modifiedFiles;
    }

    @Override
    public String getCommitUrlPath() {
        return "/commit";
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

    private String getLatestCommitIdBeforeDate(RepositoryProject repositoryProject, String branchName, Date date) {
        List<Commit> commits = retrieveCommitsFromRepository(repositoryProject, branchName, null, date);
        if(commits != null && !commits.isEmpty()) {
            return commits.get(0).getCommitId();
        } else {
            return null;
        }
    }

    private void processGitHubRepositoryTree(Map<String, Set<String>> projectFiles, String projectName, String parentKey, Repository gitHubRepository, String path, String commitId) {
        HashSet<String> files = new HashSet<String>();
        projectFiles.put(parentKey, files);
        String dirKey;

        try {
            for(RepositoryContents repositoryContent : contentsService.getContents(gitHubRepository, path, commitId)) {
                if(RepositoryContents.TYPE_DIR.equals(repositoryContent.getType())) {
                    dirKey = projectName + "/" + repositoryContent.getPath();
                    path = repositoryContent.getPath();
                    files.add(dirKey);
                    processGitHubRepositoryTree(projectFiles, projectName, dirKey, gitHubRepository, path, commitId);
                } else if(RepositoryContents.TYPE_FILE.equals(repositoryContent.getType())) {
                    files.add(repositoryContent.getName());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processGitHubCommitDiffs(List<String> modifiedFiles, List<CommitFile> commitFiles, String projectName, Set<String> ignoredFiles) {
        for(CommitFile commitFile : commitFiles) {
            String status = commitFile.getStatus();
            String filePath = projectName + "/" + commitFile.getFilename();
            if(ignoredFiles.contains(filePath)) {
                modifiedFiles.remove(filePath); //remove if present
            } else if(!status.equals("added") && !status.equals("renamed")) {
                modifiedFiles.add(filePath);
            } else if(status.equals("added")) {
                ignoredFiles.add(filePath);
            }
        }
    }
}
