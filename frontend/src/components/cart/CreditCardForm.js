import React, { useState } from 'react';
import Input from '../common/Input';
import Button from '../common/Button';

const CreditCardForm = ({ onSubmit, loading }) => {
  const [form, setForm] = useState({
    cardNumber: '',
    cardHolderName: '',
    expiryDate: '',
    cvv: '',
  });

  const validate = () => {
    if (!/^\d{16}$/.test(form.cardNumber)) return 'Invalid card number';
    if (!/^\d{3}$/.test(form.cvv)) return 'Invalid CVV';
    if (!/^\d{2}\/\d{2}$/.test(form.expiryDate)) return 'Invalid expiry date (MM/YY)';
    if (!form.cardHolderName.trim()) return 'Card holder name required';
    return null;
  };

  const handleSubmit = () => {
    const error = validate();
    if (error) {
      alert(error);
      return;
    }
    onSubmit(form);
  };

  return (
    <div className="space-y-4">
      <Input label="Card Number" placeholder="4111111111111111"
        value={form.cardNumber}
        onChange={e => setForm({ ...form, cardNumber: e.target.value })}
      />
      <Input label="Card Holder Name"
        value={form.cardHolderName}
        onChange={e => setForm({ ...form, cardHolderName: e.target.value })}
      />
      <div className="grid grid-cols-2 gap-4">
        <Input label="Expiry (MM/YY)"
          value={form.expiryDate}
          onChange={e => setForm({ ...form, expiryDate: e.target.value })}
        />
        <Input label="CVV"
          value={form.cvv}
          onChange={e => setForm({ ...form, cvv: e.target.value })}
        />
      </div>

      <Button fullWidth loading={loading} onClick={handleSubmit}>
        Pay Now
      </Button>
    </div>
  );
};

export default CreditCardForm;
