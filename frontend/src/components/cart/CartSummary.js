import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ShoppingBag, CreditCard, Smartphone } from 'lucide-react';
import Button from '../common/Button';
import Card from '../common/Card';
import { useCart } from '../../contexts/CartContext';

const CartSummary = () => {
  const [paymentMethod, setPaymentMethod] = useState('CREDIT_CARD');
  const { cart } = useCart();
  const navigate = useNavigate();

  const handleProceedToPayment = () => {
    // Navigate to payment page with selected payment method
    navigate('/payment', { state: { paymentMethod } });
  };

  if (!cart || !cart.items || cart.items.length === 0) {
    return null;
  }

  return (
    <Card className="p-6 sticky top-24">
      <h2 className="text-2xl font-bold text-gray-800 mb-6 flex items-center gap-2">
        <ShoppingBag className="h-6 w-6 text-blue-600" />
        Order Summary
      </h2>

      <div className="space-y-4 mb-6">
        {/* Items Count */}
        <div className="flex justify-between text-gray-600">
          <span>Items ({cart.items.length})</span>
          <span>EGP {cart.totalPrice?.toFixed(2)}</span>
        </div>

        {/* Divider */}
        <div className="border-t border-gray-200"></div>

        {/* Total */}
        <div className="flex justify-between text-xl font-bold text-gray-800">
          <span>Total</span>
          <span className="text-blue-600">EGP {cart.totalPrice?.toFixed(2)}</span>
        </div>
      </div>

      {/* Payment Method Selection */}
      <div className="mb-6">
        <label className="block text-sm font-medium text-gray-700 mb-3">
          Select Payment Method
        </label>
        <div className="space-y-2">
          {/* Credit Card */}
          <label className={`flex items-center p-3 border-2 rounded-lg cursor-pointer transition-colors ${
            paymentMethod === 'CREDIT_CARD' ? 'border-blue-600 bg-blue-50' : 'border-gray-200 hover:bg-gray-50'
          }`}>
            <input
              type="radio"
              name="payment"
              value="CREDIT_CARD"
              checked={paymentMethod === 'CREDIT_CARD'}
              onChange={(e) => setPaymentMethod(e.target.value)}
              className="mr-3"
            />
            <CreditCard className="h-5 w-5 mr-2 text-gray-600" />
            <span className="font-medium">Credit Card</span>
          </label>

          {/* Debit Card */}
          <label className={`flex items-center p-3 border-2 rounded-lg cursor-pointer transition-colors ${
            paymentMethod === 'DEBIT_CARD' ? 'border-blue-600 bg-blue-50' : 'border-gray-200 hover:bg-gray-50'
          }`}>
            <input
              type="radio"
              name="payment"
              value="DEBIT_CARD"
              checked={paymentMethod === 'DEBIT_CARD'}
              onChange={(e) => setPaymentMethod(e.target.value)}
              className="mr-3"
            />
            <CreditCard className="h-5 w-5 mr-2 text-gray-600" />
            <span className="font-medium">Debit Card</span>
          </label>

          {/* Mobile Payment */}
          <label className={`flex items-center p-3 border-2 rounded-lg cursor-pointer transition-colors ${
            paymentMethod === 'MOBILE_PAYMENT' ? 'border-blue-600 bg-blue-50' : 'border-gray-200 hover:bg-gray-50'
          }`}>
            <input
              type="radio"
              name="payment"
              value="MOBILE_PAYMENT"
              checked={paymentMethod === 'MOBILE_PAYMENT'}
              onChange={(e) => setPaymentMethod(e.target.value)}
              className="mr-3"
            />
            <Smartphone className="h-5 w-5 mr-2 text-gray-600" />
            <span className="font-medium">Mobile Wallet</span>
          </label>

          {/* Cash on Delivery */}
          <label className={`flex items-center p-3 border-2 rounded-lg cursor-pointer transition-colors ${
            paymentMethod === 'CASH' ? 'border-blue-600 bg-blue-50' : 'border-gray-200 hover:bg-gray-50'
          }`}>
            <input
              type="radio"
              name="payment"
              value="CASH"
              checked={paymentMethod === 'CASH'}
              onChange={(e) => setPaymentMethod(e.target.value)}
              className="mr-3"
            />
            <span className="font-medium">Cash on Delivery</span>
          </label>
        </div>
      </div>

      {/* Checkout Button */}
      <Button
        variant="primary"
        size="lg"
        fullWidth
        onClick={handleProceedToPayment}
      >
        Proceed to Payment
      </Button>

      {/* Additional Info */}
      <p className="text-xs text-gray-500 text-center mt-4">
        By completing this order, you agree to our terms and conditions
      </p>
    </Card>
  );
};

export default CartSummary;

