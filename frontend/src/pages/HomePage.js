import React from 'react';
import ItemList from '../components/items/ItemList';

const HomePage = () => {
  return (
    <div className="min-h-screen bg-gray-50">
      {/* Hero Banner */}
      <div className="bg-blue-600 text-white py-10 shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center">
            <h1 className="text-4xl font-semibold mb-3">
              Welcome to SpringMart
            </h1>
            <p className="text-lg text-blue-100">Fresh groceries delivered to your door</p>
          </div>
        </div>
      </div>

      {/* Products Section */}
      <div className="py-8">
        <ItemList />
      </div>
    </div>
  );
};

export default HomePage;

