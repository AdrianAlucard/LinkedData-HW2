import org.apache.jena.arq.querybuilder.DescribeBuilder;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.riot.resultset.rw.ResultsReader;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.describe.DescribeBNodeClosure;
import org.apache.jena.sparql.core.describe.DescribeBNodeClosureFactory;
import org.apache.jena.sparql.core.describe.DescribeHandlerFactory;
import org.apache.jena.sparql.core.describe.DescribeHandlerRegistry;

import java.util.*;

public class Homework2Queries {
    private static String PREFIX = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
            "PREFIX foaf: <http://xmlns.com/foaf/0.1/>" +
            "PREFIX univ: <http://www.cs.ccsu.edu/~neli/university.owl#>" +
            "PREFIX vcard: <http://www.w3.org/vcard/ns#>" +
            "PREFIX ex: <http://example.org/>";
    private static List<QueryObject> queryObjects;

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
        System.out.println("****************** Start SELECT Queries *************************");
        queryObjects.stream()
                    .filter(queryObject -> queryObject.queryType.equals(QueryObject.QueryType.SELECT))
                    .forEach(queryObject -> selectQuery(queryObject, owlSchema));
        System.out.println("****************** End SELECT Queries *************************");

        System.out.println("****************** Start CONSTRUCT Queries *************************");
        queryObjects.stream()
                    .filter(queryObject -> queryObject.queryType.equals(QueryObject.QueryType.CONSTRUCT))
                    .forEach(queryObject -> constructQuery(queryObject, owlSchema));
        System.out.println("****************** End CONSTRUCT Queries *************************");

        System.out.println("****************** Start ASK Query *************************");
        queryObjects.stream()
                    .filter(queryObject -> queryObject.queryType.equals(QueryObject.QueryType.ASK))
                    .forEach(queryObject -> askQuery(queryObject, owlSchema));
        System.out.println("****************** End ASK Query *************************");

        System.out.println("****************** Start DESCRIBE Query *************************");
        queryObjects.stream()
                    .filter(queryObject -> queryObject.queryType.equals(QueryObject.QueryType.DESCRIBE))
                    .forEach(queryObject -> describeQuery(queryObject, owlSchema));
        System.out.println("****************** End DESCRIBE Query *************************");
    }

    private String getFilePath() {
        return this.getClass().getResource("hw2.ttl").getFile();
    }

    public static void selectQuery(QueryObject queryObject, InfModel owlSchema) {
        selectQuery(getQueryExecution(owlSchema, queryObject.queryString), queryObject.message);
    }

    public static  void constructQuery(QueryObject queryObject, InfModel owlSchema) {
        constructQuery(getQueryExecution(owlSchema, queryObject.queryString), queryObject.message);
    }

    public static void askQuery(QueryObject queryObject, InfModel owlSchema) {
        askQuery(getQueryExecution(owlSchema, queryObject.queryString), queryObject.message);
    }

    public static void describeQuery(QueryObject queryObject, InfModel owlSchema) {
        Map<String, String> prefixes = new HashMap<>();
        prefixes.put("univ", "http://www.cs.ccsu.edu/~neli/university.owl#");
        prefixes.put("foaf", "http://xmlns.com/foaf/0.1/");
        prefixes.put("ex", "http://example.org/");
        DescribeBuilder describeBuilder = new DescribeBuilder()
                .addPrefixes(prefixes)
                .addVar("?prof")
                .addWhere("?prof", "a", "univ:Professor")
                .addWhere("?prof","foaf:name", "Neli Zlatareva");
        describeQuery(QueryExecutionFactory.create(describeBuilder.build(), owlSchema), queryObject.message);
    }

    /**
     * Method will execute a select query and print out the resultSet
     * @param queryExecution - object used to execute the select query
     * @param question - question to print to console
     */
    private static void selectQuery(QueryExecution queryExecution, String question) {
        try {
            ResultSet resultSet = queryExecution.execSelect();
            System.out.println(question);
            System.out.println(ResultSetFormatter.asText(resultSet));
        } finally {
            queryExecution.close();
        }
    }

    /**
     * Executes construct query and prints out the new graph
     * @param queryExecution
     * @param message
     */
    private static void constructQuery(QueryExecution queryExecution, String message) {
        System.out.println(message);
        try {
            Iterator<Quad> triples = queryExecution.execConstructQuads();
            while (triples.hasNext()) {
                Quad quad = triples.next();
                System.out.println(quad.getSubject() + " " + quad.getPredicate() + " " + quad.getObject());
            }
        } finally {
            queryExecution.close();
        }
        System.out.println();
    }

    private static void askQuery(QueryExecution queryExecution, String message) {
        System.out.println(message);
        try {
            System.out.println(queryExecution.execAsk() ? "Yes" : "Nope");
        } finally {
            queryExecution.close();
        }
    }

    private static void describeQuery(QueryExecution queryExecution, String message) {
        System.out.println(message);
        try {
            Iterator<Triple> triples = queryExecution.execDescribeTriples();
            while(triples.hasNext()) {
                Triple triple = triples.next();
                System.out.println(triple.getSubject() + " " + triple.getPredicate() + " " + triple.getObject());
            }
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

    // add objects to queryobjects list
    // using this to reduce code duplication
    static {
        queryObjects = new ArrayList<>();
        queryObjects.add(new QueryObject("Who are the students that can help with C Programming Language?\n" ,
                    PREFIX +
                        "SELECT ?name WHERE {" +
                        "?student a univ:Student ;" +
                        "univ:helpsWith [a univ:Course ;" +
                        "ex:name 'C Programming Language'];" +
                        " foaf:name ?name}", QueryObject.QueryType.SELECT));
        queryObjects.add(new QueryObject("Who are the professors who have the title of 'Doctor'?\n",
                PREFIX +
                "SELECT ?name WHERE {" +
                "?prof a univ:Professor ;" +
                "vcard:title 'Doctor';" +
                "foaf:name ?name}", QueryObject.QueryType.SELECT));
        queryObjects.add(new QueryObject("What classes are offered and which days (if known) are they available?\n",
                PREFIX +
                "SELECT ?title ?days WHERE {" +
                "?course a univ:Course ;" +
                "ex:name ?title ." +
                "OPTIONAL {?course ex:days ?days}}", QueryObject.QueryType.SELECT));
        queryObjects.add(new QueryObject("What are the names of the lab assistants and what are their univ ids?\n",
                PREFIX +
                "SELECT ?name ?staffId WHERE {" +
                "?student a univ:Student ;" +
                "foaf:name ?name ;" +
                "univ:staffId ?staffId ; }", QueryObject.QueryType.SELECT));
        queryObjects.add(new QueryObject("Who is the department chair and what is their contact info?\n",
                PREFIX +
                "SELECT ?name ?email ?phone WHERE {" +
                "?prof a univ:Professor ;" +
                "univ:title 'Department Chair';" +
                "foaf:name ?name ;" +
                "univ:e-mail ?email ;" +
                "foaf:phone ?phone ;}", QueryObject.QueryType.SELECT));
        queryObjects.add(new QueryObject("Is there a graph of students who are part time staff?\n",
                PREFIX +
                "CONSTRUCT {?student_name ex:status 'Part Time Student Staff' }" +
                "WHERE {?student a univ:Student ;" +
                "foaf:name ?student_name;" +
                "univ:staffId ?staffId}", QueryObject.QueryType.CONSTRUCT));
        queryObjects.add(new QueryObject("Is there a graph of students who tutor courses?\n",
                PREFIX +
                        "CONSTRUCT {?student_name ex:tutors ?course_name }" +
                        "WHERE {?student a univ:Student ;" +
                        "foaf:name ?student_name;" +
                        "univ:helpsWith ?course ." +
                        "?course ex:name ?course_name}", QueryObject.QueryType.CONSTRUCT));
        queryObjects.add(new QueryObject("Is there a student who can help with computer architecture?\n",
                PREFIX +
                "ASK {?student a univ:Student ;" +
                "univ:helpsWith ?course ." +
                "?course ex:name 'Computer Architecture'}", QueryObject.QueryType.ASK));
        queryObjects.add(new QueryObject("Describe Professor Neli Zlatareva" ,
                    PREFIX + "DESCRIBE ?prof" +
                            "WHERE {?prof a univ:Professor ;" +
                            "foaf:name Neli Zlatareva" +
                            "} LIMIT 1", QueryObject.QueryType.DESCRIBE));
    }

    static class QueryObject {
        String message;
        String queryString;
        QueryType queryType;

        enum QueryType {
            SELECT, CONSTRUCT, DESCRIBE, ASK
        }

        QueryObject(String message, String queryString, QueryType queryType) {
            this.message = message;
            this.queryString = queryString;
            this.queryType = queryType;
        }
    }
}
