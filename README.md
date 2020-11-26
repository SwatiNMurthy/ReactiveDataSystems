# ReactiveDataSystems

## Problem Definition:
Traditional databases are passive in nature i.e., commands are only executed (queries) by the database as and when requested by the users or by the application program. This is not suitable for few real world scenarios like buying or selling stocks when price goes below or above threshold.

## Solution:
The solution is to make the DBMS reactive i.e., making the database react automatically upon certain events. We have to set some rules for these scenarios. This can be done using the ECA(Event-Condition-Action) model. Event is something that happens at a point in time.
Condition is the context in which the event has taken place. Action is the task to be carried out by the rule, if the event occurs and the condition is true.
In Relational DBMS systems, we can make the database active using Triggers. We have few limitations using triggers, which can act only upon the events INSERT, DELETE, UPDATE of SQL. We may have user defined events beyond what triggers can handle. So, we have to implement the ECA model in our database which acts like a parser that checks events and triggers actions by running in the background.

## Benefits:
As indicated in the problem definition above, we can make the database active, which is suitable for real world problems. For example, we can trigger the purchase of a stock if its value goes below a certain threshold or we can buy the movie tickets to a super star’s movie as soon as the theatre releases it.

## References:
ActiveDB-p63-paton.pdf ​ https://lms.iiitb.ac.in/moodle/mod/folder/view.php?id=11776

Run the project following below commands

## Backend: (SpringBoot Application)

### Using the Maven plugin
  The Spring Boot Maven plugin includes a run goal which can be used to quickly compile and run your application. Applications run in an exploded form just like in your IDE.

  	$ mvn spring-boot:run

### or Using eclipse IDE
	Run the ReactiveDataSystems.java file (main() class)
  
  For more insight: https://docs.spring.io/spring-boot/docs/1.5.16.RELEASE/reference/html/using-boot-running-your-application.html

## Front end: reactjs application

### Go to path src/main/webapp/reactjs
	$npm install
	$npm start

## DBMS MySQL database
  Detailed installation steps for mysql on Ubuntu: https://support.rackspace.com/how-to/installing-mysql-server-on-ubuntu/
  Steps to follow if you are unable to login as root without sudo: https://stackoverflow.com/questions/37239970/connect-to-mysql-server-without-sudo
  
	$mysql -u root -p
	give ID and password
	Database name is 'reactive'
  
  
 Change if required..
