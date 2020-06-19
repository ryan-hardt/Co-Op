package coop.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import coop.model.TaskHistory;

public class TaskHistoryDao extends HibernateDao {

  public TaskHistory getTaskHistory(Integer id) {
    TaskHistory th = null;
    Session session = sessionFactory.openSession();
    Transaction tx = null;

    try {
      tx = session.beginTransaction();
      th = session.get(TaskHistory.class, id);

      // Check to see if all required fields have values when retrieved. If not, rollback.
      if (th == null || th.getDate() == null || th.getChangedValueList() == null) {
        tx.rollback();
      }
      else {
        tx.commit();
      }
    } catch (HibernateException e) {
      if (tx != null) {
        tx.rollback();
      }
      e.printStackTrace();
    } finally {
      session.close();
    }
    return th;
  }

  public boolean insertTaskHistory(TaskHistory taskHistory) {
    Session session = sessionFactory.openSession();
    Transaction tx = null;
    boolean successStatus = true;

    // If a required field value is missing, the TaskHistory object cannot be added in the database.
    if (taskHistory == null || taskHistory.getDate() == null || taskHistory.getChangedValueList() == null) {
      successStatus = false;
      session.close();
    } else {

      try {
        tx = session.beginTransaction();
        taskHistory.setId((Integer)(session.save(taskHistory)));
        tx.commit();
      } catch (HibernateException e) {
        if (tx != null) {
          tx.rollback();
        }
        successStatus = false;
        e.printStackTrace();
      } finally {
        session.close();
      }

    }
    return successStatus;
  }

  public boolean deleteTaskHistory(TaskHistory taskHistory) {
    Session session = sessionFactory.openSession();
    Transaction tx = null;
    boolean successStatus = true;

    try {
      tx = session.beginTransaction();
      session.delete(taskHistory);
      tx.commit();

    } catch (HibernateException e) {
      if (tx != null) {
        tx.rollback();
      }
      successStatus = false;
      e.printStackTrace();
    } finally {
      session.close();
    }
    return successStatus;
  }

  @SuppressWarnings("unchecked")
  public boolean deleteTaskHistoryForTask(Integer taskId) {
    Session session = sessionFactory.openSession();
    Transaction tx = null;
    boolean successStatus = true;
    List<TaskHistory> taskHistories = new ArrayList<TaskHistory>();

    try {
      tx = session.beginTransaction();
      Query<TaskHistory> q = session.createQuery("FROM TaskHistory WHERE task_id = :task_id");
      q.setParameter("task_id", taskId);
      taskHistories.addAll(q.list());

      for(TaskHistory th : taskHistories) {
        session.delete(th);
      }
      tx.commit();

    } catch (HibernateException e) {
      if (tx != null) {
        tx.rollback();
      }
      successStatus = false;
      e.printStackTrace();
    } finally {
      session.close();
    }
    return successStatus;
  }

  @SuppressWarnings("unchecked")
  public List<TaskHistory> getAllTaskHistories() {
    Session session = sessionFactory.openSession();
    List<TaskHistory> taskHistories = new ArrayList<TaskHistory>();
    Transaction transaction = null;
    try {
      transaction = session.beginTransaction();
      Query<TaskHistory> query = session.createQuery("FROM TaskHistory");
      taskHistories.addAll(query.list());
      transaction.commit();
    } catch (Exception e) {
      if (transaction != null) {
        transaction.rollback();
        System.out.println("Could net retrieve Task Histories");
      }
      e.printStackTrace();
    } finally {
      session.close();
    }
    return taskHistories;
  }

  @SuppressWarnings("unchecked")
  public List<TaskHistory> getTaskHistoriesForTask(Integer taskID){
    Session session = sessionFactory.openSession();
    List<TaskHistory> taskHistories = new ArrayList<TaskHistory>();
    Transaction transaction = null;
    try {
      transaction = session.beginTransaction();
      Query<TaskHistory> query = session.createQuery("FROM TaskHistory WHERE task_id = :task_id");
      query.setParameter("task_id", taskID);
      taskHistories.addAll(query.list());
      transaction.commit();
      Collections.reverse(taskHistories);
    }catch(Exception e){
      if(transaction != null) {
        transaction.rollback();
        System.out.println("Could not retrieve Task Histories for Task ID: " + taskID);
      }
      e.printStackTrace();
    }finally {
      session.close();
    }
    return taskHistories;
  }

}
