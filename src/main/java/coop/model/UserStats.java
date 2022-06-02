package coop.model;

import coop.model.repository.Commit;

import java.util.ArrayList;
import java.util.List;

public class UserStats {
    private int numWorkMinutesReportedAsOwner;
    private int numWorkMinutesReportedAsHelper;
    private int numWorkMinutesReportedAsReviewer;
    private int numWorkMinutesForResearch;
    private int numWorkMinutesForFeature;
    private int numWorkMinutesForUnitTest;
    private int numWorkMinutesForBugFix;
    private int numWorkMinutesForRefactor;
    private int numWorkMinutesForOther;
    private List<Commit> allCommits;
    private List<Commit> mergedCommits;

    public UserStats() {
        this.allCommits = new ArrayList<>();
        this.mergedCommits = new ArrayList<>();
    }

    public int getNumWorkMinutesReportedAsOwner() {
        return numWorkMinutesReportedAsOwner;
    }

    public void setNumWorkMinutesReportedAsOwner(int numWorkMinutesReportedAsOwner) {
        this.numWorkMinutesReportedAsOwner = numWorkMinutesReportedAsOwner;
    }

    public int getNumWorkMinutesReportedAsHelper() {
        return numWorkMinutesReportedAsHelper;
    }

    public void setNumWorkMinutesReportedAsHelper(int numWorkMinutesReportedAsHelper) {
        this.numWorkMinutesReportedAsHelper = numWorkMinutesReportedAsHelper;
    }

    public int getNumWorkMinutesReportedAsReviewer() {
        return numWorkMinutesReportedAsReviewer;
    }

    public void setNumWorkMinutesReportedAsReviewer(int numWorkMinutesReportedAsReviewer) {
        this.numWorkMinutesReportedAsReviewer = numWorkMinutesReportedAsReviewer;
    }

    public int getNumWorkMinutesForResearch() {
        return numWorkMinutesForResearch;
    }

    public void setNumWorkMinutesForResearch(int numWorkMinutesForResearch) {
        this.numWorkMinutesForResearch = numWorkMinutesForResearch;
    }

    public int getNumWorkMinutesForFeature() {
        return numWorkMinutesForFeature;
    }

    public void setNumWorkMinutesForFeature(int numWorkMinutesForFeature) {
        this.numWorkMinutesForFeature = numWorkMinutesForFeature;
    }

    public int getNumWorkMinutesForUnitTest() {
        return numWorkMinutesForUnitTest;
    }

    public void setNumWorkMinutesForUnitTest(int numWorkMinutesForUnitTest) {
        this.numWorkMinutesForUnitTest = numWorkMinutesForUnitTest;
    }

    public int getNumWorkMinutesForBugFix() {
        return numWorkMinutesForBugFix;
    }

    public void setNumWorkMinutesForBugFix(int numWorkMinutesForBugFix) {
        this.numWorkMinutesForBugFix = numWorkMinutesForBugFix;
    }

    public int getNumWorkMinutesForRefactor() {
        return numWorkMinutesForRefactor;
    }

    public void setNumWorkMinutesForRefactor(int numWorkMinutesForRefactor) {
        this.numWorkMinutesForRefactor = numWorkMinutesForRefactor;
    }

    public int getNumWorkMinutesForOther() {
        return numWorkMinutesForOther;
    }

    public void setNumWorkMinutesForOther(int numWorkMinutesForOther) {
        this.numWorkMinutesForOther = numWorkMinutesForOther;
    }

    public List<Commit> getAllCommits() {
        return allCommits;
    }

    public List<Commit> getMergedCommits() { return mergedCommits; }

    public void addCommit(Commit commit) {
        this.allCommits.add(commit);
    }

    public void addMergedCommit(Commit commit) {
        this.mergedCommits.add(commit);
    }

    public void addWorkMinutesReportedAsOwner(int workMinutes) {
        this.numWorkMinutesReportedAsOwner += workMinutes;
    }

    public void addWorkMinutesReportedAsHelper(int workMinutes) {
        this.numWorkMinutesReportedAsHelper += workMinutes;
    }

    public void addWorkMinutesReportedAsReviewer(int workMinutes) {
        this.numWorkMinutesReportedAsReviewer += workMinutes;
    }

    public void addWorkMinutesForResearch(int workMinutes) {
        this.numWorkMinutesForResearch += workMinutes;
    }

    public void addWorkMinutesForFeature(int workMinutes) {
        this.numWorkMinutesForFeature += workMinutes;
    }

    public void addWorkMinutesForBugFix(int workMinutes) {
        this.numWorkMinutesForBugFix += workMinutes;
    }

    public void addWorkMinutesForUnitTest(int workMinutes) {
        this.numWorkMinutesForUnitTest += workMinutes;
    }

    public void addWorkMinutesForRefactor(int workMinutes) {
        this.numWorkMinutesForRefactor += workMinutes;
    }

    public void addWorkMinutesForOther(int workMinutes) {
        this.numWorkMinutesForOther += workMinutes;
    }

    public boolean isEmpty() {
        return (numWorkMinutesReportedAsOwner + numWorkMinutesReportedAsHelper + numWorkMinutesReportedAsReviewer) == 0;
    }
}
