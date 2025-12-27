import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Package, Calendar, CreditCard, CheckCircle } from 'lucide-react';
import { ordersAPI } from '../api/services';
import Card from '../components/common/Card';
import Spinner from '../components/common/Spinner';

const OrderDetailsPage = () => {
  const { orderId } = useParams();
  const navigate = useNavigate();
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchOrderDetails();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [orderId]);

  const fetchOrderDetails = async () => {
    try {
      setLoading(true);
      const data = await ordersAPI.getOrderById(parseInt(orderId));
      setOrder(data);
    } catch (err) {
      console.error('Error fetching order:', err);
      setError('Failed to load order details');
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800';
      case 'PROCESSING':
        return 'bg-blue-100 text-blue-800';
      case 'SHIPPING':
        return 'bg-purple-100 text-purple-800';
      case 'DELIVERED':
        return 'bg-green-100 text-green-800';
      case 'CANCELLED':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <Spinner size="xl" />
      </div>
    );
  }

  if (error || !order) {
    return (
      <div className="min-h-screen bg-gray-50 py-12">
        <div className="max-w-3xl mx-auto px-4 text-center">
          <Package className="h-16 w-16 text-gray-400 mx-auto mb-4" />
          <h2 className="text-2xl font-bold text-gray-800 mb-2">Order Not Found</h2>
          <p className="text-gray-600 mb-6">{error || 'The order you are looking for does not exist.'}</p>
          <button
            onClick={() => navigate('/orders')}
            className="text-blue-600 hover:text-blue-700 font-medium"
          >
            Back to Orders
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Back Button */}
        <button
          onClick={() => navigate('/orders')}
          className="flex items-center gap-2 text-gray-600 hover:text-gray-800 mb-6 transition-colors"
        >
          <ArrowLeft className="h-5 w-5" />
          Back to Orders
        </button>

        {/* Order Header */}
        <Card className="p-6 mb-6">
          <div className="flex items-start justify-between mb-6">
            <div>
              <h1 className="text-3xl font-bold text-gray-800 mb-2">
                Order #{order.id}
              </h1>
              <p className="text-gray-600">
                Placed on {new Date(order.orderDate).toLocaleDateString('en-US', {
                  year: 'numeric',
                  month: 'long',
                  day: 'numeric'
                })}
              </p>
            </div>
            <span className={`px-4 py-2 rounded-full text-sm font-medium ${getStatusColor(order.status)}`}>
              {order.status}
            </span>
          </div>

          <div className="grid md:grid-cols-3 gap-6">
            <div className="flex items-center space-x-3">
              <div className="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
                <CreditCard className="h-5 w-5 text-blue-600" />
              </div>
              <div>
                <p className="text-sm text-gray-500">Payment Method</p>
                <p className="font-medium text-gray-800">{order.paymentMethod}</p>
              </div>
            </div>

            <div className="flex items-center space-x-3">
              <div className="w-10 h-10 bg-green-100 rounded-full flex items-center justify-center">
                <Package className="h-5 w-5 text-green-600" />
              </div>
              <div>
                <p className="text-sm text-gray-500">Total Amount</p>
                <p className="font-medium text-gray-800">EGP {order.totalAmount?.toFixed(2)}</p>
              </div>
            </div>

            <div className="flex items-center space-x-3">
              <div className="w-10 h-10 bg-purple-100 rounded-full flex items-center justify-center">
                <Calendar className="h-5 w-5 text-purple-600" />
              </div>
              <div>
                <p className="text-sm text-gray-500">Order Date</p>
                <p className="font-medium text-gray-800">
                  {new Date(order.orderDate).toLocaleDateString()}
                </p>
              </div>
            </div>
          </div>
        </Card>

        {/* Order Status Timeline */}
        {order.status !== 'CANCELLED' && (
          <Card className="p-6 mb-6">
            <h2 className="text-xl font-bold text-gray-800 mb-4">Order Status</h2>
            <div className="relative">
              <div className="absolute left-4 top-0 bottom-0 w-0.5 bg-gray-200"></div>

              {['PENDING', 'PROCESSING', 'SHIPPING', 'DELIVERED'].map((status, index) => {
                const isCompleted = ['PENDING', 'PROCESSING', 'SHIPPING', 'DELIVERED'].indexOf(order.status) >= index;
                const isCurrent = order.status === status;

                return (
                  <div key={status} className="relative flex items-center mb-6 last:mb-0">
                    <div className={`w-8 h-8 rounded-full flex items-center justify-center ${
                      isCompleted ? 'bg-green-600' : 'bg-gray-300'
                    }`}>
                      {isCompleted && <CheckCircle className="h-5 w-5 text-white" />}
                    </div>
                    <div className="ml-4">
                      <p className={`font-medium ${isCurrent ? 'text-blue-600' : 'text-gray-800'}`}>
                        {status}
                      </p>
                    </div>
                  </div>
                );
              })}
            </div>
          </Card>
        )}

        {/* Order Items */}
        {order.itemDetails && order.itemDetails.length > 0 && (
          <Card className="p-6">
            <h2 className="text-xl font-bold text-gray-800 mb-4">Order Items</h2>
            <div className="space-y-4">
              {order.itemDetails.map((item, index) => (
                <div key={index} className="flex items-center justify-between py-3 border-b border-gray-200 last:border-0">
                  <div className="flex-1">
                    <p className="font-medium text-gray-800">{item.name || `Item #${item.itemId}`}</p>
                    <p className="text-sm text-gray-600">Quantity: {item.quantity}</p>
                  </div>
                  <p className="font-medium text-gray-800">
                    EGP {(item.unitPrice * item.quantity).toFixed(2)}
                  </p>
                </div>
              ))}

              <div className="pt-4 border-t-2 border-gray-300">
                <div className="flex justify-between items-center">
                  <p className="text-lg font-bold text-gray-800">Total</p>
                  <p className="text-2xl font-bold text-blue-600">
                    EGP {order.totalAmount?.toFixed(2)}
                  </p>
                </div>
              </div>
            </div>
          </Card>
        )}
      </div>
    </div>
  );
};

export default OrderDetailsPage;

