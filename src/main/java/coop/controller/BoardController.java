package coop.controller;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import coop.dao.*;
import coop.model.*;
import coop.model.repository.RepositoryHost;
import coop.model.repository.RepositoryProject;
import coop.model.repository.Commit;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import coop.dao.BoardDao;
import coop.util.CoOpUtil;
import coop.util.SlackUtil;

@Controller
public class BoardController {
	private static final String folder = "board/";
	
	@RequestMapping(value = "/board/{id}", method = RequestMethod.GET)
	public String showViewPage(@PathVariable("id") String rawId, ModelMap model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
		if (UserDao.loggedIn(request)) {
			Board board = null;

			try {
				int id = Integer.parseInt(rawId);
				BoardDao dao = new BoardDao();
				board = dao.find(id);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// checks if board exists
			if (board == null) {
				redirectAttributes.addFlashAttribute("error", "Invalid board");
				return "redirect:/user/"+UserDao.getUserIDFromSession(request);
			}

			// checks to see if the board belongs to a project only if it doesn't belong to a cycle
			if (board.getCycle() != null) {
				model.addAttribute("CycleNum", CoOpUtil.getCycleNumber(board.getCycle()));
			}
			
			// check to see if the user belongs to the project
			if (!userHasProjectAccess(board.getProject(), request)) {
				/*
				redirectAttributes.addFlashAttribute("error", "You do not have access to that board");
				return "redirect:/user/"+UserDao.getUserIDFromSession(request);
				*/
				model.addAttribute("isMember", false);
			} else {
                model.addAttribute("isMember", true);
            }
			Project project = board.getProject();
			RepositoryProject repositoryProject = project.getRepositoryProject();
			RepositoryHost repositoryHost = repositoryProject.getRepositoryHost();
			model.addAttribute("isCycle", board.getCycle() != null);
			model.addAttribute("cycles", project.getCycles());
			model.addAttribute("board", board);
			model.addAttribute("statuses", Task.TASK_STATUSES);
			model.addAttribute("notStartedStatus", Task.NOT_STARTED);
			model.addAttribute("projectUsers", project.getUsers());
			model.addAttribute("projectId", project.getId());
			model.addAttribute("repositoryProjectId", repositoryProject.getId());
			model.addAttribute("repositoryProjectBranches", repositoryHost.retrieveBranchesFromRepository(repositoryProject));
			model.addAttribute("taskRoles", Task.TASK_ROLES);

			if(board.getCycle() != null) {
				model.addAttribute("cycleId", board.getCycle().getId());
			}
			return folder + "board";
			
		} else {
			redirectAttributes.addFlashAttribute("error", "You must be logged in to view that page");
			return "redirect:/login";
		}
	}

	@ResponseBody
	@RequestMapping(value = "/board/queryTasks/{id}", method = RequestMethod.POST, produces = "application/json")
	public String queryTasksResponder(@PathVariable("id") String rawBoardId, ModelMap model, HttpServletRequest request) {
		int boardId = Integer.parseInt(rawBoardId);
		Board board = new BoardDao().find(boardId);
		ObjectMapper mapper = new ObjectMapper();
		String result = "";
		
		// check to see if the user belongs to the project
		if (!UserDao.loggedIn(request) /*|| !userHasProjectAccess(board.getProject(), request)*/) {
			return result;
		}
		
		try {
			List<Task> tasks = board.getTasks();
			List<Task> sortedTasks = tasks.stream().sorted((t1, t2) -> {
				if (t1 == null || t2 == null) {
					return 0;
				}
				if (t1.getPriority() == null || t2.getPriority() == null) {
					return t1.getTaskId().compareTo(t2.getTaskId());
				}
				int blah = t1.getPriority().compareTo(t2.getPriority());
				if (blah == 0) {
					return t1.getTaskId().compareTo(t2.getTaskId());
				} else {
					return blah;
				}
			}).collect(Collectors.toList());
			result = mapper.writeValueAsString(sortedTasks);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return result;
	}

	@RequestMapping(value = "/board/updateStatus/{id}", method = RequestMethod.POST)
	public void updateStatusResponder(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") String rawTaskId, @RequestParam(value = "status", required = true) String newStatus, @RequestParam(value = "priority", required = true) String rawPriority) {
		response.setStatus(HttpServletResponse.SC_OK);
		int taskId = Integer.parseInt(rawTaskId);
		int newPriority = Integer.parseInt(rawPriority);
		TaskDao dao = new TaskDao();
		Task task = dao.getTask(taskId);
		BoardDao bDao = new BoardDao();
		Board board = bDao.find(task.getBoard().getBoardId());
		List<Task> tasks = board.getTasks();
		String oldStatus = task.getStatus();
		int oldPriority = task.getPriority();
		String missingFields = "";
		
		// check to see if the user belongs to the project
		if (!UserDao.loggedIn(request) || !userHasProjectAccess(board.getProject(), request)) {
			return;
		}
				
		//prevent updates to inactive boards
		if(!board.isActive()) {
			return;
		}
		
		//if status changed
		if(!oldStatus.equals(newStatus)) {
			//enforce fields (if appropriate)
			if(!Task.NOT_STARTED.equals(newStatus) && !Task.IMPACT_ANALYSIS.equals(newStatus)) {
				//coding task
				if(task.isCodingTask()) {
					if(task.getTag() == null || task.getRepositoryProjectBranch().isEmpty() || task.getTimeEstimate() == 0 || task.getCompletionDateEst() == null || task.getOwners().isEmpty()) {
						if(task.getTag() == null) {
							missingFields += "\nTag";
						}
						if(task.getRepositoryProjectBranch().isEmpty()) {
							missingFields += "\nBranch";
						}
						if(task.getTimeEstimate() == 0) {
							missingFields += "\nTime Estimate";
						}
						if(task.getCompletionDateEst() == null) {
							missingFields += "\nEstimated Completion Date";
						}
						if(task.getOwners().isEmpty()) {
							missingFields += "\nOwner";
						}
					}
				}
				//non-coding task
				else if(!task.isCodingTask()) {
					if(task.getTag() == null) {
						missingFields += "\nTag";
					}
					if(task.getTimeEstimate() == 0) {
						missingFields += "\nTime Estimate";
					}
					if(task.getCompletionDateEst() == null) {
						missingFields += "\nEstimated Completion Date";
					}
					if(task.getOwners().isEmpty()) {
						missingFields += "\nOwner";
					}
				}
			}
			//enforce impact analysis (if appropriate)
			if(task.isCodingTask() && !Task.IMPACT_ANALYSIS.equals(newStatus) && !Task.NOT_STARTED.equals(newStatus)) {
				if(task.getImpactedFiles() == null || task.getImpactedFiles().isEmpty()) {
					missingFields += "\nImpacted files must be identified";
				}
			}
			//enforce commit (if appropriate)
            if(Task.COMPLETED.equals(newStatus) && task.isCodingTask()) {
                if(!impactedFilesMatchRepository(task)) {
					missingFields += "\nImpacted files must match repository";
				}
            }

			//enforce review (if appropriate)
			if(Task.COMPLETED.equals(newStatus) && task.isCodingTask()) {
				boolean wasReviewed = false;
				for(Work w : task.getWork()) {
					if(task.getReviewers().contains(w.getUser())) {
						wasReviewed = true;
						break;
					}
				}
				if(!wasReviewed) {
					missingFields += "\nA reviewer must report work for this task";
				}
			}

			//enforce work reported by owner (if appropriate)
			if(Task.COMPLETED.equals(newStatus)) {
				boolean wasReviewed = false;
				for(Work w : task.getWork()) {
					if(task.getOwners().contains(w.getUser())) {
						wasReviewed = true;
						break;
					}
				}
				if(!wasReviewed) {
					missingFields += "\nAn owner must report work for this task";
				}
			}

            if(!missingFields.isEmpty()) {
				try {
					response.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
					response.getOutputStream().println("Required before task can be completed:\n"+missingFields);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			} else {
				for (Task t : tasks) {
					//decrease priority value of tasks from old status with previously higher values
					if (oldStatus.equals(t.getStatus()) && oldPriority < t.getPriority()) {
						t.setPriority(t.getPriority() - 1);
						dao.updateTask(t);
					}
					//increase priority value of tasks from new status with previously higher values
					else if (newStatus.equals(t.getStatus()) && newPriority <= t.getPriority()) {
						t.setPriority(t.getPriority() + 1);
						dao.updateTask(t);
					}
				}
			}
		}
		//if status stayed the same
		else {
			for (Task t : tasks) {
				if(newStatus.equals(t.getStatus())) {
					int otherTaskPriority = t.getPriority();
					//if dropped task was moved up
					if(oldPriority > newPriority) {
						//if other task was above it but now is below it
						if(otherTaskPriority < oldPriority && otherTaskPriority >= newPriority) {
							//increase the other task's priority
							int updatedOtherTaskPriority = ((otherTaskPriority+1)<tasks.size())?otherTaskPriority+1:tasks.size();
							t.setPriority(updatedOtherTaskPriority);
							dao.updateTask(t);
						}
					}
					//if dropped task was moved down
					else if(oldPriority < newPriority){
						//if other task was below it but now is above it
						if(otherTaskPriority > oldPriority && otherTaskPriority <= newPriority) {
							t.setPriority(t.getPriority()-1);
							dao.updateTask(t);
						}
					}
				}
			}
		}

		//update task
		task.setStatus(newStatus);
		task.setPriority(newPriority);
		if(dao.updateTask(task)) {
			TaskHistory taskHistory = new TaskHistory();
			//if status changed
			if(!oldStatus.equals(newStatus)) {
			    addTaskChange(taskHistory, "Status", oldStatus, newStatus);
			} else {
				addTaskChange(taskHistory, "Priority", ""+oldPriority, ""+newPriority);
			}
			insertTaskHistory(taskHistory, task, UserDao.getUserFromSession(request));
		}
		
		//update user session
		CoOpUtil.updateUserSession(request);
	}
	
	@RequestMapping(value = "/board/updateTaskDetails/{taskId}", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public void updateTaskDetailsResponder(HttpServletRequest request, @RequestParam Map<String,String> allRequestParams, @PathVariable("taskId") String taskIdStr) {
		
		boolean foundChange = false;
		int taskId = Integer.parseInt(taskIdStr);
		TaskDao taskDao = new TaskDao();
		Task task = taskDao.getTask(taskId);
		TaskHistory taskHistory = new TaskHistory();
		
		// check to see if the user belongs to the project
		if (!UserDao.loggedIn(request) || !userHasProjectAccess(task.getProject(), request)) {
			return;
		}
				
		//prevent updates to inactive boards
		if(!task.getBoard().isActive()) {
			return;
		}
		
		//update task description (if changed)
		String currentDescription = task.getDescription();
		String newDescription = CoOpUtil.sanitizeText(allRequestParams.get("description"));
	    if(newDescription != null && !newDescription.equals(currentDescription)) {
	    	foundChange = true;
	    	task.setDescription(newDescription);
	    	addTaskChange(taskHistory, "Description", currentDescription, newDescription);
	    }
	    
	    //update tag (if changed)
	    String currentTag = task.getTag();
	    String newTag = allRequestParams.get("tag");
	    if(newTag != null && !newTag.isEmpty() && !newTag.equals(currentTag)) {
	    	foundChange = true;
	    	task.setTag(newTag);
	    	addTaskChange(taskHistory, "Tag", currentTag, newTag);
	    }

		//update status (if changed)
		String currentStatus = task.getStatus();
		String newStatus = allRequestParams.get("newStatus");
		if(newStatus != null && !newStatus.isEmpty() && !newStatus.equals(currentStatus)) {
			foundChange = true;
			task.setStatus(newStatus);
			addTaskChange(taskHistory, "Status", currentStatus, newStatus);
		}
	    
	    //update branch (if changed)
	    String currentBranch = task.getRepositoryProjectBranch();
	    String newBranch = allRequestParams.get("branch");
	    if(newBranch != null && !newBranch.equals(currentBranch)) {
	    	foundChange = true;
	    	task.setRepositoryProjectBranch(newBranch);
	    	addTaskChange(taskHistory, "Branch", currentBranch, newBranch);
	    }
	    
	    //update timeEstimate (if changed)
	    Double currentTimeEstimate = task.getTimeEstimate();
	    String newTimeEstimate = allRequestParams.get("timeEstimate");
	    if(newTimeEstimate != null) {
	    	try {
	        	double newTimeEstimateDouble = Double.parseDouble(newTimeEstimate);
	        	if(!currentTimeEstimate.equals(newTimeEstimateDouble)) {
	        		foundChange = true;
	            	task.setTimeEstimate(newTimeEstimateDouble);
	            	addTaskChange(taskHistory, "Time Estimate", ""+currentTimeEstimate, newTimeEstimate);
	            }
	        } catch(NumberFormatException e) {
	        	System.out.println(e.getStackTrace());
	        }
	    }
	    
	    //update completionDateEst (if changed)
	    String fmtCompletionDateEst = task.getFmtCompletionDateEst();
	    String newFmtCompletionDateEst = allRequestParams.get("fmtCompletionDateEst");
    	try {
        	if(!fmtCompletionDateEst.equals(newFmtCompletionDateEst)) {
        		foundChange = true;
        		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        		Date newCompletionDateEst = null;
        		if(newFmtCompletionDateEst != null && !newFmtCompletionDateEst.isEmpty()) {
        			newCompletionDateEst = dateFormat.parse(newFmtCompletionDateEst);
        		}
            	task.setCompletionDateEst(newCompletionDateEst);
            	addTaskChange(taskHistory, "Estimated completion", ""+fmtCompletionDateEst, newFmtCompletionDateEst);
            }
        } catch(ParseException e) {
        	System.out.println(e.getStackTrace());
        }
	    
	    //update board (if changed, only for product board submission)
	    Board currentBoard = task.getBoard();
	    String newBoardId = allRequestParams.get("boardId");
	    if(newBoardId != null && !newBoardId.isEmpty()) {
	    	BoardDao boardDao = new BoardDao();
			Board newBoard = boardDao.find(Integer.parseInt(newBoardId));
			
			if(!currentBoard.equals(newBoard)) {
				currentBoard.removeTask(task);
				task.addBoard(newBoard);
				newBoard.addTask(task);
				initTaskPriority(task);
				boardDao.update(currentBoard);
				boardDao.update(newBoard);
				foundChange = true;
				addTaskChange(taskHistory, "Board", currentBoard.toString(), newBoard.toString());
			}
	    }
		
	    if(foundChange) {	
			//store updates
			if(taskDao.updateTask(task)) {
		    	insertTaskHistory(taskHistory, task, UserDao.getUserFromSession(request));
			}

			//update user session
			CoOpUtil.updateUserSession(request);
	    }
	}

	@RequestMapping(value = "/board/copyTaskToNextCycle/{taskId}", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public void copyTaskToNextCycleResponder(HttpServletRequest request, @PathVariable("taskId") String taskIdStr) {
		int taskId = Integer.parseInt(taskIdStr);
		TaskDao taskDao = new TaskDao();
		Task task = taskDao.getTask(taskId);
		TaskHistory taskHistory = new TaskHistory();

		// check to see if the user belongs to the project
		if (!UserDao.loggedIn(request) || !userHasProjectAccess(task.getProject(), request)) {
			return;
		}
		//prevent cycle change to task on active board
		if(task.getBoard().isActive()) {
			return;
		}

		Task taskCopy = new Task(task);
		List<Cycle> projectCycles = task.getProject().getCycles();
		Cycle cycle;
		Board newBoard = null;
		boolean isNextCycle = false;
		for(int i=0; i<projectCycles.size(); i++) {
			cycle = projectCycles.get(i);
			if(isNextCycle) {
				newBoard = cycle.getBoard();
				taskCopy.addBoard(newBoard);
				newBoard.addTask(taskCopy);
				initTaskPriority(taskCopy);

				if(taskDao.insertTask(taskCopy)) {
					addTaskChange(taskHistory, "Cycle", ""+i, ""+(i+1));
					insertTaskHistory(taskHistory, taskCopy, UserDao.getUserFromSession(request));
				}
				break;
			} else if(task.getBoard().getCycle().equals(cycle)) {
				isNextCycle = true;
			}
		}

	}

	@RequestMapping(value = "/board/updateTaskUsers/{taskId}", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public void updateTaskUsersResponder(HttpServletRequest request, @RequestParam Map<String,String> allRequestParams, @PathVariable("taskId") String taskIdStr) {
		
		boolean foundChange = false;
		int taskId = Integer.parseInt(taskIdStr);
		TaskDao taskDao = new TaskDao();
		UserDao userDao = new UserDao();
		Task task = taskDao.getTask(taskId);
		TaskHistory taskHistory = new TaskHistory();
		
		// check to see if the user belongs to the project
		if (!UserDao.loggedIn(request) || !userHasProjectAccess(task.getProject(), request)) {
			return;
		}
				
		//prevent updates to inactive boards
		if(!task.getBoard().isActive()) {
			return;
		}
		
	    //update users (if changed)
	    //save existing user lists
	    String currentOwners = task.getOwners().toString();
	    String currentHelpers = task.getHelpers().toString();
	    String currentReviewers = task.getReviewers().toString();
	    
	    //iterate through request parameters looking for those that start with "user-"
	    Set<String> allParamNames = allRequestParams.keySet();
	    Iterator<String> itr = allParamNames.iterator();
	    task.clearUsers();
	    while(itr.hasNext()) {
	    	String paramName = itr.next();
	    	if(paramName.startsWith("users-")) {
	    		int hyphenInd = paramName.lastIndexOf("-");
				Integer userId = Integer.parseInt(paramName.substring(hyphenInd+1));
				String role = allRequestParams.get(paramName);
				task.addUser(userDao.getUser(userId), role);
	    	}
	    }
	    	
		//see if user lists have changed for task history generation
    	String updatedOwners = task.getOwners().toString();
    	if(!currentOwners.equals(updatedOwners)) {
    		foundChange = true;
    		addTaskChange(taskHistory, "Owners", currentOwners, updatedOwners);
    	}
    	String updatedHelpers = task.getHelpers().toString();
    	if(!currentHelpers.equals(updatedHelpers)) {
    		foundChange = true;
    		addTaskChange(taskHistory, "Helpers", currentHelpers, updatedHelpers);
    	}
    	String updatedReviewers = task.getReviewers().toString();
    	if(!currentReviewers.equals(updatedReviewers)) {
    		foundChange = true;
    		addTaskChange(taskHistory, "Reviewers", currentReviewers, updatedReviewers);
    	} 
		
	    if(foundChange) {	
			//store updates
			if(taskDao.updateTask(task)) {
				insertTaskHistory(taskHistory, task, UserDao.getUserFromSession(request));
			}
			//update user session
			CoOpUtil.updateUserSession(request);
	    }
	}

	@ResponseBody
	@RequestMapping(value = "/board/addTaskImpactedFiles/{taskId}", method = RequestMethod.POST, produces = "application/json")
	public String addTaskImpactedFilesResponder(HttpServletRequest request, @RequestParam(value = "impactedFilePaths", required = true) String impactedFilePaths, @RequestParam(value = "branch", required = true) String branch, @PathVariable("taskId") String taskIdStr) {
		String result = "";
		ObjectMapper mapper = new ObjectMapper();
		boolean foundChange = false;
		int taskId = Integer.parseInt(taskIdStr);
		TaskDao taskDao = new TaskDao();
		Task task = taskDao.getTask(taskId);
		TaskHistory taskHistory = new TaskHistory();

		// check to see if the user belongs to the project
		if (!UserDao.loggedIn(request) || !userHasProjectAccess(task.getProject(), request)) {
			return result;
		}

		//prevent updates to inactive boards
		if(!task.getBoard().isActive()) {
			return result;
		}

		//save existing impacted files
		List<ImpactedProjectFile> currentlyImpactedFiles = new ArrayList<>(task.getImpactedFiles());
		try {
			String[] impactedFilePathArray = mapper.readValue(impactedFilePaths, String[].class);
			for(String impactedFilePath: impactedFilePathArray) {
				ImpactedProjectFile impactedProjectFile = taskDao.getOrInsertImpactedProjectFile(impactedFilePath, branch);
				if(impactedProjectFile != null && !task.getImpactedFiles().contains(impactedProjectFile)) {
					task.getImpactedFiles().add(impactedProjectFile);
					foundChange = true;
				}
			}
			if(foundChange) {
				if(taskDao.updateTask(task)) {
					//add task history
					addTaskChange(taskHistory, "impacted files", currentlyImpactedFiles.toString(), task.getImpactedFiles().toString());
					insertTaskHistory(taskHistory, task, UserDao.getUserFromSession(request));
				}
				//update user session
				CoOpUtil.updateUserSession(request);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			result = mapper.writeValueAsString(task);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return result;
	}

	@ResponseBody
	@RequestMapping(value = "/board/deleteImpactItem/{taskId}/{impactedProjectFileId}", method = RequestMethod.POST, produces = "application/json")
	public String deleteImpactItem(HttpServletRequest request, @PathVariable("taskId") String taskIdStr, @PathVariable("impactedProjectFileId") String impactedProjectFileIdStr) {
		ObjectMapper mapper = new ObjectMapper();
		String result = "";
		ImpactedProjectFile impactedFile;

		int taskId = Integer.parseInt(taskIdStr);
		int impactedProjectFileId = Integer.parseInt(impactedProjectFileIdStr);
		TaskDao taskDao = new TaskDao();
		Task task = taskDao.getTask(taskId);
		TaskHistory taskHistory = new TaskHistory();

		try {
			// check to see if the user belongs to the project
			if (!UserDao.loggedIn(request) || !userHasProjectAccess(task.getProject(), request)) {
				return result;
			}

			//prevent updates to inactive boards
			if(!task.getBoard().isActive()) {
				return result;
			}

			ArrayList<ImpactedProjectFile> currentlyImpactedFiles = new ArrayList<>(task.getImpactedFiles());
			for (Iterator<ImpactedProjectFile> it = task.getImpactedFiles().iterator(); it.hasNext(); ) {
				impactedFile = it.next();
				if(impactedFile.getImpactedProjectFileId().equals(impactedProjectFileId)) {
					it.remove();
				}
			}
			if(taskDao.updateTask(task)) {
				//add task history
				addTaskChange(taskHistory, "impacted files", currentlyImpactedFiles.toString(), task.getImpactedFiles().toString());
				insertTaskHistory(taskHistory, task, UserDao.getUserFromSession(request));
				deleteImpactedFileOrphans(currentlyImpactedFiles, task.getImpactedFiles());
			}
			try {
				result = mapper.writeValueAsString(task);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		} catch(NumberFormatException nfe) {
			nfe.printStackTrace();
		}
		return result;
	}

	@ResponseBody
	@RequestMapping(value = "/board/showImpactItemUsers/{taskId}/{impactedProjectFileId}", method = RequestMethod.POST, produces = "application/json")
	public String loadImpactItemUsers(HttpServletRequest request, @PathVariable("taskId") String taskIdStr, @PathVariable("impactedProjectFileId") String impactedProjectFileIdStr) {
		ObjectMapper mapper = new ObjectMapper();
		String result = "";

		int taskId = Integer.parseInt(taskIdStr);
		int impactedProjectFileId = Integer.parseInt(impactedProjectFileIdStr);
		TaskDao taskDao = new TaskDao();
		Task task = taskDao.getTask(taskId);
		ImpactedProjectFile impactedProjectFile = taskDao.getImpactedProjectFile(impactedProjectFileId);

		try {
			// check to see if the user belongs to the project
			if (!UserDao.loggedIn(request) || !userHasProjectAccess(task.getProject(), request)) {
				return result;
			}

			//prevent updates to inactive boards
			if(!task.getBoard().isActive()) {
				return result;
			}
			Map<String, Set<String>> impactedProjectFileUsers = taskDao.getImpactedFileUsers(impactedProjectFile.getPath());
			try {
				result = mapper.writeValueAsString(impactedProjectFileUsers);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		} catch(NumberFormatException nfe) {
			nfe.printStackTrace();
		}
		return result;
	}
	
	@RequestMapping(value = "/board/deleteTask/{taskId}", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public void deleteTaskResponder(HttpServletRequest request, @PathVariable("taskId") String taskIdStr) {
		int taskId = Integer.parseInt(taskIdStr);
		TaskDao taskDao = new TaskDao();
		Task t = taskDao.getTask(taskId);
		int deletedTaskPriority = t.getPriority();
		List<Task> otherTasks = t.getBoard().getTasks();

		// check to see if the user belongs to the project
		if (!UserDao.loggedIn(request) || !userHasProjectAccess(t.getProject(), request)) {
			return;
		}
				
		//prevent updates to inactive boards
		if(!t.getBoard().isActive()) {
			return;
		}
				
		if(!taskDao.deleteTask(t)) {
			System.out.println("Error deleting task id " + taskId);
		} else {
			//update task priorities
			for(Task otherTask: otherTasks) {
				int otherTaskPriority = otherTask.getPriority();
				if(otherTaskPriority > deletedTaskPriority) {
					otherTask.setPriority(otherTaskPriority-1);
					taskDao.updateTask(otherTask);
				}
			}
		}
		
		//update user session
		CoOpUtil.updateUserSession(request);
	}

	@RequestMapping(value = "/board/addTask/{id}", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public void addTask(@PathVariable("id") String rawId, @RequestParam(value = "description", required = true) String description, HttpServletRequest request) {
		int id = Integer.parseInt(rawId);
		BoardDao dao = new BoardDao();
		Board board = dao.find(id);
		TaskDao tdao = new TaskDao();

		// check to see if the user belongs to the project
		if (!UserDao.loggedIn(request) || !userHasProjectAccess(board.getProject(), request)) {
			return;
		}
				
		//prevent updates to inactive boards
		if(!board.isActive()) {
			return;
		}
				
		Task temp = new Task();
		temp.setDescription(CoOpUtil.sanitizeText(description));
		temp.setStatus(Task.NOT_STARTED);
		temp.addBoard(board);
		board.addTask(temp);
		initTaskPriority(temp); //initial task priority is set in this method call
		
		tdao.insertTask(temp);
	}
	
	@ResponseBody
	@RequestMapping(value = "/board/addNote/{taskId}", method = RequestMethod.POST, produces = "application/json")
	public String addNote(@PathVariable("taskId") String rawTaskId, @RequestParam(value = "noteText", required = true) String text, HttpServletRequest request) {
		int id = Integer.parseInt(rawTaskId);
		TaskDao tdao = new TaskDao();
		Task t = tdao.getTask(id);
		User u = UserDao.getUserFromSession(request);
		text = CoOpUtil.sanitizeText(text);
		ObjectMapper mapper = new ObjectMapper();
		String result = "";
		
		// check to see if the user belongs to the project
		if (!UserDao.loggedIn(request) || !userHasProjectAccess(t.getProject(), request)) {
			return result;
		}
				
		//prevent updates to inactive boards
		if(!t.getBoard().isActive()) {
			return result;
		}
		
		Note n = new Note(t, u, text);
		t.addNote(n);
		if(tdao.updateTask(t)) {
			//add task history
			TaskHistory taskHistory = new TaskHistory();
			addTaskChange(taskHistory, "notes", "", text);
			insertTaskHistory(taskHistory, t, u);
			
			//update user session
			CoOpUtil.updateUserSession(request);
		}
		try {
			result = mapper.writeValueAsString(t);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	@ResponseBody
	@RequestMapping(value = "/board/addWork/{taskId}", method = RequestMethod.POST, produces = "application/json")
	public String addWork(HttpServletRequest request,  
			@PathVariable("taskId") String taskIdStr,
			@RequestParam(value = "description", required = false) String description,
			@RequestParam(value = "minutes", required = true) String minutesStr) {
		
		int taskId = Integer.parseInt(taskIdStr);
		int minutes = Integer.parseInt(minutesStr);
		StatsDao wdao = new StatsDao();
		TaskDao tdao = new TaskDao();
		ObjectMapper mapper = new ObjectMapper();
		String result = "";
		Task t = tdao.getTask(taskId);
		User u = UserDao.getUserFromSession(request);
		
		// check to see if the user belongs to the project
		if (!UserDao.loggedIn(request) || !userHasProjectAccess(t.getProject(), request)) {
			return result;
		}
				
		//prevent updates to inactive boards
		if(!t.getBoard().isActive()) {
			return result;
		}
				
		Work w = new Work(t, u, CoOpUtil.sanitizeText(description), minutes, new Date());
		t.addWork(w);
		wdao.insertWork(w);
		
		//update user session
		CoOpUtil.updateUserSession(request);
		
		try {
			result = mapper.writeValueAsString(t);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	@ResponseBody
	@RequestMapping(value = "/board/deleteWork/{id}", method = RequestMethod.POST, produces = "application/json")
	public String deleteWork(HttpServletRequest request, @PathVariable("id") String workIdStr) {
		ObjectMapper mapper = new ObjectMapper();
		String result = "";
		
		try {
			int workId = Integer.parseInt(workIdStr);
			StatsDao wdao = new StatsDao();
			TaskDao tdao = new TaskDao();
			Work w = wdao.getWork(workId);
			Task t = w.getTask();
			
			// check to see if the user belongs to the project
			if (!UserDao.loggedIn(request) || !userHasProjectAccess(t.getProject(), request)) {
				return result;
			}
			
			//prevent updates to inactive boards
			if(!t.getBoard().isActive()) {
				return result;
			}
			
			if(w.getUser().equals(UserDao.getUserFromSession(request))) {
				t.removeWork(w);
				tdao.updateTask(t);
				wdao.deleteWork(w);
				//update user session
				CoOpUtil.updateUserSession(request);
			}
			try {
				result = mapper.writeValueAsString(t);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		} catch(NumberFormatException nfe) {
			nfe.printStackTrace();
		}
		return result;
	}
	
	private void addTaskChange(TaskHistory taskHistory, String field, String oldValue, String updatedValue) {
		  TaskChange taskChange = new TaskChange(field, oldValue, updatedValue);
		  taskHistory.getChangedValueList().add(taskChange);
	}
	
	private boolean insertTaskHistory(TaskHistory taskHistory, Task task, User user) {
		TaskHistoryDao taskHistoryDao = new TaskHistoryDao();
		Project project = task.getProject();
    	taskHistory.setTask(task);
    	taskHistory.setChangedByUser(user);
		if(taskHistory.isStatusChange() && project.getSlackWorkspace() != null) {
			SlackUtil.postToChannel(taskHistory, taskHistory.getTask().getProject().getSlackWorkspace());
		}
    	return taskHistoryDao.insertTaskHistory(taskHistory);
	}
	
	// this method initializes the priority of a task being created to the lowest
	// priority on the "Not Started Column"
	private void initTaskPriority(Task task) {
		BoardDao bDao = new BoardDao();

		Board board = bDao.find(task.getBoard().getBoardId());
		List<Task> tasks = board.getTasks();

		int highestPriorityValue = 0;
		for (Task t : tasks) {
			if (t.getStatus().equals(Task.NOT_STARTED) && t.getPriority() > highestPriorityValue) {
				highestPriorityValue = t.getPriority();
			}
		}
		task.setPriority(highestPriorityValue+1);
	}

	private void deleteImpactedFileOrphans(List<ImpactedProjectFile> oldImpactedFiles, List<ImpactedProjectFile> newImpactedFiles) {
		TaskDao dao = new TaskDao();
		oldImpactedFiles.removeAll(newImpactedFiles);
		for(ImpactedProjectFile oldImpactedFile : oldImpactedFiles) {
			dao.removeImpactedFileIfOrphan(oldImpactedFile);
		}
	}

	private boolean userHasProjectAccess(Project project, HttpServletRequest request) {
		User user = UserDao.getUserFromSession(request);
		return project != null && user.getIsActive() == 1 && project.getUsers().contains(user);
	}

	private boolean impactedFilesMatchRepository(Task task) {
		Project project = task.getProject();
		Cycle cycle = task.getBoard().getCycle();
		RepositoryProject repositoryProject = project.getRepositoryProject();
		RepositoryHost repositoryHost = repositoryProject.getRepositoryHost();

		List<ImpactedProjectFile> impactedProjectFiles = task.getImpactedFiles();
		//make copy of task's repository commits to modify when determining impacted files match
		List<Commit> cycleCommits = repositoryHost.retrieveCommitsFromRepository(repositoryProject, task.getRepositoryProjectBranch(), cycle.getStartDate(), cycle.getEndDate());
		List<String> impactedProjectFilePaths = new ArrayList<String>();
		List<String> impactedProjectFilePathsCopy = new ArrayList<String>();

		//generate list of impactedProjectFilePaths
		for (ImpactedProjectFile impactedProjectFile : impactedProjectFiles) {
			impactedProjectFilePaths.add(impactedProjectFile.getPath());
			impactedProjectFilePathsCopy.add(impactedProjectFile.getPath());
		}

		//don't evaluate reverted commits when comparing changes with impacted files
		handleReverts(cycleCommits);

		//all files modified/deleted in an identified commit must be present in impactedFiles
		//for each identified commit
		for(Commit commit: cycleCommits) {
			List<String> modifiedFilesFromRepository = repositoryHost.retrieveModifiedFilesFromRepositoryCommit(repositoryProject, commit.getCommitId());
			//for each modified/deleted file in a commit
			for (String modifiedRepositoryFile : modifiedFilesFromRepository) {
				//if modified/deleted commit file isn't in impacted files list
				if(!impactedProjectFilePaths.contains(modifiedRepositoryFile)) {
					return false;
				} else {
					//can't remove from impactedProjectFilePaths because another commit may have modified the same impacted file
					impactedProjectFilePathsCopy.remove(modifiedRepositoryFile);
				}
			}
		}

		//all impactedFiles must be present in some commit
		return impactedProjectFilePathsCopy.isEmpty();
	}

	private void handleReverts(List<Commit> cycleCommits) {
		//loop through cycle commits and if one of those is a revert, then remove it and referenced commit
		List<String> revertedCommitIds = new ArrayList<>();
		Commit commit;
		for(Iterator<Commit> commitItr = cycleCommits.iterator(); commitItr.hasNext();) {
			commit = commitItr.next();
			if(commit.isRevert()) {
				//remove commit from list to compare with impacted files
				commitItr.remove();
				//add reverted commit to list for removal (avoid ConcurrentModificationException)
				revertedCommitIds.add(commit.getRevertedCommitId());
			}
		}
		//remove all reverted commits
		for(String revertedCommitId: revertedCommitIds) {
			for(Iterator<Commit> commitItr = cycleCommits.iterator(); commitItr.hasNext();) {
				commit = commitItr.next();
				if(commit.getCommitId().startsWith(revertedCommitId)) {
					commitItr.remove();
					break;
				}
			}
		}
	}
}
