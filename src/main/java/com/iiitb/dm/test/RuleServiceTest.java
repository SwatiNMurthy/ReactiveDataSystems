package com.iiitb.dm.test;
import com.iiitb.dm.rules.*;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
@ExtendWith(SpringExtension.class)
class RuleServiceTest {

    RuleService ruleService=new RuleService();
    @Autowired
    DataSource dataSource;

    @Autowired
    JavaMailSender javaMailSender;

    @Test
    @Order(1)
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

    @Test
    @Order(2)
    void ruleBaseUnmarshallTest() {
        try {
            List<Rule> rules = ruleService.ruleBaseUnmarshall();
            assertEquals(false, rules.isEmpty());
        } catch (Exception e) {
            assertEquals("exception", ruleService.ruleBaseUnmarshall());
        }
    }

    @Test
    @Order(3)
    void getRuleByIdTest() {
        //Test case when rule with specified ID is present
        Rule rule = ruleService.getRuleById(0);
        assertEquals(0, rule.getRuleId());

        //Test case when rule with specified ID is not present
        rule = ruleService.getRuleById(125);
        assertEquals(null, rule);
    }

    @Test
    @Order(4)
    void updateRuleTest() {
        Rule rule = ruleService.getRuleById(200);
        rule.setRule_description("New Description");
        rule.setRule_status("Inactive");
        ruleService.updateRule(rule, 200);

        assertEquals("New Description", ruleService.getRuleById(200).getRule_description());
    }

    @Test
    @Order(5)
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

    @Test
    @Order(6)
    void deleteRuleTest() {
        //Delete the rule by ID and check if the rule exists
        ruleService.deleteRule(200);
        assertEquals(null, ruleService.getRuleById(200));
    }

    @Test
    @Order(7)
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

    @Test
    @Order(8)
    public void rulePreprocessing() throws Exception {
        //System.out.println(dataSource);
        ruleService.setDataSource(dataSource);
        ruleService.setJavaMailSender(javaMailSender);

        assertEquals(1,ruleService.rulePreprocessing());
    }

    @Test
    @Order(9)
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