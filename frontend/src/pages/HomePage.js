import React from 'react';
import ItemList from '../components/items/ItemList';

const HomePage = () => {
  return (
    <div className="min-h-screen bg-gradient-to-br from-amber-50 via-orange-50 to-yellow-50">
      {/* Hero Banner */}
      <div className="bg-gradient-to-r from-amber-500 via-orange-500 to-red-500 text-white py-12 shadow-xl">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center">
            <h1 className="text-5xl font-bold mb-4 drop-shadow-lg animate-fade-in" style={{ fontFamily: 'Georgia, serif' }}>
              🌾 Welcome to Baraka Souq 🌾
            </h1>
            <p className="text-xl mb-2 animate-slide-up">بركة السوق - Egyptian Fresh Market</p>
            <p className="text-lg opacity-90 animate-slide-up">Fresh from the Nile Valley to Your Home</p>
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

