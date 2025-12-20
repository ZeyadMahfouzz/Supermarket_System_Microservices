import React, { useState } from 'react';
import { ShoppingCart, Check } from 'lucide-react';
import Card from '../common/Card';
import Button from '../common/Button';
import { useCart } from '../../contexts/CartContext';
import { useAuth } from '../../contexts/AuthContext';

const ItemCard = ({ item }) => {
  const [quantity, setQuantity] = useState(1);
  const [added, setAdded] = useState(false);
  const [loading, setLoading] = useState(false);

  const { addItem } = useCart();
  const { isAuthenticated } = useAuth();

  const handleAddToCart = async () => {
    if (!isAuthenticated) {
      alert('Please login to add items to cart');
      return;
    }

    setLoading(true);
    const result = await addItem(item.id, quantity);

    if (result.success) {
      setAdded(true);
      setTimeout(() => setAdded(false), 2000);
      setQuantity(1);
    } else {
      alert(result.message);
    }

    setLoading(false);
  };

  return (
    <Card hover className="overflow-hidden animate-fade-in">
      {/* Item Image Placeholder */}
      <div className="h-48 bg-gradient-to-br from-blue-100 to-purple-100 flex items-center justify-center">
        <div className="text-6xl">🛒</div>
      </div>

      <div className="p-5">
        {/* Category Badge */}
        <span className="inline-block px-3 py-1 text-xs font-semibold text-blue-600 bg-blue-100 rounded-full mb-2">
          {item.category || 'General'}
        </span>

        {/* Item Name */}
        <h3 className="text-lg font-bold text-gray-800 mb-2 line-clamp-1">
          {item.name}
        </h3>

        {/* Description */}
        <p className="text-sm text-gray-600 mb-4 line-clamp-2">
          {item.description || 'No description available'}
        </p>

        {/* Price and Stock */}
        <div className="flex items-center justify-between mb-4">
          <div>
            <p className="text-2xl font-bold text-blue-600">
              ${item.price?.toFixed(2)}
            </p>
            <p className="text-xs text-gray-500">
              Stock: {item.quantity > 0 ? item.quantity : 'Out of stock'}
            </p>
          </div>

          {/* Quantity Selector */}
          <div className="flex items-center space-x-2">
            <button
              onClick={() => setQuantity(Math.max(1, quantity - 1))}
              className="w-8 h-8 rounded-full bg-gray-200 hover:bg-gray-300 transition-colors flex items-center justify-center"
            >
              -
            </button>
            <span className="w-8 text-center font-semibold">{quantity}</span>
            <button
              onClick={() => setQuantity(Math.min(item.quantity, quantity + 1))}
              className="w-8 h-8 rounded-full bg-gray-200 hover:bg-gray-300 transition-colors flex items-center justify-center"
              disabled={quantity >= item.quantity}
            >
              +
            </button>
          </div>
        </div>

        {/* Add to Cart Button */}
        <Button
          variant={added ? 'success' : 'primary'}
          size="md"
          fullWidth
          onClick={handleAddToCart}
          disabled={item.quantity === 0 || loading}
          loading={loading}
        >
          {added ? (
            <>
              <Check className="h-4 w-4" />
              Added to Cart
            </>
          ) : (
            <>
              <ShoppingCart className="h-4 w-4" />
              Add to Cart
            </>
          )}
        </Button>
      </div>
    </Card>
  );
};

export default ItemCard;

