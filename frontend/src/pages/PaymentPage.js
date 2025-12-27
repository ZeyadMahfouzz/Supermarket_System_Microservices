import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { ArrowLeft, CreditCard, Smartphone, Banknote } from 'lucide-react';
import { useCart } from '../contexts/CartContext';
import CreditCardForm from '../components/payment/CreditCardForm';
import DebitCardForm from '../components/payment/DebitCardForm';
import MobilePaymentForm from '../components/payment/MobilePaymentForm';
import CashPaymentForm from '../components/payment/CashPaymentForm';
import Card from '../components/common/Card';

const PaymentPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { cart, checkout } = useCart();
  const [loading, setLoading] = useState(false);

  // Get payment method from navigation state or default to CREDIT_CARD
  const [paymentMethod, setPaymentMethod] = useState(
    location.state?.paymentMethod || 'CREDIT_CARD'
  );

  useEffect(() => {
    // Redirect if cart is empty
    if (!cart || !cart.items || cart.items.length === 0) {
      navigate('/cart');
    }
  }, [cart, navigate]);

  const handlePaymentSubmit = async (paymentData) => {
    setLoading(true);

    console.log('Payment Data:', paymentData);
    console.log('Payment Method:', paymentMethod);

    // Additional validation before submitting
    if (!paymentData) {
      alert('Payment data is missing. Please fill in all required fields.');
      setLoading(false);
      return;
    }

    // Build the checkout request based on payment method
    const checkoutRequest = {
      paymentMethod: paymentMethod
    };

    // Add payment-specific details matching backend DTOs
    if (paymentMethod === 'CREDIT_CARD') {
      if (!paymentData.cardNumber || !paymentData.cardHolder || !paymentData.expiryDate || !paymentData.cvv) {
        alert('Please fill in all credit card details:\n- Card Number\n- Cardholder Name\n- Expiry Date\n- CVV');
        setLoading(false);
        return;
      }
      checkoutRequest.creditCardPayment = {
        cardNumber: paymentData.cardNumber.replace(/\s/g, ''), // Remove spaces
        cardholderName: paymentData.cardHolder,
        expiryDate: paymentData.expiryDate,
        cvv: paymentData.cvv
      };
    } else if (paymentMethod === 'DEBIT_CARD') {
      if (!paymentData.cardNumber || !paymentData.cardHolder || !paymentData.expiryDate || !paymentData.pin) {
        alert('Please fill in all debit card details:\n- Card Number\n- Cardholder Name\n- Expiry Date\n- CVV');
        setLoading(false);
        return;
      }
      checkoutRequest.debitCardPayment = {
        cardNumber: paymentData.cardNumber.replace(/\s/g, ''), // Remove spaces
        cardholderName: paymentData.cardHolder,
        expiryDate: paymentData.expiryDate,
        cvv: paymentData.pin // Backend uses 'cvv' field for debit too
      };
    } else if (paymentMethod === 'MOBILE_PAYMENT') {
      if (!paymentData.phoneNumber || !paymentData.provider) {
        alert('Please fill in all mobile payment details:\n- Phone Number\n- Provider');
        setLoading(false);
        return;
      }
      checkoutRequest.mobilePayment = {
        phoneNumber: paymentData.phoneNumber,
        provider: paymentData.provider
        // Note: PIN is not in backend DTO
      };
    } else if (paymentMethod === 'CASH') {
      checkoutRequest.cashPayment = {
        confirmed: true,
        notes: paymentData.notes || ''
        // Note: Backend only has confirmed and notes fields
      };
    }

    console.log('Checkout request:', checkoutRequest);

    const result = await checkout(checkoutRequest);

    if (result.success) {
      // Navigate to order success page with order details
      navigate('/order-success', {
        state: {
          orderId: result.orderId,
          totalAmount: cart.totalPrice,
          paymentMethod: paymentMethod,
          paymentData: paymentData
        }
      });
    } else {
      alert(`Payment failed!\n\n${result.message || 'Please try again.'}`);
    }

    setLoading(false);
  };

  const getPaymentForm = () => {
    switch (paymentMethod) {
      case 'CREDIT_CARD':
        return <CreditCardForm onSubmit={handlePaymentSubmit} loading={loading} />;
      case 'DEBIT_CARD':
        return <DebitCardForm onSubmit={handlePaymentSubmit} loading={loading} />;
      case 'MOBILE_PAYMENT':
        return <MobilePaymentForm onSubmit={handlePaymentSubmit} loading={loading} />;
      case 'CASH':
        return <CashPaymentForm onSubmit={handlePaymentSubmit} loading={loading} />;
      default:
        return <CreditCardForm onSubmit={handlePaymentSubmit} loading={loading} />;
    }
  };

  const getPaymentTitle = () => {
    switch (paymentMethod) {
      case 'CREDIT_CARD':
        return 'Credit Card Payment';
      case 'DEBIT_CARD':
        return 'Debit Card Payment';
      case 'MOBILE_PAYMENT':
        return 'Mobile Wallet Payment';
      case 'CASH':
        return 'Cash on Delivery';
      default:
        return 'Payment';
    }
  };

  if (!cart || !cart.items || cart.items.length === 0) {
    return null;
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Back Button */}
        <button
          onClick={() => navigate('/cart')}
          className="flex items-center gap-2 text-gray-600 hover:text-gray-800 mb-6 transition-colors"
        >
          <ArrowLeft className="h-5 w-5" />
          Back to Cart
        </button>

        <div className="grid md:grid-cols-3 gap-6">
          {/* Payment Form */}
          <div className="md:col-span-2">
            <Card className="p-6">
              <h1 className="text-3xl font-bold text-gray-800 mb-6">{getPaymentTitle()}</h1>

              {/* Payment Method Selector */}
              <div className="mb-6">
                <label className="block text-sm font-medium text-gray-700 mb-3">
                  Select Payment Method
                </label>
                <div className="grid grid-cols-2 gap-3">
                  <button
                    onClick={() => setPaymentMethod('CREDIT_CARD')}
                    className={`p-4 rounded-lg border-2 transition-all ${
                      paymentMethod === 'CREDIT_CARD'
                        ? 'border-blue-600 bg-blue-50'
                        : 'border-gray-200 hover:border-gray-300'
                    }`}
                  >
                    <CreditCard className="h-6 w-6 mx-auto mb-2 text-gray-700" />
                    <p className="text-sm font-medium">Credit Card</p>
                  </button>

                  <button
                    onClick={() => setPaymentMethod('DEBIT_CARD')}
                    className={`p-4 rounded-lg border-2 transition-all ${
                      paymentMethod === 'DEBIT_CARD'
                        ? 'border-blue-600 bg-blue-50'
                        : 'border-gray-200 hover:border-gray-300'
                    }`}
                  >
                    <CreditCard className="h-6 w-6 mx-auto mb-2 text-gray-700" />
                    <p className="text-sm font-medium">Debit Card</p>
                  </button>

                  <button
                    onClick={() => setPaymentMethod('MOBILE_PAYMENT')}
                    className={`p-4 rounded-lg border-2 transition-all ${
                      paymentMethod === 'MOBILE_PAYMENT'
                        ? 'border-blue-600 bg-blue-50'
                        : 'border-gray-200 hover:border-gray-300'
                    }`}
                  >
                    <Smartphone className="h-6 w-6 mx-auto mb-2 text-gray-700" />
                    <p className="text-sm font-medium">Mobile Wallet</p>
                  </button>

                  <button
                    onClick={() => setPaymentMethod('CASH')}
                    className={`p-4 rounded-lg border-2 transition-all ${
                      paymentMethod === 'CASH'
                        ? 'border-blue-600 bg-blue-50'
                        : 'border-gray-200 hover:border-gray-300'
                    }`}
                  >
                    <Banknote className="h-6 w-6 mx-auto mb-2 text-gray-700" />
                    <p className="text-sm font-medium">Cash</p>
                  </button>
                </div>
              </div>

              <div className="border-t border-gray-200 pt-6">
                {getPaymentForm()}
              </div>
            </Card>
          </div>

          {/* Order Summary */}
          <div className="md:col-span-1">
            <Card className="p-6 sticky top-24">
              <h2 className="text-xl font-bold text-gray-800 mb-4">Order Summary</h2>

              <div className="space-y-3">
                <div className="flex justify-between text-gray-600">
                  <span>Items ({cart.items.length})</span>
                  <span>EGP {cart.totalPrice?.toFixed(2)}</span>
                </div>

                <div className="border-t border-gray-200 pt-3">
                  <div className="flex justify-between text-lg font-bold text-gray-800">
                    <span>Total</span>
                    <span className="text-blue-600">EGP {cart.totalPrice?.toFixed(2)}</span>
                  </div>
                </div>
              </div>

              <div className="mt-6 p-3 bg-gray-50 rounded-lg">
                <p className="text-xs text-gray-600">
                  Your payment information is secure and encrypted. We never store your card details.
                </p>
              </div>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PaymentPage;

