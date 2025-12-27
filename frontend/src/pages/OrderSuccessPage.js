import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { CheckCircle, Package, FileText, ArrowRight } from 'lucide-react';
import Button from '../components/common/Button';
import Card from '../components/common/Card';

const OrderSuccessPage = () => {
  const navigate = useNavigate();
  const location = useLocation();

  const { orderId, totalAmount, paymentMethod } = location.state || {};

  // Redirect if no order data
  if (!orderId) {
    navigate('/home');
    return null;
  }

  const formatPaymentMethod = (method) => {
    switch (method) {
      case 'CREDIT_CARD':
        return 'Credit Card';
      case 'DEBIT_CARD':
        return 'Debit Card';
      case 'MOBILE_PAYMENT':
        return 'Mobile Wallet';
      case 'CASH':
        return 'Cash on Delivery';
      default:
        return method;
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-12">
      <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Success Animation */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-24 h-24 rounded-full bg-green-100 mb-4 animate-bounce">
            <CheckCircle className="h-12 w-12 text-green-600" />
          </div>
          <h1 className="text-4xl font-bold text-gray-800 mb-2">Order Placed Successfully!</h1>
          <p className="text-lg text-gray-600">
            Thank you for shopping at SpringMart
          </p>
        </div>

        {/* Order Details Card */}
        <Card className="p-8 mb-6">
          <div className="border-b border-gray-200 pb-6 mb-6">
            <h2 className="text-2xl font-bold text-gray-800 mb-4">Order Details</h2>

            <div className="grid md:grid-cols-2 gap-6">
              <div>
                <p className="text-sm text-gray-500 mb-1">Order Number</p>
                <p className="text-xl font-bold text-blue-600">#{orderId}</p>
              </div>

              <div>
                <p className="text-sm text-gray-500 mb-1">Total Amount</p>
                <p className="text-xl font-bold text-gray-800">EGP {totalAmount?.toFixed(2)}</p>
              </div>

              <div>
                <p className="text-sm text-gray-500 mb-1">Payment Method</p>
                <p className="text-lg font-medium text-gray-800">{formatPaymentMethod(paymentMethod)}</p>
              </div>

              <div>
                <p className="text-sm text-gray-500 mb-1">Order Date</p>
                <p className="text-lg font-medium text-gray-800">{new Date().toLocaleDateString()}</p>
              </div>
            </div>
          </div>

          {/* What's Next Section */}
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
            <h3 className="font-semibold text-blue-900 mb-2">What happens next?</h3>
            <ul className="space-y-2 text-sm text-blue-800">
              <li className="flex items-start">
                <span className="mr-2">•</span>
                <span>You will receive an order confirmation email shortly</span>
              </li>
              <li className="flex items-start">
                <span className="mr-2">•</span>
                <span>Track your order status in the Orders page</span>
              </li>
              <li className="flex items-start">
                <span className="mr-2">•</span>
                <span>You can contact customer support if you have any questions</span>
              </li>
            </ul>
          </div>

          {/* Action Buttons */}
          <div className="grid md:grid-cols-2 gap-4">
            <Button
              variant="primary"
              size="lg"
              fullWidth
              onClick={() => navigate('/orders')}
              className="flex items-center justify-center gap-2"
            >
              <Package className="h-5 w-5" />
              View All Orders
            </Button>

            <Button
              variant="secondary"
              size="lg"
              fullWidth
              onClick={() => navigate(`/order-details/${orderId}`)}
              className="flex items-center justify-center gap-2"
            >
              <FileText className="h-5 w-5" />
              View Order Details
            </Button>
          </div>

          <div className="mt-6 text-center">
            <button
              onClick={() => navigate('/home')}
              className="text-blue-600 hover:text-blue-700 font-medium inline-flex items-center gap-2 transition-colors"
            >
              Continue Shopping
              <ArrowRight className="h-4 w-4" />
            </button>
          </div>
        </Card>

        {/* Additional Info */}
        <div className="text-center text-sm text-gray-600">
          <p>Need help? Contact us at support@springmart.com</p>
        </div>
      </div>
    </div>
  );
};

export default OrderSuccessPage;

