package coop.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import coop.dao.ProjectDao;
import coop.dao.UserDao;
import coop.model.Project;
import coop.model.SlackWorkspace;
import coop.model.User;
import coop.util.CoOpUtil;
import coop.util.SlackUtil;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;

@Controller
public class SlackController {

    @RequestMapping(value = "/project/oauth/{projectId}", method = RequestMethod.GET)
    public static String associateSlackWorkspaceAndProject(HttpServletRequest request, @PathVariable Integer projectId, RedirectAttributes redirectAttributes) {
        User sessionUser = UserDao.getUserFromSession(request);
        ProjectDao projectDao = new ProjectDao();
        Project project = projectDao.getProject(projectId);

        if(sessionUser == null || !sessionUser.getProjects().contains(project)) {
            redirectAttributes.addFlashAttribute("error", "Your project could not be updated.");
            return "redirect:/project/"+projectId;
        }

        String code = request.getParameter("code");

        //get access token from slack
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
        body.add("client_id", "401770912423.401626455605");
        body.add("client_secret", "01c6ff7a12e172a75ec534b6c2fb8712");
        body.add("code", code);
        body.add("redirect_uri", SlackUtil.generateRedirectUri(request, projectId));
        HttpEntity<?> entity = new HttpEntity<Object>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange("https://slack.com/api/oauth.v2.access", HttpMethod.POST, entity, String.class);

        //parse response
        JSONObject jsonObj = new JSONObject(response.getBody());
        String accessToken = jsonObj.getString("access_token");
        JSONObject requestingWorkspace = jsonObj.getJSONObject("team");
        String workspaceId = requestingWorkspace.getString("id");
        JSONObject webhookObj = jsonObj.getJSONObject("incoming_webhook");
        String postToChannelId = webhookObj.getString("channel_id");

        //associate access token with project
        SlackWorkspace slackWorkspace = project.getSlackWorkspace();
        if(slackWorkspace == null) {
            slackWorkspace = new SlackWorkspace();
            project.setSlackWorkspace(slackWorkspace);
        }
        slackWorkspace.setSlackWorkspaceToken(accessToken);
        slackWorkspace.setSlackWorkspaceId(workspaceId);
        slackWorkspace.setSlackWorkspaceChannelId(postToChannelId);

        if(projectDao.updateProject(project)) {
            redirectAttributes.addFlashAttribute("success", "Your project has been updated.");
            return "redirect:/project/"+projectId;
        } else {
            redirectAttributes.addFlashAttribute("error", "Your project could not be updated.");
            return "redirect:/project/"+projectId;
        }
    }


    @ResponseBody
    @RequestMapping(value = "/project/slack/remove/{projectId}", method = RequestMethod.POST, produces = "application/json")
    public static String removeSlackWorkspaceFromProject(HttpServletRequest request, @PathVariable Integer projectId, RedirectAttributes redirectAttributes) {
        String result = "";
        User sessionUser = UserDao.getUserFromSession(request);
        ProjectDao projectDao = new ProjectDao();
        Project project = projectDao.getProject(projectId);
        if(sessionUser == null || !sessionUser.getProjects().contains(project)) {
            redirectAttributes.addFlashAttribute("error", "Your project could not be updated.");
            return result;
        }
        ObjectMapper mapper = new ObjectMapper();
        project.setSlackWorkspace(null);
        try {
            if(projectDao.updateProject(project)) {
                result = mapper.writeValueAsString("success");
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return result;
    }
}
