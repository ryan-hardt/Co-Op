package coop;

import coop.model.*;
import coop.model.repository.GitHubRepositoryHost;
import coop.model.repository.GitLabRepositoryHost;
import coop.model.repository.RepositoryHost;
import coop.model.repository.RepositoryProject;
import org.hibernate.cfg.Configuration;

import coop.util.SessionFactoryUtil;
import org.junit.BeforeClass;

public class DerbyTestSetup {
  @BeforeClass
  public static void setUpBeforeClass() {
    Configuration c = new Configuration();

    // set hibernate properties
    c.setProperty("hibernate.show_sql", "true");
    c.setProperty("hibernate.connection.url", "jdbc:derby:coopDB;create=true");
    c.setProperty("hibernate.connection.driver_class", "org.apache.derby.jdbc.EmbeddedDriver");
    c.setProperty("hibernate.dialect", "org.hibernate.dialect.DerbyDialect");
    c.setProperty("hibernate.hbm2ddl.auto", "update");

    // add annotated classes to configuration
    c.addAnnotatedClass(Cycle.class);
    c.addAnnotatedClass(Project.class);
    c.addAnnotatedClass(Board.class);
    c.addAnnotatedClass(User.class);
    c.addAnnotatedClass(Task.class);
    c.addAnnotatedClass(TaskHistory.class);
    c.addAnnotatedClass(TaskChange.class);
    c.addAnnotatedClass(Work.class);
    c.addAnnotatedClass(Note.class);
    c.addAnnotatedClass(ImpactedProjectFile.class);
    c.addAnnotatedClass(RepositoryHost.class);
    c.addAnnotatedClass(RepositoryProject.class);
    c.addAnnotatedClass(GitLabRepositoryHost.class);
    c.addAnnotatedClass(GitHubRepositoryHost.class);
    c.addAnnotatedClass(SlackWorkspace.class);

    SessionFactoryUtil.initWithConfig(c);
  }
}
