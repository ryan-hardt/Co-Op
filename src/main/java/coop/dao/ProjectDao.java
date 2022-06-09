package coop.dao;

import coop.model.Project;
import coop.model.SlackWorkspace;
import coop.model.User;
import coop.model.repository.RepositoryProject;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;

public class ProjectDao extends HibernateDao {

	public Project getProject(Integer id) {
		Session session = sessionFactory.openSession();
		Transaction t = null;	
		Project project = null;
		try {
			t = session.beginTransaction();
			if(id != null) {
			  project = (Project)session.get(Project.class,id);
			  t.commit();
			}
		} catch (HibernateException e) {
			if (t != null) {
				t.rollback();
			}
			e.printStackTrace();
		} finally {
			
			session.close();
		}
		
		return project;
	}

	public List<Project> getUnassignedProjectsWithSameRepository(User user) {
		Session session = sessionFactory.openSession();
		Transaction transaction = null;
		List<Project> otherProjects = new ArrayList<Project>();
		List<Project> userProjects = user.getProjects();
		try {
			transaction = session.beginTransaction();
			for(Project userProject: userProjects) {
				RepositoryProject repositoryProject = userProject.getRepositoryProject();
				if(repositoryProject != null) {
					String repositoryProjectUrl = repositoryProject.getRepositoryProjectUrl();
					Query<Project> query = session.createQuery("FROM Project WHERE repositoryProject.repositoryProjectUrl = :repositoryProjectUrl", Project.class);
					query.setParameter("repositoryProjectUrl", repositoryProjectUrl);
					List<Project> projectList = query.list();
					for(Project otherProject: projectList) {
						if(!userProjects.contains(otherProject) && !otherProjects.contains(otherProject)) {
							otherProjects.add(otherProject);
						}
					}
				}
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
		return otherProjects;
	}

	
	public boolean insertProject(Project project) {
		boolean success = false;
		Session session = sessionFactory.openSession();
		Transaction insertProjectTransaction = null;
		try {
			insertProjectTransaction = session.beginTransaction();
			session.persist(project.getRepositoryProject());
			session.persist(project);
			insertProjectTransaction.commit();
			success = true;
		} catch (HibernateException e) {
			if (insertProjectTransaction != null) {
				insertProjectTransaction.rollback();
			}
			e.printStackTrace();
		} finally {
			session.close();
		}
		return success;
	}
	
	public boolean deleteProject(Project project) {
		boolean success = false;
		SlackWorkspace slackWorkspace = project.getSlackWorkspace();
		Session session = sessionFactory.openSession();
		Transaction deleteProjectTransaction = null;

		try {
			deleteProjectTransaction = session.beginTransaction();
			session.remove(project);
			Query<Project> query = session.createQuery("FROM Project WHERE slackWorkspace = :slackWorkspace", Project.class);
			query.setParameter("slackWorkspace", slackWorkspace);
			List<Project> projectsWithWorkspace = query.list();
			if(projectsWithWorkspace != null && !projectsWithWorkspace.isEmpty()) {
				session.remove(slackWorkspace);
			}
			deleteProjectTransaction.commit();
			success = true;
		} catch (HibernateException e) {
	         if (deleteProjectTransaction !=null) {
	        	 	deleteProjectTransaction.rollback();
	         }
	         e.printStackTrace(); 
	      } finally {
	         session.close(); 
	      }
		return success;	
	}
	
	public boolean updateProject(Project project) {
		Session session = sessionFactory.openSession();
		boolean success = false;
		Transaction updateProjectTransaction = null; 
		try {
			updateProjectTransaction = session.beginTransaction();
			session.merge(project.getRepositoryProject());
			session.merge(project);
			updateProjectTransaction.commit();
			success = true;
		} catch (HibernateException e) {
	         if (updateProjectTransaction !=null) {
	        	 	updateProjectTransaction.rollback();
	         }
	         e.printStackTrace(); 
	      } finally {
	         session.close(); 
	      }
		return success;	
	}
}
