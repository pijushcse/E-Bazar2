package middleware.dataaccess;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import middleware.DatabaseException;
import middleware.externalinterfaces.Cleanup;
import middleware.externalinterfaces.IDataAccessSubsystem;
import middleware.externalinterfaces.IDataAccessTest;
import middleware.externalinterfaces.IDbClass;

/**
 * @author pcorazza
 * @since Nov 10, 2004 Class Description:
 *
 *
 */
public class DataAccessSubsystemFacade implements IDataAccessSubsystem, IDataAccessTest {

    private static final Logger LOG = Logger.getLogger(DataAccessSubsystemFacade.class.getPackage().getName());
    DbAction action;
    Connection con;

    public void createConnection(IDbClass dbClass) throws DatabaseException {
        if (dbClass != null) {
            action = new DbAction(dbClass);
            con = action.pool.getConnection(dbClass.getDbUrl());
            LOG.info("Connection ## " + (con== null));
        }
    }

    /**
     * Note: autocommit is set back to true when connection is returned to pool
     */
    public void startTransaction() throws DatabaseException {
        try {
            con.setAutoCommit(false);
        } catch (SQLException e) {
            throw new DatabaseException("DataAccessSubsystemFacade.startTransaction() "
                    + "encountered a SQLException " + e.getMessage());
        }
    }

    public void commit() throws DatabaseException {
        try {
            con.commit();
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    /**
     * This convenience method carries out a typical insert/update within a
     * transaction. To wrap multiple or complex sql operations in a transaction,
     * use startTransaction instead.
     */
    public Integer saveWithinTransaction(IDbClass dbClass) throws DatabaseException {
        createConnection(dbClass);
        startTransaction();
        try {
            int result = save();
            commit();
            return result;
        } catch (DatabaseException e) {
            rollback();
            LOG.warning("Attempting to rollback...");
            throw (e);
        }
    }

    public void rollback() throws DatabaseException {
        try {
            con.rollback();
        } catch (SQLException e) {
            throw new DatabaseException("rollback encountered a SQLException " + e.getMessage());
        }
    }

    public void iread() throws DatabaseException {
        action.performRead();
    }

    public void releaseConnections(Cleanup c) {
        SimpleConnectionPool pool = SimpleConnectionPool.getInstance(c);
        if (pool != null) {
            pool.releaseConnections();
        }
    }

    public Integer save() throws DatabaseException {
        return action.performUpdate();
    }

    public void delete() throws DatabaseException {
        action.performUpdate();
    }

    //Testing interface
    public ResultSet[] multipleInstanceQueries(String[] queries, String[] dburls) throws DatabaseException {
        if (queries == null || dburls == null) {
            return null;
        }
        if (queries.length != dburls.length) {
            return null;
        }
        int numConnections = queries.length;
        ResultSet[] results = new ResultSet[numConnections];
        SimpleConnectionPool pool = SimpleConnectionPool.getInstance(numConnections);
        ArrayList<Connection> cons = new ArrayList<Connection>();
        for (int i = 0; i < numConnections; ++i) {
            cons.add(pool.getConnection(dburls[i]));
        }
        for (int i = 0; i < numConnections; ++i) {
            results[i] = SimpleConnectionPool.doQuery(cons.get(i), queries[i]);
        }
        return results;

    }
}
