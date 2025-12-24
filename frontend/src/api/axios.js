import axios from 'axios';

// Base API URL - Gateway runs on port 8080
const API_BASE_URL = 'http://localhost:8080';

// Create axios instance with default config
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add JWT token to headers
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    const userId = localStorage.getItem('userId');
    const userRole = localStorage.getItem('userRole');

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    if (userId) {
      config.headers['X-User-Id'] = userId;
    }

    if (userRole) {
      config.headers['X-User-Role'] = userRole;
    }

    console.log('API Request Headers:', {
      userId: config.headers['X-User-Id'],
      userRole: config.headers['X-User-Role'],
      url: config.url
    });

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor for error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Unauthorized - clear token and redirect to login
      localStorage.removeItem('token');
      localStorage.removeItem('userId');
      localStorage.removeItem('userEmail');
      localStorage.removeItem('userName');
      localStorage.removeItem('userRole');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;

