package helio.action.sparql.query.test;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;

import helio.action.sparql.query.SparqlEngine;

public class TestGeoRun {
	// SET TRUE FOR FOLLOWING TRACES
		private boolean showErrorMessages = false;

		// static
		private static final String QUERY_SELECT = " "
				+ "		PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  \n"
				+ "		PREFIX wgs: <http://www.w3.org/2003/01/geo/wgs84_pos#>  \n"
				+ "		PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>  \n"
				+ "		PREFIX ssn: <http://purl.oclc.org/NET/ssnx/ssn#>  \n"
				+ "     PREFIX spatialF: <http://jena.apache.org/function/spatial#> \n"
				+ "     PREFIX units: <http://www.opengis.net/def/uom/OGC/1.0/>"
				+ "		SELECT ?point ?point2 ?distance WHERE {\n"
				+ "		?subj wgs:lat ?lat .\n"
				+ "  	?subj wgs:long ?lon .\n"
				+ "  	BIND(spatialF:convertLatLon(?lat, ?lon) as ?point) ."
				+ "  	BIND(spatialF:convertLatLon(32, 35.5) as ?point2) ."
				+ "     BIND( spatialF:distance(?point, ?point2, units:kilometer) as ?distance) . "
				+ "}";
		private static final String QUERY_TOKEN = "query";

		private SparqlEngine engine;
		private String rdf = "<rdf:RDF\n"
				+ "        xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
				+ "        xmlns:geo=\"http://www.w3.org/2003/01/geo/wgs84_pos#\"\n"
				+ "        xmlns:ssn=\"http://purl.oclc.org/NET/ssnx/ssn#\"\n"
				+ "        xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\" > \n"
				+ "   <ssn:ObservationValue rdf:about=\"http://example.com/ObservationValues/GasCO/1450439142\">\n"
				+ "            <geo:location>\n"
				+ "              <rdf:Description rdf:about=\"http://example.com/locations/GasCO/1450439142\">\n"
				+ "                <geo:lat>35.4</geo:lat>\n"
				+ "                <geo:long>32</geo:long>\n"
				+ "              </rdf:Description>\n"
				+ "            </geo:location>\n"
				+ "    <!-- here go more triples -->\n"
				+ "    </ssn:ObservationValue>\n"
				+ "    </rdf:RDF>";
		private Model rdfModel = ModelFactory.createDefaultModel();
		@Before
		public void setup() {
			engine = new SparqlEngine();
			rdfModel.read(new ByteArrayInputStream(rdf.getBytes()), null, "rdf/xml");
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
				e.printStackTrace();
				if(showErrorMessages)
					System.out.println(e.toString());
			}
			return queryResults;
		}
		
		@Test
		public void testRunQueryFormatDefault() {
			JsonObject config = new JsonObject();
			// SELECT
			config.addProperty(QUERY_TOKEN, QUERY_SELECT);
			config.addProperty("data-format", "rdf/xml");
			String queryResults = solve(config, this.rdf);
			System.out.println(QUERY_SELECT);
			System.out.println(">"+queryResults);
			Assert.assertTrue(queryResults.contains("\"value\": \"7\""));
		}
}
