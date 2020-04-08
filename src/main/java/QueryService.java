import org.apache.jena.query.*;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.sparql.core.Quad;

import java.util.Iterator;

/**
 * Class contains methods to allow
 * for easier querying. Encapsulates creation of
 * objects necessary for querying.
 */
public class QueryService {
    private Model model;
    private Reasoner reasoner;
    private InfModel infModel;

    /**
     * Method used to build QueryService object
     * @param filepath
     * @return Query Service
     */
    public static QueryService buildOwlQueryService(String filepath) {
        QueryService queryService = new QueryService();

        Model model = ModelFactory.createOntologyModel();
        try {
            model.read(queryService.getFilePath(filepath));
        } catch (Exception ex) {
            System.err.println("welp this sucks: " + ex);
        }

        Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
        reasoner = reasoner.bindSchema(model);
        InfModel owlSchema = ModelFactory.createInfModel(reasoner, model);

        queryService.setModel(model);
        queryService.setReasoner(reasoner);
        queryService.setInfModel(owlSchema);

        return queryService;
    }

    private QueryService() {
        // private constructor to be used by builder
    }

    public void selectQuery(QueryObject queryObject) {
        selectQuery(getQueryExecution(this.infModel, queryObject.queryString), queryObject.message);
    }

    public void constructQuery(QueryObject queryObject) {
        constructQuery(getQueryExecution(this.infModel, queryObject.queryString), queryObject.message);
    }

    public void askQuery(QueryObject queryObject) {
        askQuery(getQueryExecution(this.infModel, queryObject.queryString), queryObject.message);
    }

    public void describeQuery(QueryObject queryObject) {
        describeQuery(getQueryExecution(this.infModel, queryObject.queryString), queryObject.message);
    }

    public void printSchema() {
        infModel.write(System.out, "TTL");
    }

    /**
     * Method will execute a select query and print out the resultSet
     * @param queryExecution - object used to execute the select query
     * @param question - question to print to console
     */
    private void selectQuery(QueryExecution queryExecution, String question) {
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
    private void constructQuery(QueryExecution queryExecution, String message) {
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

    private void askQuery(QueryExecution queryExecution, String message) {
        System.out.println(message);
        try {
            System.out.println(queryExecution.execAsk() ? "Yes" : "Nope");
        } finally {
            queryExecution.close();
        }
    }

    private void describeQuery(QueryExecution queryExecution, String message) {
        System.out.println(message);
        try {
            queryExecution.execDescribe().write(System.out, "TTL");
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
    private QueryExecution getQueryExecution(InfModel owlSchema, String queryString) {
        return QueryExecutionFactory.create(QueryFactory.create(queryString), owlSchema);
    }

    private String getFilePath(String fileName) {
        return this.getClass().getResource(fileName).getFile();
    }

    private void setModel(Model model) {
        this.model = model;
    }

    private void setReasoner(Reasoner reasoner) {
        this.reasoner = reasoner;
    }

    private void setInfModel(InfModel infModel) {
        this.infModel = infModel;
    }
}
