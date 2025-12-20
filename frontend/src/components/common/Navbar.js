import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ShoppingCart, User, LogOut } from 'lucide-react';
import { useAuth } from '../../contexts/AuthContext';
import { useCart } from '../../contexts/CartContext';

const Navbar = () => {
  const { user, logout, isAuthenticated } = useAuth();
  const { cartItemCount } = useCart();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <nav className="bg-gradient-to-r from-amber-500 via-orange-500 to-red-500 shadow-xl sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          {/* Logo */}
          <Link to="/home" className="flex items-center space-x-3 group">
            <div className="relative w-10 h-10 bg-white rounded-full flex items-center justify-center shadow-lg group-hover:scale-110 transition-transform">
              <svg className="w-6 h-6 text-amber-600" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 2L2 7v10c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V7l-10-5zm0 18c-3.86-1.04-7-5.35-7-10V8.3l7-3.11 7 3.11V10c0 4.65-3.14 8.96-7 10z"/>
                <path d="M12 6L6 9v4c0 3.31 2.23 6.37 5 7.24V6h2v14.24c2.77-.87 5-3.93 5-7.24V9l-6-3z"/>
              </svg>
            </div>
            <div className="flex flex-col">
              <span className="text-xl font-bold text-white drop-shadow-lg" style={{ fontFamily: 'Georgia, serif' }}>
                بركة السوق
              </span>
              <span className="text-xs text-white/90 -mt-1">Baraka Souq</span>
            </div>
          </Link>

          {/* Navigation Links */}
          <div className="flex items-center space-x-6">
            <Link
              to="/home"
              className="text-white hover:text-yellow-200 transition-colors font-medium"
            >
              Products
            </Link>

            {isAuthenticated ? (
              <>
                {/* Cart Icon with Badge */}
                <Link
                  to="/cart"
                  className="relative text-white hover:text-yellow-200 transition-colors"
                >
                  <ShoppingCart className="h-6 w-6" />
                  {cartItemCount > 0 && (
                    <span className="absolute -top-2 -right-2 bg-white text-amber-600 text-xs font-bold rounded-full h-5 w-5 flex items-center justify-center animate-bounce-subtle shadow-lg">
                      {cartItemCount}
                    </span>
                  )}
                </Link>

                {/* User Menu */}
                <div className="flex items-center space-x-4">
                  <div className="flex items-center space-x-2 bg-white/20 px-3 py-2 rounded-lg">
                    <User className="h-5 w-5 text-white" />
                    <span className="text-sm font-medium text-white">
                      {user?.name || user?.email}
                    </span>
                  </div>
                  <button
                    onClick={handleLogout}
                    className="flex items-center gap-2 bg-white/20 hover:bg-white/30 text-white px-4 py-2 rounded-lg transition-all font-medium"
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

