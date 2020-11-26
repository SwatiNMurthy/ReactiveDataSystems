package com.iiitb.dm.rules;

import java.lang.reflect.*;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.Subject;
import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;


@Service
public class RuleService {
	
	//@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	public void setDataSource(DataSource dataSource){
		this.jdbcTemplate=new JdbcTemplate(dataSource);
	}
	
	//@Autowired
	private JavaMailSender javaMailSender;

	@Autowired
	public  void setJavaMailSender(JavaMailSender javaMailSender){ this.javaMailSender=javaMailSender;}


	/*
	 * Sample JSON request
	 * {
        "rule_description": "Adding extra compartments (seats) on demand",
        "table": "train",
        "event": {
            "event_type": "select",
            "conditions": {
                "condition": [
                    {
                        "attribute": "remaining_seats",
                        "operator": "<=",
                        "value": "5"
                    }
                ],
                "conjunction": "none"
            }
        },
        "action": {
            "action_type": "query",
            "queries": [
                {
                    "query": "update train set remaining_seats=remaining_seats+10 where train_id=?"
                },
                {
                    "query": "update train set remaining_seats=remaining_seats-10 where train_id=?"
                }
            ],
            "method_path": "none"
        },
        "rule_type": "deferred",
        "rule_status": "Active"
        }
	 * */
	
	public boolean ruleBaseMarshall(Rule rule) {
		
        List<Rule> rules = ruleBaseUnmarshall();
        try {
            rules.add(rule);
            RuleBase rulebase = new RuleBase();
            rulebase.setRules(rules);
            JAXBContext context = JAXBContext.newInstance(RuleBase.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(rulebase, new File("RuleBase.xml"));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            return false;
        }
    }

    public List<Rule> ruleBaseUnmarshall() {
        try {
            File file = new File("RuleBase.xml");

            if (file.createNewFile()) {
                List<Rule> temp = new ArrayList<Rule>();
                return temp;
            } else {
                JAXBContext context = JAXBContext.newInstance(RuleBase.class);
                Unmarshaller umarshaller = context.createUnmarshaller();
                RuleBase rulebase = (RuleBase) umarshaller.unmarshal(file);

                List<Rule> rules = new ArrayList<Rule>();

                for (Rule rule : rulebase.getRules()) {
                    rules.add(rule);
                }
                return rules;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public Rule getRuleById(int ruleId) {
    	List<Rule> rules = ruleBaseUnmarshall();
    	for (Rule rule : rules) {
    		if (rule.getRuleId() == ruleId) {
    			return rule;
    		}
    	}
    	return null;
    }
    
    //Deletes rule specified by ruleId, removes the file and puts back
    // all the remaining rules back into the file
    public void deleteRule(int ruleId) {
    	List<Rule> rules = ruleBaseUnmarshall();
    	Rule ruleToBeDeleted=null;
    	for (Rule rule : rules) {
    		if (rule.getRuleId() == ruleId) {
    			ruleToBeDeleted = rule;
    			break;
    		}
    	}
    	rules.remove(ruleToBeDeleted);
    	File file = new File("RuleBase.xml");
    	file.delete();
    	
    	for (Rule rule : rules) {
    		ruleBaseMarshall(rule);
    	}
    }
    
    public void updateRule(Rule rule, int ruleId) {
    	deleteRule(ruleId);
    	ruleBaseMarshall(rule);
    }

    public int process(Rule rule){
        System.out.println(rule.getRule_status());
        System.out.println(rule.getRuleId());
        if(rule.getRule_status().equals("Active"))
        {

            String event="";
            String action="";
            String actiontype=rule.getAction().getAction_type();

            event="Select *from "+rule.getTable()+" where ";

            // for each condition, getting the attribute, operator, value and appending with next conditions
            int count=(rule.getEvent().getConditions().getCondition()).size();
            for(Condition con:rule.getEvent().getConditions().getCondition())
            {
                event=event+con.getAttribute()+con.getOperator()+con.getValue();
                if(count>1)
                {
                    event=event+rule.getEvent().getConditions().getConjunction();
                }
                count--;
            }

            if (actiontype.equals("method")) {
                System.out.println("method");
                action=rule.getAction().getMethod_path();
                try {
                    ruleExecute(event,action,actiontype);
                    return 1;
                }
                catch (Exception e) {
                    // TODO: handle exception
                    return 0;
                }
            } else {
                for (Query query : rule.getAction().getQueries()) {
                    try {
                        ruleExecute(event,query.getQuery(),actiontype);
                        return 1;
                    }
                    catch (Exception e) {
                        System.err.println("Error : "+e);
                        return 0;
                    }
                }
            }
        }
        else
            return 0;

        return 1;
    }
    // function for forming the event, action queries for each rule
    public int rulePreprocessing() {
    	
    	// this list gives json object addresses on printing
    	List<Rule> rules=ruleBaseUnmarshall();
    	
    	// so converting the contents of each rule to an event and action query and passing to execute function
    	for(Rule rule:rules)
    	{
			process(rule);
    	}
    	return 1;
    }
    
    public String sendmail(String toMail,String context) throws MailException
    {
    	System.out.println("Begin mail");
    	System.out.println(toMail);
    	String subjectString="";
    	String textString="";
    
    	try {
    	
	    	SimpleMailMessage msgMailMessage=new SimpleMailMessage();
	    	
	    	//msgMailMessage.setTo("toMail");
            // sending to a static mail id to avoid spam for other users now
            msgMailMessage.setTo("dm.reactive@gmail.com");
	    	if(context.equals("pwdchange"))
	    	{
	    		subjectString="Password change required";
	    		textString="Please change your password. It has been 30 days since you last changed it";
	    	}
	    	else if(context.contains("offers")){
	    		subjectString="Exciting Offers";
	    		textString="We have exciting offers on ticket booking, please go to our website";
			}
	    	System.out.println(subjectString);
	    	
	    	msgMailMessage.setSubject(subjectString);
	    	msgMailMessage.setText(textString);
	    	
	    	System.out.println("Prepared mail");
	        
	    	javaMailSender.send(msgMailMessage);
	        System.out.println("Done mailing");

	        return subjectString;
    	}
    	catch(Exception e) {
    		System.out.println(e.toString());
    		return "";
    	}
    }

    // function for executing the action
    public boolean ruleExecute(String event,String action,String actiontype) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException
    {
    	
    	String actionSelectString="";
    	if(actiontype.equals("query"))
    	{
	    	if(action.contains("insert"))
	    		actionSelectString="Select * from "+(action.split(" "))[2]+"";
	    	else if(action.contains("update"))
	    		actionSelectString="Select * from "+(action.split(" "))[1]+"";
	    	else
				actionSelectString="Select * from "+(action.split(" "))[2]+"";
    	}
    	else if(actiontype.equals("method"))
    		actionSelectString=event;
		
		try {
				System.out.println(jdbcTemplate);
				// finding the column names of action query table
				ResultSetMetaData actionMetaData=jdbcTemplate.query(actionSelectString, new ResultSetExtractor<ResultSetMetaData>(){
					@Override
                     public ResultSetMetaData extractData(ResultSet rs) throws SQLException, DataAccessException {
						ResultSetMetaData rsmd = rs.getMetaData();
                            return rsmd;
                        }
					});

				// finding ResultSet of event query and for each row applying action *********
                jdbcTemplate.query(event,new ResultSetExtractor<ResultSet>() {

                	@Override
                	public ResultSet extractData(ResultSet eventResultSet) throws SQLException, DataAccessException {
                		
                		// for insert action
                		if(action.contains("insert"))
                		{	// for each row of result set
	                		while(eventResultSet.next())
	                		{
	                			try {
	                            	jdbcTemplate.update(new PreparedStatementCreator() {
	                            		 
	                                    public PreparedStatement createPreparedStatement(Connection con)
	                                            throws SQLException {
	                                        PreparedStatement stmt = con.prepareStatement(action);
	                                        System.out.println(stmt);
	                                        
	                                        String parse=action.replace(", ", "");
	                                        System.out.println(parse);
	                                        // setting the ? as ordinal parameters
	                                        for(int j=1,k=1;j<=actionMetaData.getColumnCount();j++)
	                                        {
	                                        	if((parse.split(",")[j-1]).contains("?"))
	                                        	{
	                                                stmt.setString(k, eventResultSet.getString(actionMetaData.getColumnLabel(j)));
	                                                k++;
	                                        	}
	                                        }
	                                        System.out.println(stmt);
	                                        return stmt;
	                                    }
	                                });
	                            	}
	                            	catch(Exception e) {
	                                    e.printStackTrace();
	                                }
	                		}
                		}
                		else if(action.contains("update")||action.contains("delete"))
                		{
                			while(eventResultSet.next())
	                		{
	                			try {
	                            	jdbcTemplate.update(new PreparedStatementCreator() {
	                            		 
	                                    public PreparedStatement createPreparedStatement(Connection con)
	                                            throws SQLException {
	                                        PreparedStatement stmt = con.prepareStatement(action);
	                                        
	                                        System.out.println(stmt);
	                                        
	                                        stmt.setInt(1, eventResultSet.getInt(actionMetaData.getColumnLabel(1)));
	                                        
	                                        System.out.println(stmt);
	                                        return stmt;
	                                    }
	                                });
	                            	}
	                            	catch(Exception e) {
	                                    e.printStackTrace();
	                                }
	                		}
                		}
                		else if(actiontype.equals("method"))
                		{
                			System.out.println(action);
                			RuleService cls = new RuleService();
                	        Class c = cls.getClass();
							int i=0;
							Method[] methods = RuleService.class.getMethods();
							for(i=0;i<methods.length;i++){
								if(methods[i].toString().contains(action))
								{
									System.out.println("method found");
									break;
								}
							}
                	        
                	        if(action.contains("sendmail"))
                	        {
                	        	while(eventResultSet.next())
    	                		{
                	        		sendmail(eventResultSet.getString("mail"),(action.split(" "))[1]);
    	                		}
                	        }
                	        else {
								while(eventResultSet.next()) {
									try {
										methods[i].invoke(cls,0);
									} catch (Exception e) {
										System.out.println(e.getCause());
									}
								}
                	        }
                		}
						return eventResultSet;
	                        }});
                // 	CLOSE***************** finding ResultSet of event query and for each row applying action *********
					return true;
				}
				catch(Exception e) {
					e.printStackTrace();
					return false;
        		}
	}


}