import React, { useState } from 'react';
import { Banknote } from 'lucide-react';
import Button from '../common/Button';

const CashPaymentForm = ({ onSubmit, loading }) => {
  const [formData, setFormData] = useState({
    notes: ''
  });

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit(formData);
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {/* Delivery Notes */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Delivery Notes (Optional)
        </label>
        <textarea
          name="notes"
          value={formData.notes}
          onChange={handleChange}
          placeholder="Any special instructions for delivery (e.g., address, contact number, preferred time)..."
          rows="4"
          className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:border-blue-500"
        />
      </div>

      <div className="bg-green-50 border border-green-200 rounded-lg p-4">
        <div className="flex items-start space-x-3">
          <Banknote className="h-5 w-5 text-green-600 mt-0.5" />
          <div>
            <p className="text-sm font-medium text-green-800">Cash on Delivery</p>
            <p className="text-sm text-green-700 mt-1">
              Pay in cash when your order is delivered. Please have the exact amount ready.
            </p>
            <p className="text-sm text-green-700 mt-2">
              Our delivery team will contact you to confirm delivery details.
            </p>
          </div>
        </div>
      </div>

      <Button
        type="submit"
        variant="primary"
        size="lg"
        fullWidth
        loading={loading}
        disabled={loading}
      >
        Confirm Order
      </Button>
    </form>
  );
};

export default CashPaymentForm;

