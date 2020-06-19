package coop.dao;

import coop.util.SessionFactoryUtil;
import org.hibernate.SessionFactory;

public class HibernateDao {
    protected static SessionFactory sessionFactory = SessionFactoryUtil.getInstance();
}
