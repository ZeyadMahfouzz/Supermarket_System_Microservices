import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import { CartProvider } from './contexts/CartContext';
import Navbar from './components/common/Navbar';
import ProtectedRoute from './components/common/ProtectedRoute';
import AuthPage from './pages/AuthPage';
import HomePage from './pages/HomePage';
import CartPage from './pages/CartPage';
import OrdersPage from './pages/OrdersPage';
import ProfilePage from './pages/ProfilePage';
import PaymentPage from './pages/PaymentPage';
import OrderSuccessPage from './pages/OrderSuccessPage';
import OrderDetailsPage from './pages/OrderDetailsPage';

function App() {
  return (
    <Router>
      <AuthProvider>
        <CartProvider>
          <div className="App min-h-screen bg-gray-50">
            <Routes>
              {/* Auth Page - Landing Page */}
              <Route path="/" element={<AuthPage />} />

              {/* Protected Routes with Navbar */}
              <Route
                path="/home"
                element={
                  <ProtectedRoute>
                    <>
                      <Navbar />
                      <HomePage />
                    </>
                  </ProtectedRoute>
                }
              />
              <Route
                path="/cart"
                element={
                  <ProtectedRoute>
                    <>
                      <Navbar />
                      <CartPage />
                    </>
                  </ProtectedRoute>
                }
              />
              <Route
                path="/orders"
                element={
                  <ProtectedRoute>
                    <>
                      <Navbar />
                      <OrdersPage />
                    </>
                  </ProtectedRoute>
                }
              />
              <Route
                path="/profile"
                element={
                  <ProtectedRoute>
                    <>
                      <Navbar />
                      <ProfilePage />
                    </>
                  </ProtectedRoute>
                }
              />
              <Route
                path="/payment"
                element={
                  <ProtectedRoute>
                    <>
                      <Navbar />
                      <PaymentPage />
                    </>
                  </ProtectedRoute>
                }
              />
              <Route
                path="/order-success"
                element={
                  <ProtectedRoute>
                    <>
                      <Navbar />
                      <OrderSuccessPage />
                    </>
                  </ProtectedRoute>
                }
              />
              <Route
                path="/order-details/:orderId"
                element={
                  <ProtectedRoute>
                    <>
                      <Navbar />
                      <OrderDetailsPage />
                    </>
                  </ProtectedRoute>
                }
              />

              {/* Redirect unknown routes to landing page */}
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </div>
        </CartProvider>
      </AuthProvider>
    </Router>
  );
}

export default App;
