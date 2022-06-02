package coop.controller;

import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import coop.model.*;
import coop.model.repository.RepositoryHost;
import coop.model.repository.Commit;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import coop.dao.ProjectDao;
import coop.dao.CycleDao;
import coop.dao.UserDao;
import coop.dao.StatsDao;
import coop.model.Cycle;

@Controller
public class StatsController {
	private StatsDao workDao = new StatsDao();

	@ResponseBody
	@RequestMapping(value = "/work/stats/cycle/{cycleId}/{type}", method = RequestMethod.POST, produces = "application/json")
	public Map<Integer, Map<String, Integer>> getStatsForCycle(@PathVariable("cycleId") int cycleId, @PathVariable("type") String type, HttpServletRequest request) {
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
		}

		return null;
	}

	@ResponseBody
	@RequestMapping(value = "/work/stats/project/{projectId}/{type}", method = RequestMethod.POST, produces = "application/json")
	public Map<Integer, Map<String, Integer>> getStatsForProject(@PathVariable("projectId") int projectId, @PathVariable("type") String type, ModelMap model, HttpServletRequest request) {
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
		}

		return null;
	}

	@ResponseBody
	@RequestMapping(value = "/work/cycleWork/{cycleId}", method = RequestMethod.POST, produces = "application/json")
	public Map<Integer,List<Integer>> getCycleWorkTimeline(@PathVariable("cycleId") int cycleId, ModelMap model, HttpServletRequest request) {
		Map<Integer, List<Integer>> userDailyWork = new TreeMap<>();
		CycleDao cycleDao = new CycleDao();
		Cycle cycle = cycleDao.getCycle(cycleId);
		Board board = cycle.getBoard();

		User user = UserDao.getUserFromSession(request);
		if(user == null || !user.getProjects().contains(cycle.getProject())) {
			return null;
		}
		
		List<Work> work = workDao.getCycleWork(board);
		int numDaysInCycle = (int)Math.floor(TimeUnit.DAYS.convert(cycle.getEndDate().getTime() - cycle.getStartDate().getTime(), TimeUnit.MILLISECONDS)) + 1;
		
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
			int dailyWorkIndex = convertDateToIndex(w.getDate(),  cycle.getStartDate());
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

	private Map<Integer, Map<String, Integer>> getCycleWorkByRole(Cycle cycle) {
		Map<Integer, Map<String, Integer>> cycleWorkByRole = new HashMap<>();
		addCycleWorkByRole(cycle, cycleWorkByRole);
		return cycleWorkByRole;
	}

	//workByRole: {userId : {role : minutes}})
	private void addCycleWorkByRole(Cycle cycle, Map<Integer, Map<String, Integer>> workByRole) {
		List<Work> cycleWork = workDao.getCycleWork(cycle.getBoard());
		Map<String, Integer> roleMinutesMap;
		Integer mappedMinutes;
		int workMinutes;
		Task workTask;
		User workUser;
		String workRole;

		//for all cycle work
		for(Work w : cycleWork) {
			workTask = w.getTask();
			workUser = w.getUser();
			workRole = workTask.getUserType(workUser);
			workMinutes = w.getNumMinutes();

			//roleMinutesMap: {role : minutes} for given userId
			roleMinutesMap = workByRole.get(workUser.getId());
			if (roleMinutesMap == null) {
				roleMinutesMap = new HashMap<>();
				workByRole.put(workUser.getId(), roleMinutesMap);
			}
			mappedMinutes = roleMinutesMap.get(workRole);
			if (mappedMinutes == null) {
				mappedMinutes = 0;
				roleMinutesMap.put(workRole, mappedMinutes);
			}
			roleMinutesMap.put(workRole, mappedMinutes + workMinutes);
		}
	}
	
	private Map<Integer, Map<String, Integer>> getProjectWorkByRole(Project p) {
		Map<Integer, Map<String, Integer>> projectWorkByRole = new HashMap<>();
		for(Cycle cycle: p.getCycles()) {
			addCycleWorkByRole(cycle, projectWorkByRole);
		}
		return projectWorkByRole;
	}
	


	private void addCycleWorkByTag(Cycle s, Map<Integer, Map<String, Integer>> workByTag) {
		List<Work> cycleWork = workDao.getCycleWork(s.getBoard());
		Map<String, Integer> tagMinutesMap;
		Integer mappedMinutes;
		int workMinutes;
		Task workTask;
		User workUser;
		String workTag;

		//for all cycle work
		for(Work w : cycleWork) {
			workTask = w.getTask();
			workUser = w.getUser();
			workTag = workTask.getTag();
			workMinutes = w.getNumMinutes();

			tagMinutesMap = workByTag.get(workUser.getId());
			if (tagMinutesMap == null) {
				tagMinutesMap = new HashMap<>();
				workByTag.put(workUser.getId(), tagMinutesMap);
			}
			mappedMinutes = tagMinutesMap.get(workTag);
			if (mappedMinutes == null) {
				mappedMinutes = 0;
				tagMinutesMap.put(workTag, mappedMinutes);
			}
			tagMinutesMap.put(workTag, mappedMinutes + workMinutes);
		}
	}

	private Map<Integer, Map<String, Integer>> getCycleWorkByTag(Cycle cycle) {
		Map<Integer, Map<String, Integer>> cycleWorkByTag = new HashMap<>();
		addCycleWorkByTag(cycle, cycleWorkByTag);
		return cycleWorkByTag;
	}

	private Map<Integer, Map<String, Integer>> getProjectWorkByTag(Project p) {
		Map<Integer, Map<String, Integer>> projectWorkByTag = new HashMap<>();
		for(Cycle cycle: p.getCycles()) {
			addCycleWorkByTag(cycle, projectWorkByTag);
		}
		return projectWorkByTag;
	}

	public static Map<User, UserStats> generateCycleStats(Cycle cycle) {
		Map<User, UserStats> userStatsMap = new TreeMap<>();
		for(User cycleUser: cycle.getProject().getUsers()) {
			userStatsMap.put(cycleUser, new UserStats());
		}
		List<Task> cycleTasks = cycle.getBoard().getTasks();
		Set<Commit> userCommits = new HashSet<>();

		for(Task task: cycleTasks) {
			updateCommitStats(userStatsMap, userCommits, task);
			updateWorkStats(userStatsMap, task);
		}

		return userStatsMap;
	}

	public static Map<User, UserStats> generateProjectStats(Project project) {
		Map<User, UserStats> userStatsMap = new TreeMap<>();
		for(User projectUser: project.getUsers()) {
			userStatsMap.put(projectUser, new UserStats());
		}
		List<Cycle> cycles = new ArrayList<>(project.getCycles());
		Collections.reverse(cycles);
		for(Cycle cycle: cycles) {
			List<Task> cycleTasks = cycle.getBoard().getTasks();
			Set<Commit> userCommits = new HashSet<>();

			for(Task task: cycleTasks) {
				updateCommitStats(userStatsMap, userCommits, task);
				updateWorkStats(userStatsMap, task);
			}
		}

		return userStatsMap;
	}

	/*
	Commit-related notes
	A student's name *as recognized by git* must match that in Co-Op.
	*This can be configured on a project-by-project (repo-by-repo) basis*
	If a student pushes a commit, it should be recognized in a Co-Op task (within the identified branch).
	If that commit was merged into the team branch, that same commit should show up under that branch.
	 */
	private static void updateCommitStats(Map<User, UserStats> cycleStatsMap, Set<Commit> userCommits, Task task) {
		Cycle cycle = task.getBoard().getCycle();
		String teamBranchName = cycle.getCycleTeamBranchName();
		Project project = cycle.getProject();
		RepositoryHost projectRepository = project.getRepository();
		String taskBranch = task.getRepositoryProjectBranch();
		if(taskBranch != null && !taskBranch.isEmpty()) {
			for(Commit cycleCommit: projectRepository.retrieveCommitsFromRepository(project.getRepositoryProject(), taskBranch, cycle.getStartDate(), cycle.getEndDate())) {
				if (cycleCommit != null && !userCommits.contains(cycleCommit)) {
					User committer = project.getProjectUserWithName(cycleCommit.getCommitterName());
					if (committer != null) {
						UserStats userStats = cycleStatsMap.get(committer);
						userStats.addCommit(cycleCommit);
						if (teamBranchName != null && teamBranchName.equals(taskBranch)) {
							userStats.addMergedCommit(cycleCommit);
						}
					}
					userCommits.add(cycleCommit);
				}
			}
		}
	}

	private static void updateWorkStats(Map<User, UserStats> cycleStatsMap, Task task) {
		UserStats userStats;
		User user;
		String userType;
		String tagType;
		for(Work work: task.getWork()) {
			user = work.getUser();
			userStats = cycleStatsMap.get(work.getUser());
			userType = task.getUserType(user);
			tagType = task.getTag();
			if(Task.OWNER.equals(userType)) {
				userStats.addWorkMinutesReportedAsOwner(work.getNumMinutes());
			} else if(Task.HELPER.equals(userType)) {
				userStats.addWorkMinutesReportedAsHelper(work.getNumMinutes());
			} else if(Task.REVIEWER.equals(userType)) {
				userStats.addWorkMinutesReportedAsReviewer(work.getNumMinutes());
			}
			if(Task.RESEARCH.equals(tagType)) {
				userStats.addWorkMinutesForResearch(work.getNumMinutes());
			} else if(Task.FEATURE.equals(tagType)) {
				userStats.addWorkMinutesForFeature(work.getNumMinutes());
			} else if(Task.BUG_FIX.equals(tagType)) {
				userStats.addWorkMinutesForBugFix(work.getNumMinutes());
			} else if(Task.UNIT_TEST.equals(tagType)) {
				userStats.addWorkMinutesForUnitTest(work.getNumMinutes());
			} else if(Task.REFACTOR.equals(tagType)) {
				userStats.addWorkMinutesForRefactor(work.getNumMinutes());
			} else if(Task.OTHER.equals(tagType)) {
				userStats.addWorkMinutesForOther(work.getNumMinutes());
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
