package coop.model.repository;

import org.hibernate.annotations.Cascade;

import javax.persistence.*;

public class RepositoryProjectBranchCommit {
    private String commitId;
    private String commitMessage;
    private String committerName;

    public RepositoryProjectBranchCommit(String commitId, String commitMessage, String committerName) {
        this.commitId = commitId;
        this.commitMessage = commitMessage;
        this.committerName = committerName;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public String getCommitMessage() {
        return commitMessage;
    }

    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }

    public String getCommitterName() {
        return committerName;
    }

    public void setCommitterName(String committerName) {
        this.committerName = committerName;
    }
}
