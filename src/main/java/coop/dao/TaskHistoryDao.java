package coop.dao;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import coop.model.TaskHistory;

public class TaskHistoryDao extends HibernateDao {

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
        session.persist(taskHistory);
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
}
