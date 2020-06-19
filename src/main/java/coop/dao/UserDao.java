package coop.dao;

import java.util.ArrayList;
import java.util.List;

import coop.model.Project;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.hibernate.Session;
import javax.servlet.http.HttpServletRequest;

import coop.model.User;
import coop.util.BCrypt;

public class UserDao extends HibernateDao {
  
  @SuppressWarnings("unchecked")
  public List<User> getAllUsers() {
	  List<User> users = new ArrayList<User>();
	  Session session = sessionFactory.openSession();
	    Transaction tx = null;
	    try {
	    	  tx = session.beginTransaction();	          
	          Query<User> query = session.createQuery("FROM User");	          
	          users.addAll(query.list());
	          tx.commit();
	        } catch (Exception e) {
	          if (tx != null) {
	            tx.rollback();
	          }
	          e.printStackTrace();
	        } finally {
	          session.close();
	        }
	    return users;
  }

  @SuppressWarnings("unchecked")
  public List<User> getActiveUsers() {
	  List<User> users = new ArrayList<User>();
	  Session session = sessionFactory.openSession();
	    Transaction tx = null;
	    try {
	    	  tx = session.beginTransaction();	          
	          Query<User> query = session.createQuery("FROM User WHERE isActive = 1");	          
	          users.addAll(query.list());
              tx.commit();
	        } catch (Exception e) {
	          if (tx != null) {
	            tx.rollback();
	          }
	          e.printStackTrace();
	        } finally {
	          session.close();
	        }
	    return users;
  }
  
  @SuppressWarnings("unchecked")
  public User getUser(String username, String hash) {
    Session session = sessionFactory.openSession();
    Transaction tx = null;
    try {
      tx = session.beginTransaction();
      // #TODO PASSWORD VALIDATION
      Query<User> query =
          session.createQuery("FROM User WHERE username = :username AND hash = :hash");
      query.setParameter("username", username);
      query.setParameter("hash", hash);
      List<User> users = query.list();
      tx.commit();
      if (users.size() == 1) {
        User u = users.get(0);
        return u;
      }
    } catch (Exception e) {
      if (tx != null) {
        tx.rollback();
      }
      e.printStackTrace();
    } finally {
      session.close();
    }
    return null;
  }
  
  @SuppressWarnings("unchecked")
  public User getUser(String username) {
    Session session = sessionFactory.openSession();
    Transaction tx = null;
    try {
      tx = session.beginTransaction();
      // #TODO PASSWORD VALIDATION
      Query<User> query =
          session.createQuery("FROM User WHERE username = :username");
      query.setParameter("username", username);
      List<User> users = query.list();
      tx.commit();
      if (users.size() == 1) {
        User u = users.get(0);
        return u;
      }
    } catch (Exception e) {
      if (tx != null) {
        tx.rollback();
      }
      e.printStackTrace();
    } finally {
      session.close();
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public User getUser(Integer id) {
    Session session = sessionFactory.openSession();
    Transaction tx = null;
    try {
      tx = session.beginTransaction();
      // #TODO PASSWORD VALIDATION
      Query<User> query = session.createQuery("FROM User WHERE id = :id");
      query.setParameter("id", id);
      List<User> users = query.list();
      tx.commit();
      if (users.size() == 1) {
        User u = users.get(0);
        return u;
      }
    } catch (Exception e) {
      if (tx != null) {
        tx.rollback();
      }
      e.printStackTrace();
    } finally {
      session.close();
    }
    return null;
  }

  public boolean updateUser(User user) {
    Session session = sessionFactory.openSession();
    Transaction tx = null;
    boolean isSuccessful = true;
    try {
      tx = session.beginTransaction();
      session.update(user);
      tx.commit();
    } catch (Exception e) {
      if (tx != null) {
        tx.rollback();
      }
      isSuccessful = false;
      e.printStackTrace();
    } finally {
      session.close();
    }
    return isSuccessful;
  }

  public boolean insertUser(User user) {
    Session session = sessionFactory.openSession();
    Transaction tx = null;
    boolean isSuccessful = true;
    try {
      tx = session.beginTransaction();
      session.save(user);
      tx.commit();
    } catch (Exception e) {
      if (tx != null) {
        tx.rollback();
      }
      isSuccessful = false;
      e.printStackTrace();
    } finally {
      session.close();
    }
    return isSuccessful;
  }
  
  //Simple helper method to hash posted passwords with a strength of 32
  public static String hashPassword(String plainTextPassword, String salt) {
    return BCrypt.hashpw(plainTextPassword, salt);
  }
  
  public static boolean isValidated(User u, String postedPassword) {
    boolean verified = false;
    //Get the user's password hash stored in the database
    String userPasswordHash = u.getHash();
    //Quick validation
    if(userPasswordHash == null || postedPassword == null) {
      //TEMP
      System.out.println("USERPASSWORDHASH OR POSTEDPASSWORD IS NULL");
      throw new java.lang.IllegalArgumentException("No user hash or password found");
    }
    //Validated user hash stored against posted password
    verified = userPasswordHash.equals(hashPassword(postedPassword, u.getSalt()));
    //TEMP
    if(!verified) {
      System.out.println("VERIFIED IS FALSE");
    }
    return verified;
  }

  public boolean deleteUser(User user) {
    Session session = sessionFactory.openSession();
    Transaction tx = null;
    boolean isSuccessful = true;
    try {
      tx = session.beginTransaction();
      session.delete(user);
      tx.commit();
    } catch (Exception e) {
      if (tx != null) {
        tx.rollback();
      }
      isSuccessful = false;
      e.printStackTrace();
    } finally {
      session.close();
    }
    return isSuccessful;
  }

  public static boolean loggedIn(HttpServletRequest request) {
    if (request.getSession().getAttribute("user") == null) {
      return false;
    }
    return true;
  }

  public static Integer getUserIDFromSession(HttpServletRequest request) {
    User u = (User) request.getSession().getAttribute("user");
    if (u != null) {
      return u.getId();
    }
    return null;
  }

  public static User getUserFromSession(HttpServletRequest request) {
    return (User) request.getSession().getAttribute("user");
  }
}
