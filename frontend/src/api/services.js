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
    // Backend uses GET /items/details with @RequestBody which is non-standard
    // Use axios POST as workaround or fetch all items and filter
    try {
      // Try to use POST method to send body data (more reliable)
      const response = await api.post('/items/details', { id });
      return response.data;
    } catch (error) {
      // If POST fails, try GET with params
      console.warn('POST to /items/details failed, trying alternative');
      try {
        // Fallback: get all items and find the one we need
        const allItems = await api.get('/items');
        const item = allItems.data.find(item => item.id === id);
        if (!item) {
          throw new Error('Item not found');
        }
        return item;
      } catch (fallbackError) {
        console.error('Both methods failed to get item', error, fallbackError);
        throw error;
      }
    }
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
    const response = await api.delete('/items', {
      data: { id }
    });
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
    console.log('cartAPI: Sending add to cart request', { itemId, quantity });
    try {
      const response = await api.post('/cart/items', { itemId, quantity });
      console.log('cartAPI: Add to cart successful', response.data);
      return response.data;
    } catch (error) {
      console.error('cartAPI: Add to cart failed', error);
      console.error('cartAPI: Error response:', error.response?.data);
      throw error;
    }
  },

  updateCartItemQuantity: async (cartItemId, quantity) => {
    const response = await api.put('/cart/items', { cartItemId, quantity });
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

