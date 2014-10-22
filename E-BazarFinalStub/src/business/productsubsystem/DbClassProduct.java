package business.productsubsystem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import business.util.TwoKeyHashMap;
import business.*;
import business.externalinterfaces.IProductFromDb;
import business.externalinterfaces.IProductFromGui;
import static business.util.StringParse.*;
import java.util.logging.Level;
import middleware.DatabaseException;
import middleware.DbConfigProperties;
import middleware.dataaccess.DataAccessSubsystemFacade;
import middleware.dataaccess.DataAccessUtil;
import middleware.externalinterfaces.IDataAccessSubsystem;
import middleware.externalinterfaces.IDbClass;
import middleware.externalinterfaces.DbConfigKey;

class DbClassProduct implements IDbClass {

    private static final Logger LOG = Logger.getLogger(DbClassProduct.class
            .getPackage().getName());
    private IDataAccessSubsystem dataAccessSS = new DataAccessSubsystemFacade();

    /**
     * The productTable matches product ID with Product object. It is static so
     * that requests for "read product" based on product ID can be handled
     * without extra db hits
     */
    private static TwoKeyHashMap<Integer, String, IProductFromDb> productTable;
    private String queryType;
    private String query;
    private IProductFromDb product;
    private IProductFromGui prodFromGui;
    private List<IProductFromDb> productList;
    private Integer catalogId;
    private Integer productId;
    private String productName; 
    private String productQuantity;
    private String productPricePerUnit;
    private String productMfgDate;
    private String productDescription; 

    private final String LOAD_PROD_TABLE = "LoadProdTable";
    private final String READ_PRODUCT = "ReadProduct";
    private final String READ_PROD_LIST = "ReadProdList";
    private final String INSERT_NEW_PROD = "InsertNewProd";

    public void buildQuery() {
        if (getQueryType().equals(LOAD_PROD_TABLE)) {
            buildProdTableQuery();
        } else if (getQueryType().equals(READ_PRODUCT)) {
            buildReadProductQuery();
        } else if (getQueryType().equals(READ_PROD_LIST)) {
            buildProdListQuery();
        } else if (getQueryType().equals(INSERT_NEW_PROD)) {
            buildProdInsertQuery();
        }
    }

    private void buildProdInsertQuery() {
        query = "INSERT INTO product (catalogid, productname, totalquantity, priceperunit, mfgdate, description) VALUES("+this.catalogId+", '"+this.productName+"' "
                + ", '"+this.productQuantity+"', '"+this.productPricePerUnit+"', '"+this.productMfgDate+"', '"+this.productDescription+"' )";
    }    
    
    private void buildProdTableQuery() {
        query = "SELECT * FROM product";
    }

    private void buildProdListQuery() {
         query = "SELECT * FROM Product WHERE catalogid = " + getCatalogId();
    }

    private void buildReadProductQuery() {
        query = "SELECT * FROM Product WHERE productid = " + getProductId();
    }

    public TwoKeyHashMap<Integer, String, IProductFromDb> readProductTable() throws DatabaseException {
         if (productTable != null) {
            return productTable.clone();
        }
        return refreshProductTable();
    }

    /**
     * Force a database call
     */
    public TwoKeyHashMap<Integer, String, IProductFromDb> refreshProductTable()
            throws DatabaseException {
        setQueryType(LOAD_PROD_TABLE);
        dataAccessSS.createConnection(this);
        dataAccessSS.iread();

        // Return a clone since productTable must not be corrupted
        return productTable.clone();
    }

    public List<IProductFromDb> readProductList(Integer catalogId) throws DatabaseException {
        if (productList == null) {
             return refreshProductList(catalogId);
        }  
        return Collections.unmodifiableList(productList);
    }

    public List<IProductFromDb> refreshProductList(Integer catalogId)
            throws DatabaseException {
        this.setCatalogId(catalogId);
        setQueryType(READ_PROD_LIST);
        dataAccessSS.createConnection(this);
        dataAccessSS.iread();
        return productList;
    }

    public IProductFromDb readProduct(Integer productId)
            throws DatabaseException {
        if (productTable != null && productTable.isAFirstKey(productId)) {
            return productTable.getValWithFirstKey(productId);
        }
        setQueryType(READ_PRODUCT);
        this.setProductId(productId);
        dataAccessSS.createConnection(this);
        dataAccessSS.iread();
        return product;
    }

    /**
     * Database columns: productid, productname, totalquantity, priceperunit,
     * mfgdate, catalogid, description
     */
    public void saveNewProduct(IProductFromGui product, Integer catalogid, String description) throws DatabaseException {
        this.setCatalogId(catalogid);
        this.setProductDescription(description);
        this.setProductName(product.getProductName());
        this.setProductPricePerUnit(product.getUnitPrice());
        this.setProductQuantity(product.getQuantityAvail());
        this.setProductMfgDate(product.getMfgDate()); 
        
        setQueryType(INSERT_NEW_PROD); 
        dataAccessSS.createConnection(this);
        dataAccessSS.saveWithinTransaction(this); 
        //dataAccessSS.commit();

    }

    public void populateEntity(ResultSet resultSet) throws DatabaseException {
        if (getQueryType().equals(LOAD_PROD_TABLE)) {
            populateProdTable(resultSet);
        } else if (getQueryType().equals(READ_PRODUCT)) {
            populateProduct(resultSet);
        } else if (getQueryType().equals(READ_PROD_LIST)) {
            populateProdList(resultSet);
        }
    }

    private void populateProdList(ResultSet rs) throws DatabaseException {
        productList = new LinkedList<IProductFromDb>();

        try {
            IProductFromDb product = null;
            Integer prodId = null;
            String productName = null;
            String quantityAvail = null;
            String unitPrice = null;
            String mfgDate = null;
            Integer catalogId = null;
            String description = null;
            while (rs.next()) {
                prodId = rs.getInt("productid");
                productName = rs.getString("productname");
                quantityAvail = makeString(rs.getInt("totalquantity"));
                unitPrice = makeString(rs.getDouble("priceperunit"));
                mfgDate = rs.getString("mfgdate");
                catalogId = rs.getInt("catalogid");
                description = rs.getString("description");
                product = new Product(prodId, productName, quantityAvail,
                        unitPrice, mfgDate, catalogId, description);
                productList.add(product);
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * Internal method to ensure that product table is up to date.
     */
    private void populateProdTable(ResultSet rs) throws DatabaseException {
        try {
            //IMPLEMENT
            productTable = new TwoKeyHashMap<Integer, String, IProductFromDb>();
            while(rs.next()){
                int tempProductId = rs.getInt(1) ; 
                int tempCatalogId = rs.getInt(2) ; 
                String tempProductName = rs.getString(3); 
                int tempQuantity = rs.getInt(4);
                double tempPriceUnit =rs.getDouble(5);
                String tempMfgDate = rs.getString(6);
                String tempDescrption =rs.getString(7);
                IProductFromDb prod = new Product(tempProductId, tempProductName, ""+tempQuantity, ""+tempPriceUnit, tempMfgDate, tempCatalogId, tempDescrption);
                productTable.put(tempProductId, tempProductName, prod);                               
            }
        } catch (SQLException ex) {
            Logger.getLogger(DbClassProduct.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void populateProduct(ResultSet rs) throws DatabaseException {
        //IMPLEMENT
        product = new Product(1, "", "", "", "", 1, "");
    }

    public String getDbUrl() {
        DbConfigProperties props = new DbConfigProperties();
        return props.getProperty(DbConfigKey.PRODUCT_DB_URL.getVal());
    }

    public String getQuery() {
        return query;
    }

    /**
     * @return the queryType
     */
    public String getQueryType() {
        return queryType;
    }

    /**
     * @param queryType the queryType to set
     */
    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }

    public Integer getCatalogId() {
        return catalogId;
    }

    public void setCatalogId(Integer catalogId) {
        this.catalogId = catalogId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(String productQuantity) {
        this.productQuantity = productQuantity;
    }

    public String getProductPricePerUnit() {
        return productPricePerUnit;
    }

    public void setProductPricePerUnit(String productPricePerUnit) {
        this.productPricePerUnit = productPricePerUnit;
    }

    public String getProductMfgDate() {
        return productMfgDate;
    }

    public void setProductMfgDate(String productMfgDate) {
        this.productMfgDate = productMfgDate;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

}
