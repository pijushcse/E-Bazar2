package business.productsubsystem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import middleware.DatabaseException;
import middleware.DbConfigProperties;
import middleware.dataaccess.DataAccessSubsystemFacade;
import middleware.externalinterfaces.IDbClass;
import middleware.externalinterfaces.DbConfigKey;
import middleware.externalinterfaces.IDataAccessSubsystem;

/**
 * @author pcorazza
 * <p>
 * Class Description:
 */
public class DbClassCatalogTypes implements IDbClass {

    private String query;
    private String queryType;
    final String GET_TYPES = "GetTypes";
    final String GET_TYPES_ID = "GetTypesId";
    private CatalogTypes types;
    private String catalogName; 
    private Integer catalogId;

    private IDataAccessSubsystem dataAccessSS = new DataAccessSubsystemFacade();

    public CatalogTypes getCatalogTypes() throws DatabaseException {
        dataAccessSS.createConnection(this);
        dataAccessSS.iread();
        return types;
    }
    
    public Integer getCatalogId()throws DatabaseException {
        dataAccessSS.createConnection(this);
        dataAccessSS.iread();
        return catalogId;
    }

    public void buildQuery() {
        System.out.println(queryType + " : GET_TYPES " + GET_TYPES);
        if (queryType.equals(GET_TYPES)) {
            buildGetTypesQuery();
        } else if (queryType.equals(GET_TYPES_ID)) {
            buildCreateTypeIdQuery();
        } 
    }

    void buildCreateTypeIdQuery() {
        query = "SELECT catalogid FROM CatalogType WHERE catalogname='"+this.catalogName+"' ";
    }    
        
    void buildGetTypesQuery() {
        query = "SELECT * FROM CatalogType";
    }

    /**
     * This is activated when getting all catalog types.
     */
    public void populateEntity(ResultSet resultSet) throws DatabaseException {

        if (queryType.equals(GET_TYPES_ID)) {
            try {
                if (resultSet.next()) {
                    this.catalogId = resultSet.getInt(1);
                }
            } catch (SQLException ex) {
                Logger.getLogger(DbClassCatalogTypes.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else if (queryType.equals(GET_TYPES)) {
            types = new CatalogTypes();
            try {
                while (resultSet.next()) {
                    int tempId = resultSet.getInt(1);
                    String tempCatalogName = resultSet.getString(2);
                    types.addCatalog(tempId, tempCatalogName);
                }
            } catch (SQLException ex) {
                throw new DatabaseException(ex);
            }
        }
    }

    public String getDbUrl() {
        DbConfigProperties props = new DbConfigProperties();
        return props.getProperty(DbConfigKey.PRODUCT_DB_URL.getVal());
    }

    public String getQuery() {

        return query;
    }

    void setQueryType(String types) {
        this.queryType = types;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }
    
}
