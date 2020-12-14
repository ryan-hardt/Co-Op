package coop.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import coop.dao.*;
import coop.model.*;
import coop.model.repository.RepositoryHost;
import coop.model.repository.RepositoryProject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import coop.util.CoOpUtil;
import coop.util.SlackUtil;


@Controller
public class ProjectController {

	private ProjectDao pDao = new ProjectDao();
	private UserDao uDao = new UserDao();
	private RepositoryDao rDao = new RepositoryDao();

	@RequestMapping(value = "/project/{id}", method=RequestMethod.GET)
	public String getProject(@PathVariable Integer id, Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
		String dest;
		User user = (User)request.getSession().getAttribute("user");
		Project project = pDao.getProject(id);
		if(user != null) {
			if(project != null) {
				StatsDao workDao = new StatsDao();
				List<User> projectUsers = project.getUsers();
				List<User> owners = project.getOwners();

				//assumes all users have access to all projects
                model.addAttribute("project", project);
                List<Cycle> cycles = project.getCycles();
                Collections.sort(cycles, new Comparator<Cycle>() {
                    public int compare(Cycle o1, Cycle o2) {
                        return o1.getStartDate().compareTo(o2.getStartDate());
                    }
                });

                double workPercentile = workDao.getProjectWorkPercentile(project, user);
                double collaboratorPercentile = workDao.getProjectCollaboratorPercentile(project, user);
                model.addAttribute("cycles", cycles);
                model.addAttribute("users", projectUsers);
                model.addAttribute("owners", owners);
                model.addAttribute("isProjectOwner", owners.contains(user));
                model.addAttribute("slackRegistrationLink", SlackUtil.generateRegistrationLink(request, project.getId()));
                model.addAttribute("workPercentile", CoOpUtil.toPercentage(workPercentile, 1));
                model.addAttribute("collaboratorPercentile", CoOpUtil.toPercentage(collaboratorPercentile, 1));
                dest = "/project/viewProject";

				if(projectUsers.contains(user)) {
                    model.addAttribute("isMember", true);
				} else {
			        /*
					redirectAttributes.addFlashAttribute("error", "You do not have access to that project");
					dest = "redirect:/user/"+UserDao.getUserIDFromSession(request);
					*/
                    model.addAttribute("isMember", false);
				}
			} else {
				redirectAttributes.addFlashAttribute("error", "You do not have access to that project");
				dest = "redirect:/user/"+UserDao.getUserIDFromSession(request);
			}
		} else {
			redirectAttributes.addFlashAttribute("error", "You must be logged in to view that page");
			dest = "redirect:/login"; 
		}
		return dest;
	}

	@RequestMapping(value = "/project/add", method=RequestMethod.GET)
	public String addProjectForm(Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
		String dest;
		User user = (User)request.getSession().getAttribute("user");
		if(user != null) {
			//List<RepositoryHost> repositories = rDao.getAllRepositories();
			List<RepositoryHost> repositories = user.getRepositoryHosts();
			List<User> otherUsers = uDao.getActiveUsers();
			otherUsers.remove(user);
			
			model.addAttribute("users", otherUsers);
			model.addAttribute("repositories", repositories);
			
			dest = "project/addProject";
		}
		else {
			redirectAttributes.addFlashAttribute("error", "You must be logged in to view that page");
			dest = "redirect:/login"; 
		}
		return dest;
	}

	@RequestMapping(value = "/project/add", method=RequestMethod.POST)
	public String addProjectSubmit(ModelMap model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
		User user = (User)request.getSession().getAttribute("user");
		if(user != null) {
			String name = CoOpUtil.sanitizeText(request.getParameter("projName"));
			String repositoryHostId = request.getParameter("repositoryHostId");
			String repositoryProjectUrl = request.getParameter("repositoryProjectUrl");
			String[] userIds = request.getParameter("users").split(",");
			String[] owners = request.getParameterValues("owners");

			if(name == null || userIds == null || owners == null || repositoryHostId == null || repositoryProjectUrl == null) {
				redirectAttributes.addFlashAttribute("error", "Enter all required fields");
				return "redirect:/project/add";
			} else {
				Project proj = new Project();
				proj.setName(name);

				RepositoryHost repositoryHost = rDao.find(Integer.parseInt(repositoryHostId));
				RepositoryProject repositoryProject = repositoryHost.getProjectByUrl(repositoryProjectUrl);
				proj.setRepositoryProject(repositoryProject);
				
				for(int i=0;i<userIds.length;i++) {
					proj.addUser(uDao.getUser(Integer.parseInt(userIds[i])));
				}
				for(int i=0;i<owners.length;i++) {
					proj.addOwner(uDao.getUser(Integer.parseInt(owners[i])));
				}
				if(pDao.insertProject(proj)) {
					redirectAttributes.addFlashAttribute("success", "Your project has been created");
				}
				CoOpUtil.updateUserSession(request);
				return "redirect:/project/" + proj.getId();
			}
		} else {
			redirectAttributes.addFlashAttribute("error", "You must be logged in to view that page");
			return "redirect:/login"; 
		}
	}

	@RequestMapping(value="/project/update/{id}", method=RequestMethod.GET)
	public String updateProjectForm(Model model, @PathVariable Integer id, HttpServletRequest request, RedirectAttributes redirectAttributes) {
		String dest;
		User user = (User)request.getSession().getAttribute("user");
		if(user != null) {
			Project project = pDao.getProject(id);
			if(project != null && project.getUsers().contains(user)) {
				List<User> owners = project.getOwners();
				if(owners.contains(user)) {
					List<RepositoryHost> repositories = user.getRepositoryHosts();
					List<RepositoryProject> repositoryProjects = project.getRepository().retrieveProjectsFromRepository();
					List<User> otherUsers = uDao.getActiveUsers();
					//remove project users from all users list for display purposes
					otherUsers.removeAll(project.getUsers());
					//remove owners from users list for display purposes
					project.getUsers().removeAll(owners);
					//remove user from owners list for display purposes
					owners.remove(user);
					
					model.addAttribute("repositories", repositories);
					model.addAttribute("repositoryProjects", repositoryProjects);
					model.addAttribute("project", project);
					model.addAttribute("otherUsers", otherUsers);
					model.addAttribute("slackRegistrationLink", SlackUtil.generateRegistrationLink(request, project.getId()));

					dest = "project/updateProject";
				} else {
					redirectAttributes.addFlashAttribute("error", "Only project owners can update projects");
					dest = "redirect:/user/"+UserDao.getUserIDFromSession(request);
				}
			} else {
				redirectAttributes.addFlashAttribute("error", "You do not have access to that project");
				dest = "redirect:/user/"+UserDao.getUserIDFromSession(request);
			}
		} else {
			redirectAttributes.addFlashAttribute("error", "You must be logged in to view that page");
			dest = "redirect:/login"; 
		}
		return dest;
	}

	@RequestMapping(value = "/project/update/{id}", method=RequestMethod.POST)
	public String updateProjectSubmit(@PathVariable Integer id, HttpServletRequest request, RedirectAttributes redirectAttributes) {
		User user = (User)request.getSession().getAttribute("user");
		if(user != null) {
			String name = CoOpUtil.sanitizeText(request.getParameter("projName"));
			String repositoryHostId = request.getParameter("repositoryHostId");
			String repositoryProjectUrl = request.getParameter("repositoryProjectUrl");
			String[] userIds = request.getParameter("users").split(",");
			String[] owners = request.getParameterValues("owners");

			if(name == null || userIds == null || owners == null || repositoryHostId == null || repositoryProjectUrl == null) {
				redirectAttributes.addFlashAttribute("error", "Enter all required fields");
				return "redirect:/project/update/"+id;
			} else {
				Project project = pDao.getProject(id);
				String oldProjectName = project.getName();
				String newProjectName = name;
				project.setName(name);

				//update repositoryHost project
				RepositoryHost repositoryHost = rDao.find(Integer.parseInt(repositoryHostId));
				RepositoryProject repositoryProject = repositoryHost.getProjectByUrl(repositoryProjectUrl);
				project.setRepositoryProject(repositoryProject);
				
				//update users
				List<User> existingUsers = new ArrayList<User>(project.getUsers());
				for(int i=0;i<existingUsers.size();i++) {
					project.deleteUser(existingUsers.get(i));
				}
				for(int i=0;i<userIds.length;i++) {
					project.addUser(uDao.getUser(Integer.parseInt(userIds[i])));
				}
				
				//update owners
				List<User> existingOwners = new ArrayList<User>(project.getOwners());
				for(int i=0;i<existingOwners.size();i++) {
					project.deleteOwner(existingOwners.get(i));
				}
				for(int i=0;i<owners.length;i++) {
					project.addOwner(uDao.getUser(Integer.parseInt(owners[i])));
				}

				if(pDao.updateProject(project) && !oldProjectName.equals(newProjectName)) {
					//SlackUtil.updateSlackChannelName(oldProjectName, newProjectName, project.getSlackWorkspaceToken());
					redirectAttributes.addFlashAttribute("message", "Your project has been updated");
				}
				
				return "redirect:/project/" + project.getId();
			}
		} else {
			redirectAttributes.addFlashAttribute("error", "You must be logged in to view this page");
			return "redirect:/login"; 
		}
	}
	
	@RequestMapping(value = "/project/leave/{projectId}", method=RequestMethod.GET)
	public String removeProjectUser(@PathVariable Integer projectId, HttpServletRequest request, RedirectAttributes redirectAttributes) {
		User user = (User)request.getSession().getAttribute("user");
		if(user != null) {
			Project project = pDao.getProject(projectId);
			List<User> projectOwners = project.getOwners();
			if(projectOwners.contains(user)) {
				//if user is only owner
				if(projectOwners.size() == 1) {
					redirectAttributes.addFlashAttribute("error", "You must assign another owner before leaving this project");
					return "redirect:/project/" + project.getId();
				} else {
					project.deleteOwner(user);
				}
			}
			project.deleteUser(user);
			pDao.updateProject(project);
			CoOpUtil.updateUserSession(request);
			redirectAttributes.addFlashAttribute("message", "You have left the project");
			return "redirect:/user/" + user.getId();
		} else {
			redirectAttributes.addFlashAttribute("error", "You must be logged in to view this page");
			return "redirect:/login"; 
		}
	}

	@RequestMapping(value="project/delete/{id}", method=RequestMethod.GET)
	public String deleteProject(@PathVariable Integer id, Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
		String dest;
		User user = (User)request.getSession().getAttribute("user");
		if(user != null) {
			Project project = pDao.getProject(id);
			if(project != null) {
				if(project.getOwners().contains(user)) {
					String projectName = project.getName();
					if(pDao.deleteProject(project)) {
						//SlackUtil.archiveSlackChannel(projectName, project.getSlackWorkspaceToken());
						redirectAttributes.addFlashAttribute("success", "Your project has been deleted");
					}
					CoOpUtil.updateUserSession(request);
					dest="redirect:/user/"+UserDao.getUserIDFromSession(request);
				} else {
					redirectAttributes.addFlashAttribute("error", "Only project owners can delete projects");
					dest = "redirect:/user/"+UserDao.getUserIDFromSession(request);
				}
			} else {
				redirectAttributes.addFlashAttribute("error", "You do not have access to that project");
				dest = "redirect:/user/"+UserDao.getUserIDFromSession(request);
			}
		} else {
			redirectAttributes.addFlashAttribute("error", "You must be logged in to view that page");
			dest = "redirect:/login"; 
		}
		return dest;
	}
}
