package coop.dao;

import java.util.ArrayList;
import java.util.List;

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
			session.persist(w);
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
			session.remove(w);
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
			Query<Work> query = session.createQuery(hql, Work.class);
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
}
