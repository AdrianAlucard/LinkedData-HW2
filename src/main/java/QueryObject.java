public class QueryObject {
    public String message;
    public String queryString;
    public QueryType queryType;

    public QueryObject(String message, String queryString, QueryType queryType) {
        this.message = message;
        this.queryString = queryString;
        this.queryType = queryType;
    }

    enum QueryType {
        SELECT, CONSTRUCT, DESCRIBE, ASK
    }
}
