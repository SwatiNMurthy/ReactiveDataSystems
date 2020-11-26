package com.iiitb.dm.rules;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
 
@RestController
@CrossOrigin(origins="http://localhost:3000")
public class RulesController {
	@Autowired
	private RuleService ruleService;
	
	@RequestMapping(method=RequestMethod.POST, value="/rules")
	public boolean ruleBaseMarshall(@RequestBody Rule rule) {
		return ruleService.ruleBaseMarshall(rule);
	}
	@RequestMapping(value="/rules")
	public List<Rule> ruleBaseUnmarshall() {
		System.out.println("Got here");
		
		return ruleService.ruleBaseUnmarshall();
	}
	
	@RequestMapping(value="/rules/execute")
	public int ruleExecute() {
		System.out.println("Rule execution started");
		
		// for now, calling runPreprocessing from here
		return ruleService.rulePreprocessing();
	}
	
	@RequestMapping(value="/rules/{ruleId}")
	public Rule getRuleById(@PathVariable int ruleId) {
		return ruleService.getRuleById(ruleId);
	}
	
	@RequestMapping(method=RequestMethod.DELETE, value="/rules/{ruleId}")
	public void deleteRule(@PathVariable int ruleId) {
		ruleService.deleteRule(ruleId);
	}
	
	@RequestMapping(method=RequestMethod.PUT, value="/rules/{ruleId}")
	public void updateRule(@RequestBody Rule rule, @PathVariable int ruleId) {
		ruleService.updateRule(rule, ruleId);
	}
}
