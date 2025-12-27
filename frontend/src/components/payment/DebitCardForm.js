import React, { useState } from 'react';
import { CreditCard, Lock } from 'lucide-react';
import Button from '../common/Button';

const DebitCardForm = ({ onSubmit, loading }) => {
  const [formData, setFormData] = useState({
    cardNumber: '',
    cardHolder: '',
    expiryDate: '',
    pin: ''
  });
  const [touched, setTouched] = useState({
    cardNumber: false,
    cardHolder: false,
    expiryDate: false,
    pin: false
  });

  const handleBlur = (field) => {
    setTouched({ ...touched, [field]: true });
  };

  const handleChange = (e) => {
    let { name, value } = e.target;

    // Format card number with spaces
    if (name === 'cardNumber') {
      value = value.replace(/\s/g, '').replace(/(\d{4})/g, '$1 ').trim();
      value = value.slice(0, 19);
    }

    // Format expiry date
    if (name === 'expiryDate') {
      value = value.replace(/\D/g, '');
      if (value.length >= 2) {
        value = value.slice(0, 2) + '/' + value.slice(2, 4);
      }
    }

    // Limit PIN to 4 digits
    if (name === 'pin') {
      value = value.replace(/\D/g, '').slice(0, 4);
    }

    setFormData({ ...formData, [name]: value });
  };

  const validateForm = () => {
    const errors = [];

    // Validate card number
    const cardDigits = formData.cardNumber.replace(/\s/g, '');
    if (!cardDigits || cardDigits.trim() === '') {
      errors.push('Card number is required');
      return errors; // Stop validation early
    } else if (cardDigits.length < 13 || cardDigits.length > 19) {
      errors.push('Card number must be between 13 and 19 digits (currently ' + cardDigits.length + ' digits)');
    } else if (!/^\d+$/.test(cardDigits)) {
      errors.push('Card number must contain only digits');
    }

    // Validate cardholder name
    if (!formData.cardHolder || formData.cardHolder.trim().length === 0) {
      errors.push('Cardholder name is required');
    } else if (formData.cardHolder.trim().length < 3) {
      errors.push('Cardholder name must be at least 3 characters (currently ' + formData.cardHolder.trim().length + ' characters)');
    } else if (!/^[a-zA-Z\s]+$/.test(formData.cardHolder.trim())) {
      errors.push('Cardholder name must contain only letters and spaces');
    }

    // Validate expiry date
    if (!formData.expiryDate || formData.expiryDate.trim() === '') {
      errors.push('Expiry date is required');
    } else if (!/^\d{2}\/\d{2}$/.test(formData.expiryDate)) {
      errors.push('Expiry date must be in MM/YY format (e.g., 12/25)');
    } else {
      const [month, year] = formData.expiryDate.split('/');
      const monthNum = parseInt(month, 10);
      const yearNum = parseInt('20' + year, 10);
      const currentDate = new Date();
      const currentYear = currentDate.getFullYear();
      const currentMonth = currentDate.getMonth() + 1;

      if (monthNum < 1 || monthNum > 12) {
        errors.push('Invalid month in expiry date (must be 01-12, entered: ' + month + ')');
      } else if (yearNum < currentYear || (yearNum === currentYear && monthNum < currentMonth)) {
        errors.push('Card has expired (entered: ' + formData.expiryDate + ')');
      }
    }

    // Validate CVV
    if (!formData.pin || formData.pin.trim() === '') {
      errors.push('CVV is required');
    } else if (formData.pin.length < 3 || formData.pin.length > 4) {
      errors.push('CVV must be 3 or 4 digits (currently ' + formData.pin.length + ' digits)');
    } else if (!/^\d+$/.test(formData.pin)) {
      errors.push('CVV must contain only digits');
    }

    return errors;
  };

  const handleSubmit = (e) => {
    e.preventDefault();

    const errors = validateForm();
    if (errors.length > 0) {
      const errorMessage = 'Please fix the following errors:\n\n' +
        errors.map((err, i) => `${i + 1}. ${err}`).join('\n\n');
      alert(errorMessage);
      return;
    }

    onSubmit(formData);
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {/* Validation Requirements Notice */}
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-3">
        <p className="text-xs text-blue-800 font-medium mb-1">Payment Information Requirements:</p>
        <ul className="text-xs text-blue-700 space-y-0.5 ml-4 list-disc">
          <li>Card number: 13-19 digits</li>
          <li>Name: At least 3 characters (letters only)</li>
          <li>Expiry: MM/YY format (future date)</li>
          <li>CVV: 3 or 4 digits</li>
        </ul>
      </div>

      {/* Card Number */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Debit Card Number <span className="text-red-500">*</span>
        </label>
        <div className="relative">
          <CreditCard className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
          <input
            type="text"
            name="cardNumber"
            value={formData.cardNumber}
            onChange={handleChange}
            onBlur={() => handleBlur('cardNumber')}
            placeholder="1234 5678 9012 3456"
            required
            className="w-full pl-12 pr-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:border-blue-500"
          />
        </div>
      </div>

      {/* Card Holder Name */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Cardholder Name <span className="text-red-500">*</span>
        </label>
        <input
          type="text"
          name="cardHolder"
          value={formData.cardHolder}
          onChange={handleChange}
          onBlur={() => handleBlur('cardHolder')}
          placeholder="John Doe"
          required
          className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:border-blue-500"
        />
      </div>

      {/* Expiry Date and PIN */}
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Expiry Date <span className="text-red-500">*</span>
          </label>
          <input
            type="text"
            name="expiryDate"
            value={formData.expiryDate}
            onChange={handleChange}
            onBlur={() => handleBlur('expiryDate')}
            placeholder="MM/YY"
            required
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:border-blue-500"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            CVV <span className="text-red-500">*</span>
          </label>
          <div className="relative">
            <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
            <input
              type="password"
              name="pin"
              value={formData.pin}
              onChange={handleChange}
              onBlur={() => handleBlur('pin')}
              placeholder="•••"
              required
              maxLength="4"
              className="w-full pl-12 pr-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:border-blue-500"
            />
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
        Pay Now
      </Button>
    </form>
  );
};

export default DebitCardForm;

