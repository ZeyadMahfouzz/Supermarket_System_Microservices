import React, { createContext, useContext, useState, useEffect } from 'react';
import { authAPI } from '../api/services';

const AuthContext = createContext(null);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check if user is logged in on mount
    const token = localStorage.getItem('token');
    const userId = localStorage.getItem('userId');
    const userEmail = localStorage.getItem('userEmail');
    const userName = localStorage.getItem('userName');
    const userRole = localStorage.getItem('userRole');

    if (token && userId) {
      setUser({
        id: userId,
        email: userEmail,
        name: userName,
        role: userRole,
      });
    }
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    try {
      const response = await authAPI.login(email, password);

      if (response.token) {
        // Decode JWT to get user info (simple base64 decode)
        const tokenParts = response.token.split('.');
        const payload = JSON.parse(atob(tokenParts[1]));

        const userData = {
          id: payload.userId,
          email: payload.sub,
          name: payload.name || email.split('@')[0],
          role: payload.role || 'USER',
        };

        console.log('Login successful - User data:', userData);
        console.log('Role from JWT:', payload.role);
        console.log('Is Admin?', userData.role === 'ADMIN');

        localStorage.setItem('token', response.token);
        localStorage.setItem('userId', userData.id);
        localStorage.setItem('userEmail', userData.email);
        localStorage.setItem('userName', userData.name);
        localStorage.setItem('userRole', userData.role);

        setUser(userData);
        return { success: true };
      } else {
        return { success: false, message: response.message };
      }
    } catch (error) {
      console.error('Login error:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Login failed. Please try again.'
      };
    }
  };

  const register = async (userData) => {
    try {
      await authAPI.register(userData);
      // After successful registration, login
      return await login(userData.email, userData.password);
    } catch (error) {
      console.error('Registration error:', error);
      return {
        success: false,
        message: error.response?.data?.message || 'Registration failed. Please try again.'
      };
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('userName');
    localStorage.removeItem('userRole');
    setUser(null);
  };

  const value = {
    user,
    loading,
    login,
    register,
    logout,
    isAuthenticated: !!user,
    isAdmin: user?.role === 'ADMIN',
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

