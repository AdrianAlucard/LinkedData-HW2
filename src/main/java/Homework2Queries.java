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
        Query query = QueryFactory.create(queryString);
        QueryExecution queryExecution = QueryExecutionFactory.create(query, owlSchema);
        try {
            ResultSet resultSet = queryExecution.execSelect();
            System.out.println("Who are the students that can help with C Programming Language?\n");
            printResultSet(Arrays.asList("?name"), resultSet);
        } finally {
            queryExecution.close();
        }
    }

    private static void query2(InfModel owlSchema) {
        String queryString = PREFIX +
                            "SELECT ?name WHERE {" +
                            "?prof a univ:Professor ;" +
                            "vcard:title 'Doctor';" +
                            "foaf:name ?name}";
        Query query = QueryFactory.create(queryString);
        QueryExecution queryExecution = QueryExecutionFactory.create(query, owlSchema);
        try {
            ResultSet resultSet = queryExecution.execSelect();
            System.out.println("Who are the professors who have the title of 'Doctor'?\n");
            printResultSet(Arrays.asList("?name"), resultSet);
        } finally {
            queryExecution.close();
        }
    }

    private static void query3(InfModel owlSchema) {
        String queryString = PREFIX +
                                "SELECT ?title ?days WHERE {" +
                                "?course a univ:Course ;" +
                                "ex:name ?title ." +
                                "OPTIONAL {?course ex:days ?days}}";
        Query query = QueryFactory.create(queryString);
        QueryExecution queryExecution = QueryExecutionFactory.create(query, owlSchema);
        try {
            ResultSet resultSet = queryExecution.execSelect();
            System.out.println("What classes are offered and which days (if known) are they available?\n");
            printResultSet(Arrays.asList("?title", "?days"), resultSet);
        } finally {
            queryExecution.close();
        }
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
