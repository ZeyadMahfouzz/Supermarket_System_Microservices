import api from './axios';

// User Authentication APIs
export const authAPI = {
  register: async (userData) => {
    const response = await api.post('/users/register', userData);
    return response.data;
  },

  login: async (email, password) => {
    const response = await api.post('/users/login', { email, password });
    return response.data;
  },
};

// Items APIs
export const itemsAPI = {
  getAllItems: async () => {
    const response = await api.get('/items');
    return response.data;
  },

  getItemById: async (id) => {
    const response = await api.get(`/items/${id}`);
    return response.data;
  },

  createItem: async (itemData) => {
    const response = await api.post('/items', itemData);
    return response.data;
  },

  updateItem: async (id, itemData) => {
    const response = await api.put('/items/update', {
      id,
      ...itemData
    });
    return response.data;
  },

  deleteItem: async (id) => {
    const response = await api.delete(`/items/${id}`);
    return response.data;
  },
};

// Cart APIs
export const cartAPI = {
  getCart: async () => {
    const response = await api.get('/cart');
    return response.data;
  },

  addItemToCart: async (itemId, quantity) => {
    const response = await api.post('/cart/items', { itemId, quantity });
    return response.data;
  },

  updateCartItemQuantity: async (cartItemId, quantity) => {
    const response = await api.put(`/cart/items/${cartItemId}`, { quantity });
    return response.data;
  },

  removeCartItem: async (cartItemId) => {
    const response = await api.delete('/cart/items', {
      data: { cartItemId }
    });
    return response.data;
  },

  clearCart: async () => {
    const response = await api.delete('/cart');
    return response.data;
  },

  checkout: async (checkoutRequest) => {
    const response = await api.post('/cart/checkout', checkoutRequest);
    return response.data;
  },
};

// Orders APIs
export const ordersAPI = {
  // Get user's order history
  getUserOrders: async () => {
    const response = await api.get('/orders/history');
    return response.data;
  },

  // Get specific order by ID
  getOrderById: async (orderId) => {
    const response = await api.post('/orders/details', { orderId });
    return response.data;
  },

  // Get all orders (ADMIN only)
  getAllOrders: async () => {
    const response = await api.get('/orders/all');
    return response.data;
  },

  // Get orders by status (using POST for better compatibility with body)
  getOrdersByStatus: async (status) => {
    const response = await api.post('/orders/status', { status });
    return response.data;
  },

  // Update order status (ADMIN only) - uses PATCH
  updateOrderStatus: async (orderId, status) => {
    const response = await api.patch('/orders/status/update', { orderId, status });
    return response.data;
  },

  // Cancel order - uses DELETE
  cancelOrder: async (orderId) => {
    const response = await api.delete('/orders/cancel', {
      data: { orderId }
    });
    return response.data;
  },
};

