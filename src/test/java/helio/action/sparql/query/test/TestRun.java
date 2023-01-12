package helio.action.sparql.query.test;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.jsonldjava.shaded.com.google.common.collect.Lists;
import com.google.gson.JsonObject;

import helio.action.sparql.query.SparqlEngine;

public class TestRun {

	// SET TRUE FOR FOLLOWING TRACES
	private boolean showErrorMessages = false;

	// static
	private static final String QUERY_SELECT = "SELECT (count (distinct *) as ?count) { ?s ?p ?o }";
	private static final String QUERY_ASK = "ASK { ?s <https://schema.org/name> ?o }";
	private static final String QUERY_CONSTRUCT = "";
	private static final String QUERY_TOKEN = "query";

	private SparqlEngine engine;
	private String rdf = "@prefix : <http://example.org/resources/> ."
			+ "@prefix schema: <https://schema.org/> ."
			+ ":alice schema:name      \"Alice Cooper\" .\n"
			+ " \n"
			+ ":bob   schema:givenName \"Bob\", \"Robert\" ;\n"
			+ "       schema:lastName  \"Smith\" .\n"
			+ "\n"
			+ ":carol schema:name      \"Carol King\" ;\n"
			+ "       schema:givenName \"Carol\" ;\n"
			+ "       schema:lastName  \"King\" .\n"
			+ "";
	private Model rdfModel = ModelFactory.createDefaultModel();
	@Before
	public void setup() {
		engine = new SparqlEngine();
		rdfModel.read(new ByteArrayInputStream(rdf.getBytes()), null, "turtle");
	}
	
	private String dataTo(String format) {
		Writer w = new StringWriter();
		rdfModel.write(w, format);
		return w.toString();
	}
	
	private String solve(JsonObject config, String rdf) {
		String queryResults= "";
		try {
			engine.configure(config);
			queryResults = engine.run(rdf);
		}catch(Exception e) {
			if(showErrorMessages)
				System.out.println(e.toString());
		}
		return queryResults;
	}
	
	@Test
	public void testNullConfiguration() {
		boolean thrown = false;
		try {
			engine.configure(null);
		}catch(Exception e) {
			if(showErrorMessages)
				System.out.println(e.toString());
			thrown = true; 
		}
		Assert.assertTrue(thrown);
		
	}
	
	@Test
	public void testEmptyConfiguration() {
		JsonObject config = new JsonObject();
		boolean thrown = false;
		try {
			engine.configure(config);
		}catch(Exception e) {
			if(showErrorMessages)
				System.out.println(e.toString());
			thrown = true; 
		}
		Assert.assertTrue(thrown);	
	}
	
	@Test
	public void testConfigurationIncorrectDataFormat() {
		boolean thrown = false;
		JsonObject config = new JsonObject();
		try {
			config.addProperty(QUERY_TOKEN, QUERY_SELECT);
			config.addProperty("data-format", "fake-format");
			engine.configure(config);
		}catch(Exception e) {
			if(showErrorMessages)
				System.out.println(e.toString());
			thrown = true; 
		}
		Assert.assertTrue(thrown);
	}
	
	@Test
	public void testConfigurationIncorrectOutputFormat() {
		boolean thrown = false;
		JsonObject config = new JsonObject();
		try {
			config.addProperty(QUERY_TOKEN, QUERY_SELECT);
			config.addProperty("output-format", "fake-format");
			engine.configure(config);
		}catch(Exception e) {
			if(showErrorMessages)
				System.out.println(e.toString());
			thrown = true; 
		}
		Assert.assertTrue(thrown);
	}
	
	@Test
	public void testConfigurationIncorrectQuery() {
		boolean thrown = false;
		JsonObject config = new JsonObject();
		try {
			config.addProperty(QUERY_TOKEN, " ");
			engine.configure(config);
		}catch(Exception e) {
			if(showErrorMessages)
				System.out.println(e.toString());
			thrown = true; 
		}
		Assert.assertTrue(thrown);
	}
	
	@Test
	public void testQueryConfigurationWithoutFormat() {
		JsonObject config = new JsonObject();
		String query =  "SELECT * WHERE { ?s ?p ?o .}";
		config.addProperty("query",query);

		try {
			engine.configure(config);
		}catch(Exception e) {
			if(showErrorMessages)
				System.out.println(e.toString());
		}
		Assert.assertTrue(engine.getQuery().replaceAll("\n", "").contains("SELECT  *WHERE  { ?s  ?p  ?o }"));	
		Assert.assertTrue(engine.getOutputFormat().contains("application/sparql-results+json"));	
	}
	
	@Test
	public void testQueryConfigurationWithoutFormatII() {
		JsonObject config = new JsonObject();
		String query =  "DESCRIBE <http://localhost/resource>";
		config.addProperty("query",query);

		try {
			engine.configure(config);
		}catch(Exception e) {
			if(showErrorMessages)
				System.out.println(e.toString());
		}

		Assert.assertTrue(engine.getQuery().contains(query));	
		Assert.assertTrue(engine.getOutputFormat().contains("text/turtle"));	
	}
	
	@Test
	public void testRunNullData() {
		boolean thrown = false;
		JsonObject config = new JsonObject();
		try {
			config.addProperty(QUERY_TOKEN, QUERY_SELECT);
			engine.configure(config);
			engine.run(null);
		}catch(Exception e) {
			if(showErrorMessages)
				System.out.println(e.toString());
			thrown = true; 
		}
		Assert.assertTrue(thrown);
	}
	
	@Test
	public void testRunEmptyData() {
		boolean thrown = false;
		JsonObject config = new JsonObject();
		try {
			config.addProperty(QUERY_TOKEN, QUERY_SELECT);
			engine.configure(config);
			String queryResults = engine.run("");
			Assert.assertTrue(queryResults.contains("\"value\": \"0\""));
		}catch(Exception e) {
			if(showErrorMessages)
				System.out.println(e.toString());
			thrown = true; 
		}
		Assert.assertTrue(!thrown);
	}
	
	@Test
	public void testRunQueryDifferentFormats() {
		// TODO: missing json-ld-11
		List<String> formats = Lists.newArrayList("turtle", "n3","n-triples", "nt", "ttl", "json-ld", "rdf/xml");
		boolean match = formats.stream().map(format ->  {
			JsonObject config = new JsonObject();
			config.addProperty(QUERY_TOKEN, QUERY_SELECT);
			config.addProperty("data-format", format);
			String queryResults= solve(config, dataTo(format));
			
			return queryResults.contains("\"value\": \"7\"");
		}).allMatch(elem -> elem.booleanValue()==true);
	
		
		Assert.assertTrue(match);	
	}
	
	
	
	@Test
	public void testRunQueryFormatDefault() {
		JsonObject config = new JsonObject();
		// SELECT
		config.addProperty(QUERY_TOKEN, QUERY_SELECT);
		String queryResults = solve(config, this.rdf);
		Assert.assertTrue(queryResults.contains("\"value\": \"7\""));	
		// ASK
		config.addProperty(QUERY_TOKEN, QUERY_ASK);
		queryResults = solve(config, this.rdf);
		Assert.assertTrue(queryResults.contains("\"boolean\" : true"));	
		// DESCRIBE
		config.addProperty(QUERY_TOKEN, "DESCRIBE <http://example.org/resources/alice>");
		queryResults = solve(config, this.rdf);
		Assert.assertTrue(queryResults.contains("<http://example.org/resources/alice> <https://schema.org/name> \"Alice Cooper\" ."));		
		// CONSTRUCT
		config.addProperty(QUERY_TOKEN, "CONSTRUCT { <http://example.org/resources/cooper> ?p ?o . } WHERE { ?s <https://schema.org/name> \"Alice Cooper\" . ?s ?p ?o . }");
		queryResults = solve(config, this.rdf);
		Assert.assertTrue(queryResults.contains("<http://example.org/resources/cooper> <https://schema.org/name> \"Alice Cooper\" ."));		
	}
	
	@Test
	public void testRunQueryFormatCSVAndTSV() {
		JsonObject config = new JsonObject();
		config.addProperty(QUERY_TOKEN, QUERY_SELECT);
		config.addProperty("output-format", "csv");
		String queryResults= solve(config,rdf);
		Assert.assertTrue(queryResults.contains("7") && queryResults.contains("count") && queryResults.replace("count", "").replace("7", "").isBlank());		
		
		config.addProperty(QUERY_TOKEN, QUERY_SELECT);
		config.addProperty("output-format", "tsv");
		queryResults= solve(config,rdf);
		Assert.assertTrue(queryResults.contains("7") && queryResults.contains("?count") && queryResults.replace("?count", "").replace("7", "").isBlank());		
		
		// ASK queries do not support output in CSV or TSV in jena
	}
	
	@Test
	public void testRunQueryFormatXML() {
		JsonObject config = new JsonObject();
		config.addProperty(QUERY_TOKEN, QUERY_SELECT);
		config.addProperty("output-format", "xml");
		String queryResults= solve(config,rdf);
		Assert.assertTrue(queryResults.contains("<literal datatype=\"http://www.w3.org/2001/XMLSchema#integer\">7</literal>"));		
		
		config.addProperty(QUERY_TOKEN, QUERY_ASK);
		config.addProperty("output-format", "xml");
		queryResults= solve(config,rdf);
		Assert.assertTrue(queryResults.contains("<boolean>true</boolean>"));
	}
	
}
