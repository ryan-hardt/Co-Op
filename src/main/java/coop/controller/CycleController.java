package coop.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import coop.model.*;
import coop.model.repository.RepositoryHost;
import coop.model.repository.RepositoryProject;
import coop.model.repository.RepositoryProjectBranchCommit;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import coop.dao.ProjectDao;
import coop.dao.CycleDao;
import coop.dao.UserDao;
import coop.dao.StatsDao;
import coop.util.CoOpUtil;

@Controller
public class CycleController {

	private SimpleDateFormat returnDateFormat = new SimpleDateFormat("MM/dd/yyyy");
	private SimpleDateFormat displayDateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy");
	private CycleDao cycleDao = new CycleDao();
	private ProjectDao projectDao = new ProjectDao();
	private UserDao userDao = new UserDao();
	private static final String VIEW_REQUEST = "view";
	private static final String ADD_REQUEST = "add";
	private static final String MODIFY_REQUEST = "modify";

	/**
	 * GET the web page for viewing a Cycle, coop/cycle/{id}
	 */
	@RequestMapping(value = "/cycle/{id}", method = RequestMethod.GET)
	public String viewCycleGet(@PathVariable("id") int id, HttpServletRequest request, RedirectAttributes redirectAttributes) {
		Cycle cycle = cycleDao.getCycle(id);

		// checks if user logged in or allowed to view cycle
		String sessCheck = checkSession(redirectAttributes, request, cycle, null, VIEW_REQUEST);
		if (sessCheck != null) { 
			return sessCheck;
		}

		if (cycle != null) {
			User u = UserDao.getUserFromSession(request);
			Map<User, UserStats> cycleStatsMap = StatsController.generateCycleStats(cycle);
			Project project = cycle.getProject();
			RepositoryProject repositoryProject = project.getRepositoryProject();
			RepositoryHost repositoryHost = repositoryProject.getRepositoryHost();
			String projectUrl = repositoryProject.getRepositoryProjectUrl();
			
			request.setAttribute("cycle", cycle);
			request.setAttribute("cycleNumber", CoOpUtil.getCycleNumber(cycle));
			request.setAttribute("startDateStr", displayDateFormat.format(cycle.getStartDate()));
			request.setAttribute("endDateStr", displayDateFormat.format(cycle.getEndDate()));
			request.setAttribute("cycleStatsMap", cycleStatsMap);
			request.setAttribute("projectUrl", projectUrl);
			request.setAttribute("repositoryProjectBranches", repositoryHost.retrieveProjectBranchesFromRepository(repositoryProject));

			return "cycle/viewCycle";
		} else {
			redirectAttributes.addFlashAttribute("error", "An invalid cycle was requested");
			return "redirect:/cycle";
		}
	}

	/**
	 * GET the web page for updating a Cycle, coop/cycle/update/{id}
	 */
	@RequestMapping(value = "/cycle/update/{id}", method = RequestMethod.GET)
	public String updateCycleGet(@PathVariable("id") int id, HttpServletRequest request, RedirectAttributes redirectAttributes) {
		Cycle cycle = cycleDao.getCycle(id);

		if (cycle != null) {
			// checks if user logged in or allowed to view cycle
			String sessCheck = checkSession(redirectAttributes, request, cycle, cycle.getProject(), MODIFY_REQUEST);
			if (sessCheck != null) {
				return sessCheck;
			}
			
			request.setAttribute("cycleid", cycle.getId());
			request.setAttribute("cycleNumber", CoOpUtil.getCycleNumber(cycle));
			request.setAttribute("startdate", returnDateFormat.format(cycle.getStartDate()));
			request.setAttribute("enddate", returnDateFormat.format(cycle.getEndDate()));

			return "cycle/updateCycle";
		} else {
			redirectAttributes.addFlashAttribute("error", "An invalid cycle was requested");
			return "redirect:/user/"+UserDao.getUserIDFromSession(request);
		}
	}

	/**
	 * GET the web page for adding a new cycle. coop/cycle/add
	 */
	@RequestMapping(value = "/cycle/add", method = RequestMethod.GET)
	public String addCycleGet(@RequestParam(value = "project", required = false) Integer projectId, HttpServletRequest request, RedirectAttributes redirectAttributes) {
		ProjectDao projectDao = new ProjectDao();
		Project project = projectDao.getProject(projectId);
		
		// checks if user logged in
		String sessCheck = checkSession(redirectAttributes, request, null, project, ADD_REQUEST);
		if (sessCheck != null) {
			return sessCheck;
		}

		String path = null;
		
		if (projectId == null) {
			redirectAttributes.addFlashAttribute("error", "You must select a project before creating a cycle");
			path = "redirect:/user/"+UserDao.getUserIDFromSession(request);
		} else if (project == null) {
			redirectAttributes.addFlashAttribute("error", "You must select a valid project before creating a cycle");
			path = "redirect:/user/"+UserDao.getUserIDFromSession(request);
		} else {
			request.setAttribute("project", project);
		}

		if (project != null) {
			Cycle cycle = new Cycle();
			cycle.setProject(project);
			path = "cycle/addCycle";
		}
		return path;
	}

	/**
	 * DELETE the cycle which is requesting a delete
	 */
	@RequestMapping(value = "/cycle/delete/{id}", method = RequestMethod.GET)
	public String deleteCycle(@PathVariable("id") int id, RedirectAttributes redirectAttributes, HttpServletRequest request) {
		Cycle cycle = cycleDao.getCycle(id);

		if (cycle != null) {
			Project project = projectDao.getProject(cycle.getProject().getId());
			// checks if user logged in or allowed to view cycle
			String sessCheck = checkSession(redirectAttributes, request, cycle, project, MODIFY_REQUEST);
			if (sessCheck != null) {
				return sessCheck;
			}
			
			if (cycleDao.deleteCycle(cycle)) {
				redirectAttributes.addFlashAttribute("success", "Your cycle was deleted");
			} else {
				redirectAttributes.addFlashAttribute("error", "Your cycle could not be deleted");
			}
		} else {
			redirectAttributes.addFlashAttribute("error", "An invalid cycle was requested");
		}
		return "redirect:/user/"+UserDao.getUserIDFromSession(request);
	}

	/**
	 * POST handle the form submission from the add web page
	 */
	@RequestMapping(value = "/cycle/add", method = RequestMethod.POST)
	public String addCycle(HttpServletRequest request, RedirectAttributes redirectAttributes) {
		Cycle cycle = new Cycle();
		Date sDate = new Date();
		Date eDate = new Date();
		
		String projectIdString = request.getParameter("projectId");
		Integer projectId = Integer.parseInt(projectIdString);
		Project project = projectDao.getProject(projectId);

		// checks if user logged in
		String sessCheck = checkSession(redirectAttributes, request, null, project, ADD_REQUEST);
		if (sessCheck != null) {
			return sessCheck;
		}

		try {
			sDate = returnDateFormat.parse(request.getParameter("startdate"));
			eDate = returnDateFormat.parse(request.getParameter("enddate"));
		} catch (ParseException | NumberFormatException ex) {
			redirectAttributes.addFlashAttribute("error", "Incorrect date format");
			return "redirect:/cycle/add";
		}

		cycle.setStartDate(sDate);
		cycle.setEndDate(eDate);
		cycle.setProject(project);
		cycle.getBoard().setProject(cycle.getProject());

		if (cycleDao.insert(cycle)) {
			redirectAttributes.addFlashAttribute("success", "Your cycle has been created");
			return "redirect:/cycle/" + cycle.getId();
		} else {
			redirectAttributes.addFlashAttribute("error", "Your cycle could not be created");
			return "redirect:/cycle/add";
		}
	}

	/**
	 * POST to handle updating the given cycle from the update web page
	 */
	@RequestMapping(value = "/cycle/update/{id}", method = RequestMethod.POST)
	public String updateCycle(@PathVariable("id") int id, HttpServletRequest request, RedirectAttributes redirectAttributes) {
		Cycle cycle = cycleDao.getCycle(id);
		
		if(cycle != null) {

			// checks if user logged in or allowed to view cycle
			String sessCheck = checkSession(redirectAttributes, request, cycle, cycle.getProject(), MODIFY_REQUEST);
			if (sessCheck != null) {
				return sessCheck;
			}
	
			Date sDate = new Date();
			Date eDate = new Date();
			try {
				sDate = returnDateFormat.parse(request.getParameter("startdate"));
				eDate = returnDateFormat.parse(request.getParameter("enddate"));
			} catch (ParseException e) {
				redirectAttributes.addFlashAttribute("error", "Incorrect date format");
				return "redirect:/cycle/" + cycle.getId();
			}
	
			cycle.setStartDate(sDate);
			cycle.setEndDate(eDate);
	
			if (cycleDao.updateCycle(cycle)) {
				redirectAttributes.addFlashAttribute("success", "Your cycle has been updated");
				return "redirect:/cycle/" + id;
			} else {
				redirectAttributes.addFlashAttribute("error", "Your cycle could not be updated");
				return "redirect:/user/"+UserDao.getUserIDFromSession(request);
			}
		} else {
			redirectAttributes.addFlashAttribute("error", "An invalid cycle was requested");
			return "redirect:/user/"+UserDao.getUserIDFromSession(request);
		}
	}

	/**
	 * POST to handle updating the given cycle from the update web page
	 */
	@RequestMapping(value = "/cycle/updateTeamBranch/{id}", method = RequestMethod.POST)
	public String updateCycleTeamBranch(@PathVariable("id") int id, HttpServletRequest request, RedirectAttributes redirectAttributes) {
		Cycle cycle = cycleDao.getCycle(id);

		if(cycle != null) {
			// checks if user logged in or allowed to view cycle
			String sessCheck = checkSession(redirectAttributes, request, cycle, cycle.getProject(), MODIFY_REQUEST);
			if (sessCheck != null) {
				return sessCheck;
			}
			String teamBranchName = request.getParameter("teamBranch");
			if(teamBranchName != null && !teamBranchName.isEmpty()) {
				cycle.setCycleTeamBranchName(teamBranchName);
				if (cycleDao.updateCycle(cycle)) {
					redirectAttributes.addFlashAttribute("success", "Your cycle team branch has been updated");
				} else {
					redirectAttributes.addFlashAttribute("error", "Your cycle team branch could not be updated");
				}
			}
			return "redirect:/cycle/" + id;
		} else {
			redirectAttributes.addFlashAttribute("error", "An invalid cycle was requested");
			return "redirect:/user/"+UserDao.getUserIDFromSession(request);
		}
	}

	private String checkSession(RedirectAttributes redirectAttributes, HttpServletRequest request, Cycle cycle, Project project, String requestType) {
		// Check if the user is in the session, if not, redirect to Login
		User user = userDao.getUser(UserDao.getUserIDFromSession(request));
		if (request.getSession().getAttribute("user") == null) {
			redirectAttributes.addFlashAttribute("message", "You must be logged in to view that page");
			return "redirect:/login/";
		}

		//If not adding and cycle does not exist
		if (!requestType.equals(ADD_REQUEST) && cycle == null) {
			redirectAttributes.addFlashAttribute("error", "An invalid cycle was requested");
			return "redirect:/user/"+UserDao.getUserIDFromSession(request);
		}

		//if view cycle is requested
		if(requestType.equals(VIEW_REQUEST)) {
			// Check if the user belongs to a team working on the project
			if (!cycle.getProject().getUsers().contains(user)) {
			    /*
				redirectAttributes.addFlashAttribute("error", "You are not assigned to a team working on this project");
				return "redirect:/user/"+UserDao.getUserIDFromSession(request);
				*/
                request.setAttribute("isMember", false);
			}
			//otherwise, add parameter indicating whether user is project owner
			else {
                request.setAttribute("isMember", true);
				request.setAttribute("isProjectOwner", cycle.getProject().getOwners().contains(user));
			}
		}
		//if anything other than view is requested, make sure user is owner of project
		else if(project == null || !project.getOwners().contains(user)) {
			redirectAttributes.addFlashAttribute("error", "Only project owners can update cycles");
			return "redirect:/user/"+UserDao.getUserIDFromSession(request);
		}
		
		return null;
	}
}
