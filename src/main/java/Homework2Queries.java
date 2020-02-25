import org.apache.jena.query.*;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.sparql.core.Quad;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
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
        System.out.println("****************** Start SELECT Queries *************************");
        selectQuery1(owlSchema);
        selectQuery2(owlSchema);
        selectQuery3(owlSchema);
        selectQuery4(owlSchema);
        selectQuery5(owlSchema);
        System.out.println("****************** End SELECT Queries *************************");

        System.out.println("****************** Start CONSTRUCT Queries *************************");
        constructQuery1(owlSchema);
        constructQuery2(owlSchema);
        System.out.println("****************** End CONSTRUCT Queries *************************");

        System.out.println("****************** Start ASK Query *************************");
        askQuery1(owlSchema);
        System.out.println("****************** End ASK Query *************************");
    }

    private String getFilePath() {
        return this.getClass().getResource("hw2.ttl").getFile();
    }

    private static void selectQuery1(InfModel owlSchema) {
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

    private static void selectQuery2(InfModel owlSchema) {
        String queryString = PREFIX +
                            "SELECT ?name WHERE {" +
                            "?prof a univ:Professor ;" +
                            "vcard:title 'Doctor';" +
                            "foaf:name ?name}";
        selectQuery(getQueryExecution(owlSchema, queryString) ,
                    "Who are the professors who have the title of 'Doctor'?\n",
                    Arrays.asList("?name"));
    }

    private static void selectQuery3(InfModel owlSchema) {
        String queryString = PREFIX +
                                "SELECT ?title ?days WHERE {" +
                                "?course a univ:Course ;" +
                                "ex:name ?title ." +
                                "OPTIONAL {?course ex:days ?days}}";
        selectQuery(getQueryExecution(owlSchema, queryString),
                "What classes are offered and which days (if known) are they available?\n",
                    Arrays.asList("?title", "?days"));
    }

    private static void selectQuery4(InfModel owlSchema) {
        String queryString = PREFIX +
                            "SELECT ?name ?staffId WHERE {" +
                            "?student a univ:Student ;" +
                            "foaf:name ?name ;" +
                            "univ:staffId ?staffId ; }";
        selectQuery(getQueryExecution(owlSchema, queryString),
                    "What are the names of the lab assistants and what are their univ ids?\n",
                    Arrays.asList("?name", "?staffId"));
    }

    private static void selectQuery5(InfModel owlSchema) {
        String queryString = PREFIX +
                            "SELECT ?name ?email ?phone WHERE {" +
                            "?prof a univ:Professor ;" +
                            "univ:title 'Department Chair';" +
                            "foaf:name ?name ;" +
                            "univ:e-mail ?email ;" +
                            "foaf:phone ?phone ;}";
        selectQuery(getQueryExecution(owlSchema, queryString),
                    "Who is the department chair and what is their contact info?\n",
                    Arrays.asList("?name", "?email", "?phone"));
    }

   private static void constructQuery1(InfModel owlSchema) {
        String queryString = PREFIX +
                            "CONSTRUCT {?student_name ex:status 'Part Time Student Staff' }" +
                            "WHERE {?student a univ:Student ;" +
                            "foaf:name ?student_name;" +
                            "univ:staffId ?staffId}";
        constructQuery(getQueryExecution(owlSchema, queryString), "Is there a graph of students who are part time staff?\n");
   }

   private static void constructQuery2(InfModel owlSchema) {
        String queryString = PREFIX +
                            "CONSTRUCT {?student_name ex:tutors ?course_name }" +
                             "WHERE {?student a univ:Student ;" +
                                "foaf:name ?student_name;" +
                                "univ:helpsWith ?course ." +
                                "?course ex:name ?course_name}";
        constructQuery(getQueryExecution(owlSchema, queryString), "Is there a graph of students who tutor courses?\n");
   }

   private static void askQuery1(InfModel owlSchema) {
        String queryString = PREFIX +
                            "ASK {?student a univ:Student ;" +
                            "univ:helpsWith ?course ." +
                            "?course ex:name 'Computer Architecture'}";
        askQuery(getQueryExecution(owlSchema, queryString), "Is there a student who can help with computer architecture?\n");
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

    /**
     *
     * @param owlSchema
     * @param queryString
     * @return returns the QueryExecution to be used
     */
    private static QueryExecution getQueryExecution(InfModel owlSchema, String queryString) {
        return QueryExecutionFactory.create(QueryFactory.create(queryString), owlSchema);
    }
}
