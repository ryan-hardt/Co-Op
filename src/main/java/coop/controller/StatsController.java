package coop.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import coop.model.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import coop.dao.ProjectDao;
import coop.dao.CycleDao;
import coop.dao.UserDao;
import coop.dao.StatsDao;
import coop.model.Cycle;

@Controller
public class StatsController {
	private StatsDao workDao = new StatsDao();
	
	@ResponseBody
	@RequestMapping(value = "/work/stats/{type}", method = RequestMethod.POST, produces = "application/json")
	public Map<String, Map<User, Integer>> getUserStatsForAllProjects(@PathVariable("type") String type, ModelMap model, HttpServletRequest request) {
		Map<String, Map<User, Integer>> allProjectWorkStats = new HashMap<String, Map<User, Integer>>();
		Map<String, Map<User, Integer>> projectWorkStats = null;
		User user = UserDao.getUserFromSession(request);
		if(user == null) {
			return null;
		}

		for(Project p : user.getProjects()) {
			if("role".equals(type)) {
				projectWorkStats = getProjectWorkByRole(p);
			} else if("tag".equals(type)) {
				projectWorkStats = getProjectWorkByTag(p);
			} else if("collaborators".equals(type)) {
				projectWorkStats = getNumProjectCollaboratorsByRole(p);
			}
			incorporateProjectWorkStats(projectWorkStats, allProjectWorkStats);
		}
		
		return allProjectWorkStats;
	}
	
	@ResponseBody
	@RequestMapping(value = "/work/stats/project/{projectId}/{type}", method = RequestMethod.POST, produces = "application/json")
	public Map<String, Map<User, Integer>> getProjectWorkStats(@PathVariable("projectId") int projectId, @PathVariable("type") String type, ModelMap model, HttpServletRequest request) {
		ProjectDao projectDao = new ProjectDao();
		Project p = projectDao.getProject(projectId);
		
		User user = UserDao.getUserFromSession(request);
		if(user == null || !user.getProjects().contains(p)) {
			return null;
		}
		
		if("role".equals(type)) {
			return getProjectWorkByRole(p);
		} else if("tag".equals(type)) {
			return getProjectWorkByTag(p);
		} else if("collaborators".equals(type)) {
			return getNumProjectCollaboratorsByRole(p);
		}
		return null;
	}
	
	@ResponseBody
	@RequestMapping(value = "/work/stats/cycle/{cycleId}/{type}", method = RequestMethod.POST, produces = "application/json")
	public Map<String, Map<User, Integer>> getProjectWorkStatsForCycle(@PathVariable("cycleId") int cycleId, @PathVariable("type") String type, ModelMap model, HttpServletRequest request) {
		CycleDao cycleDao = new CycleDao();
		Cycle s = cycleDao.getCycle(cycleId);
		
		User user = UserDao.getUserFromSession(request);
		if(user == null || !user.getProjects().contains(s.getProject())) {
			return null;
		}
		
		if("role".equals(type)) {
			return getCycleWorkByRole(s);
		} else if("tag".equals(type)) {
			return getCycleWorkByTag(s);
		} else if("collaborators".equals(type)) {
			return getNumCycleCollaboratorsByRole(s);
		}
		return null;
	}

	@ResponseBody
	@RequestMapping(value = "/work/cycleWork/{cycleId}", method = RequestMethod.POST, produces = "application/json")
	public Map<Integer,List<Integer>> getCycleWork(@PathVariable("cycleId") int cycleId, ModelMap model, HttpServletRequest request) {
		Map<Integer, List<Integer>> userDailyWork = new TreeMap<Integer, List<Integer>>();
		CycleDao cycleDao = new CycleDao();
		Cycle s = cycleDao.getCycle(cycleId);
		Board b = s.getBoard();

		User user = UserDao.getUserFromSession(request);
		if(user == null || !user.getProjects().contains(s.getProject())) {
			return null;
		}
		
		List<Work> work = workDao.getCycleWork(b);
		int numDaysInCycle = (int)Math.floor(TimeUnit.DAYS.convert(s.getEndDate().getTime() - s.getStartDate().getTime(), TimeUnit.MILLISECONDS)) + 1;
		
		//initialize all users daily work time to 0
		List<User> cycleUsers = cycleDao.getCycleUsers(cycleId);
		for(User u : cycleUsers) {
			ArrayList<Integer> dailyWork = new ArrayList<Integer>();
			initializeDailyWork(dailyWork, numDaysInCycle);
			userDailyWork.put(u.getId(), dailyWork);
		}
		
		//update all users daily work time
		for(Work w : work) {
			Integer userId = w.getUser().getId();
			List<Integer> dailyWork = userDailyWork.get(userId);
			int dailyWorkIndex = convertDateToIndex(w.getDate(),  s.getStartDate());
			int currentMinutes = dailyWork.get(dailyWorkIndex);
			dailyWork.set(dailyWorkIndex, currentMinutes + w.getNumMinutes());
		}
		
		//update all users daily values with their min sum thus far
		Iterator<List<Integer>> itr = userDailyWork.values().iterator();
		while(itr.hasNext()) {
			List<Integer> dailyWork = itr.next();
			int sum = 0;
			for(int i=0; i<dailyWork.size(); i++) {
				sum += dailyWork.get(i);
				dailyWork.set(i, sum);
			}
		}
		return userDailyWork;
	}
	
	@ResponseBody
	@RequestMapping(value = "/work/cycleUsers/{cycleId}", method = RequestMethod.POST, produces = "application/json")
	public List<User> getCycleUsers(@PathVariable("cycleId") int cycleId, ModelMap model, HttpServletRequest request) {
		CycleDao cycleDao = new CycleDao();
		
		Cycle cycle = cycleDao.getCycle(cycleId);
		User user = UserDao.getUserFromSession(request);
		if(user == null || !user.getProjects().contains(cycle.getProject())) {
			return null;
		}
		
		return cycleDao.getCycleUsers(cycleId);
	}
	
	private Map<String, Map<User, Integer>> getNumProjectCollaboratorsByRole(Project p) {
		Map<String, Map<User, Integer>> userTypeMaps = new HashMap<String, Map<User, Integer>>();
		Set<Task> projectTasks = new HashSet<Task>();
		Map<User, Set<User>> ownersMap = new HashMap<User, Set<User>>();
		Map<User, Set<User>> helpersMap = new HashMap<User, Set<User>>();
		Map<User, Set<User>> reviewersMap = new HashMap<User, Set<User>>();
		Map<User, Set<User>> totalMap = new HashMap<User, Set<User>>();
		Map<User, Integer> ownerCountMap = new HashMap<User, Integer>();
		Map<User, Integer> helperCountMap = new HashMap<User, Integer>();
		Map<User, Integer> reviewerCountMap = new HashMap<User, Integer>();
		Map<User, Integer> totalCountMap = new HashMap<User, Integer>();
		Set<User> ownedUsers;
		Set<User> helpedUsers;
		Set<User> reviewedUsers;
		Set<User> allUsers;
		
		for(Cycle s : p.getCycles()) {
			projectTasks.addAll(s.getBoard().getTasks());
		}
		
		//create unique collaborators lists
		for(Task t : projectTasks) {
			//owners
			for(User owner : t.getOwners()) {
				ownedUsers = ownersMap.get(owner);
				if(ownedUsers == null) {
					ownedUsers = new HashSet<User>();
					ownersMap.put(owner, ownedUsers);
				}
				//give owner credit for working with helpers
				for(User helper : t.getHelpers()) {
					ownedUsers.add(helper);
				}
				//give owner credit for working with reviewers
				for(User reviewer : t.getReviewers()) {
					ownedUsers.add(reviewer);
				}
				//give owner credit for working with other owners
				for(User otherOwner : t.getOwners()) {
					if(!owner.equals(otherOwner)) {
						ownedUsers.add(otherOwner);
					}
				}
			}
			//add owners to total map
			totalMap.putAll(ownersMap);
			
			//helpers
			for(User helper : t.getHelpers()) {
				helpedUsers = helpersMap.get(helper);
				if(helpedUsers == null) {
					helpedUsers = new HashSet<User>();
					helpersMap.put(helper, helpedUsers);
				}
				//give helper credit for working with owners
				for(User owner : t.getOwners()) {
					helpedUsers.add(owner);
				}
				//give helper credit for working with other helpers
				for(User otherHelper : t.getHelpers()) {
					if(!helper.equals(otherHelper)) {
						helpedUsers.add(otherHelper);
					}
				}
				//add helpers to total map
				allUsers = totalMap.get(helper);
				if(allUsers == null) {
					allUsers = new HashSet<User>();
					totalMap.put(helper,  allUsers);
				}
				totalMap.get(helper).addAll(helpedUsers);
			}
			//reviewers
			for(User reviewer : t.getReviewers()) {
				
				reviewedUsers = reviewersMap.get(reviewer);
				if(reviewedUsers == null) {
					reviewedUsers = new HashSet<User>();
					reviewersMap.put(reviewer, reviewedUsers);
				}
				//give reviewer credit for working with owners
				for(User owner : t.getOwners()) {
					reviewedUsers.add(owner);
				}
				//give reviewer credit for working with other reviewers
				for(User otherReviewer : t.getReviewers()) {
					if(!reviewer.equals(otherReviewer)) {
						reviewedUsers.add(otherReviewer);
					}
				}
				//add reviewers to total map
				allUsers = totalMap.get(reviewer);
				if(allUsers == null) {
					allUsers = new HashSet<User>();
					totalMap.put(reviewer,  allUsers);
				}
				totalMap.get(reviewer).addAll(reviewedUsers);
			}
		}
		
		//create count maps
		for(User u : helpersMap.keySet()) {
			helperCountMap.put(u, helpersMap.get(u).size());
		}
		for(User u : reviewersMap.keySet()) {
			reviewerCountMap.put(u, reviewersMap.get(u).size());
		}
		for(User u : ownersMap.keySet()) {
			ownerCountMap.put(u, ownersMap.get(u).size());
		}
		for(User u : totalMap.keySet()) {
			totalCountMap.put(u, totalMap.get(u).size());
		}
		
		userTypeMaps.put("allRolesCollaborator", totalCountMap);
		userTypeMaps.put("ownerCollaborator", ownerCountMap);
		userTypeMaps.put("helperCollaborator", helperCountMap);
		userTypeMaps.put("reviewerCollaborator", reviewerCountMap);
		
		return userTypeMaps;
	}
	
	private Map<String, Map<User, Integer>> getNumCycleCollaboratorsByRole(Cycle s) {
		Map<String, Map<User, Integer>> userTypeMaps = new HashMap<String, Map<User, Integer>>();
		Set<Task> projectTasks = new HashSet<Task>();
		Map<User, Set<User>> ownersMap = new HashMap<User, Set<User>>();
		Map<User, Set<User>> helpersMap = new HashMap<User, Set<User>>();
		Map<User, Set<User>> reviewersMap = new HashMap<User, Set<User>>();
		Map<User, Set<User>> totalMap = new HashMap<User, Set<User>>();
		Map<User, Integer> ownerCountMap = new HashMap<User, Integer>();
		Map<User, Integer> helperCountMap = new HashMap<User, Integer>();
		Map<User, Integer> reviewerCountMap = new HashMap<User, Integer>();
		Map<User, Integer> totalCountMap = new HashMap<User, Integer>();
		Set<User> ownedUsers;
		Set<User> helpedUsers;
		Set<User> reviewedUsers;
		Set<User> allUsers;
		
		projectTasks.addAll(s.getBoard().getTasks());
		
		//create unique collaborators lists
		for(Task t : projectTasks) {
			//owners
			for(User owner : t.getOwners()) {
				ownedUsers = ownersMap.get(owner);
				if(ownedUsers == null) {
					ownedUsers = new HashSet<User>();
					ownersMap.put(owner, ownedUsers);
				}
				//give owner credit for working with helpers
				for(User helper : t.getHelpers()) {
					ownedUsers.add(helper);
				}
				//give owner credit for working with reviewers
				for(User reviewer : t.getReviewers()) {
					ownedUsers.add(reviewer);
				}
				//give owner credit for working with other owners
				for(User otherOwner : t.getOwners()) {
					if(!owner.equals(otherOwner)) {
						ownedUsers.add(otherOwner);
					}
				}
			}
			//add owners to total map
			totalMap.putAll(ownersMap);
			
			//helpers
			for(User helper : t.getHelpers()) {
				helpedUsers = helpersMap.get(helper);
				if(helpedUsers == null) {
					helpedUsers = new HashSet<User>();
					helpersMap.put(helper, helpedUsers);
				}
				//give helper credit for working with owners
				for(User owner : t.getOwners()) {
					helpedUsers.add(owner);
				}
				//give helper credit for working with other helpers
				for(User otherHelper : t.getHelpers()) {
					if(!helper.equals(otherHelper)) {
						helpedUsers.add(otherHelper);
					}
				}
				//add helpers to total map
				allUsers = totalMap.get(helper);
				if(allUsers == null) {
					allUsers = new HashSet<User>();
					totalMap.put(helper,  allUsers);
				}
				totalMap.get(helper).addAll(helpedUsers);
			}
			//reviewers
			for(User reviewer : t.getReviewers()) {
				
				reviewedUsers = reviewersMap.get(reviewer);
				if(reviewedUsers == null) {
					reviewedUsers = new HashSet<User>();
					reviewersMap.put(reviewer, reviewedUsers);
				}
				//give reviewer credit for working with owners
				for(User owner : t.getOwners()) {
					reviewedUsers.add(owner);
				}
				//give reviewer credit for working with other reviewers
				for(User otherReviewer : t.getReviewers()) {
					if(!reviewer.equals(otherReviewer)) {
						reviewedUsers.add(otherReviewer);
					}
				}
				//add reviewers to total map
				allUsers = totalMap.get(reviewer);
				if(allUsers == null) {
					allUsers = new HashSet<User>();
					totalMap.put(reviewer,  allUsers);
				}
				totalMap.get(reviewer).addAll(reviewedUsers);
			}
		}
		
		//create count maps
		for(User u : helpersMap.keySet()) {
			helperCountMap.put(u, helpersMap.get(u).size());
		}
		for(User u : reviewersMap.keySet()) {
			reviewerCountMap.put(u, reviewersMap.get(u).size());
		}
		for(User u : ownersMap.keySet()) {
			ownerCountMap.put(u, ownersMap.get(u).size());
		}
		for(User u : totalMap.keySet()) {
			totalCountMap.put(u, totalMap.get(u).size());
		}
		
		userTypeMaps.put("allRolesCollaborator", totalCountMap);
		userTypeMaps.put("ownerCollaborator", ownerCountMap);
		userTypeMaps.put("helperCollaborator", helperCountMap);
		userTypeMaps.put("reviewerCollaborator", reviewerCountMap);
		
		return userTypeMaps;
	}
	
	private Map<String, Map<User, Integer>> getProjectWorkByRole(Project p) {
		Map<String, Map<User, Integer>> userTypeMaps = new HashMap<String, Map<User, Integer>>();
		List<Work> projectWork = workDao.getProjectWork(p);
		Map<User, Integer> ownerMap = new HashMap<User, Integer>();
		Map<User, Integer> helperMap = new HashMap<User, Integer>();
		Map<User, Integer> reviewerMap = new HashMap<User, Integer>();
		Map<User, Integer> totalMap = new HashMap<User, Integer>();
		int workMinutes;
		Task workTask;
		User workUser;
		
		//for all project work
		for(Work w : projectWork) {
			workTask = w.getTask();
			workUser = w.getUser();
			workMinutes = w.getNumMinutes();
			
			if(Task.OWNER.equals(workTask.getUserType(workUser))) {
				if(!ownerMap.containsKey(workUser)) {
					ownerMap.put(workUser, workMinutes);
				} else {
					ownerMap.put(workUser, workMinutes + ownerMap.get(workUser));
				}
			} else if(Task.HELPER.equals(workTask.getUserType(workUser))) {
				if(!helperMap.containsKey(workUser)) {
					helperMap.put(workUser, workMinutes);
				} else {
					helperMap.put(workUser, workMinutes + helperMap.get(workUser));
				}
			} else if(Task.REVIEWER.equals(workTask.getUserType(workUser))) {
				if(!reviewerMap.containsKey(workUser)) {
					reviewerMap.put(workUser, workMinutes);
				} else {
					reviewerMap.put(workUser, workMinutes + reviewerMap.get(workUser));
				}
			}
			if(!totalMap.containsKey(workUser)) {
				totalMap.put(workUser, workMinutes);
			} else {
				totalMap.put(workUser, workMinutes + totalMap.get(workUser));
			}
		}
		
		userTypeMaps.put("allRoles", totalMap);
		userTypeMaps.put("owner", ownerMap);
		userTypeMaps.put("helper", helperMap);
		userTypeMaps.put("reviewer", reviewerMap);
		
		return userTypeMaps;
	}
	
	private Map<String, Map<User, Integer>> getCycleWorkByRole(Cycle s) {
		Map<String, Map<User, Integer>> userTypeMaps = new HashMap<String, Map<User, Integer>>();
		List<Work> cycleWork = workDao.getCycleWork(s.getBoard());
		Map<User, Integer> ownerMap = new HashMap<User, Integer>();
		Map<User, Integer> helperMap = new HashMap<User, Integer>();
		Map<User, Integer> reviewerMap = new HashMap<User, Integer>();
		Map<User, Integer> totalMap = new HashMap<User, Integer>();
		int workMinutes;
		Task workTask;
		User workUser;
		
		//for all project work
		for(Work w : cycleWork) {
			workTask = w.getTask();
			workUser = w.getUser();
			workMinutes = w.getNumMinutes();
			
			if(Task.OWNER.equals(workTask.getUserType(workUser))) {
				if(!ownerMap.containsKey(workUser)) {
					ownerMap.put(workUser, workMinutes);
				} else {
					ownerMap.put(workUser, workMinutes + ownerMap.get(workUser));
				}
			} else if(Task.HELPER.equals(workTask.getUserType(workUser))) {
				if(!helperMap.containsKey(workUser)) {
					helperMap.put(workUser, workMinutes);
				} else {
					helperMap.put(workUser, workMinutes + helperMap.get(workUser));
				}
			} else if(Task.REVIEWER.equals(workTask.getUserType(workUser))) {
				if(!reviewerMap.containsKey(workUser)) {
					reviewerMap.put(workUser, workMinutes);
				} else {
					reviewerMap.put(workUser, workMinutes + reviewerMap.get(workUser));
				}
			}
			if(!totalMap.containsKey(workUser)) {
				totalMap.put(workUser, workMinutes);
			} else {
				totalMap.put(workUser, workMinutes + totalMap.get(workUser));
			}
		}
		
		userTypeMaps.put("allRoles", totalMap);
		userTypeMaps.put("owner", ownerMap);
		userTypeMaps.put("helper", helperMap);
		userTypeMaps.put("reviewer", reviewerMap);
		
		return userTypeMaps;
	}
	
	private Map<String, Map<User, Integer>> getProjectWorkByTag(Project p) {
		Map<String, Map<User, Integer>> tagMaps = new HashMap<String, Map<User, Integer>>();
		List<Work> projectWork = workDao.getProjectWork(p);
		Map<User, Integer> researchMap = new HashMap<User, Integer>();
		Map<User, Integer> featureMap = new HashMap<User, Integer>();
		Map<User, Integer> testMap = new HashMap<User, Integer>();
		Map<User, Integer> bugFixMap = new HashMap<User, Integer>();
		Map<User, Integer> refactorMap = new HashMap<User, Integer>();
		Map<User, Integer> otherMap = new HashMap<User, Integer>();
		Map<User, Integer> totalMap = new HashMap<User, Integer>();
		int workMinutes;
		Task workTask;
		User workUser;
		
		//for all project work
		for(Work w : projectWork) {
			workTask = w.getTask();
			workUser = w.getUser();
			workMinutes = w.getNumMinutes();
			
			if(Task.RESEARCH.equals(workTask.getTag())) {
				if(!researchMap.containsKey(workUser)) {
					researchMap.put(workUser, workMinutes);
				} else {
					researchMap.put(workUser, workMinutes + researchMap.get(workUser));
				}
			} else if(Task.FEATURE.equals(workTask.getTag())) {
				if(!featureMap.containsKey(workUser)) {
					featureMap.put(workUser, workMinutes);
				} else {
					featureMap.put(workUser, workMinutes + featureMap.get(workUser));
				}
			} else if(Task.UNIT_TEST.equals(workTask.getTag())) {
				if(!testMap.containsKey(workUser)) {
					testMap.put(workUser, workMinutes);
				} else {
					testMap.put(workUser, workMinutes + testMap.get(workUser));
				}
			} else if(Task.BUG_FIX.equals(workTask.getTag())) {
				if(!bugFixMap.containsKey(workUser)) {
					bugFixMap.put(workUser, workMinutes);
				} else {
					bugFixMap.put(workUser, workMinutes + bugFixMap.get(workUser));
				}
			} else if(Task.REFACTOR.equals(workTask.getTag())) {
				if(!refactorMap.containsKey(workUser)) {
					refactorMap.put(workUser, workMinutes);
				} else {
					refactorMap.put(workUser, workMinutes + refactorMap.get(workUser));
				}
			} else if(Task.OTHER.equals(workTask.getTag())) {
				if(!otherMap.containsKey(workUser)) {
					otherMap.put(workUser, workMinutes);
				} else {
					otherMap.put(workUser, workMinutes + otherMap.get(workUser));
				}
			}
			if(!totalMap.containsKey(workUser)) {
				totalMap.put(workUser, workMinutes);
			} else {
				totalMap.put(workUser, workMinutes + totalMap.get(workUser));
			}
		}
		
		tagMaps.put("allTags", totalMap);
		tagMaps.put("research", researchMap);
		tagMaps.put("feature", featureMap);
		tagMaps.put("test", testMap);
		tagMaps.put("bugFix", bugFixMap);
		tagMaps.put("refactor", refactorMap);
		tagMaps.put("other", otherMap);
		
		return tagMaps;
	}
	
	private Map<String, Map<User, Integer>> getCycleWorkByTag(Cycle s) {
		Map<String, Map<User, Integer>> tagMaps = new HashMap<String, Map<User, Integer>>();
		List<Work> cycleWork = workDao.getCycleWork(s.getBoard());
		Map<User, Integer> researchMap = new HashMap<User, Integer>();
		Map<User, Integer> featureMap = new HashMap<User, Integer>();
		Map<User, Integer> testMap = new HashMap<User, Integer>();
		Map<User, Integer> bugFixMap = new HashMap<User, Integer>();
		Map<User, Integer> refactorMap = new HashMap<User, Integer>();
		Map<User, Integer> otherMap = new HashMap<User, Integer>();
		Map<User, Integer> totalMap = new HashMap<User, Integer>();
		int workMinutes;
		Task workTask;
		User workUser;
		
		//for all project work
		for(Work w : cycleWork) {
			workTask = w.getTask();
			workUser = w.getUser();
			workMinutes = w.getNumMinutes();
			
			if(Task.RESEARCH.equals(workTask.getTag())) {
				if(!researchMap.containsKey(workUser)) {
					researchMap.put(workUser, workMinutes);
				} else {
					researchMap.put(workUser, workMinutes + researchMap.get(workUser));
				}
			} else if(Task.FEATURE.equals(workTask.getTag())) {
				if(!featureMap.containsKey(workUser)) {
					featureMap.put(workUser, workMinutes);
				} else {
					featureMap.put(workUser, workMinutes + featureMap.get(workUser));
				}
			} else if(Task.UNIT_TEST.equals(workTask.getTag())) {
				if(!testMap.containsKey(workUser)) {
					testMap.put(workUser, workMinutes);
				} else {
					testMap.put(workUser, workMinutes + testMap.get(workUser));
				}
			} else if(Task.BUG_FIX.equals(workTask.getTag())) {
				if(!bugFixMap.containsKey(workUser)) {
					bugFixMap.put(workUser, workMinutes);
				} else {
					bugFixMap.put(workUser, workMinutes + bugFixMap.get(workUser));
				}
			} else if(Task.REFACTOR.equals(workTask.getTag())) {
				if(!refactorMap.containsKey(workUser)) {
					refactorMap.put(workUser, workMinutes);
				} else {
					refactorMap.put(workUser, workMinutes + refactorMap.get(workUser));
				}
			} else if(Task.OTHER.equals(workTask.getTag())) {
				if(!otherMap.containsKey(workUser)) {
					otherMap.put(workUser, workMinutes);
				} else {
					otherMap.put(workUser, workMinutes + otherMap.get(workUser));
				}
			}
			if(!totalMap.containsKey(workUser)) {
				totalMap.put(workUser, workMinutes);
			} else {
				totalMap.put(workUser, workMinutes + totalMap.get(workUser));
			}
		}
		
		tagMaps.put("allTags", totalMap);
		tagMaps.put("research", researchMap);
		tagMaps.put("feature", featureMap);
		tagMaps.put("test", testMap);
		tagMaps.put("bugFix", bugFixMap);
		tagMaps.put("refactor", refactorMap);
		tagMaps.put("other", otherMap);
		
		return tagMaps;
	}
	
	private void incorporateProjectWorkStats(Map<String, Map<User, Integer>> projectWorkMap, Map<String, Map<User, Integer>> allProjectWorkMap) {
		for(String userType : projectWorkMap.keySet()) {
			Map<User, Integer> projectUserTypeMap = projectWorkMap.get(userType);
			if(!allProjectWorkMap.containsKey(userType)) {
				allProjectWorkMap.put(userType, projectUserTypeMap);
			} else {
				Map<User, Integer> allProjectUserTypeMap = allProjectWorkMap.get(userType);
				for(User u : projectUserTypeMap.keySet()) {
					if(!allProjectUserTypeMap.containsKey(u)) {
						allProjectUserTypeMap.put(u, projectUserTypeMap.get(u));
					} else {
						allProjectUserTypeMap.put(u, projectUserTypeMap.get(u) + allProjectUserTypeMap.get(u));
					}
				}
			}
		}
	}
	
	private int convertDateToIndex(Date workDate, Date startDate) {
	    long dateDifference = workDate.getTime() - startDate.getTime();
	    return (int)Math.ceil(TimeUnit.DAYS.convert(dateDifference, TimeUnit.MILLISECONDS));
	}
	
	private void initializeDailyWork(ArrayList<Integer> dailyWork, int numDays) {
		for(int i=0; i<numDays; i++) {
			dailyWork.add(0);
		}
	}
}
