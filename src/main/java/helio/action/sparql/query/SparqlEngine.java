package helio.action.sparql.query;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.sparql.resultset.ResultsFormat;

import com.google.gson.JsonObject;

import helio.blueprints.Action;
import helio.blueprints.exceptions.ActionException;


public class SparqlEngine implements Action {

	private ResultsFormat outputFormat;
	private String query;
	private String dataFormat;
	private String namespace;

	private static final String NT_TOKEN = "nt";
	private static final String TTL_TOKEN = "ttl";
	
	public void configure(JsonObject configuration) {
		if (configuration== null || !configuration.has("query"))
			throw new IllegalArgumentException("Provide a valid configuration containing the key 'query' which value is a SPARQL query.");
		// query retrieve
		String queryStr = configuration.get("query").getAsString();
		try {
			Query query = QueryFactory.create(queryStr);
			if(configuration.has("output-format")) {
				this.outputFormat = ResultsFormat.lookup(configuration.get("output-format").getAsString());
				if(this.outputFormat==null)
					throw new IllegalArgumentException("Provided format is not supported, for SELECT use ('json', 'csv', 'tsv', or 'xml'), for ASK ('json' or 'xml'), and for CONSTRUCT and DESCRIBE ('turtle', 'ttl', 'json-ld', 'rdf/xml' )");
			}else {
				if ((query.isAskType() || query.isSelectType()) && outputFormat == null) {
					outputFormat = ResultsFormat.FMT_RS_JSON;
				} else if ((query.isConstructType() || query.isDescribeType()) && outputFormat == null) {
					outputFormat = ResultsFormat.FMT_RDF_TURTLE;
				} 
			}
			this.query = query.toString();
		} catch (Exception e) {
			throw new IllegalArgumentException(e.getMessage());
		}
		// retrieve other parameters
		if(configuration.has("namespace"))
			this.namespace = configuration.get("namespace").getAsString();
		
		if(configuration.has("data-format")) {
			this.dataFormat = parseFormat(configuration.get("data-format").getAsString()).getName();
		}else {
			this.dataFormat = "TURTLE";
		}
	}

	public String run(String values) throws ActionException {
		try {
			if(values==null)
				throw new ActionException("Provide some RDF data as input for querying.");
			Model model = ModelFactory.createDefaultModel();
			model.read(new ByteArrayInputStream(values.getBytes()), null, dataFormat);
			return queryModel(query, model, outputFormat, namespace);
		} catch (Exception e) {
			throw new ActionException(e.getMessage());
		}
	}

	private String queryModel(String sparql, Model model, ResultsFormat format, String namespace) throws ActionException {
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			Query query = QueryFactory.create(sparql) ;
			QueryExecution qexec =  QueryExecutionFactory.create(query, model);
			// init format if null
			if(format==null && (query.isSelectType() || query.isAskType()))
				format = ResultsFormat.FMT_RS_JSON;
			if(format==null && (query.isConstructType() || query.isDescribeType()))
				format = ResultsFormat.FMT_RDF_TURTLE;
			// proceed solving the query
			if(query.isSelectType()) {
				ResultSetFormatter.output(stream, qexec.execSelect(), format);
			}else if(query.isAskType()) {
				ResultSetFormatter.output(stream, qexec.execAsk(), ResultsFormat.convert(format));
	        }else if(query.isConstructType()) {
	        	RDFFormat formatOutput = toRDFFormat(format);
	        	RDFWriter.create().source(qexec.execConstruct()).format(formatOutput).base(namespace).output(stream);
	        }else if(query.isDescribeType()) {
	        	RDFFormat formatOutput = toRDFFormat(format);
	        	RDFWriter.create().source(qexec.execDescribe()).format(formatOutput).base(namespace).output(stream);
	        }else {
	        	throw new ActionException("Query not supported, provided one query SELECT, ASK, DESCRIBE or CONSTRUCT");
	        }
		}catch(QueryException e) {
			e.printStackTrace();
			throw new ActionException(e.toString());
        }catch(Exception e) {
        	throw new ActionException(e.toString());
        }
        return new String(stream.toByteArray());
	}
	
	private Lang parseFormat(String format) {
		if(Lang.TURTLE.getName().equalsIgnoreCase(format))
			return Lang.TURTLE;
		if(Lang.JSONLD.getName().equalsIgnoreCase(format))
			return Lang.JSONLD;
		if(Lang.JSONLD11.getName().equalsIgnoreCase(format))
			return Lang.JSONLD11;
		if(Lang.N3.getName().equalsIgnoreCase(format))
			return Lang.N3;
		if(Lang.NTRIPLES.getName().equalsIgnoreCase(format))
			return Lang.NTRIPLES;
		if(format.equalsIgnoreCase(NT_TOKEN))
			return Lang.NT;
		if(format.equalsIgnoreCase(TTL_TOKEN))
			return Lang.TTL;
		if(Lang.RDFXML.getName().equalsIgnoreCase(format))
			return Lang.RDFXML;
		throw new IllegalArgumentException("Provided format is not supported, choose one from (case insensitive): turtle, ttl, json-ld, json-ld-11, n3, N-Triples, rdf/xml");
	}

	public String getOutputFormat() {
		return outputFormat.getSymbol();
	}

	public String getQuery() {
		return query;
	}

	public String getDataformat() {
		return dataFormat;
	}


	public String getNamespace() {
		return namespace;
	}

	protected static RDFFormat toRDFFormat(ResultsFormat format) {
		if(ResultsFormat.FMT_RDF_JSONLD.equals(format)) return RDFFormat.JSONLD;
		else if(ResultsFormat.FMT_RDF_TURTLE.equals(format)) return RDFFormat.TURTLE;
		else if(ResultsFormat.FMT_RDF_NT.equals(format)) return RDFFormat.NTRIPLES;
		else if(ResultsFormat.FMT_RDF_NQ.equals(format)) return RDFFormat.NQ;
		else return RDFFormat.NT;
	}

	
}
