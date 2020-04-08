import java.util.ArrayList;
import java.util.List;

public class Homework2Queries {
    private static String PREFIX = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
            "PREFIX foaf: <http://xmlns.com/foaf/0.1/>" +
            "PREFIX univ: <http://www.cs.ccsu.edu/~neli/university.owl#>" +
            "PREFIX vcard: <http://www.w3.org/vcard/ns#>" +
            "PREFIX ex: <http://example.org/>";
    private static List<QueryObject> queryObjects;

    public static void main(String[] args) {
        QueryService queryService = QueryService.buildOwlQueryService("hw2.ttl");

        System.out.println("****************** Start SELECT Queries *************************");
        queryObjects.stream()
                    .filter(queryObject -> queryObject.queryType.equals(QueryObject.QueryType.SELECT))
                    .forEach(queryObject -> queryService.selectQuery(queryObject));
        System.out.println("****************** End SELECT Queries *************************");

        System.out.println("****************** Start CONSTRUCT Queries *************************");
        queryObjects.stream()
                    .filter(queryObject -> queryObject.queryType.equals(QueryObject.QueryType.CONSTRUCT))
                    .forEach(queryObject -> queryService.constructQuery(queryObject));
        System.out.println("****************** End CONSTRUCT Queries *************************");

        System.out.println("****************** Start ASK Query *************************");
        queryObjects.stream()
                    .filter(queryObject -> queryObject.queryType.equals(QueryObject.QueryType.ASK))
                    .forEach(queryObject -> queryService.askQuery(queryObject));
        System.out.println("****************** End ASK Query *************************");

        System.out.println("****************** Start DESCRIBE Query *************************");
        queryObjects.stream()
                    .filter(queryObject -> queryObject.queryType.equals(QueryObject.QueryType.DESCRIBE))
                    .forEach(queryObject -> queryService.describeQuery(queryObject));
        System.out.println("****************** End DESCRIBE Query *************************");
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
                    PREFIX + "DESCRIBE ?prof " +
                            "WHERE {?prof a univ:Professor ;" +
                            "foaf:name 'Neli Zlatareva'" +
                            "} LIMIT 1", QueryObject.QueryType.DESCRIBE));
    }
}
