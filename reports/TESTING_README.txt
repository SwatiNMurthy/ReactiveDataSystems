SOFTWARE TESTING PROJECT

TEAM DETAILS:
	MT2019114 - Sravya M
	MT2019120 - Swati N Murthy

TEST CASE STRATEGY:
	Symbolic Execution Testing

TESTING TOOLS USED:
	JUnit 5

EXECUTABLE FILE PATH:(from Project folder)
	/src/main/java/com/iiitb/dm/test/RuleServiceTest.java

SCREENSHOTS PATH: (from Project folder)
	/Screenshots

Source Code Functionality/ Feature:
	Project: ReactiveDataSystems
	Backend Language: Java
	Frontend: Reactjs
	Framework: Spring Boot
	DBMS: MySQL
	Driver: JDBC
	DataBase Configurations can be done in few lines at : /src/main/resources/application.properties
	IDE: IntelliJ (Exclipse also is fine)

	This project has been implemented by a group of 4 people, along with us.
	The main focus was on building a Rule Based Event Condition Action model which makes the database reactive by running in the background.
	Railway ticket booking system has been implemented as the use case on top of this system.
	The Class "Rules" and its functions have been more focused, rest of the classes like User, Train, BookingDetails are used to implement the use cases in Spring Boot.

	Overview: The function is to make the DBMS reactive i.e., making the database react automatically upon certain events. We have to set some rules for these scenarios. This can be done using the ECA(Event-Condition-Action) model. Event is something that happens at a point in time. Condition is the context in which the event has taken place. Action is the task to be carried out by the rule, if the event occurs and the condition is true.
	In Relational DBMS systems, we can make the database active using Triggers. We have few limitations using triggers, which can act only upon the events INSERT, DELETE, UPDATE of SQL. We may have user defined events beyond what triggers can handle. So, we have to implement the ECA model in our database which acts like a parser that checks events and triggers actions by running in the background.
	You can find the configuration steps of application at 
	Github link of last Sem project: https://github.com/SwatiNMurthy/ReactiveDataSystems

DESIGNED TESTCASES:

	In Symbolic Testing, we have focused on path coverage for methods, reachability/ coverage of nested decision statements and looping constructs.

1. RulebaseMarshallTest
	This method will take in a rule and add it into rulebase. It will convert json to xml and add rule to rulebase.xml file
	This method checks what happens when a well formed rule is sent and when a badly formed rule is sent

    @Test @Order(1)
    void ruleBaseMarshallTest() {

        // preparing inputs
        Rule rule = new Rule();
        rule.setRule_description("Adding extra compartments (seats) on demand");
        rule.setTable("train");
        rule.setRule_type("deferred");
        rule.setRule_status("Active");

        Query query = new Query();
        query.setQuery("update train set remaining_seats=remaining_seats+10 where train_id=?");

        Action action = new Action();
        List<Query> queries= new ArrayList<Query>();
        queries.add(query);
        action.setQueries(queries);
        action.setAction_type("query");
        action.setMethod_path("none");

        Condition condition = new Condition();
        condition.setAttribute("remaining_seats");
        condition.setOperator("<=");
        condition.setValue("5");
        List<Condition> con = new ArrayList<>();
        con.add(condition);

        Conditions conditions = new Conditions();
        conditions.setCondition(con);
        conditions.setConjunction("none");

        Event event = new Event();
        event.setConditions(conditions);
        event.setEvent_type("select");

        rule.setRuleId(200);
        rule.setEvent(event);
        rule.setAction(action);

        //Testing if condition by giving a wellformed rule
        assertEquals(true, ruleService.ruleBaseMarshall(rule));

        //Testing else condition by giving a badly formed rule
        Rule rule1 = new Rule();
        rule1.setRule_status("Inactive");
        rule1.setRuleId(150);
        try {
            boolean op = ruleService.ruleBaseMarshall(rule1);
            assertEquals(true, op);
        } catch (Exception e) {
            assertEquals("exception", "java.lang.Exception");
        }
        ruleService.deleteRule(150);
    }

2. RulebaseUnmarshallTest
	Rulebase unmarshall converts or unmarshall the rules from xml to json format.

    @Test @Order(2)
    void ruleBaseUnmarshallTest() {
        try {
            List<Rule> rules = ruleService.ruleBaseUnmarshall();
            assertEquals(false, rules.isEmpty());
        } catch (Exception e) {
            assertEquals("exception", ruleService.ruleBaseUnmarshall());
        }
    }

3. getRuleByIdTest
	This checks the method getRuleById which takes input as ID and returns the rule.
	Checking the outputs in case of a valid Rule Id and invalid RuleId

    @Test @Order(3)
    void getRuleByIdTest() {
        //Test case when rule with specified ID is present
        Rule rule = ruleService.getRuleById(0);
        assertEquals(0, rule.getRuleId());

        //Test case when rule with specified ID is not present
        rule = ruleService.getRuleById(125);
        assertEquals(null, rule);
    }

4. updateRuleTest
	This checks the method updateRule which takes input as modified_rule, ID and updates the rule at ID with modified_rule.
	Checking the outputs in case of a valid Rule Id.

    @Test @Order(4)
    void updateRuleTest() {
        Rule rule = ruleService.getRuleById(200);
        rule.setRule_description("New Description");
        rule.setRule_status("Inactive");
        ruleService.updateRule(rule, 200);

        assertEquals("New Description", ruleService.getRuleById(200).getRule_description());
    }

5. processTest
	This method tries to cover the decision statements and the paths.
	process() method takes a Rule as argument and takes 3 paths based on decisions.
	Executes the Rule if it is Active and returns 1 on success.

    @Test @Order(5)
    public void processTest() throws Exception {
        //System.out.println(dataSource);
        ruleService.setDataSource(dataSource);
        ruleService.setJavaMailSender(javaMailSender);

        // 1 Inactive Rule
        Rule rule = new Rule();
        rule.setRule_status("Inactive");

        assertEquals(0,ruleService.process(rule));

        // 2 Active Rule && actionType = "query"
        Rule rule1=ruleService.getRuleById(200);
        rule1.setRule_status("Active");

        assertEquals(1,ruleService.process(rule1));

        // 3 Active Rule && Action Type = "method"
        rule1.getAction().setAction_type("method");
        rule1.getAction().setMethod_path("getRuleById");

        assertEquals(1,ruleService.process(rule1));

    }

6. deleteRuleTest
	This checks the method deleteRule which takes input as ID and deletes the rule at ID from xml file.
	Checking if it is deleting from the xml file or not.

    @Test @Order(6)
    void deleteRuleTest() {
        //Delete the rule by ID and check if the rule exists
        ruleService.deleteRule(200);
        assertEquals(null, ruleService.getRuleById(200));
    }

7. ruleExecute
	This is for testing ruleExecute() method which takes 3 arguments event, action, actionType.
	nested decision statements are based on 1. actionType, 2. action
	while loop based on resultDataSet from a database query.
	Wrote test cases to cover all the paths from these control structures.

    @Test @Order(7)
    public void ruleExecute() throws Exception {
        //System.out.println(dataSource);
        ruleService.setDataSource(dataSource);
        ruleService.setJavaMailSender(javaMailSender);

        // There are 5 paths the ruleExecute can take based on the decision statements and state
        // let us try to cover them by passing the symbolic values

        String actionType, event, action;
        event="select *from user where id=1";

        // 1 actionType == "query" && action.contains("insert")
        actionType="query";
        action="insert into user values(?+15,?,?,?,?,?,?,?)";
        assertTrue(ruleService.ruleExecute(event,action,actionType));

        // 2 actionType == "query" && action.contains("update")
        action="update user set mail = 'sravya.m@iiitb.org' where id=?";
        assertTrue(ruleService.ruleExecute(event,action,actionType));

        // 3 actionType == "query" && action.contains("delete")
        action="delete from user where id=?+15";
        assertTrue(ruleService.ruleExecute(event,action,actionType));

        // 4 actionType == "method" && action.contains("sendmail")
        actionType="method";
        action="sendmail pwdchange";
        assertTrue(ruleService.ruleExecute(event,action,actionType));

        // 5 actionType == "query" && !action.contains("sendmail")
        action="getRuleById";
        assertTrue(ruleService.ruleExecute(event,action,actionType));

        // We also have looping constructs in this method like while() loop
        // All the above test inputs will pass through the while() loops because the queries return at least 1 tuple
        // I am trying to send the query parameters for which the result will be empty

        // Same as above test cases, but doesn't enter while loop
        // while(eventResultSet.next()) == false
        event="select *from user where id<0";

        action="sendmail pwdchange";
        assertTrue(ruleService.ruleExecute(event,action,actionType));

        action="getRuleById";
        assertTrue(ruleService.ruleExecute(event,action,actionType));

        actionType = "query";
        action="insert into user values(?+15,?,?,?,?,?,?,?)";
        assertTrue(ruleService.ruleExecute(event,action,actionType));

        action="update user set mail = 'sravya.m@iiitb.org' where id=?";
        assertTrue(ruleService.ruleExecute(event,action,actionType));

        action="delete from user where id=?+15";
        assertTrue(ruleService.ruleExecute(event,action,actionType));

    }

8. rulePreprocessing
	This method calls process() for each rule in xml file.
	Testing if it is successful after internal function calls.

    @Test @Order(8)
    public void rulePreprocessing() throws Exception {
        //System.out.println(dataSource);
        ruleService.setDataSource(dataSource);
        ruleService.setJavaMailSender(javaMailSender);

        assertEquals(1,ruleService.rulePreprocessing());
    }

9. sendMailTest
	This method tests sendmail(toMail, context)
	decision statements based on the "context"
	Tried to cover the paths using symbolic variables.

    @Test @Order(9)
    void sendMailTest(){

        ruleService.setJavaMailSender(javaMailSender);

        String toMail="sravya.m@iiitb.org";
        String context;

        // 1 sendmail pwdchange
        context="pwdchange";
        assertEquals("Password change required",ruleService.sendmail(toMail,context));

        // 2 sendmail offers
        context="offers";
        assertEquals("Exciting Offers",ruleService.sendmail(toMail,context));
    }

}

CONTRIBUTION BY TEAM MATES:

	MT2019114: ruleExecute()
		 : sendMailTest()
		 : rulePreprocessing()
		 : deleteRuleTest()

	MT2019120: processTest()
		 : RulebaseMarshallTest()
		 : RulebaseUnmarshallTest()
		 : getRuleByIdTest()
		 : updateRuleTest()

THANK YOU

