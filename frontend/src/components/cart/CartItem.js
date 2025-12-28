import React, { useState, useEffect } from 'react';
import { Trash2, Plus, Minus } from 'lucide-react';
import { useCart } from '../../contexts/CartContext';
import { itemsAPI } from '../../api/services';

const CartItem = ({ item, onShowToast }) => {
  const [updating, setUpdating] = useState(false);
  const [availableStock, setAvailableStock] = useState(null);
  const { updateQuantity, removeItem } = useCart();

  // Check stock when component mounts
  useEffect(() => {
    checkAvailableStock();
  }, [item.itemId]); // eslint-disable-line react-hooks/exhaustive-deps

  const checkAvailableStock = async () => {
    try {
      const itemData = await itemsAPI.getItemById(item.itemId);
      setAvailableStock(itemData.quantity);
      return itemData.quantity;
    } catch (error) {
      console.error('Failed to check stock for item', item.itemId, error);
      // Don't show toast for stock check failures - just log it
      // Return current item quantity as fallback
      return item.quantity;
    }
  };

  const handleQuantityChange = async (newQuantity) => {
    if (newQuantity < 1) return;

    setUpdating(true);

    try {
      // If increasing quantity, check stock first
      if (newQuantity > item.quantity) {
        const stock = await checkAvailableStock();

        // Check if requested quantity exceeds available stock
        if (newQuantity > stock) {
          // This is a feature-related error - show to user
          onShowToast?.(`Cannot add more. Only ${stock} items available in stock`, 'error');
          setUpdating(false);
          return;
        }
      }

      const result = await updateQuantity(item.cartItemId, newQuantity);

      if (!result.success) {
        // Check if it's a feature-related error (stock, validation, etc.)
        const errorMsg = result.message || '';
        const isFeatureError =
          errorMsg.toLowerCase().includes('stock') ||
          errorMsg.toLowerCase().includes('quantity') ||
          errorMsg.toLowerCase().includes('available') ||
          errorMsg.toLowerCase().includes('not found') ||
          errorMsg.toLowerCase().includes('invalid');

        if (isFeatureError) {
          // Show user-facing errors
          onShowToast?.(result.message || 'Failed to update quantity', 'error');
        } else {
          // Log server errors only
          console.error('Server error updating quantity:', result.message);
          onShowToast?.('Unable to update quantity. Please try again later.', 'error');
        }
      } else {
        // Refresh stock after successful update
        await checkAvailableStock();
      }
    } catch (error) {
      // Unexpected errors - log only, show generic message
      console.error('Unexpected error updating cart quantity:', error);
      onShowToast?.('Unable to update quantity. Please try again later.', 'error');
    } finally {
      setUpdating(false);
    }
  };

  const handleRemove = async () => {
    if (!window.confirm('Remove this item from cart?')) return;

    setUpdating(true);

    const result = await removeItem(item.cartItemId);

    if (!result.success) {
      onShowToast?.(result.message || 'Failed to remove item', 'error');
    }

    setUpdating(false);
  };

  return (
    <div className="flex items-center gap-4 p-4 bg-white rounded-lg shadow-md animate-slide-down hover:shadow-lg transition-shadow">
      {/* Item Image */}
      <div className="w-20 h-20 rounded-lg bg-gray-100 overflow-hidden flex-shrink-0">
        {item.imageUrl ? (
          <img
            src={item.imageUrl}
            alt={item.name || `Item ${item.itemId}`}
            className="w-full h-full object-cover"
            onError={(e) => {
              e.target.onerror = null;
              e.target.parentElement.innerHTML = '<div class="w-full h-full flex items-center justify-center bg-gray-100"><span class="text-sm text-gray-400">No Image</span></div>';
            }}
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center">
            <span className="text-sm text-gray-400">No Image</span>
          </div>
        )}
      </div>

      {/* Item Details */}
      <div className="flex-1 min-w-0">
        <h3 className="font-semibold text-gray-800 truncate">{item.name || `Item #${item.itemId}`}</h3>
        <p className="text-sm text-gray-600">EGP {item.unitPrice?.toFixed(2)} each</p>
        {/* Only show stock warnings when low or out - don't show when plenty in stock */}
        {availableStock !== null && availableStock <= 10 && (
          <p className="text-xs mt-1">
            {availableStock === 0 ? (
              <span className="text-red-600 font-medium">Out of stock</span>
            ) : availableStock <= 5 ? (
              <span className="text-orange-600 font-medium">Only {availableStock} available</span>
            ) : (
              <span className="text-yellow-600">Stock: {availableStock}</span>
            )}
          </p>
        )}
      </div>

      {/* Quantity Controls */}
      <div className="flex items-center space-x-2">
        <button
          onClick={() => handleQuantityChange(item.quantity - 1)}
          disabled={updating || item.quantity <= 1}
          className="w-8 h-8 rounded-full bg-gray-200 hover:bg-gray-300 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center justify-center"
          title="Decrease quantity"
        >
          <Minus className="h-4 w-4" />
        </button>
        <span className="w-12 text-center font-semibold">{item.quantity}</span>
        <button
          onClick={() => handleQuantityChange(item.quantity + 1)}
          disabled={updating || (availableStock !== null && item.quantity >= availableStock)}
          className="w-8 h-8 rounded-full bg-blue-600 hover:bg-blue-700 text-white disabled:opacity-50 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors flex items-center justify-center"
          title={availableStock !== null && item.quantity >= availableStock ? "Maximum stock reached" : "Increase quantity"}
        >
          <Plus className="h-4 w-4" />
        </button>
      </div>

      {/* Subtotal */}
      <div className="text-right min-w-[100px]">
        <p className="font-bold text-lg text-blue-600">
          EGP {item.subtotal?.toFixed(2)}
        </p>
      </div>

      {/* Remove Button */}
      <button
        onClick={handleRemove}
        disabled={updating}
        className="p-2 text-red-500 hover:bg-red-50 rounded-lg transition-colors disabled:opacity-50"
        title="Remove from cart"
      >
        <Trash2 className="h-5 w-5" />
      </button>
    </div>
  );
};

export default CartItem;
