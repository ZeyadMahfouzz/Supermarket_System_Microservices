import React, { useState } from 'react';
import Card from '../common/Card';
import Button from '../common/Button';

const CheckoutModal = ({ onClose, onPay, loading }) => {
  const [cardData, setCardData] = useState({
    cardNumber: '',
    cardHolderName: '',
    expiryDate: '',
    cvv: '',
  });

  const [error, setError] = useState(null);

  const handleChange = (e) => {
    setCardData({
      ...cardData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async () => {
    setError(null);

    // ✅ FRONTEND VALIDATION (simple but correct)
    if (!cardData.cardNumber || cardData.cardNumber.length < 16) {
      setError('Card number must be 16 digits');
      return;
    }

    if (!cardData.cardHolderName) {
      setError('Card holder name is required');
      return;
    }

    if (!/^\d{2}\/\d{2}$/.test(cardData.expiryDate)) {
      setError('Expiry date must be in MM/YY format');
      return;
    }

    if (!/^\d{3}$/.test(cardData.cvv)) {
      setError('CVV must be 3 digits');
      return;
    }

    // ✅ Send to CartSummary
    const result = await onPay(cardData);

    // If backend fails, CartSummary will return error
    if (result?.success === false) {
      setError(result.message || 'Payment failed');
    }
  };

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <Card className="w-full max-w-md p-6 relative">
        <h2 className="text-xl font-bold mb-4">Credit Card Payment</h2>

        {/* ❌ ERROR MESSAGE — SAME BOX */}
        {error && (
          <div className="bg-red-50 border border-red-300 text-red-700 p-3 rounded mb-4">
            {error}
          </div>
        )}

        <div className="space-y-3">
          <input
            name="cardNumber"
            placeholder="Card Number"
            value={cardData.cardNumber}
            onChange={handleChange}
            className="w-full border p-2 rounded"
          />

          <input
            name="cardHolderName"
            placeholder="Card Holder Name"
            value={cardData.cardHolderName}
            onChange={handleChange}
            className="w-full border p-2 rounded"
          />

          <input
            name="expiryDate"
            placeholder="MM/YY"
            value={cardData.expiryDate}
            onChange={handleChange}
            className="w-full border p-2 rounded"
          />

          <input
            name="cvv"
            placeholder="CVV"
            value={cardData.cvv}
            onChange={handleChange}
            className="w-full border p-2 rounded"
            type="password"
          />
        </div>

        <div className="flex gap-2 mt-6">
          <Button
            variant="secondary"
            fullWidth
            onClick={onClose}
            disabled={loading}
          >
            Cancel
          </Button>

          <Button
            variant="primary"
            fullWidth
            loading={loading}
            onClick={handleSubmit}
          >
            Pay Now
          </Button>
        </div>
      </Card>
    </div>
  );
};

export default CheckoutModal;
