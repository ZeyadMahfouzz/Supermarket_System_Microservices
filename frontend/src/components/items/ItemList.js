import React, { useState, useEffect, useCallback } from 'react';
import { Search, Plus, Edit2, Trash2 } from 'lucide-react';
import { itemsAPI } from '../../api/services';
import { useAuth } from '../../contexts/AuthContext';
import ItemCard from './ItemCard';
import ItemModal from './ItemModal';
import Spinner from '../common/Spinner';
import Input from '../common/Input';
import Button from '../common/Button';

const ItemList = () => {
  const [items, setItems] = useState([]);
  const [filteredItems, setFilteredItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('All');
  const [showModal, setShowModal] = useState(false);
  const [editingItem, setEditingItem] = useState(null);
  const [modalMode, setModalMode] = useState('add');
  const { isAdmin } = useAuth();

  useEffect(() => {
    fetchItems();
  }, []);

  useEffect(() => {
    filterItems();
  }, [searchQuery, selectedCategory, items]);

  const fetchItems = async () => {
    try {
      setLoading(true);
      const data = await itemsAPI.getAllItems();
      setItems(data);
      setFilteredItems(data);
    } catch (error) {
      console.error('Error fetching items:', error);
    } finally {
      setLoading(false);
    }
  };

  const filterItems = useCallback(() => {
    let filtered = items;

    // Filter by search query
    if (searchQuery) {
      filtered = filtered.filter(item =>
        item.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
        item.description?.toLowerCase().includes(searchQuery.toLowerCase())
      );
    }

    // Filter by category
    if (selectedCategory !== 'All') {
      filtered = filtered.filter(item => item.category === selectedCategory);
    }

    setFilteredItems(filtered);
  }, [items, searchQuery, selectedCategory]);

  const handleAddItem = () => {
    setModalMode('add');
    setEditingItem(null);
    setShowModal(true);
  };

  const handleEditItem = (item) => {
    setModalMode('edit');
    setEditingItem(item);
    setShowModal(true);
  };

  const handleDeleteItem = async (itemId) => {
    if (!window.confirm('Are you sure you want to delete this item?')) return;

    try {
      await itemsAPI.deleteItem(itemId);
      alert('Item deleted successfully!');
      fetchItems(); // Refresh the list
    } catch (error) {
      console.error('Error deleting item:', error);
      alert('Failed to delete item');
    }
  };

  const handleSaveItem = async (itemData) => {
    try {
      console.log('Saving item data:', itemData);

      if (modalMode === 'add') {
        await itemsAPI.createItem(itemData);
        alert('Item added successfully!');
      } else {
        console.log('Updating item ID:', editingItem.id);
        const response = await itemsAPI.updateItem(editingItem.id, itemData);
        console.log('Update response:', response);
        alert('Item updated successfully!');
      }
      fetchItems(); // Refresh the list
      return true;
    } catch (error) {
      console.error('Error saving item:', error);
      console.error('Error response:', error.response?.data);
      alert(`Failed to save item: ${error.response?.data?.error || error.message}`);
      return false;
    }
  };

  const categories = ['All', ...new Set(items.map(item => item.category || 'General'))];

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-[60vh]">
        <Spinner size="xl" />
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header */}
      <div className="mb-8 animate-slide-up flex items-center justify-between">
        <div>
          <h1 className="text-4xl font-bold text-gray-800 mb-2">Our Products</h1>
          <p className="text-gray-600">
            {isAdmin ? 'Manage your product inventory' : 'Browse and add items to your cart'}
          </p>
        </div>

        {/* Add Product Button - Admin Only */}
        {isAdmin && (
          <Button
            variant="primary"
            onClick={handleAddItem}
            className="flex items-center gap-2"
          >
            <Plus className="h-5 w-5" />
            Add Product
          </Button>
        )}
      </div>

      {/* Search and Filter */}
      <div className="mb-8 space-y-4 animate-slide-up" style={{ animationDelay: '0.1s' }}>
        {/* Search Bar */}
        <Input
          type="text"
          icon={Search}
          placeholder="Search products..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
        />

        {/* Category Filter */}
        <div className="flex flex-wrap gap-2">
          {categories.map((category) => (
            <button
              key={category}
              onClick={() => setSelectedCategory(category)}
              className={`
                px-4 py-2 rounded-lg font-medium transition-all duration-200
                ${selectedCategory === category
                  ? 'bg-blue-600 text-white shadow-md'
                  : 'bg-white text-gray-700 hover:bg-gray-100 border border-gray-300'
                }
              `}
            >
              {category}
            </button>
          ))}
        </div>
      </div>

      {/* Items Grid */}
      {filteredItems.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-gray-500 text-lg">No items found</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {filteredItems.map((item, index) => (
            <div
              key={item.id}
              style={{ animationDelay: `${index * 0.05}s` }}
              className="relative group"
            >
              <ItemCard item={item} />

              {/* Admin Controls Overlay */}
              {isAdmin && (
                <div className="absolute top-2 right-2 flex gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                  <button
                    onClick={() => handleEditItem(item)}
                    className="p-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 shadow-lg transition"
                    title="Edit item"
                  >
                    <Edit2 className="h-4 w-4" />
                  </button>
                  <button
                    onClick={() => handleDeleteItem(item.id)}
                    className="p-2 bg-red-600 text-white rounded-lg hover:bg-red-700 shadow-lg transition"
                    title="Delete item"
                  >
                    <Trash2 className="h-4 w-4" />
                  </button>
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {/* Item Modal for Add/Edit */}
      {showModal && (
        <ItemModal
          item={editingItem}
          mode={modalMode}
          onClose={() => setShowModal(false)}
          onSave={handleSaveItem}
        />
      )}
    </div>
  );
};

export default ItemList;

