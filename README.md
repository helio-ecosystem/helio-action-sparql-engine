# SPARQL Engine

This action allows querying RDF data or remote SPARQL endpoints using the `SERVICE` statement.

## Register component

Use the following coordinates for import this action:

````json
{
    "source": "https://github.com/helio-ecosystem/helio-action-sparql-engine/releases/download/v1.2.0/helio-action-sparql-engine-1.2.1.jar",
    "clazz": "helio.action.sparql.query.SparqlEngine",
    "type": "ACTION"
}
````

### Configuration

This action must be provided with a JSON as configuration, specifying the following:
 - 'query' must have as value a valid SPARQL query
 - 'data-format' can be specified if the RDF data to be queried is not in TURTLE, possible values are turtle, ttl, json-ld, json-ld-11, rdf/xml, n-triples, nt, n3.
 - 'output-format' can be specified to change the serialisation of the output report (by default in `JSON` for SELECT and ASK and `TURTLE` for CONSTRUCT and DESCRIBE). Possible values for ASK are json or xml (note that csv and tsv for ASK is not supported). For SELECT possible values are json, csv, tsv, xml. For DESCRIBE and CONSTRUCT are turtle, ttl, json-ld, json-ld-11, rdf/xml, n-triples, nt, n3.
 - 'namespace' can be specified for declaring a URL as namespace for the SPARQL query
 
### Geosparql 
The SPARQL Engine implements the [geosparql features provided by jena](https://jena.apache.org/documentation/geosparql/index.html)
