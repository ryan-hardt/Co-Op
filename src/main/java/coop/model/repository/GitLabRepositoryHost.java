package coop.model.repository;

import coop.util.JasyptUtil;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.persistence.Entity;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.*;

@Entity
public class GitLabRepositoryHost extends RepositoryHost {

    public GitLabRepositoryHost() {}

    public GitLabRepositoryHost(String name, String url) {
        this.name = name;
        this.repositoryHostUrl = url;
    }

    @Override
    public RepositoryProject retrieveProjectFromRepository(String repositoryProjectUrl) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Private-Token", JasyptUtil.decrypt(accessToken));
        HttpEntity<?> entity = new HttpEntity<>(headers);
        try {
            List<RepositoryProject> repositoryProjects = retrieveProjectsFromRepository();
            for(RepositoryProject repositoryProject : repositoryProjects) {
                if(repositoryProject.getRepositoryProjectUrl().equals(repositoryProjectUrl)) {
                    return repositoryProject;
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<RepositoryProject> retrieveProjectsFromRepository() {
        List<RepositoryProject> gitLabProjects = new ArrayList<RepositoryProject>();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Private-Token", JasyptUtil.decrypt(accessToken));
        HttpEntity<?> entity = new HttpEntity<>(headers);
        try {
            String url = repositoryHostUrl + "/api/v4/projects?simple=true&order_by=created_at&sort=asc&per_page=100&search="+namespace;
            ResponseEntity<GitLabProject[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, GitLabProject[].class);

            for(GitLabProject gitLabProject : response.getBody()) {
                if(gitLabProject.getName_with_namespace() != null && gitLabProject.getName_with_namespace().startsWith(namespace)) {
                    gitLabProjects.add(new RepositoryProject(gitLabProject.getName(), gitLabProject.getWeb_url(), this));
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return gitLabProjects;
    }

    @Override
    public List<String> retrieveProjectBranchesFromRepository(RepositoryProject repositoryProject) {
        List<String> gitLabProjectBranches = new ArrayList<String>();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Private-Token", JasyptUtil.decrypt(accessToken));
        HttpEntity<?> entity = new HttpEntity<>(headers);
        try {
            String repositoryProjectUrl = getNamespacedPathEncoding(repositoryProject.getRepositoryProjectUrl());
            String url = repositoryHostUrl + "/api/v4/projects/" + repositoryProjectUrl + "/repository/branches?per_page=100";

            URI apiUri = new URI(url);
            ResponseEntity<GitLabProjectBranch[]> response = restTemplate.exchange(apiUri, HttpMethod.GET, entity, GitLabProjectBranch[].class);

            for(GitLabProjectBranch gitLabProjectBranch : response.getBody()) {
                gitLabProjectBranches.add(gitLabProjectBranch.getName());
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return gitLabProjectBranches;
    }

    @Override
    public List<RepositoryProjectBranchCommit> retrieveProjectBranchCommitsFromRepository(RepositoryProject repositoryProject, String branchName) {
        List<RepositoryProjectBranchCommit> gitLabCommits = new ArrayList<RepositoryProjectBranchCommit>();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Private-Token", JasyptUtil.decrypt(accessToken));
        HttpEntity<?> entity = new HttpEntity<>(headers);
        try {
            String repositoryProjectUrl = getNamespacedPathEncoding(repositoryProject.getRepositoryProjectUrl());
            String url = repositoryHostUrl + "/api/v4/projects/" + repositoryProjectUrl + "/repository/commits?ref_name=" + branchName;
            URI apiUri = new URI(url);
            ResponseEntity<GitLabCommit[]> response = restTemplate.exchange(apiUri, HttpMethod.GET, entity, GitLabCommit[].class);

            for(GitLabCommit gitLabCommit : response.getBody()) {
                gitLabCommits.add(new RepositoryProjectBranchCommit(gitLabCommit.getShort_id(), gitLabCommit.getMessage(), gitLabCommit.getCommiter_name()));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return gitLabCommits;
    }

    @Override
    public Map<String, Set<String>> retrieveProjectFilesFromRepository(RepositoryProject repositoryProject, String branchName) {
        Map<String, Set<String>> projectFiles = new TreeMap<String, Set<String>>();
        try {
            String repositoryProjectUrl = getNamespacedPathEncoding(repositoryProject.getRepositoryProjectUrl());
            String url = repositoryHostUrl + "/api/v4/projects/" + repositoryProjectUrl + "/repository/tree?ref="+getNamespacedPathEncoding(branchName);
            processGitLabRepositoryTree(projectFiles, repositoryProject.getName(), repositoryProject.getName(), url);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return projectFiles;
    }

    @Override
    public List<String> retrieveModifiedFilesFromRepositoryCommit(RepositoryProject repositoryProject, String commitId) {
        List<String> modifiedFiles = new ArrayList<String>();
        try {
            String repositoryProjectUrl = getNamespacedPathEncoding(repositoryProject.getRepositoryProjectUrl());
            String url = repositoryHostUrl + "/api/v4/projects/" + repositoryProjectUrl + "/repository/commits/"+getNamespacedPathEncoding(commitId)+"/diff";
            processGitLabCommitDiffs(modifiedFiles, repositoryProject.getName(), url);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return modifiedFiles;
    }

    private void processGitLabRepositoryTree(Map<String, Set<String>> projectFiles, String projectName, String parentKey, String url) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Private-Token", JasyptUtil.decrypt(accessToken));
        HttpEntity<?> entity = new HttpEntity<>(headers);
        HashSet<String> files = new HashSet<String>();
        projectFiles.put(parentKey, files);
        String dirKey;

        try {
            URI apiUri = new URI(url);
            ResponseEntity<GitLabRepositoryTree[]> response = restTemplate.exchange(apiUri, HttpMethod.GET, entity, GitLabRepositoryTree[].class);
            for(GitLabRepositoryTree treeItem : response.getBody()) {
                if("tree".equals(treeItem.getType())) {
                    //parentKey += "/"+treeItem.getName();
                    dirKey = projectName + "/" + treeItem.path;
                    url += "&path="+treeItem.path;
                    files.add(dirKey);
                    processGitLabRepositoryTree(projectFiles, projectName, dirKey, url);
                } else if("blob".equals(treeItem.getType())) {
                    files.add(treeItem.getName());
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void processGitLabCommitDiffs(List<String> modifiedFiles, String projectName, String url) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Private-Token", JasyptUtil.decrypt(accessToken));
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            URI apiUri = new URI(url);
            ResponseEntity<GitLabCommitDiff[]> response = restTemplate.exchange(apiUri, HttpMethod.GET, entity, GitLabCommitDiff[].class);
            for(GitLabCommitDiff commitDiff : response.getBody()) {
                if(!commitDiff.new_file && !commitDiff.renamed_file) {
                    modifiedFiles.add(projectName + "/" + commitDiff.new_path);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static class GitLabProject {
        private String name;
        private String name_with_namespace;
        private String web_url;

        public String getName() {
            return this.name;
        }

        public String getName_with_namespace() {
            return this.name_with_namespace;
        }

        public String getWeb_url() {
            return this.web_url;
        }
    }

    public static class GitLabProjectBranch {
        private String name;

        public String getName() {
            return this.name;
        }
    }

    public static class GitLabCommit {
        private String short_id;
        private String message;
        private String committer_name;

        public String getShort_id() {
            return this.short_id;
        }

        public String getMessage() {
            return this.message;
        }

        public String getCommiter_name() {
            return this.committer_name;
        }
    }

    public static class GitLabRepositoryTree {
        private String name;
        private String type;
        private String path;

        public String getName() {
            return this.name;
        }

        public String getType() {
            return this.type;
        }

        public String getPath() {
            return this.path;
        }
    }

    public static class GitLabCommitDiff {
        private String new_path;
        private String old_path;
        private boolean new_file;
        private boolean renamed_file;
        private boolean deleted_file;

        public String getNew_path() {
            return new_path;
        }

        public String getOld_path() {
            return old_path;
        }

        public boolean isNew_file() {
            return new_file;
        }

        public boolean isRenamed_file() {
            return renamed_file;
        }

        public boolean isDeleted_file() {
            return deleted_file;
        }
    }

    public String getNamespacedPathEncoding(String webUrl) throws UnsupportedEncodingException {
        String namespacedPath = webUrl.replace(repositoryHostUrl, "");
        if(namespacedPath.startsWith("/")) {
            namespacedPath = namespacedPath.substring(1);
        }
        return URLEncoder.encode(namespacedPath, "UTF-8");
    }
}
