import React, { createContext, useContext, useState, useEffect } from 'react';
import { cartAPI } from '../api/services';
import { useAuth } from './AuthContext';

const CartContext = createContext(null);

export const useCart = () => {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error('useCart must be used within CartProvider');
  }
  return context;
};

export const CartProvider = ({ children }) => {
  const { isAuthenticated } = useAuth();
  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(false);

  const fetchCart = async () => {
    if (!isAuthenticated) return;
    try {
      setLoading(true);
      const data = await cartAPI.getCart();
      setCart(data);
    } catch (err) {
      console.error('Fetch cart failed', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (isAuthenticated) fetchCart();
  }, [isAuthenticated]);

  const addItem = async (itemId, quantity) => {
    try {
      const updated = await cartAPI.addItemToCart(itemId, quantity);
      setCart(updated);
      return { success: true };
    } catch (err) {
      return {
        success: false,
        message: err.response?.data?.error || 'Failed to add item'
      };
    }
  };

  const updateQuantity = async (cartItemId, quantity) => {
    try {
      const updated = await cartAPI.updateCartItemQuantity(cartItemId, quantity);
      setCart(updated);
      return { success: true };
    } catch {
      return { success: false };
    }
  };

  const removeItem = async (cartItemId) => {
    try {
      const updated = await cartAPI.removeCartItem(cartItemId);
      setCart(updated);
      return { success: true };
    } catch {
      return { success: false };
    }
  };

  const clearCart = async () => {
    try {
      const updated = await cartAPI.clearCart();
      setCart(updated);
      return { success: true };
    } catch {
      return { success: false };
    }
  };

  // 🔑 THIS IS THE MOST IMPORTANT FIX
  const checkout = async (paymentData) => {
    try {
      const response = await cartAPI.checkout(paymentData);

      // backend returns COMPLETED → success
      if (response?.status === 'COMPLETED') {
        await fetchCart();
        return {
          success: true,
          orderId: response.orderId,
          transactionId: response.transactionId
        };
      }

      return {
        success: false,
        message: response?.message || 'Payment failed'
      };
    } catch (err) {
      return {
        success: false,
        message:
          err.response?.data?.error ||
          err.response?.data?.message ||
          'Checkout failed'
      };
    }
  };

  return (
    <CartContext.Provider
      value={{
        cart,
        loading,
        fetchCart,
        addItem,
        updateQuantity,
        removeItem,
        clearCart,
        checkout,
        cartItemCount:
          cart?.items?.reduce((sum, i) => sum + i.quantity, 0) || 0
      }}
    >
      {children}
    </CartContext.Provider>
  );
};
