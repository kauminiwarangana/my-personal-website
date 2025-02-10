package com.example.layeredarchitecture.DAO;

import com.example.layeredarchitecture.BluePrint.PlaceOrdersInterface;
import com.example.layeredarchitecture.CrudUtill.DBMGT;
import com.example.layeredarchitecture.db.DBConnection;
import com.example.layeredarchitecture.model.OrderDetailDTO;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class PlaceOrderDaoImpl implements PlaceOrdersInterface {
   ItemDAOImpl itemDaoImpl= new ItemDAOImpl();
    @Override
    public ResultSet loadAllCustomerIds() throws SQLException, ClassNotFoundException {
        return DBMGT.execute("SELECT * FROM Customer");
    }

    @Override
    public ResultSet loadAllItemCodes() throws SQLException, ClassNotFoundException {
        return  DBMGT.execute("SELECT * FROM Item");
    }

    @Override
    public boolean saveOrders(String orderId, LocalDate orderDate, String customerId, List<OrderDetailDTO> orderDetails) throws SQLException, ClassNotFoundException {
        Connection connection= DBConnection.getDbConnection().getConnection();
        try {
               connection.setAutoCommit(false);
                if (!existOrder(orderId)) {
                    if (isOrderSaved(orderId,orderDate,customerId)) {
                        boolean isOrderDetailsSaved = true;
                        for (OrderDetailDTO orderDetail : orderDetails) {
                            if(DBMGT.execute("INSERT INTO OrderDetails (oid, itemCode, unitPrice, qty) VALUES (?,?,?,?)",orderId,orderDetail.getItemCode(),orderDetail.getUnitPrice(),orderDetail.getQty())){
                                int qtyOnHand=itemDaoImpl.getQtyOnHand(orderDetail.getItemCode());
                                if(itemDaoImpl.isItemQtyUpdated(orderDetail.getItemCode(),(qtyOnHand-orderDetail.getQty()))){
                                    isOrderDetailsSaved=true;
                                }else{
                                    isOrderDetailsSaved=false;
                                }
                            }else{
                                System.out.println("OrderDetail isn't saved");
                                isOrderDetailsSaved = false;
                            }
                        }
                        if(isOrderDetailsSaved){
                            connection.commit();
                            return true;
                        }
                    }
                }
                connection.rollback();
                return false;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            return false;
        }finally {
            connection.setAutoCommit(true);
        }
    }

    @Override
    public ResultSet existCustomer(String id) throws SQLException, ClassNotFoundException {
        return DBMGT.execute("SELECT id FROM Customer WHERE id=?", id);
    }

    @Override
    public ResultSet existItem(String code) throws SQLException, ClassNotFoundException {
        return  DBMGT.execute("SELECT code FROM Item WHERE code=?", code);
    }

    @Override
    public ResultSet generateNewOrderId() throws SQLException, ClassNotFoundException {
        return DBMGT.execute("SELECT oid FROM `Orders` ORDER BY oid DESC LIMIT 1");
    }

    @Override
    public ResultSet findItem(String code) throws SQLException, ClassNotFoundException {
        return DBMGT.execute("SELECT * FROM Item WHERE code=?",code);
    }
    private boolean isOrderSaved(String orderId,LocalDate orderDate,String customerId) throws SQLException, ClassNotFoundException {
        return DBMGT.execute("INSERT INTO `Orders` (oid, date, customerID) VALUES (?,?,?)",orderId,orderDate,customerId);
    }
    private boolean existOrder(String oid) throws SQLException, ClassNotFoundException {
        ResultSet resultSet=DBMGT.execute("SELECT oid FROM `Orders` WHERE oid=?",oid);
        if(resultSet.next()){
            return true;
        }
        return false;
    }
}
