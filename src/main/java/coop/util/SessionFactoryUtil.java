package coop.util;

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

	public static void close() {
		if (sessionFactory != null) {
			sessionFactory.close();
		}
		sessionFactory = null;
	}
}
