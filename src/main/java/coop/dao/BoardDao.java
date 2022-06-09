package coop.dao;

import java.util.ArrayList;

import coop.model.Board;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import coop.model.Task;

public class BoardDao extends HibernateDao {

  public boolean insert(ArrayList<Task> tasks) {
    if (tasks == null) {
      return false;
    }
    
    Board board = new Board();
    board.setTasks(tasks);
    return insert(board);
  }

  public boolean insert(Board board) {
    if (board == null) {
      return false;
    }

    Session session = sessionFactory.openSession();
    boolean hasWorked = true;
    Transaction transaction = null;

    try {
      transaction = session.beginTransaction();
      session.persist(board);
      transaction.commit();
      //session.flush();
      if (board.getBoardId() != null) {
        hasWorked = true;
      }
    } catch (HibernateException e) {
      if (transaction != null) {
        transaction.rollback();
      }
      hasWorked = false;
    } finally {
      session.close();
    }

    return hasWorked;
  }

  public boolean update(Board board) {
    if (board == null) {
      return false;
    }

    Session session = sessionFactory.openSession();
    boolean hasWorked = false;
    Transaction transaction = null;

    try {
      transaction = session.beginTransaction();
      session.merge(board);
      transaction.commit();
      //session.flush();
      hasWorked = true;
    } catch (HibernateException e) {
      if (transaction != null) {
        transaction.rollback();
      }
      hasWorked = false;
    } finally {
      session.close();
    }

    return hasWorked;
  }

  public boolean delete(Board board) {
    if (board == null) {
      return false;
    }
    Session session = sessionFactory.openSession();
    boolean hasWorked = false;
    Transaction transaction = null;

    try {
      transaction = session.beginTransaction();
      session.remove(board);
      transaction.commit();
      //session.flush();
      hasWorked = true;
    } catch (HibernateException e) {
      if (transaction != null) {
        transaction.rollback();
      }
    } finally {
      session.close();
    }

    return hasWorked;
  }

  public Board find(Integer id) { // AKA Retrieve.
    if (id == null) {
      return null;
    }

    Session session = sessionFactory.openSession();
    Transaction transaction = null;
    Board board = null;
    try {
      transaction = session.beginTransaction();
      board = session.get(Board.class, id);
      transaction.commit();
    } catch (HibernateException e) {
      if (transaction != null) {
        transaction.rollback();
      }
    } finally {
      session.close();
    }

    return board;
  }
}

