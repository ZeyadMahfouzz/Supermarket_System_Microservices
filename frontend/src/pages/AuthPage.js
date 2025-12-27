import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Mail, Lock, User, Phone, MapPin } from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';

const AuthPage = () => {
  const [isLogin, setIsLogin] = useState(true);
  const [loginData, setLoginData] = useState({ email: '', password: '' });
  const [registerData, setRegisterData] = useState({
    name: '',
    email: '',
    password: '',
    confirmPassword: '',
    phone: '',
    address: '',
    role: 'USER',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const { login, register } = useAuth();
  const navigate = useNavigate();

  const handleLoginSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    const result = await login(loginData.email, loginData.password);

    if (result.success) {
      navigate('/home');
    } else {
      setError(result.message || 'Login failed. Please try again.');
    }

    setLoading(false);
  };

  const handleRegisterSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (registerData.password !== registerData.confirmPassword) {
      setError('Passwords do not match');
      return;
    }

    if (registerData.password.length < 6) {
      setError('Password must be at least 6 characters');
      return;
    }

    setLoading(true);

    const { confirmPassword, ...submitData } = registerData;
    const result = await register(submitData);

    if (result.success) {
      navigate('/home');
    } else {
      setError(result.message || 'Registration failed. Please try again.');
    }

    setLoading(false);
  };

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
      <div className="w-full max-w-5xl">
        <div className="bg-white rounded-2xl shadow-lg overflow-hidden border border-gray-100">
          <div className="grid md:grid-cols-2">
            {/* Left Side - Branding */}
            <div className="bg-blue-600 p-12 flex flex-col justify-center items-center text-white relative">
              {/* Simple decorative elements */}
              <div className="absolute top-8 right-8 text-3xl opacity-20"></div>
              <div className="absolute bottom-8 left-8 text-3xl opacity-20"></div>

              <div className="text-center space-y-6">
                {/* Logo */}
                <div className="mb-8 flex justify-center">
                  <div className="w-24 h-24 bg-white rounded-2xl flex items-center justify-center shadow-md">
                    <svg className="w-14 h-14 text-blue-600" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
                    </svg>
                  </div>
                </div>

                {/* Brand Name */}
                <div>
                  <h1 className="text-5xl font-bold mb-3">
                    SpringMart
                  </h1>
                  <p className="text-xl text-blue-100">Fresh groceries, delivered fast</p>
                </div>

                <div className="pt-8 space-y-3 text-blue-50">
                  <p className="text-base">• Easy online shopping</p>
                  <p className="text-base">• Quick delivery</p>
                  <p className="text-base">• Quality products</p>
                </div>
              </div>
            </div>

            {/* Right Side - Auth Forms */}
            <div className="p-12 relative bg-white">
              {/* Toggle Buttons */}
              <div className="flex gap-2 mb-8">
                <button
                  onClick={() => setIsLogin(true)}
                  className={`flex-1 py-3 px-4 rounded-lg font-medium transition ${
                    isLogin
                      ? 'bg-blue-600 text-white shadow-sm'
                      : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                  }`}
                >
                  Login
                </button>
                <button
                  onClick={() => setIsLogin(false)}
                  className={`flex-1 py-3 px-4 rounded-lg font-medium transition ${
                    !isLogin
                      ? 'bg-blue-600 text-white shadow-sm'
                      : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                  }`}
                >
                  Sign Up
                </button>
              </div>

              {/* Error Message */}
              {error && (
                <div className="mb-6 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
                  {error}
                </div>
              )}

              {/* Login Form */}
              <div
                className={`transition-all duration-500 ease-in-out ${
                  isLogin 
                    ? 'opacity-100 block' 
                    : 'opacity-0 hidden'
                }`}
              >
                <h2 className="text-2xl font-semibold text-gray-900 mb-6">Welcome back</h2>
                <form onSubmit={handleLoginSubmit} className="space-y-5">
                  {/* ...existing form fields... */}
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Email</label>
                    <div className="relative">
                      <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                      <input
                        type="email"
                        value={loginData.email}
                        onChange={(e) => setLoginData({ ...loginData, email: e.target.value })}
                        className="w-full pl-12 pr-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:border-blue-500 transition"
                        placeholder="your@email.com"
                        required
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Password</label>
                    <div className="relative">
                      <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                      <input
                        type="password"
                        value={loginData.password}
                        onChange={(e) => setLoginData({ ...loginData, password: e.target.value })}
                        className="w-full pl-12 pr-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:border-blue-500 transition"
                        placeholder="••••••••"
                        required
                      />
                    </div>
                  </div>

                  <button
                    type="submit"
                    disabled={loading}
                    className="w-full bg-blue-600 text-white py-2.5 rounded-lg font-medium hover:bg-blue-700 transition disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {loading ? 'Signing in...' : 'Sign in'}
                  </button>

                  <div className="mt-4 text-center">
                    <p className="text-xs text-gray-500">
                      Your account type (User/Admin) was set during signup
                    </p>
                  </div>
                </form>
              </div>

              {/* Register Form */}
              <div
                className={`transition-all duration-500 ease-in-out ${
                  !isLogin 
                    ? 'opacity-100 block' 
                    : 'opacity-0 hidden'
                }`}
              >
                <h2 className="text-2xl font-semibold text-gray-900 mb-6">Create an account</h2>
                <form onSubmit={handleRegisterSubmit} className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Full Name</label>
                    <div className="relative">
                      <User className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                      <input
                        type="text"
                        value={registerData.name}
                        onChange={(e) => setRegisterData({ ...registerData, name: e.target.value })}
                        className="w-full pl-12 pr-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:border-blue-500 transition"
                        placeholder="John Doe"
                        required
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Email</label>
                    <div className="relative">
                      <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                      <input
                        type="email"
                        value={registerData.email}
                        onChange={(e) => setRegisterData({ ...registerData, email: e.target.value })}
                        className="w-full pl-12 pr-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:border-blue-500 transition"
                        placeholder="your@email.com"
                        required
                      />
                    </div>
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">Password</label>
                      <div className="relative">
                        <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                        <input
                          type="password"
                          value={registerData.password}
                          onChange={(e) => setRegisterData({ ...registerData, password: e.target.value })}
                          className="w-full pl-12 pr-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:border-blue-500 transition"
                          placeholder="••••••••"
                          required
                        />
                      </div>
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">Confirm</label>
                      <div className="relative">
                        <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                        <input
                          type="password"
                          value={registerData.confirmPassword}
                          onChange={(e) => setRegisterData({ ...registerData, confirmPassword: e.target.value })}
                          className="w-full pl-12 pr-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:border-blue-500 transition"
                          placeholder="••••••••"
                          required
                        />
                      </div>
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Phone (Optional)</label>
                    <div className="relative">
                      <Phone className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                      <input
                        type="tel"
                        value={registerData.phone}
                        onChange={(e) => setRegisterData({ ...registerData, phone: e.target.value })}
                        className="w-full pl-12 pr-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:border-blue-500 transition"
                        placeholder="+1 (555) 123-4567"
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Address (Optional)</label>
                    <div className="relative">
                      <MapPin className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                      <input
                        type="text"
                        value={registerData.address}
                        onChange={(e) => setRegisterData({ ...registerData, address: e.target.value })}
                        className="w-full pl-12 pr-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:border-blue-500 transition"
                        placeholder="123 Main St, City, Country"
                      />
                    </div>
                  </div>

                  <div className="border-2 border-blue-200 rounded-lg p-4 bg-blue-50">
                    <label className="block text-sm font-bold text-gray-900 mb-2">
                      ⭐ Account Type
                    </label>
                    <div className="relative">
                      <User className="absolute left-3 top-1/2 transform -translate-y-1/2 text-blue-600 w-5 h-5 z-10" />
                      <select
                        value={registerData.role}
                        onChange={(e) => setRegisterData({ ...registerData, role: e.target.value })}
                        className="w-full pl-12 pr-4 py-3 border-2 border-blue-300 rounded-lg focus:outline-none focus:border-blue-600 transition appearance-none bg-white font-medium text-gray-900 cursor-pointer"
                      >
                        <option value="USER">Regular User - Shop & Buy</option>
                        <option value="ADMIN">Admin - Manage Products & Orders</option>
                      </select>
                      <div className="absolute inset-y-0 right-0 flex items-center pr-3 pointer-events-none">
                        <svg className="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 9l-7 7-7-7" />
                        </svg>
                      </div>
                    </div>
                    <p className="text-xs text-blue-700 mt-2 font-medium">
                      <strong>Regular Users</strong> can shop. <strong>Admins</strong> can manage the store.
                    </p>
                  </div>

                  <button
                    type="submit"
                    disabled={loading}
                    className="w-full bg-blue-600 text-white py-2.5 rounded-lg font-medium hover:bg-blue-700 transition disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {loading ? 'Creating account...' : 'Create account'}
                  </button>
                </form>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AuthPage;

