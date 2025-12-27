import React, { useState } from 'react';
import { Smartphone } from 'lucide-react';
import Button from '../common/Button';

const MobilePaymentForm = ({ onSubmit, loading }) => {
  const [formData, setFormData] = useState({
    phoneNumber: '',
    provider: 'VODAFONE_CASH'
  });
  const [touched, setTouched] = useState({
    phoneNumber: false
  });

  const handleBlur = (field) => {
    setTouched({ ...touched, [field]: true });
  };

  const handleChange = (e) => {
    let { name, value } = e.target;

    // Format phone number
    if (name === 'phoneNumber') {
      value = value.replace(/\D/g, '').slice(0, 11);
    }


    setFormData({ ...formData, [name]: value });
  };

  const validateForm = () => {
    const errors = [];

    // Validate provider
    if (!formData.provider || formData.provider.trim() === '') {
      errors.push('Mobile wallet provider is required');
    }

    // Validate phone number (Egyptian format)
    if (!formData.phoneNumber || formData.phoneNumber.trim() === '') {
      errors.push('Mobile number is required');
    } else if (!/^01[0-2,5]\d{8}$/.test(formData.phoneNumber)) {
      errors.push('Invalid Egyptian mobile number. Must be 11 digits starting with 010, 011, 012, or 015 (entered: ' + formData.phoneNumber + ')');
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
        <p className="text-xs text-blue-800 font-medium mb-1">Mobile Payment Requirements:</p>
        <ul className="text-xs text-blue-700 space-y-0.5 ml-4 list-disc">
          <li>Phone number: 11 digits starting with 010, 011, 012, or 015</li>
        </ul>
      </div>

      {/* Provider Selection */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Mobile Wallet Provider <span className="text-red-500">*</span>
        </label>
        <select
          name="provider"
          value={formData.provider}
          onChange={handleChange}
          className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:border-blue-500"
        >
          <option value="VODAFONE_CASH">Vodafone Cash</option>
          <option value="ORANGE_CASH">Orange Cash</option>
          <option value="ETISALAT_CASH">Etisalat Cash</option>
          <option value="INSTAPAY">InstaPay</option>
          <option value="FAWRY">Fawry</option>
        </select>
      </div>

      {/* Phone Number */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Mobile Number <span className="text-red-500">*</span>
        </label>
        <div className="relative">
          <Smartphone className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
          <input
            type="tel"
            name="phoneNumber"
            value={formData.phoneNumber}
            onChange={handleChange}
            onBlur={() => handleBlur('phoneNumber')}
            placeholder="01234567890"
            required
            className="w-full pl-12 pr-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:border-blue-500"
          />
        </div>
      </div>

      <div className="bg-blue-50 border border-blue-200 rounded-lg p-3">
        <p className="text-sm text-blue-800">
          You will receive a confirmation request on your mobile wallet app to complete the payment.
        </p>
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

export default MobilePaymentForm;

