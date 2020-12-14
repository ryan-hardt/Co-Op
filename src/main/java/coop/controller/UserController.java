package coop.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import coop.dao.ProjectDao;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import coop.dao.UserDao;
import coop.dao.StatsDao;
import coop.model.Project;
import coop.model.User;
import coop.util.BCrypt;
import coop.util.CoOpUtil;

@Controller
public class UserController {
	UserDao userDao = new UserDao();
	ProjectDao projectDao = new ProjectDao();

	@RequestMapping(value = "/user/{userId}", method = RequestMethod.GET)
	public String getUserPage(ModelMap model, @PathVariable int userId, HttpServletRequest request, RedirectAttributes redirectAttributes) {
		if (request.getSession().getAttribute("user") == null) {
			redirectAttributes.addFlashAttribute("error", "You must be logged in to view that page");
			return "redirect:/login";
		}		
		
		User sessionUser = (User)request.getSession().getAttribute("user");
		User requestedUser = userDao.getUser(userId);
		if (requestedUser == null) {
			redirectAttributes.addFlashAttribute("error", "User not found");
		}
		//user viewing own page
		if (requestedUser == null || userId == sessionUser.getId().intValue()) {
			StatsDao workDao = new StatsDao();
			double workPercentile = workDao.getTotalWorkPercentile(sessionUser);
			double collaboratorPercentile = workDao.getTotalCollaboratorPercentile(sessionUser);
			
			model.addAttribute("user", sessionUser);
			model.addAttribute("userProject", sessionUser.getProjects());
			model.addAttribute("userTasks", sessionUser.getAllTasks());
			model.addAttribute("updatable", true);
			model.addAttribute("workPercentile", CoOpUtil.toPercentage(workPercentile, 1));
			model.addAttribute("collaboratorPercentile", CoOpUtil.toPercentage(collaboratorPercentile, 1));
			//add other projects with same repository
			List<Project> otherProjects = projectDao.getUnassignedProjectsWithSameRepository(sessionUser);
            model.addAttribute("otherProjects", otherProjects);
		} 
		//user viewing other's page
		else {
            model.addAttribute("user", requestedUser);
            model.addAttribute("userProject", requestedUser.getProjects());
            model.addAttribute("userTasks", requestedUser.getAllTasks());
			model.addAttribute("updatable", false);
            //add other projects with same repository
            List<Project> otherProjects = projectDao.getUnassignedProjectsWithSameRepository(sessionUser);
            model.addAttribute("otherProjects", otherProjects);
		}

		return "user/viewUser";
	}

	@RequestMapping(value = "/user/add", method = RequestMethod.GET)
	public String getAddPage(ModelMap model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
		return "user/addUser";
	}

	@RequestMapping(value = "/user/add", method = RequestMethod.POST)
	public String handleAddUser(ModelMap model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
	  User user = new User();
      user.setUsername(CoOpUtil.sanitizeText(request.getParameter("username")));
      if (userDao.getUser(user.getUsername()) != null) {
          redirectAttributes.addFlashAttribute("error", "That username is taken, please choose another");
          return "redirect:/user/add";
      } else {
    	  user.setFirstName(CoOpUtil.sanitizeText(request.getParameter("firstName")));
    	  user.setLastName(CoOpUtil.sanitizeText(request.getParameter("lastName")));
          user.setSalt(BCrypt.gensalt(12));
          user.setHash(UserDao.hashPassword(CoOpUtil.sanitizeText(request.getParameter("password")), user.getSalt()));
          user.setIsActive(1);
          userDao.insertUser(user);

          request.getSession().setAttribute("user", user);
          redirectAttributes.addFlashAttribute("success", "Your account has been created");
          return "redirect:/user/" + user.getId();
      }
	}

	@RequestMapping(value = "/user/update/{userId}", method = RequestMethod.GET)
	public String getUpdatePage(ModelMap model, @PathVariable int userId, HttpServletRequest request, RedirectAttributes redirectAttributes) {
		if (request.getSession().getAttribute("user") == null) {
			redirectAttributes.addFlashAttribute("error", "You must be logged in to view that page");
			return "redirect:/login";
		}
		int sessionUid = ((User) request.getSession().getAttribute("user")).getId().intValue();
		if (sessionUid != userId) {
			redirectAttributes.addFlashAttribute("error", "You do not permission to modify that user");
			return "redirect:/user/" + sessionUid;
		}
		User user = userDao.getUser(Integer.valueOf(userId));
		model.addAttribute("user", user);
		return "user/updateUser";
	}

	@RequestMapping(value = "/user/update/{userId}", method = RequestMethod.POST)
	public String handleUpdateUser(ModelMap model, @PathVariable int userId, HttpServletRequest request, RedirectAttributes redirectAttributes) {
		if (request.getSession().getAttribute("user") == null) {
			redirectAttributes.addFlashAttribute("error", "You must be logged in to view that page");
			return "redirect:/login";
		}
		int sessionUid = ((User) request.getSession().getAttribute("user")).getId().intValue();
		if (sessionUid != userId) {
			redirectAttributes.addFlashAttribute("error", "You do not have permission to modify that user");
			return "redirect:/user/" + sessionUid;
		}
		User user = userDao.getUser(Integer.valueOf(userId));

		user.setUsername(CoOpUtil.sanitizeText(request.getParameter("username")));
		user.setFirstName(CoOpUtil.sanitizeText(request.getParameter("firstName")));
		user.setLastName(CoOpUtil.sanitizeText(request.getParameter("lastName")));

		String password = CoOpUtil.sanitizeText(request.getParameter("password"));
		if (password != null && password.length() > 0) {
			user.setHash(UserDao.hashPassword(password, user.getSalt()));
		}

		redirectAttributes.addFlashAttribute("success", "User updated");
		request.getSession().setAttribute("user", user);
		userDao.updateUser(user);
		return "redirect:/user/" + user.getId();
	}

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String getLoginPage(ModelMap model, HttpServletRequest request) {
		return "/login";
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public String handleLogin(HttpServletRequest request, RedirectAttributes redirectAttributes) {
		String dest = "";
		String uname = CoOpUtil.sanitizeText(request.getParameter("username"));
		String pwd = CoOpUtil.sanitizeText(request.getParameter("password"));
		User u = userDao.getUser(uname);

		if(u == null) {
			System.out.println("USER IS NULL AT LOGIN");
		}
		if (u != null && UserDao.isValidated(u, pwd)) {
			request.getSession().setAttribute("user", u);
			dest = "redirect:/user/" + u.getId();
		} else {
			redirectAttributes.addFlashAttribute("error", "Invalid username and/or password");
			dest = "redirect:/login";
		}
		return dest;
	}

	@RequestMapping(value = "/user/{uid}/changeState/{state}", produces = "application/json", method = RequestMethod.POST)
	public String changeState(ModelMap model, @PathVariable int uid, @PathVariable int state, HttpServletRequest request, HttpServletResponse response) {
		if (((User) request.getSession().getAttribute("user")).getId().intValue() != uid) {// can add admin right here
			try {
				response.sendError(550, "Permission denied");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (state == userDao.getUser(uid).getIsActive()) {
			try {
				response.sendError(400, "Bad request");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			User u = userDao.getUser(uid);
			if (state == 1) {
				u.activate();
			} else if (state == 0) {
				u.deactivate();
			}
			CoOpUtil.updateUserSession(request);
		}
		return "redirect:/user/"+uid;
	}

	@RequestMapping(value = "/user/checkduplicate", produces = "application/json", method = RequestMethod.POST)
	public void checkDuplicate(HttpServletRequest request, HttpServletResponse response) {
		User sessionUser = (User) request.getSession().getAttribute("user");
		String username = CoOpUtil.sanitizeText(request.getParameter("uname"));
		if (sessionUser != null && sessionUser.getUsername().equals(username)) {
			response.setStatus(200);
		} else {
			User u = userDao.getUser(username);
			if (u == null) {
				response.setStatus(200);
			} else {
				response.setStatus(412);
			}
		}
	}
}