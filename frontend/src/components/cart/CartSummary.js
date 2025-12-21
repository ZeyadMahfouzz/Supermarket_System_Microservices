import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ShoppingBag, CreditCard } from 'lucide-react';
import Button from '../common/Button';
import Card from '../common/Card';
import { useCart } from '../../contexts/CartContext';

const CartSummary = () => {
  const [paymentMethod, setPaymentMethod] = useState('CREDIT_CARD');
  const [loading, setLoading] = useState(false);
  const { cart, checkout } = useCart();
  const navigate = useNavigate();

  const handleCheckout = async () => {
    setLoading(true);
    const result = await checkout(paymentMethod);

    if (result.success) {
      alert('Checkout successful! Your order has been placed.');
      navigate('/');
    } else {
      alert(result.message);
    }

    setLoading(false);
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
          <span>${cart.totalPrice?.toFixed(2)}</span>
        </div>

        {/* Divider */}
        <div className="border-t border-gray-200"></div>

        {/* Total */}
        <div className="flex justify-between text-xl font-bold text-gray-800">
          <span>Total</span>
          <span className="text-blue-600">${cart.totalPrice?.toFixed(2)}</span>
        </div>
      </div>

      {/* Payment Method Selection */}
      <div className="mb-6">
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Payment Method
        </label>
        <div className="space-y-2">
          <label className="flex items-center p-3 border-2 rounded-lg cursor-pointer transition-colors hover:bg-gray-50">
            <input
              type="radio"
              name="payment"
              value="CREDIT_CARD"
              checked={paymentMethod === 'CREDIT_CARD'}
              onChange={(e) => setPaymentMethod(e.target.value)}
              className="mr-3"
            />
            <CreditCard className="h-5 w-5 mr-2 text-gray-600" />
            <span>Credit Card</span>
          </label>
          <label className="flex items-center p-3 border-2 rounded-lg cursor-pointer transition-colors hover:bg-gray-50">
            <input
              type="radio"
              name="payment"
              value="CASH"
              checked={paymentMethod === 'CASH'}
              onChange={(e) => setPaymentMethod(e.target.value)}
              className="mr-3"
            />
            <span className="text-2xl mr-2">💵</span>
            <span>Cash on Delivery</span>
          </label>
        </div>
      </div>

      {/* Checkout Button */}
      <Button
        variant="primary"
        size="lg"
        fullWidth
        onClick={handleCheckout}
        loading={loading}
        disabled={loading}
      >
        Proceed to Checkout
      </Button>

      {/* Additional Info */}
      <p className="text-xs text-gray-500 text-center mt-4">
        By completing this order, you agree to our terms and conditions
      </p>
    </Card>
  );
};

export default CartSummary;

