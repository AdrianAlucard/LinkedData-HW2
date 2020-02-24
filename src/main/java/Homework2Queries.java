import org.apache.jena.query.*;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Homework2Queries {

    private static String PREFIX = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
            "PREFIX foaf: <http://xmlns.com/foaf/0.1/>" +
            "PREFIX univ: <http://www.cs.ccsu.edu/~neli/university.owl#>" +
            "PREFIX vcard: <http://www.w3.org/vcard/ns#>" +
            "PREFIX ex: <http://example.org/>";
    public static void main(String[] args) {
        Homework2Queries homework2Queries = new Homework2Queries();
        Model schema = ModelFactory.createOntologyModel();
        try {
            schema.read(homework2Queries.getFilePath());
        } catch(Exception ex) {
            System.err.println("welp this sucks: " + ex);
        }

        Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
        reasoner = reasoner.bindSchema(schema);
        InfModel owlSchema = ModelFactory.createInfModel(reasoner, schema);
//        owlSchema.write(System.out, "TTL");
        query1(owlSchema);
        query2(owlSchema);
        query3(owlSchema);
        query4(owlSchema);
    }

    private String getFilePath() {
        return this.getClass().getResource("hw2.ttl").getFile();
    }

    private static void query1(InfModel owlSchema) {
        String queryString = PREFIX +
                            "SELECT ?name WHERE {" +
                            "?student a univ:Student ;" +
                            "univ:helpsWith [a univ:Course ;" +
                            "ex:name 'C Programming Language'];" +
                            " foaf:name ?name}";
        selectQuery(getQueryExecution(owlSchema, queryString),
                    "Who are the students that can help with C Programming Language?\n",
                    Arrays.asList("?name"));
    }

    private static void query2(InfModel owlSchema) {
        String queryString = PREFIX +
                            "SELECT ?name WHERE {" +
                            "?prof a univ:Professor ;" +
                            "vcard:title 'Doctor';" +
                            "foaf:name ?name}";
        selectQuery(getQueryExecution(owlSchema, queryString) ,
                    "Who are the professors who have the title of 'Doctor'?\n",
                    Arrays.asList("?name"));
    }

    private static void query3(InfModel owlSchema) {
        String queryString = PREFIX +
                                "SELECT ?title ?days WHERE {" +
                                "?course a univ:Course ;" +
                                "ex:name ?title ." +
                                "OPTIONAL {?course ex:days ?days}}";
        selectQuery(getQueryExecution(owlSchema, queryString),
                "What classes are offered and which days (if known) are they available?\n",
                    Arrays.asList("?title", "?days"));
    }

    private static void query4(InfModel owlSchema) {
        String queryString = PREFIX +
                            "SELECT ?name ?staffId WHERE {" +
                            "?student a univ:Student ;" +
                            "foaf:name ?name ;" +
                            "univ:staffId ?staffId ; }";
        selectQuery(getQueryExecution(owlSchema, queryString),
                    "What are the names of the lab assistants and what are their univ ids?\n",
                    Arrays.asList("?name", "?staffId"));
    }

    /**
     * Method will execute a select query and print out the resultSet
     * @param queryExecution - object used to execute the select query
     * @param question - question to print to console
     * @param questionWords - params in the select query to be printed out
     */
    private static void selectQuery(QueryExecution queryExecution, String question, List<String> questionWords) {
        try {
            ResultSet resultSet = queryExecution.execSelect();
            System.out.println(question);
            printResultSet(questionWords, resultSet);
        } finally {
            queryExecution.close();
        }
    }

    /**
     *
     * @param owlSchema
     * @param queryString
     * @return returns the QueryExecution to be used
     */
    private static QueryExecution getQueryExecution(InfModel owlSchema, String queryString) {
        return QueryExecutionFactory.create(QueryFactory.create(queryString), owlSchema);
    }

    /**
     *
     * @param questionWords - question words from the select clause
     * @param resultSet - results from the query
     */
    private static void printResultSet(List<String> questionWords , ResultSet resultSet) {
        final List<RDFNode> rdfNodes = new ArrayList<RDFNode>(questionWords.size());
        while(resultSet.hasNext()) {
            QuerySolution soln = resultSet.nextSolution();
            for(String word: questionWords) {
                rdfNodes.add(soln.get(word));
            }
            rdfNodes.forEach(rdfNode -> System.out.print(rdfNode != null ? rdfNode.toString() + " " : ""));
            System.out.println();
            rdfNodes.clear();
        }
        System.out.println();
    }
}
