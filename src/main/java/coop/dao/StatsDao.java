package coop.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import coop.model.*;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import coop.model.Board;

public class StatsDao extends HibernateDao {

	public Work getWork(Integer id) {
		Session session = sessionFactory.openSession();
		Transaction t = null;	
		Work w = null;
		try {
			t = session.beginTransaction();
			if(id != null) {
			  w = (Work)session.get(Work.class,id);
			}
			t.commit();
		} catch (HibernateException e) {
			if (t != null) {
				t.rollback();
			}
			e.printStackTrace();
		} finally {
			session.close();
		}
		
		return w;
	}

	public boolean insertWork(Work w) {
		boolean success = false;
		Session session = sessionFactory.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			session.save(w);
			transaction.commit();
			success = true;
		} catch (HibernateException e) {
			if (transaction != null) {
				transaction.rollback();
			}
			e.printStackTrace();
		} finally {
			session.close();
		}
		return success;
	}
	
	public boolean deleteWork(Work w) {
		boolean success = false;
		Session session = sessionFactory.openSession();
		Transaction transaction = null; 
		try {
			transaction = session.beginTransaction();
			session.delete(w);
			transaction.commit();
			success = true;
		} catch (HibernateException e) {
	         if (transaction !=null) {
	        	 transaction.rollback();
	         }
	         e.printStackTrace(); 
	      } finally {
	         session.close(); 
	      }
		return success;	
	}
	
	public boolean updateWork(Work w) {
		Session session = sessionFactory.openSession();
		boolean success = false;
		Transaction transaction = null; 
		try {
			transaction = session.beginTransaction();
			session.update(w);
			transaction.commit();
			success = true;
		} catch (HibernateException e) {
	         if (transaction !=null) {
	        	 transaction.rollback();
	         }
	         e.printStackTrace(); 
	      } finally {
	         session.close(); 
	      }
		return success;	
	}
	
	@SuppressWarnings("unchecked")
	public List<Work> getCycleWork(Board b) {
		Session session = sessionFactory.openSession();
		ArrayList<Work> work = new ArrayList<Work>();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			String hql = "FROM Work WHERE task.board = :b";
			Query<Work> query = session.createQuery(hql);
			query.setParameter("b", b);          
			work.addAll(query.list());
			transaction.commit();
		} catch (HibernateException e) {
			if (transaction != null) {
				transaction.rollback();
			}
			e.printStackTrace();
	      } finally {
	         session.close(); 
	      }
		return work;	
	}
	
	@SuppressWarnings("unchecked")
	public List<Work> getUserWork(User u) {
		Session session = sessionFactory.openSession();
		ArrayList<Work> work = new ArrayList<Work>();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			String hql = "FROM Work WHERE user = :u";
			Query<Work> query = session.createQuery(hql);
			query.setParameter("u", u);          
			work.addAll(query.list());
			transaction.commit();
		} catch (HibernateException e) {
			if (transaction != null) {
				transaction.rollback();
			}
			e.printStackTrace();
	      } finally {
	         session.close(); 
	      }
		return work;	
	}
	
	@SuppressWarnings("unchecked")
	public List<Work> getProjectWork(Project p) {
		Session session = sessionFactory.openSession();
		ArrayList<Work> work = new ArrayList<Work>();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			String hql = "FROM Work WHERE task.board.cycle.project = :p";
			Query<Work> query = session.createQuery(hql);
			query.setParameter("p", p);          
			work.addAll(query.list());
			transaction.commit();
		} catch (HibernateException e) {
			if (transaction != null) {
				transaction.rollback();
			}
			e.printStackTrace();
	      } finally {
	         session.close(); 
	      }
		return work;	
	}
	
	@SuppressWarnings("unchecked")
	public List<Work> getAllProjectWorkForUser(User u) {
		Session session = sessionFactory.openSession();
		Transaction transaction = null;
		ArrayList<Work> work = new ArrayList<Work>();
		try {
			transaction = session.beginTransaction();
			for(Project project: u.getProjects()) {
				String hql = "FROM Work WHERE task.board.project = :project";
				Query<Work> query = session.createQuery(hql);
				query.setParameter("project", project);
				work.addAll(query.list());
			}
			transaction.commit();
		} catch (HibernateException e) {
			if (transaction != null) {
				transaction.rollback();
			}
			e.printStackTrace();
	      } finally {
	         session.close(); 
	      }
		return work;	
	}
	
	public double getCycleWorkPercentile(Cycle s, User u) {
		List<Work> cycleWork = getCycleWork(s.getBoard());
		return getUserWorkPercentile(cycleWork, u);
	}
	
	public double getProjectWorkPercentile(Project p, User u) {
		List<Work> projectWork = getProjectWork(p);
		return getUserWorkPercentile(projectWork, u);
	}
	
	public double getTotalWorkPercentile(User u) {
		List<Work> allWork = getAllProjectWorkForUser(u);
		return getUserWorkPercentile(allWork, u);
	}
	
	public double getCycleCollaboratorPercentile(Cycle s, User u) {
		List<Task> cycleTasks = s.getBoard().getTasks();
		return getCollaboratorPercentile(cycleTasks, u);
	}
	
	public double getProjectCollaboratorPercentile(Project p, User u) {
		List<Task> projectTasks = new ArrayList<Task>();
		List<Cycle> cycles = p.getCycles();
		for(Cycle s : cycles) {
			projectTasks.addAll(s.getBoard().getTasks());
		}
		return getCollaboratorPercentile(projectTasks, u);
	}
	
	public double getTotalCollaboratorPercentile(User u) {
		List<Task> allTasks = new ArrayList<Task>();
		ProjectDao projectDao = new ProjectDao();
		List<Project> allProjects = projectDao.getUnassignedProjectsWithSameRepository(u);
		for(Project p : allProjects) {
			List<Cycle> cycles = p.getCycles();
			for(Cycle s : cycles) {
				allTasks.addAll(s.getBoard().getTasks());
			}
		}
		return getCollaboratorPercentile(allTasks, u);
	}
	
	private double getUserWorkPercentile(List<Work> work, User u) {
		int userRank = 1;		//higher is better
		HashMap<User, Double> userWorkMap = new HashMap<User, Double>();
		
		for(Work w : work) {
			User workUser = w.getUser();
			double existingWorkMinutes = 0.0;
			if(userWorkMap.containsKey(workUser)) {
				existingWorkMinutes = userWorkMap.get(workUser);
			}
			userWorkMap.put(workUser, existingWorkMinutes + w.getNumMinutes());
		}
		Double totalUserWorkMinutes = userWorkMap.get(u);
		double percentile = 0.0;
		if(totalUserWorkMinutes == null) {
			totalUserWorkMinutes = 0.0;
		} else {
			int numUsers = userWorkMap.size();
			userWorkMap.remove(u);
			for(User mapUser : userWorkMap.keySet()) {
				double otherUserWorkMinutes = userWorkMap.get(mapUser);
				if(totalUserWorkMinutes >= otherUserWorkMinutes) {
					userRank++;
				}
			}
			percentile = userRank / (double)numUsers;
		}
		return percentile;
	}
	
	private double getCollaboratorPercentile(List<Task> tasks, User u) {
		Map<User, Set<User>> collaboratorMap = new HashMap<User, Set<User>>();
		Map<User, Integer> collaboratorCountMap = new HashMap<User, Integer>();
		int userRank = 1;		//higher is better
		
		for(Task t : tasks) {
			for(User owner : t.getOwners()) {
				Set<User> collaborators = collaboratorMap.get(owner);
				if(collaborators == null) {
					collaborators = new HashSet<User>();
					collaboratorMap.put(owner, collaborators);
				}
				for(User helper : t.getHelpers()) {
					//give owner credit for working with helper
					collaborators.add(helper);

					Set<User> helpers = collaboratorMap.get(helper);
					if(helpers == null) {
						helpers = new HashSet<User>();
						collaboratorMap.put(helper, helpers);
					}
					//give helper credit for working with owner
					helpers.add(owner);
				}
				
				for(User reviewer : t.getReviewers()) {
					//give owner credit for working with reviewer
					collaborators.add(reviewer);
					
					Set<User> reviewers = collaboratorMap.get(reviewer);
					if(reviewers == null) {
						reviewers = new HashSet<User>();
						collaboratorMap.put(reviewer, reviewers);
					}
					//give reviewer credit for working with owner
					reviewers.add(owner);
				}
			}
		}
		
		//create count map
		for(User user : collaboratorMap.keySet()) {
			collaboratorCountMap.put(user, collaboratorMap.get(user).size());
		}
		
		Integer numCollaborators = collaboratorCountMap.get(u);
		double percentile = 0.0;
		if(numCollaborators == null) {
			numCollaborators = 0;
		} else {
			int numUsers = collaboratorCountMap.size();
			collaboratorCountMap.remove(u);
			for(User mapUser : collaboratorCountMap.keySet()) {
				int otherUserNumCollaborators = collaboratorCountMap.get(mapUser);
				if(numCollaborators >= otherUserNumCollaborators) {
					userRank++;
				}
			}
			percentile = userRank / (double)numUsers;
		}
		return percentile;
	}
}
