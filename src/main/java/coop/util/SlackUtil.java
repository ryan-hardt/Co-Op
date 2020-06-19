package coop.util;

import java.util.Arrays;
import java.util.List;

import coop.model.SlackWorkspace;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import coop.model.TaskChange;
import coop.model.TaskHistory;

import javax.servlet.http.HttpServletRequest;

public class SlackUtil {

	public static void createSlackChannel(String projectName, String accessToken) {
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer "+accessToken);
		MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();     
		body.add("name", projectName);
		HttpEntity<?> entity = new HttpEntity<Object>(body,headers);
		restTemplate.exchange("https://slack.com/api/channels.create", HttpMethod.POST, entity, String.class);
	}
	
	public static void archiveSlackChannel(String projectName, String accessToken) {
		String channelId = getChannelId(projectName, accessToken);
		if(channelId != null) {
			RestTemplate restTemplate = new RestTemplate();
			MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();   
			HttpHeaders headers = new HttpHeaders();
			
			body.add("channel", channelId);
			headers.set("Authorization", "Bearer "+accessToken);
			HttpEntity<?> entity = new HttpEntity<Object>(body, headers);
			
			restTemplate.exchange("https://slack.com/api/channels.archive", HttpMethod.POST, entity, String.class);
		}
	}
	
	public static void updateSlackChannelName(String oldProjectName, String newProjectName, String accessToken) {
		String channelId = getChannelId(oldProjectName, accessToken);
		if(channelId != null) {
			RestTemplate restTemplate = new RestTemplate();
			MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();   
			HttpHeaders headers = new HttpHeaders();
			
			body.add("channel", channelId);
			body.add("name", newProjectName);
			headers.set("Authorization", "Bearer "+accessToken);
			HttpEntity<?> entity = new HttpEntity<Object>(body, headers);
			
			restTemplate.exchange("https://slack.com/api/channels.rename", HttpMethod.POST, entity, String.class);
		}
	}
	
	public static void postToChannel(TaskHistory taskHistory, SlackWorkspace slackWorkspace) {
		String channelId = slackWorkspace.getSlackWorkspaceChannelId();
		String accessToken = slackWorkspace.getSlackWorkspaceToken();

		if(channelId != null) {
			RestTemplate restTemplate = new RestTemplate();
			MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();   
			HttpHeaders headers = new HttpHeaders();
			
			String slackMessage = generateTaskHistoryDescription(taskHistory);
			body.add("channel", channelId);
			body.add("text", slackMessage);
			body.add("username", "Co-Op");
			headers.set("Authorization", "Bearer "+accessToken);
			HttpEntity<?> entity = new HttpEntity<Object>(body, headers);
			
			restTemplate.exchange("https://slack.com/api/chat.postMessage", HttpMethod.POST, entity, String.class);
		}
	}

	public static String generateRedirectUri(HttpServletRequest request, int projectId) {
		String requestUrl = request.getRequestURL().toString();
		int coopInd = requestUrl.indexOf("coop/")+5;
		return requestUrl.substring(0,coopInd) + "project/oauth/" + projectId;
	}

	public static String generateRegistrationLink(HttpServletRequest request, int projectId) {
		String redirectUri = generateRedirectUri(request, projectId);
		String slackRegistrationLink = "https://slack.com/oauth/v2/authorize?client_id=401770912423.401626455605&scope=channels:manage,channels:read,chat:write,chat:write.public,incoming-webhook&redirect_uri="+redirectUri;
		return slackRegistrationLink;
	}
	
	private static String getChannelId(String projectName, String accessToken) {
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer "+accessToken);
		HttpEntity<?> entity = new HttpEntity<>(headers);
		ResponseEntity<SlackChannelList> response = restTemplate.exchange("https://slack.com/api/channels.list", HttpMethod.POST, entity, SlackChannelList.class);
		
		String channelId = null;
		projectName = generateSlackName(projectName);
		for(SlackChannel slackChannel : response.getBody().channels) {
			if(slackChannel.getName().equals(projectName)) {
				channelId = slackChannel.getId();
			}
		}
		return channelId;
	}
	
	private static String generateSlackName(String name) {
		String slackName = name.toLowerCase();
		slackName = slackName.replaceAll(" ", "-");
		slackName = slackName.replaceAll("[^A-Za-z0-9 -]+", "_");
		return slackName;
	}
	
	private static String generateTaskHistoryDescription(TaskHistory taskHistory) {
		StringBuilder sb = new StringBuilder();
		sb.append("\"");
		sb.append(taskHistory.getTask().getDescription());
		sb.append("\" ");
		//all changes
		/*
		sb.append("\" updates: \n");
		for(TaskChange taskChange : taskHistory.getChangedValueList()) {
			sb.append(generateTaskChangeDescription(taskChange));
			sb.append("\n");
		}
		*/
		//just status changes
		for(TaskChange taskChange : taskHistory.getChangedValueList()) {
			if(taskChange.getChangedField().equals("Status")) {
				sb.append(generateTaskChangeDescription(taskChange));
			}
		}
		sb.append(" by ");
		sb.append(taskHistory.getChangedByUser());
		return sb.toString();
	}
	
	private static String generateTaskChangeDescription(TaskChange taskChange) {
		StringBuilder sb = new StringBuilder();
		//if changed field value is a list
		if(taskChange.getOldValue() != null && taskChange.getOldValue().startsWith("[")) {
			String fmtOldValue = taskChange.getOldValue().substring(1, taskChange.getOldValue().length()-1);
			String fmtNewValue = taskChange.getNewValue().substring(1, taskChange.getNewValue().length()-1);
			List<String> oldValues = Arrays.asList(fmtOldValue.split(","));
			List<String> newValues = Arrays.asList(fmtNewValue.split(","));

			for(String ov : oldValues) {
				if(!ov.isEmpty() && newValues.indexOf(ov) == -1) {
					if(sb.length()!=0) {
						sb.append(", ");
					}
					sb.append("\"");
					sb.append(ov);
					sb.append("\" was removed from *");
					sb.append(taskChange.getChangedField());
					sb.append("*");
				}
			}
			for(String nv : newValues) {
				if(!nv.isEmpty() && oldValues.indexOf(nv) == -1) {
					if(sb.length()!=0) {
						sb.append(", ");
					}
					sb.append("\"");
					sb.append(nv);
					sb.append("\" was added to *");
					sb.append(taskChange.getChangedField());
					sb.append("*");
				}
			}
		} 
		//if changed field value is not a list
		else {
			sb.append("*");
			sb.append(taskChange.getChangedField());
			if(taskChange.getOldValue() == null || taskChange.getOldValue().isEmpty()) {
				sb.append("* was set to \"");
				sb.append(taskChange.getNewValue());
				sb.append("\"");
			} else {
				sb.append("* was changed from \"");
				sb.append(taskChange.getOldValue());
				sb.append("\" to \"");
				sb.append(taskChange.getNewValue());
				sb.append("\"");
			}
		}
		return sb.toString();
	}
	
	private static class SlackChannel {
		private String id;
		private String name;
		
		public String getId() {
			return this.id;
		}
		
		public String getName() {
			return this.name;
		}
	}
	
	private static class SlackChannelList {
		private SlackChannel[] channels;
		
		@SuppressWarnings("unused")
		public SlackChannel[] getChannels() {
			return this.channels;
		}
	}
}
