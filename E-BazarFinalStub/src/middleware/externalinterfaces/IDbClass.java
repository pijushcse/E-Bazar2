package middleware.externalinterfaces;

import business.productsubsystem.Product;
import java.sql.ResultSet;

import middleware.DatabaseException;

/**
 * All concrete dbclasses implement this interface
 */
public interface IDbClass {

    public void buildQuery() throws DatabaseException;

    public void populateEntity(ResultSet resultSet) throws DatabaseException;

    public String getDbUrl();

    public String getQuery();
    // public Product readProduct(String id);
}
