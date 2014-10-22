package business.ordersubsystem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import static business.util.StringParse.*;

import java.util.List;
import java.util.logging.Logger;

import business.externalinterfaces.IAddress;
import business.externalinterfaces.ICartItem;
import business.externalinterfaces.ICreditCard;
import business.externalinterfaces.ICustomerProfile;
import business.externalinterfaces.IOrder;
import business.externalinterfaces.IOrderItem;
import business.externalinterfaces.IShoppingCart;
import business.util.OrderUtil;
import java.util.logging.Level;
import middleware.DatabaseException;
import middleware.DbConfigProperties;
import middleware.dataaccess.DataAccessSubsystemFacade;
import middleware.externalinterfaces.IDataAccessSubsystem;
import middleware.externalinterfaces.IDbClass;
import middleware.externalinterfaces.DbConfigKey;

public class DbClassOrder implements IDbClass {

    private static final Logger LOG
            = Logger.getLogger(DbClassOrder.class.getPackage().getName());
   // private IDataAccessSubsystem dataAccessSS = new DataAccessSubsystemFacade();
    private String query;
    private String queryType;
    private final String GET_ORDER_ITEMS = "GetOrderItems";
    private final String GET_ORDER_IDS = "GetOrderIds";
    private final String GET_ORDER_DATA = "GetOrderData";
    private ICustomerProfile customerProfile;
    private String orderId;
    private List<String> orderIds;
    private List<IOrderItem> orderItems;
    private IOrder orderSummary;
    private Order orderData;

    public List<String> getAllOrderIds(ICustomerProfile customerProfile) throws DatabaseException {
        this.customerProfile = customerProfile;
        return orderIds;
    }
    
    public IOrder getOrderSummary() throws DatabaseException {
        return orderSummary;
    }    

    public Order getOrderData(String orderId) throws DatabaseException {
        //implement
        orderData = new Order(1, "", "");
        return orderData;
    }

    public List<IOrderItem> getOrderItems() throws DatabaseException {
        return orderItems;
    }

    public void buildQuery() {
        if (queryType.equals(GET_ORDER_ITEMS)) {
            buildGetOrderItemsQuery();
        } else if (queryType.equals(GET_ORDER_IDS)) {
            buildGetOrderIdsQuery();
        } else if (queryType.equals(GET_ORDER_DATA)) {
            buildGetOrderDataQuery();
        }

    }

    private void buildGetOrderDataQuery() {
        query = "SELECT orderid, orderdate, totalpriceamount FROM Ord WHERE orderid = '" + orderId + "'";

    }

    private void buildGetOrderIdsQuery() {
         query = "SELECT orderid FROM Ord WHERE custid = '" + customerProfile.getCustId() + "'";

    }

    private void buildGetOrderItemsQuery() {
        query = "SELECT * FROM OrderItem WHERE orderid = '" + orderId + "'";

    }

    private void populateOrderItems(ResultSet resultSet) throws DatabaseException {
        try {
            orderItems = new LinkedList<IOrderItem>();
            
            while(resultSet.next()){
                
                int tempItemId = resultSet.getInt(1); 
                int tempOrderId = resultSet.getInt(2); 
                int tempProductId = resultSet.getInt(3); 
                int tempQuantity = resultSet.getInt(4); 
                double tempTotalPrice = resultSet.getDouble(5);
                
                 OrderItem tempOrderItem = new OrderItem(tempItemId, tempProductId, tempOrderId, ""+tempQuantity, ""+tempTotalPrice);
                 orderItems.add(tempOrderItem); 
            }
        } catch (SQLException ex) {
            Logger.getLogger(DbClassOrder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void populateOrderIds(ResultSet resultSet) throws DatabaseException {
        try {
            orderIds = new LinkedList<String>();
            while(resultSet.next()){
                orderIds.add(""+resultSet.getInt(1));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DbClassOrder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void populateOrderData(ResultSet resultSet) throws DatabaseException {
        try {
             if(resultSet.next()){
                 int tempOrderId = resultSet.getInt(1);
                 String tempOrderDate = resultSet.getString(2);
                 double tempOrderTotalPrice = resultSet.getDouble(3);                  
                 orderSummary = new Order(tempOrderId, tempOrderDate, ""+tempOrderTotalPrice); 
             }
        } catch (SQLException ex) {
            Logger.getLogger(DbClassOrder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void populateEntity(ResultSet resultSet) throws DatabaseException {
        if (queryType.equals(GET_ORDER_ITEMS)) {
            populateOrderItems(resultSet);
        } else if (queryType.equals(GET_ORDER_IDS)) {
            populateOrderIds(resultSet);
        } else if (queryType.equals(GET_ORDER_DATA)) {
            populateOrderData(resultSet);
        }

    }

    public String getDbUrl() {
        DbConfigProperties props = new DbConfigProperties();
        return props.getProperty(DbConfigKey.ACCOUNT_DB_URL.getVal());

    }

    public String getQuery() {
        return query;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;

    }

    public String getQueryType() {
        return queryType;
    }

    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }

    public ICustomerProfile getCustomerProfile() {
        return customerProfile;
    }

    public void setCustomerProfile(ICustomerProfile customerProfile) {
        this.customerProfile = customerProfile;
    }
        
}
