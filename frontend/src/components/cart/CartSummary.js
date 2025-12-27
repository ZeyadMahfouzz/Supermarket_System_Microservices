import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ShoppingBag, CreditCard } from 'lucide-react';
import Card from '../common/Card';
import Button from '../common/Button';
import { useCart } from '../../contexts/CartContext';
import CheckoutModal from './CheckoutModal';

const CartSummary = () => {
  const { cart, checkout } = useCart();
  const navigate = useNavigate();

  const [paymentMethod, setPaymentMethod] = useState('CREDIT_CARD');
  const [loading, setLoading] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [error, setError] = useState(null);

  // CASH OR OPEN MODAL
  const handleCheckout = async () => {
    setError(null);

    if (paymentMethod === 'CREDIT_CARD') {
      setShowModal(true);
      return;
    }

    setLoading(true);
    const result = await checkout({
      paymentMethod: 'CASH',
      cashPayment: { confirmed: true },
    });
    setLoading(false);

    if (result.success) {
      navigate('/orders');
    } else {
      setError(result.message);
    }
  };

  // CREDIT CARD PAYMENT (CALLED FROM MODAL)
  const handleCardPayment = async (cardData) => {
    setLoading(true);
    setError(null);

    const result = await checkout({
      paymentMethod: 'CREDIT_CARD',
      creditCardPayment: cardData,
    });

    setLoading(false);

    if (result.success) {
      setShowModal(false);
      navigate('/orders');
    }

    // 🔑 IMPORTANT: return result so modal can show error
    return result;
  };

  if (!cart || cart.items.length === 0) return null;

  return (
    <>
      <Card className="p-6 sticky top-24">
        <h2 className="text-2xl font-bold mb-6 flex items-center gap-2">
          <ShoppingBag className="h-6 w-6 text-blue-600" />
          Order Summary
        </h2>

        <div className="space-y-4 mb-6">
          <div className="flex justify-between">
            <span>Items ({cart.items.length})</span>
            <span>${cart.totalPrice.toFixed(2)}</span>
          </div>

          <div className="border-t" />

          <div className="flex justify-between text-xl font-bold">
            <span>Total</span>
            <span className="text-blue-600">
              ${cart.totalPrice.toFixed(2)}
            </span>
          </div>
        </div>

        {/* PAYMENT METHOD */}
        <div className="mb-6 space-y-2">
          <label className="flex items-center p-3 border rounded cursor-pointer">
            <input
              type="radio"
              value="CREDIT_CARD"
              checked={paymentMethod === 'CREDIT_CARD'}
              onChange={(e) => setPaymentMethod(e.target.value)}
              className="mr-3"
            />
            <CreditCard className="h-5 w-5 mr-2" />
            Credit Card
          </label>

          <label className="flex items-center p-3 border rounded cursor-pointer">
            <input
              type="radio"
              value="CASH"
              checked={paymentMethod === 'CASH'}
              onChange={(e) => setPaymentMethod(e.target.value)}
              className="mr-3"
            />
            💵 Cash on Delivery
          </label>
        </div>

        {/* ERROR MESSAGE */}
        {error && (
          <div className="bg-red-50 border border-red-300 text-red-700 p-3 rounded mb-4">
            {error}
          </div>
        )}

        <Button
          fullWidth
          size="lg"
          loading={loading}
          onClick={handleCheckout}
        >
          Proceed to Checkout
        </Button>
      </Card>

      {/* CREDIT CARD MODAL */}
      {showModal && (
        <CheckoutModal
          loading={loading}
          onClose={() => setShowModal(false)}
          onPay={handleCardPayment}
        />
      )}
    </>
  );
};

export default CartSummary;
