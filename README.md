# Co-Op
Co-Op is a software maintenance-focused process and supporting toolset designed for use in academic environments. 
The toolset is implemented as a web application that focuses on change impact analysis, use of version control systems that adheres to the process, and communication amongst part-time developers.

## Purpose
![Co-Op process model](src/main/webapp/resources/images/coop-model.png)

Software engineering and maintenance processes are designed to provide structure and organization around a set of activities involved in the production or maintenance of a software product. 
Understanding these processes and learning to follow them are important experiences for students in a software engineering course. 
But it can be difficult for both students and instructors to recognize when a process isn’t being followed. 
Tools designed to guide a process can help. 
In these environments, tool support can also help ensure that students are using version control systems appropriately while fostering an environment in which students learn from their peers. 

## Installation
1. Clone or download and unpack the project. 
2. If desired, update hibernate.cfg.xml (located in the coop/src/main/resources directory) to reference your MySQL database.
If no updates are made to this file, an embedded Apache Derby database will be created and used.
3. Generate the coop.jar file by either a) importing the project into your IDE and running the maven package command, or 
b) executing the commands below:
   ```bash
   $ cd [coop_directory]
   $ mvn package
   ```
4. Deploy the coop.jar file generated in the [coop_directory]/target directory to an Apache Tomcat server.

## Integration
Co-Op integrates with GitHub, GitLab, and Slack.
Each Co-Op project requires a corresponding git repository, hosted on either GitHub or a GitLab instance.
Slack integration is encouraged but not required.

### GitHub
1. Generate a GitHub [personal access token](https://github.com/settings/tokens).
Select **Generate new token**.
To provide Co-Op with access to public repositories only, select **public_repo**.
To provide Co-Op with access to both public and private repositories, select **repo**.
Select **Generate token**.
Copy the provided token.
2. In Co-Op, when creating a new project or updating an existing project, select **Setup new repository host**.
Fill out the form, and paste the copied GitHub personal access token into the **Access token** field.
Select **Add repository host**.
3. On the create/update project page, you should now be able to select your GitHub repository host.
Only the user creating the repository host will be able to select it, and as a result, associate a repository it contains with a Co-Op project.

### GitLab
1. Generate a GitLab personal access token by logging into your GitLab instance > Settings (User Settings) > Access Tokens.
Fill out the form, and select the **api** scope.
Select **Create personal access token**.
Copy the provided token.
2. In Co-Op, when creating a new project or updating an existing project, select **Setup new repository host**.
Fill out the form, and paste the copied GitLab personal access token into the **Access token** field.
Select **Add repository host**.
3. On the create/update project page, you should now be able to select your GitLab repository host.
Only the user creating the repository host will be able to select it, and as a result, associate a repository it contains with a Co-Op project.

### Slack
1. Create a [Slack](https://slack.com) workspace.
2. Create a project in Co-Op.
From the project page, select **Add to Slack**.
3. In the opened web browser page, select or sign-in to the desired Slack workspace.
Select the channel to receive the Co-Op bot messages. Select **Allow**.
4. On the project page, you should now see a link to **View Slack Workspace**.