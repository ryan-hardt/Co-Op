package coop.dao;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.CriteriaQuery;

import coop.model.*;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import coop.model.Cycle;

public class CycleDao extends HibernateDao {

	/**
	 * Gets a Cycle object from the database based on the given ID and creates an
	 * instance of it.
	 *
	 * @param id ID of the Cycle in the database
	 * @return A new instance of a Cycle object, equivalent to one in the database, if it exists
	 */
	public Cycle getCycle(Integer id) {
		if(id == null) {
			return null;
		}

		Session session = sessionFactory.openSession();
		Transaction transaction = null;
		Cycle cycle = null;

		try {
			transaction = session.beginTransaction();
			cycle = session.get(Cycle.class, id);
			transaction.commit();
		} catch (HibernateException e) {
			if (transaction != null) {
				transaction.rollback();
			}
			e.printStackTrace();
			cycle = null;
		} finally {
			session.close();
		}

		return cycle;
	}

	/**
	 * Gets all Cycles from the database
	 * @return A List of all Cycle objects in the database, or an empty list if there are none
	 */
	public List<Cycle> getAllCycles() {
		Session session = sessionFactory.openSession();
		Transaction transaction = null;
		List<Cycle> cycles = null;

		try {
			transaction = session.beginTransaction();
			CriteriaQuery<Cycle> criteria = session.getCriteriaBuilder().createQuery(Cycle.class);
			criteria.from(Cycle.class);
			cycles = session.createQuery(criteria).getResultList();
			transaction.commit();
		} catch (HibernateException e) {
			if (transaction != null) {
				transaction.rollback();
			}
			e.printStackTrace();
		} finally {
			session.close();
		}

		return cycles;
	}

	/**
	 * Updates the corresponding Cycle object in the database
	 *
	 * @param cycle Cycle object that should be updated in the database
	 * @return True if the update was successful
	 */
	public boolean updateCycle(Cycle cycle) {
		if (cycle == null) {
			return false;
		} else {
			if (cycle.getBoard() == null || cycle.getProject() == null || cycle.getEndDate() == null
					|| cycle.getStartDate() == null) {
				return false;
			}
		}
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		boolean success = true;

		try {
			tx = session.beginTransaction();
			session.update(cycle);
			tx.commit();
		} catch (HibernateException e) {
			success = false;
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
		} finally {
			session.close();
		}
		return success;
	}

	/**
	 * Deletes the corresponding Cycle object in the database
	 *
	 * @param cycle Cycle object to be deleted from the database
	 * @return True if the cycle was deleted successfully
	 */
	public boolean deleteCycle(Cycle cycle) {
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		boolean result = false;

		try {
			if(cycle != null) {
				tx = session.beginTransaction();
				session.delete(cycle);
				tx.commit();
				result = true;
			}
		} catch (HibernateException e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
		} finally {
			session.close();
		}
		return result;
	}

	/**
	 * Deletes the corresponding Cycle object in the database
	 * as well as its assosciated board, that board's tasks,
	 * and each of those tasks' assosciated task histories
	 *
	 * @param cycle Cycle object to be deleted from the database
	 * @return True if the cycle and all of its assosciated
	 * objects were deleted successfully
	 */
	public boolean deleteCycleCascade(Cycle cycle) {
		Session session = sessionFactory.openSession();
		Transaction tx = null;
		boolean result = false;

		try {
			if(cycle != null) {
				List<Task> tasks = cycle.getBoard().getTasks();
				List<TaskHistory> taskHistories = tasks.stream().map(Task::getTaskHistories).flatMap(Collection::stream).collect(Collectors.toList());

				tx = session.beginTransaction();
				for (TaskHistory th : taskHistories) {
					session.remove(th);
				}
				for (Task task : tasks) {
					session.remove(task);
				}
				session.remove(cycle);
				session.remove(cycle.getBoard());
				tx.commit();
				result = true;
			}
		} catch (HibernateException e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
		} finally {
			session.close();
		}
		return result;
	}

	/**
	 * Inserts a new Cycle object into the database
	 *
	 * @param cycle cycle object
	 * @return True if successful False if not
	 */
	public boolean insert(Cycle cycle) {
		if (cycle == null) {
			return false;
		}else {
			if((cycle.getBoard() == null) || (cycle.getProject() == null) || (cycle.getStartDate() == null) || (cycle.getEndDate() == null)) {
				return false;
			}
		}

		Session session = sessionFactory.openSession();
		Transaction tx = null;
		boolean isSuccessful = true;

		try {
			tx = session.beginTransaction();
			session.persist(cycle);
			tx.commit();

			if (Integer.valueOf(cycle.getId()) != null) {
				isSuccessful = true;
			}

		} catch (HibernateException e) {
			if (tx != null) {
				tx.rollback();
			}
			e.printStackTrace();
			isSuccessful = false;
		} finally {
			session.close();
		}
		return isSuccessful;
	}
	
	public List<User> getCycleUsers(int cycleId) {
		Cycle s = getCycle(cycleId);
		Project p = s.getProject();
		List<User> cycleUsers = p.getUsers();
		Collections.sort(cycleUsers, new User.UserStatsSorter());
		return cycleUsers;
	}
}
