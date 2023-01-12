package helio.action.sparql.query.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;

import helio.action.sparql.query.SparqlEngine;

public class TestConfiguration {

	// SET TRUE FOR FOLLOWING TRACES
	private boolean showErrorMessages = true;
	
	
	private SparqlEngine engine ;
	
	@Before
	public void setup() {
		engine = new SparqlEngine();
	}
	
	
	
	
}
