package coop.model;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "slack_workspace")
public class SlackWorkspace {
    @Id
    @Column(name = "slack_workspace_id", length =25)
    private String slackWorkspaceId;

    @Column(name = "slack_workspace_token", length =75)
    private String slackWorkspaceToken;

    @Column(name = "slack_workspace_channel_id", length =25)
    private String slackWorkspaceChannelId;

    public SlackWorkspace() {

    }

    public String getSlackWorkspaceId() {
        return slackWorkspaceId;
    }

    public void setSlackWorkspaceId(String slackWorkspaceId) {
        this.slackWorkspaceId = slackWorkspaceId;
    }

    public String getSlackWorkspaceToken() {
        return slackWorkspaceToken;
    }

    public void setSlackWorkspaceToken(String slackWorkspaceToken) {
        this.slackWorkspaceToken = slackWorkspaceToken;
    }

    public String getSlackWorkspaceChannelId() {
        return slackWorkspaceChannelId;
    }

    public void setSlackWorkspaceChannelId(String slackWorkspaceChannelId) {
        this.slackWorkspaceChannelId = slackWorkspaceChannelId;
    }

    @Override
    public String toString() {
        return "SlackWorkspace{" +
                "slackWorkspaceId='" + slackWorkspaceId + '\'' +
                ", slackWorkspaceToken='" + slackWorkspaceToken + '\'' +
                ", slackWorkspaceChannelId='" + slackWorkspaceChannelId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SlackWorkspace)) return false;
        SlackWorkspace that = (SlackWorkspace) o;
        return Objects.equals(slackWorkspaceId, that.slackWorkspaceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(slackWorkspaceId);
    }
}
