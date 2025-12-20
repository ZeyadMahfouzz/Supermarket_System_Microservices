import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ShoppingCart, User, LogOut } from 'lucide-react';
import { useAuth } from '../../contexts/AuthContext';
import { useCart } from '../../contexts/CartContext';

const Navbar = () => {
  const { user, logout, isAuthenticated, isAdmin } = useAuth();
  const { cartItemCount } = useCart();
  const navigate = useNavigate();

  // Debug log
  console.log('Navbar - User:', user, 'isAdmin:', isAdmin);

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <nav className="bg-blue-600 shadow-md sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          {/* Logo */}
          <Link to="/home" className="flex items-center space-x-3 group">
            <div className="w-10 h-10 bg-white rounded-lg flex items-center justify-center shadow-sm group-hover:shadow-md transition">
              <svg className="w-6 h-6 text-blue-600" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
              </svg>
            </div>
            <div className="flex items-center space-x-2">
              <span className="text-xl font-semibold text-white">
                SpringMart
              </span>
              <span className="text-sm text-white/80">🍃</span>
            </div>
          </Link>

          {/* Navigation Links */}
          <div className="flex items-center space-x-6">
            <Link
              to="/home"
              className="text-white hover:text-blue-100 transition font-medium"
            >
              Products
            </Link>

            {isAuthenticated && (
              <Link
                to="/orders"
                className="text-white hover:text-blue-100 transition font-medium"
              >
                Orders
              </Link>
            )}

            {isAuthenticated ? (
              <>
                {/* Cart Icon with Badge - Only for regular users */}
                {!isAdmin && (
                  <Link
                    to="/cart"
                    className="relative text-white hover:text-blue-100 transition"
                  >
                    <ShoppingCart className="h-6 w-6" />
                    {cartItemCount > 0 && (
                      <span className="absolute -top-2 -right-2 bg-white text-blue-600 text-xs font-bold rounded-full h-5 w-5 flex items-center justify-center shadow-sm">
                        {cartItemCount}
                      </span>
                    )}
                  </Link>
                )}

                {/* User Menu */}
                <div className="flex items-center space-x-4">
                  <div className="flex items-center space-x-2 text-white">
                    <User className="h-5 w-5" />
                    <div className="flex flex-col">
                      <span className="text-sm font-medium">
                        {user?.name || user?.email}
                      </span>
                      {isAdmin && (
                        <span className="text-xs text-blue-200">👨‍💼 Admin</span>
                      )}
                    </div>
                  </div>
                  <button
                    onClick={handleLogout}
                    className="flex items-center gap-2 bg-blue-700 hover:bg-blue-800 text-white px-4 py-2 rounded-lg transition font-medium"
                  >
                    <LogOut className="h-4 w-4" />
                    Logout
                  </button>
                </div>
              </>
            ) : null}
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;

