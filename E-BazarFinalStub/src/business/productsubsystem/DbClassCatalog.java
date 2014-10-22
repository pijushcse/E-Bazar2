package business.productsubsystem;

import java.sql.ResultSet;

import middleware.DatabaseException;
import middleware.DbConfigProperties;
import middleware.dataaccess.DataAccessSubsystemFacade;
import middleware.dataaccess.DataAccessUtil;
import middleware.externalinterfaces.IDataAccessSubsystem;
import middleware.externalinterfaces.IDbClass;
import middleware.externalinterfaces.DbConfigKey;

public class DbClassCatalog implements IDbClass {

    private String query;
    private String queryType;
    private final String SAVE = "Save";
    private String catalogName;

    private IDataAccessSubsystem dataAccessSS = new DataAccessSubsystemFacade();

    public void saveNewCatalog(String name) throws DatabaseException {
        this.catalogName = name;
        queryType= SAVE;
        dataAccessSS.createConnection(this);
        dataAccessSS.saveWithinTransaction(this);
    }

    public void buildQuery() throws DatabaseException {
        if (queryType.equals(SAVE)) {
            buildSaveQuery();
        }
    }

    void buildSaveQuery() throws DatabaseException {
        query = "INSERT INTO CatalogType (catalogname) VALUES('" + catalogName + "')";
    }

    public String getDbUrl() {
        DbConfigProperties props = new DbConfigProperties();
        return props.getProperty(DbConfigKey.PRODUCT_DB_URL.getVal());
    }

    public String getQuery() {
        return query;
    }

    public void populateEntity(ResultSet resultSet) throws DatabaseException {
    }

}
