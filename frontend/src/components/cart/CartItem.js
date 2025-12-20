import React, { useState } from 'react';
import { Trash2, Plus, Minus } from 'lucide-react';
import { useCart } from '../../contexts/CartContext';

const CartItem = ({ item }) => {
  const [updating, setUpdating] = useState(false);
  const { updateQuantity, removeItem } = useCart();

  const handleQuantityChange = async (newQuantity) => {
    if (newQuantity < 1) return;

    setUpdating(true);
    await updateQuantity(item.cartItemId, newQuantity);
    setUpdating(false);
  };

  const handleRemove = async () => {
    setUpdating(true);
    await removeItem(item.cartItemId);
    setUpdating(false);
  };

  return (
    <div className="flex items-center gap-4 p-4 bg-white rounded-lg shadow-md animate-slide-down hover:shadow-lg transition-shadow">
      {/* Item Image Placeholder */}
      <div className="w-20 h-20 rounded-lg bg-gradient-to-br from-blue-100 to-purple-100 flex items-center justify-center flex-shrink-0">
        <span className="text-3xl">🛒</span>
      </div>

      {/* Item Details */}
      <div className="flex-1 min-w-0">
        <h3 className="font-semibold text-gray-800 truncate">Item #{item.itemId}</h3>
        <p className="text-sm text-gray-600">${item.unitPrice?.toFixed(2)} each</p>
      </div>

      {/* Quantity Controls */}
      <div className="flex items-center space-x-2">
        <button
          onClick={() => handleQuantityChange(item.quantity - 1)}
          disabled={updating || item.quantity <= 1}
          className="w-8 h-8 rounded-full bg-gray-200 hover:bg-gray-300 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center justify-center"
        >
          <Minus className="h-4 w-4" />
        </button>
        <span className="w-12 text-center font-semibold">{item.quantity}</span>
        <button
          onClick={() => handleQuantityChange(item.quantity + 1)}
          disabled={updating}
          className="w-8 h-8 rounded-full bg-gray-200 hover:bg-gray-300 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center justify-center"
        >
          <Plus className="h-4 w-4" />
        </button>
      </div>

      {/* Subtotal */}
      <div className="text-right min-w-[100px]">
        <p className="font-bold text-lg text-blue-600">
          ${item.subtotal?.toFixed(2)}
        </p>
      </div>

      {/* Remove Button */}
      <button
        onClick={handleRemove}
        disabled={updating}
        className="p-2 text-red-500 hover:bg-red-50 rounded-lg transition-colors disabled:opacity-50"
      >
        <Trash2 className="h-5 w-5" />
      </button>
    </div>
  );
};

export default CartItem;

