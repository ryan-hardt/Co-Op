package coop.dao;

import coop.model.repository.RepositoryHost;
import coop.model.repository.RepositoryProject;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class RepositoryDao extends HibernateDao {
    public boolean insert(RepositoryHost repositoryHost) {
        if (repositoryHost == null) {
            return false;
        }

        Session session = sessionFactory.openSession();
        boolean hasWorked = true;
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            session.persist(repositoryHost);
            transaction.commit();
            if (repositoryHost.getId() != null) {
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

    public RepositoryHost find(Integer id) { // AKA Retrieve.
        if (id == null) {
            return null;
        }

        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        RepositoryHost repositoryHost = null;
        try {
            transaction = session.beginTransaction();
            repositoryHost = session.get(RepositoryHost.class, id);
            transaction.commit();
        } catch (HibernateException e) {
            if (transaction != null) {
                transaction.rollback();
            }
        } finally {
            session.close();
        }

        return repositoryHost;
    }

    public RepositoryProject findRepositoryProject(Integer id) {
        if (id == null) {
            return null;
        }

        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        RepositoryProject repositoryProject = null;
        try {
            transaction = session.beginTransaction();
            repositoryProject = session.get(RepositoryProject.class, id);
            transaction.commit();
        } catch (HibernateException e) {
            if (transaction != null) {
                transaction.rollback();
            }
        } finally {
            session.close();
        }

        return repositoryProject;
    }
}
