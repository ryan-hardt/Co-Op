package coop.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import coop.dao.RepositoryDao;
import coop.dao.TaskDao;
import coop.dao.UserDao;
import coop.model.Cycle;
import coop.model.ImpactedProjectFile;
import coop.model.Task;
import coop.model.User;
import coop.model.repository.GitHubRepositoryHost;
import coop.model.repository.GitLabRepositoryHost;
import coop.model.repository.RepositoryHost;
import coop.model.repository.RepositoryProject;
import coop.util.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class RepositoryController {
    private RepositoryDao rDao = new RepositoryDao();

    @RequestMapping(value = "/repository/add", method=RequestMethod.GET)
    public String addRepositoryForm(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        String dest;
        User user = (User)request.getSession().getAttribute("user");

        if(user != null) {
            dest = "repository/addRepositoryHost";
        } else {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to view that page");
            dest = "redirect:/login";
        }
        return dest;
    }

    @RequestMapping(value = "/repository/add", method=RequestMethod.POST)
    public String addRepositorySubmit(ModelMap model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        User user = (User)request.getSession().getAttribute("user");
        UserDao userDao = new UserDao();

        if(user != null) {
            String type = request.getParameter("repositoryHostType");
            String name = CoOpUtil.sanitizeText(request.getParameter("repositoryHostName"));
            String url = CoOpUtil.sanitizeText(request.getParameter("repositoryHostUrl"));
            String accessToken = CoOpUtil.sanitizeText(request.getParameter("repositoryHostAccessToken"));
            String namespace = CoOpUtil.sanitizeText(request.getParameter("repositoryHostNamespace"));

            if(type == null || name == null || url == null || accessToken == null) {
                redirectAttributes.addFlashAttribute("error", "Please enter all required fields");
                return "redirect:/repository/add";
            } else {
                RepositoryHost repositoryHost = null;
                if(type.equals("gitlab")) {
                    repositoryHost = new GitLabRepositoryHost(name, url);

                } else if(type.equals("github")) {
                    repositoryHost = new GitHubRepositoryHost(name, url);
                }
                repositoryHost.setAccessToken(JasyptUtil.encrypt(accessToken));
                if(namespace != null && !namespace.isEmpty()) {
                    repositoryHost.setNamespace(namespace);
                }
                if(rDao.insert(repositoryHost)) {
                    user = userDao.getUser(user.getId());
                    user.addRepositoryHost(repositoryHost);
                    userDao.updateUser(user);
                    CoOpUtil.updateUserSession(request);
                    redirectAttributes.addFlashAttribute("success", "Your repository host has been added");
                    return "redirect:/project/add";
                }
                redirectAttributes.addFlashAttribute("error", "Your repository host could not be added");
                return "redirect:/project/add";
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to view that page");
            return "redirect:/login";
        }
    }

    @ResponseBody
    @RequestMapping(value = "/repository/queryRepositoryProjects/{repositoryId}", method = RequestMethod.POST, produces = "application/json")
    public String queryRepositoryProjectsResponder(@PathVariable("repositoryId") String rawRepositoryId, HttpServletRequest request) {
        int repositoryId = Integer.parseInt(rawRepositoryId);
        RepositoryHost repositoryHost = new RepositoryDao().find(repositoryId);
        ObjectMapper mapper = new ObjectMapper();
        String result = "";

        if (!UserDao.loggedIn(request)) {
            return result;
        }

        try {
            List<RepositoryProject> repositoryProjects = repositoryHost.retrieveProjectsFromRepository();
            List<RepositoryProject> sortedRepositoryProjects = repositoryProjects.stream().sorted((p1, p2) -> {
                if (p1 == null || p2 == null) {
                    return 0;
                }
                if (p1.getName() != null && p2.getName() != null) {
                    return p1.getName().compareTo(p2.getName());
                } else {
                    return p1.getId().compareTo(p2.getId());
                }
            }).collect(Collectors.toList());
            result = mapper.writeValueAsString(sortedRepositoryProjects);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/repository/queryRepositoryProjectFiles/{repositoryProjectId}/{branchName}/{taskId}", method = RequestMethod.POST, produces = "application/json")
    public String queryRepositoryProjectFilesResponder(@PathVariable("repositoryProjectId") String rawRepositoryProjectId, @PathVariable("branchName") String branchName, @PathVariable("taskId") String rawTaskId, HttpServletRequest request) {
        int repositoryProjectId = Integer.parseInt(rawRepositoryProjectId);
        int taskId = Integer.parseInt(rawTaskId);
        TaskDao taskDao = new TaskDao();
        Task task = taskDao.getTask(taskId);
        Cycle cycle = task.getBoard().getCycle();
        Date cycleStartDate = cycle.getStartDate();

        RepositoryProject repositoryProject = new RepositoryDao().findRepositoryProject(repositoryProjectId);
        RepositoryHost repositoryHost = repositoryProject.getRepositoryHost();
        ObjectMapper mapper = new ObjectMapper();
        String result = "";
        String path;
        String dirPath;
        String filename;
        Set<String> dirFiles;

        if (!UserDao.loggedIn(request)) {
            return result;
        }

        try {
            Map<String, Set<String>> projectFileTree = repositoryHost.retrieveProjectFilesFromRepository(repositoryProject, branchName, cycleStartDate);
            if(task != null && task.getImpactedFiles() != null && !task.getImpactedFiles().isEmpty()) {
                for(ImpactedProjectFile impactedProjectFile: task.getImpactedFiles()) {
                    path = impactedProjectFile.getPath();
                    dirPath = path.substring(0, path.lastIndexOf("/"));
                    dirFiles = projectFileTree.get(dirPath);
                    filename = path.substring(path.lastIndexOf("/")+1);
                    if(dirFiles != null) {
                        dirFiles.remove(filename);
                    }
                }
            }
            result = mapper.writeValueAsString(projectFileTree);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return result;
    }
}
