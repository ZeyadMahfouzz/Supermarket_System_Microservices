import React, { useState } from 'react';
import { ShoppingCart, Check, AlertCircle } from 'lucide-react';
import Card from '../common/Card';
import Button from '../common/Button';
import { useCart } from '../../contexts/CartContext';
import { useAuth } from '../../contexts/AuthContext';

const ItemCard = ({ item }) => {
  const [quantity, setQuantity] = useState(1);
  const [added, setAdded] = useState(false);
  const [loading, setLoading] = useState(false);
  const [stockError, setStockError] = useState(null);

  const { addItem } = useCart();
  const { isAuthenticated, isAdmin } = useAuth();

  const handleAddToCart = async () => {
    if (!isAuthenticated) {
      alert('Please login to add items to cart');
      return;
    }

    if (loading) {
      return;
    }

    setStockError(null);
    setLoading(true);

    try {
      // Check if we have enough stock locally first
      if (item.quantity === 0) {
        // Feature error - show to user
        setStockError('This item is now out of stock');
        alert('This item is now out of stock');
        setLoading(false);
        return;
      }

      if (quantity > item.quantity) {
        // Feature error - show to user
        setStockError(`Only ${item.quantity} items available in stock`);
        alert(`Only ${item.quantity} items available in stock`);
        setQuantity(Math.min(quantity, item.quantity));
        setLoading(false);
        return;
      }

      console.log('Adding to cart - Item ID:', item.id, 'Quantity:', quantity);
      const result = await addItem(item.id, quantity);
      console.log('Add to cart result:', result);

      if (result.success) {
        setAdded(true);
        setTimeout(() => setAdded(false), 2000);
        setQuantity(1);
        setStockError(null);
      } else {
        // Check if it's a feature-related error or server error
        const errorMsg = result.message || '';
        const isFeatureError =
          errorMsg.toLowerCase().includes('stock') ||
          errorMsg.toLowerCase().includes('quantity') ||
          errorMsg.toLowerCase().includes('available') ||
          errorMsg.toLowerCase().includes('not found') ||
          errorMsg.toLowerCase().includes('invalid') ||
          errorMsg.toLowerCase().includes('not available');

        if (isFeatureError) {
          // Feature error - show to user
          setStockError(result.message);
          alert(result.message);
        } else {
          // Server/network error - log only, show generic message
          console.error('Server error adding to cart:', result.message);
          alert('Unable to add item to cart. Please try again later.');
        }
      }
    } catch (error) {
      console.error('Error in handleAddToCart:', error);
      console.error('Error details:', error.response?.data);

      // Determine if it's a feature error or server error
      const errorData = error.response?.data;
      const errorMsg = errorData?.error || errorData?.message || error.message || '';
      const statusCode = error.response?.status;

      const isFeatureError =
        errorMsg.toLowerCase().includes('stock') ||
        errorMsg.toLowerCase().includes('quantity') ||
        errorMsg.toLowerCase().includes('available') ||
        errorMsg.toLowerCase().includes('not found') ||
        errorMsg.toLowerCase().includes('invalid') ||
        statusCode === 400 || // Bad request - usually validation
        statusCode === 404;   // Not found

      if (isFeatureError) {
        // Feature error - show to user
        const userMsg = errorData?.error || errorData?.message || 'Unable to add item to cart';
        setStockError(userMsg);
        alert(userMsg);
      } else {
        // Server error (5xx) or network error - log only
        console.error('Server/network error:', statusCode, errorMsg);
        alert('Unable to add item to cart. Please try again later.');
      }
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

        {/* Stock Error Message */}
        {stockError && (
          <div className="flex items-center gap-2 p-2 bg-red-50 border border-red-200 rounded-lg mb-3">
            <AlertCircle className="h-4 w-4 text-red-600 flex-shrink-0" />
            <p className="text-xs text-red-600">{stockError}</p>
          </div>
        )}

        {/* Price and Stock */}
        <div className="flex items-center justify-between mb-4">
          <div className="flex-1">
            <p className="text-2xl font-bold text-blue-600">
              EGP {item.price?.toFixed(2)}
            </p>
            {/* Admin sees actual stock count */}
            {isAdmin ? (
              <p className="text-xs text-gray-500">
                Stock: {item.quantity > 0 ? item.quantity : 'Out of stock'}
              </p>
            ) : (
              /* Regular users see warnings only for low/out of stock */
              item.quantity === 0 ? (
                <p className="text-xs text-red-600 font-medium">Out of stock</p>
              ) : item.quantity <= 5 ? (
                <p className="text-xs text-orange-600 font-medium animate-pulse">
                  Only {item.quantity} left!
                </p>
              ) : item.quantity <= 10 ? (
                <p className="text-xs text-yellow-600 font-medium">
                  Low stock - {item.quantity} remaining
                </p>
              ) : null
            )}
          </div>

          {/* Quantity Selector - Only for regular users */}
          {!isAdmin && item.quantity > 0 && (
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
            Admin View - Use edit/delete buttons above
          </div>
        )}
      </div>
    </Card>
  );
};

export default ItemCard;

