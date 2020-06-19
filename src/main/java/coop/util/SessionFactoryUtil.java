package coop.util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class SessionFactoryUtil {

	private static SessionFactory sessionFactory;
	private static Configuration configuration;

	private SessionFactoryUtil() {
	}

	public static void initWithConfig(Configuration c) {
		configuration = c;
	}

	public static SessionFactory getInstance() {
		if (sessionFactory == null) {
			if (configuration == null) {
				sessionFactory = new Configuration().configure().buildSessionFactory();
			} else {
				sessionFactory = configuration.buildSessionFactory();
			}
		}
		return sessionFactory;
	}

	public Session openSession() {
		return sessionFactory.openSession();
	}

	public Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}

	public static void close() {
		if (sessionFactory != null) {
			sessionFactory.close();
		}
		sessionFactory = null;
	}
}
