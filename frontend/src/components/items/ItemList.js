import React, { useState, useEffect, useCallback } from 'react';
import { Search } from 'lucide-react';
import { itemsAPI } from '../../api/services';
import ItemCard from './ItemCard';
import Spinner from '../common/Spinner';
import Input from '../common/Input';

const ItemList = () => {
  const [items, setItems] = useState([]);
  const [filteredItems, setFilteredItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('All');

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
      <div className="mb-8 animate-slide-up">
        <h1 className="text-4xl font-bold text-gray-800 mb-2">Our Products</h1>
        <p className="text-gray-600">Browse and add items to your cart</p>
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
            >
              <ItemCard item={item} />
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default ItemList;

