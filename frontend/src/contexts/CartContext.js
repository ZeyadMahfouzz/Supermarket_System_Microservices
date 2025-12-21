import React, { createContext, useContext, useState, useEffect } from 'react';
import { cartAPI } from '../api/services';
import { useAuth } from './AuthContext';

const CartContext = createContext(null);

export const useCart = () => {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error('useCart must be used within a CartProvider');
  }
  return context;
};

export const CartProvider = ({ children }) => {
  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(false);
  const { isAuthenticated } = useAuth();

  const fetchCart = async () => {
    if (!isAuthenticated) return;

    try {
      setLoading(true);
      const cartData = await cartAPI.getCart();
      setCart(cartData);
    } catch (error) {
      console.error('Error fetching cart:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (isAuthenticated) {
      fetchCart();
    }
  }, [isAuthenticated]); // eslint-disable-line react-hooks/exhaustive-deps

  const addItem = async (itemId, quantity = 1) => {
    try {
      const updatedCart = await cartAPI.addItemToCart(itemId, quantity);
      setCart(updatedCart);
      return { success: true };
    } catch (error) {
      console.error('Error adding item to cart:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Failed to add item to cart'
      };
    }
  };

  const updateQuantity = async (cartItemId, quantity) => {
    try {
      const updatedCart = await cartAPI.updateCartItemQuantity(cartItemId, quantity);
      setCart(updatedCart);
      return { success: true };
    } catch (error) {
      console.error('Error updating quantity:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Failed to update quantity'
      };
    }
  };

  const removeItem = async (cartItemId) => {
    try {
      const updatedCart = await cartAPI.removeCartItem(cartItemId);
      setCart(updatedCart);
      return { success: true };
    } catch (error) {
      console.error('Error removing item:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Failed to remove item'
      };
    }
  };

  const clearCart = async () => {
    try {
      const updatedCart = await cartAPI.clearCart();
      setCart(updatedCart);
      return { success: true };
    } catch (error) {
      console.error('Error clearing cart:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Failed to clear cart'
      };
    }
  };

  const checkout = async (paymentMethod) => {
    try {
      await cartAPI.checkout(paymentMethod);
      await fetchCart(); // Refresh cart after checkout
      return { success: true };
    } catch (error) {
      console.error('Error during checkout:', error);
      return {
        success: false,
        message: error.response?.data || 'Checkout failed'
      };
    }
  };

  const getCartItemCount = () => {
    if (!cart || !cart.items) return 0;
    return cart.items.reduce((total, item) => total + item.quantity, 0);
  };

  const value = {
    cart,
    loading,
    fetchCart,
    addItem,
    updateQuantity,
    removeItem,
    clearCart,
    checkout,
    cartItemCount: getCartItemCount(),
  };

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
};

