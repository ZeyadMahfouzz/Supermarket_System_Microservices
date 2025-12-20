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
  const { isAuthenticated, isAdmin } = useAuth();

  // Debug log
  console.log('ItemCard - isAdmin:', isAdmin, 'isAuthenticated:', isAuthenticated);

  const handleAddToCart = async () => {
    if (!isAuthenticated) {
      alert('Please login to add items to cart');
      return;
    }

    if (loading) {
      console.log('Already adding to cart, ignoring click');
      return; // Prevent multiple clicks while loading
    }

    console.log('Adding item to cart:', item.id, 'quantity:', quantity);
    setLoading(true);

    try {
      const result = await addItem(item.id, quantity);
      console.log('Add to cart result:', result);

      if (result.success) {
        setAdded(true);
        setTimeout(() => setAdded(false), 2000);
        setQuantity(1);
      } else {
        alert(result.message || 'Failed to add item to cart');
      }
    } catch (error) {
      console.error('Error in handleAddToCart:', error);
      alert('Failed to add item to cart');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Card hover className="overflow-hidden animate-fade-in">
      {/* Item Image */}
      <div className="h-48 bg-gray-100 overflow-hidden">
        {item.imageUrl ? (
          <img
            src={item.imageUrl}
            alt={item.name}
            className="w-full h-full object-cover hover:scale-105 transition-transform duration-300"
            onError={(e) => {
              e.target.onerror = null;
              e.target.src = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="200" height="200"%3E%3Crect fill="%23f3f4f6" width="200" height="200"/%3E%3Ctext fill="%239ca3af" font-family="sans-serif" font-size="18" dy="100" dx="50"%3ENo Image%3C/text%3E%3C/svg%3E';
            }}
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center bg-gray-100">
            <div className="text-center text-gray-400">
              <ShoppingCart className="w-16 h-16 mx-auto mb-2 opacity-30" />
              <p className="text-sm">No image</p>
            </div>
          </div>
        )}
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
          <div className="flex-1">
            <p className="text-2xl font-bold text-blue-600">
              ${item.price?.toFixed(2)}
            </p>
            {/* Admin sees actual stock count */}
            {isAdmin ? (
              <p className="text-xs text-gray-500">
                Stock: {item.quantity > 0 ? item.quantity : 'Out of stock'}
              </p>
            ) : (
              /* Regular users see low stock warning or availability */
              item.quantity === 0 ? (
                <p className="text-xs text-red-600 font-medium">❌ Out of stock</p>
              ) : item.quantity <= 5 ? (
                <p className="text-xs text-orange-600 font-medium animate-pulse">
                  🔥 Hurry! Only {item.quantity} left
                </p>
              ) : item.quantity <= 10 ? (
                <p className="text-xs text-yellow-600 font-medium">
                  ⚠️ Low stock - {item.quantity} remaining
                </p>
              ) : (
                <p className="text-xs text-green-600 font-medium">✅ In stock</p>
              )
            )}
          </div>

          {/* Quantity Selector - Only for regular users */}
          {!isAdmin && (
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
          )}
        </div>

        {/* Add to Cart Button - Only for regular users */}
        {!isAdmin ? (
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
        ) : (
          <div className="text-center py-2 text-sm text-gray-500">
            👨‍💼 Admin View - Use edit/delete buttons above
          </div>
        )}
      </div>
    </Card>
  );
};

export default ItemCard;

