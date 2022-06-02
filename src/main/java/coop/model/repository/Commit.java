package coop.model.repository;

import java.util.Date;
import java.util.Objects;

public class Commit {
    private String commitId;
    private String commitMessage;
    private String committerName;
    private Date committedDate;
    private int numLinesAdded;
    private int numLinesDeleted;

    public Commit(String commitId, String commitMessage, String committerName, Date committedDate, int numLinesAdded, int numLinesDeleted) {
        this.commitId = commitId;
        this.commitMessage = commitMessage;
        this.committerName = committerName;
        this.committedDate = committedDate;
        this.numLinesAdded = numLinesAdded;
        this.numLinesDeleted = numLinesDeleted;
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

    public Date getCommittedDate() {
        return committedDate;
    }

    public void setCommittedDate(Date committedDate) {
        this.committedDate = committedDate;
    }

    public int getNumLinesAdded() {
        return numLinesAdded;
    }

    public void setNumLinesAdded(int numLinesAdded) {
        this.numLinesAdded = numLinesAdded;
    }

    public int getNumLinesDeleted() {
        return numLinesDeleted;
    }

    public void setNumLinesDeleted(int numLinesDeleted) {
        this.numLinesDeleted = numLinesDeleted;
    }

    public boolean isRevert() {
        return this.commitMessage.contains("reverts commit");
    }

    public String getRevertedCommitId() {
        String revertedCommitId = null;
        int revertedCommitIdInd = this.commitMessage.indexOf("reverts commit");
        if(revertedCommitIdInd != -1) {
            revertedCommitId = this.commitMessage.substring(revertedCommitIdInd+15);
        }
        return revertedCommitId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Commit that = (Commit) o;
        return numLinesAdded == that.numLinesAdded && numLinesDeleted == that.numLinesDeleted && Objects.equals(commitId, that.commitId) && Objects.equals(commitMessage, that.commitMessage) && Objects.equals(committerName, that.committerName) && Objects.equals(committedDate, that.committedDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commitId, commitMessage, committerName, committedDate, numLinesAdded, numLinesDeleted);
    }
}
