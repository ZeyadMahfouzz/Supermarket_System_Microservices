import React from 'react';
import { User, Mail, ShieldCheck, Package, ShoppingCart } from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';
import { useCart } from '../contexts/CartContext';
import Card from '../components/common/Card';

const ProfilePage = () => {
  const { user, isAdmin } = useAuth();
  const { cartItemCount } = useCart();

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-4xl font-bold text-gray-800 mb-2">My Profile</h1>
          <p className="text-gray-600">Manage your account information</p>
        </div>

        {/* Profile Card */}
        <Card className="p-8 mb-6">
          <div className="flex items-center space-x-6 mb-6">
            <div className="w-24 h-24 rounded-full bg-gradient-to-br from-blue-500 to-blue-700 flex items-center justify-center">
              <User className="h-12 w-12 text-white" />
            </div>
            <div>
              <h2 className="text-3xl font-bold text-gray-800">{user?.name}</h2>
              <div className="flex items-center space-x-2 mt-2">
                {isAdmin ? (
                  <span className="px-3 py-1 bg-purple-100 text-purple-700 rounded-full text-sm font-medium flex items-center gap-1">
                    <ShieldCheck className="h-4 w-4" />
                    Administrator
                  </span>
                ) : (
                  <span className="px-3 py-1 bg-green-100 text-green-700 rounded-full text-sm font-medium">
                    Customer
                  </span>
                )}
              </div>
            </div>
          </div>

          <div className="grid md:grid-cols-1 gap-6">
            {/* Email */}
            <div className="flex items-center space-x-3 p-4 bg-gray-50 rounded-lg">
              <Mail className="h-6 w-6 text-blue-600" />
              <div>
                <p className="text-sm text-gray-500">Email Address</p>
                <p className="text-gray-800 font-medium">{user?.email}</p>
              </div>
            </div>
          </div>
        </Card>

        {/* Quick Stats - Only for regular users */}
        {!isAdmin && (
          <div className="grid md:grid-cols-2 gap-6">
            <Card className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-500 mb-1">Items in Cart</p>
                  <p className="text-3xl font-bold text-blue-600">{cartItemCount}</p>
                </div>
                <div className="w-14 h-14 bg-blue-100 rounded-full flex items-center justify-center">
                  <ShoppingCart className="h-8 w-8 text-blue-600" />
                </div>
              </div>
            </Card>

            <Card className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-500 mb-1">Account Type</p>
                  <p className="text-xl font-bold text-gray-800">Customer Account</p>
                </div>
                <div className="w-14 h-14 bg-green-100 rounded-full flex items-center justify-center">
                  <Package className="h-8 w-8 text-green-600" />
                </div>
              </div>
            </Card>
          </div>
        )}

        {/* Admin Info */}
        {isAdmin && (
          <Card className="p-6 bg-gradient-to-r from-purple-50 to-blue-50 border-purple-200">
            <div className="flex items-center space-x-3">
              <ShieldCheck className="h-8 w-8 text-purple-600" />
              <div>
                <h3 className="text-xl font-bold text-gray-800">Administrator Access</h3>
                <p className="text-gray-600">
                  You have full access to manage products, view all orders, and update order statuses.
                </p>
              </div>
            </div>
          </Card>
        )}
      </div>
    </div>
  );
};

export default ProfilePage;

