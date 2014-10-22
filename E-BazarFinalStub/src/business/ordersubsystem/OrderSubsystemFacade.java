package business.ordersubsystem;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import middleware.DatabaseException;
import business.externalinterfaces.ICustomerProfile;
import business.externalinterfaces.IOrder;
import business.externalinterfaces.IOrderItem;
import business.externalinterfaces.IOrderSubsystem;
import business.externalinterfaces.IShoppingCart;
import business.shoppingcartsubsystem.ShoppingCart;
import middleware.dataaccess.DataAccessSubsystemFacade;
import middleware.externalinterfaces.IDataAccessSubsystem;

public class OrderSubsystemFacade implements IOrderSubsystem {

    private static final Logger LOG= Logger.getLogger(OrderSubsystemFacade.class.getPackage().getName());
    ICustomerProfile custProfile;
    
    private IDataAccessSubsystem dataAccessSS = new DataAccessSubsystemFacade();
        DbClassOrder dbClass = new DbClassOrder();

        
    public OrderSubsystemFacade(ICustomerProfile custProfile) {
        this.custProfile = custProfile;
        dbClass.setCustomerProfile(custProfile);
    }
        ///////////// Interface methods

    ///////////// Convenience methods internal to the Order Subsystem
    List<String> getAllOrderIds() throws DatabaseException {

       dataAccessSS.createConnection(dbClass);
       dbClass.setQueryType("GetOrderIds");  
       dataAccessSS.iread();
       
       return dbClass.getAllOrderIds(custProfile);
    }
    
    public IOrder getOrderSummary(String orderId) throws DatabaseException {
        //dataAccessSS.createConnection(dbClass);
        dbClass.setQueryType("GetOrderData");  
        dbClass.setOrderId(orderId); 
        dataAccessSS.iread();

        return dbClass.getOrderSummary();
    }
    

    public List<IOrderItem> getOrderItem(String orderId) throws DatabaseException {
        //dataAccessSS.createConnection(dbClass);
        dbClass.setQueryType("GetOrderItems");  
        dbClass.setOrderId(orderId); 
        dataAccessSS.iread();

        return dbClass.getOrderItems();
    }
    
    
    List<IOrderItem> getOrderItems(String orderId) throws DatabaseException {
        //need to implement
        return new ArrayList<IOrderItem>();
    }

    Order getOrderData(String orderId) throws DatabaseException {
        //need to implement
        return new Order(1, "11/20/2011", "20.20");
    }

    @Override
    public List<IOrder> getOrderHistory() throws DatabaseException {
        List<IOrder> list = new ArrayList<IOrder>();
        List<IOrder> iOrderSummary = new ArrayList<>();
        List< List<IOrderItem>> iOrderItem = new ArrayList<>();
        
        List<String> orderIdList = getAllOrderIds();
        for (String varOrderId : orderIdList) {
            IOrder tempIOrder  = this.getOrderSummary(varOrderId);
            List<IOrderItem> tempIOrderItem  = this.getOrderItem(varOrderId);
            
            iOrderItem.add(tempIOrderItem); 
            iOrderSummary.add(tempIOrder); 
        }

        for (int i = 0; i < orderIdList.size(); i++) {  
            IOrder iOrder = iOrderSummary.get(i); 
            Order order = new Order(iOrder.getOrderId(), iOrder.getOrderDate(), iOrder.getTotalPrice()); 
            order.setOrderItems(iOrderItem.get(i)); 
            list.add(order) ;
        }        
        
        return list;
    }

    public IOrder createOrder(Integer id, String dateOfOrder, String totalPrice) {
        return new Order(id, dateOfOrder, totalPrice);
    }

    @Override
    public void submitOrder(IShoppingCart shopCart) throws DatabaseException {
		IShoppingCart cart = shopCart;
    }

    @Override
    public IOrderItem createOrderItem(Integer prodId, Integer orderId,
            String quantityReq, String totalPrice) {
        return new OrderItem(prodId, orderId, quantityReq, totalPrice);
    }
}
