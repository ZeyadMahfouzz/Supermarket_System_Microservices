import React, { useState, useEffect, useCallback } from 'react';
import { Package, Clock, CheckCircle, XCircle, TruckIcon } from 'lucide-react';
import { ordersAPI } from '../api/services';
import { useAuth } from '../contexts/AuthContext';
import Spinner from '../components/common/Spinner';

const OrdersPage = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedStatus, setSelectedStatus] = useState('ALL');
  const { isAdmin } = useAuth();

  const fetchOrders = useCallback(async () => {
    try {
      setLoading(true);
      let data;

      if (selectedStatus === 'ALL') {
        data = isAdmin ? await ordersAPI.getAllOrders() : await ordersAPI.getUserOrders();
      } else {
        data = await ordersAPI.getOrdersByStatus(selectedStatus);
      }

      console.log('Fetched orders:', data);
      setOrders(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error('Error fetching orders:', error);
      console.error('Error response:', error.response?.data);
      setOrders([]);
    } finally {
      setLoading(false);
    }
  }, [selectedStatus, isAdmin]);

  useEffect(() => {
    fetchOrders();
  }, [fetchOrders]);

  const handleUpdateStatus = async (orderId, newStatus) => {
    try {
      await ordersAPI.updateOrderStatus(orderId, newStatus);
      fetchOrders(); // Refresh orders
      alert('Order status updated successfully!');
    } catch (error) {
      console.error('Error updating status:', error);
      alert(error.response?.data?.error || 'Failed to update order status');
    }
  };

  const handleCancelOrder = async (orderId) => {
    if (!window.confirm('Are you sure you want to cancel this order?')) return;

    try {
      await ordersAPI.cancelOrder(orderId);
      fetchOrders(); // Refresh orders
      alert('Order cancelled successfully!');
    } catch (error) {
      console.error('Error cancelling order:', error);
      alert(error.response?.data?.error || 'Failed to cancel order');
    }
  };

  const getStatusIcon = (status) => {
    switch (status) {
      case 'PENDING': return <Clock className="h-5 w-5 text-yellow-600" />;
      case 'CONFIRMED': return <CheckCircle className="h-5 w-5 text-blue-600" />;
      case 'SHIPPED': return <TruckIcon className="h-5 w-5 text-purple-600" />;
      case 'DELIVERED': return <CheckCircle className="h-5 w-5 text-green-600" />;
      case 'CANCELLED': return <XCircle className="h-5 w-5 text-red-600" />;
      default: return <Package className="h-5 w-5 text-gray-600" />;
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'PENDING': return 'bg-yellow-100 text-yellow-800';
      case 'CONFIRMED': return 'bg-blue-100 text-blue-800';
      case 'SHIPPED': return 'bg-purple-100 text-purple-800';
      case 'DELIVERED': return 'bg-green-100 text-green-800';
      case 'CANCELLED': return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const statusFilters = ['ALL', 'PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED'];

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <Spinner size="xl" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-4xl font-bold text-gray-800 mb-2">
            {isAdmin ? 'All Orders' : 'My Orders'}
          </h1>
          <p className="text-gray-600">
            {isAdmin ? 'Manage all customer orders' : 'View and track your orders'}
          </p>
        </div>

        {/* Status Filters */}
        <div className="mb-6 flex flex-wrap gap-2">
          {statusFilters.map((status) => (
            <button
              key={status}
              onClick={() => setSelectedStatus(status)}
              className={`px-4 py-2 rounded-lg font-medium transition ${
                selectedStatus === status
                  ? 'bg-blue-600 text-white shadow-md'
                  : 'bg-white text-gray-700 hover:bg-gray-100 border border-gray-300'
              }`}
            >
              {status}
            </button>
          ))}
        </div>

        {/* Orders List */}
        {orders.length === 0 ? (
          <div className="bg-white rounded-lg shadow-md p-12 text-center">
            <Package className="h-16 w-16 text-gray-400 mx-auto mb-4" />
            <h3 className="text-xl font-semibold text-gray-900 mb-2">No orders found</h3>
            <p className="text-gray-600">
              {selectedStatus === 'ALL'
                ? "You haven't placed any orders yet"
                : `No orders with status: ${selectedStatus}`}
            </p>
          </div>
        ) : (
          <div className="space-y-4">
            {orders.map((order) => (
              <div key={order.id} className="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition">
                <div className="flex items-start justify-between mb-4">
                  <div className="flex items-center space-x-3">
                    {getStatusIcon(order.status)}
                    <div>
                      <h3 className="text-lg font-semibold text-gray-900">
                        Order #{order.id}
                      </h3>
                      {isAdmin && (
                        <p className="text-sm text-gray-600">User ID: {order.userId}</p>
                      )}
                    </div>
                  </div>

                  <span className={`px-3 py-1 rounded-full text-sm font-medium ${getStatusColor(order.status)}`}>
                    {order.status}
                  </span>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
                  <div>
                    <p className="text-sm text-gray-500">Total Amount</p>
                    <p className="text-lg font-bold text-blue-600">EGP {order.totalAmount?.toFixed(2)}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">Payment Method</p>
                    <p className="text-md font-medium text-gray-900">{order.paymentMethod}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">Order Date</p>
                    <p className="text-md font-medium text-gray-900">
                      {new Date(order.orderDate).toLocaleDateString()}
                    </p>
                  </div>
                </div>

                {/* Admin Controls */}
                {isAdmin && order.status !== 'CANCELLED' && order.status !== 'DELIVERED' && (
                  <div className="flex gap-2 mt-4 pt-4 border-t border-gray-200">
                    <select
                      onChange={(e) => handleUpdateStatus(order.id, e.target.value)}
                      className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:border-blue-500"
                      defaultValue=""
                    >
                      <option value="" disabled>Update Status</option>
                      <option value="PENDING">PENDING</option>
                      <option value="CONFIRMED">CONFIRMED</option>
                      <option value="SHIPPED">SHIPPED</option>
                      <option value="DELIVERED">DELIVERED</option>
                      <option value="CANCELLED">CANCELLED</option>
                    </select>
                  </div>
                )}

                {/* User Controls */}
                {!isAdmin && order.status === 'PENDING' && (
                  <div className="flex gap-2 mt-4 pt-4 border-t border-gray-200">
                    <button
                      onClick={() => handleCancelOrder(order.id)}
                      className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition font-medium"
                    >
                      Cancel Order
                    </button>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default OrdersPage;

