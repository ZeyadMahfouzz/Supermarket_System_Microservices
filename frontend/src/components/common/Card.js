import React from 'react';

const Card = ({ children, className = '', hover = false, onClick }) => {
  const hoverStyles = hover ? 'hover:shadow-xl hover:-translate-y-1 cursor-pointer' : '';

  return (
    <div
      onClick={onClick}
      className={`
        bg-white rounded-xl shadow-md transition-all duration-300
        ${hoverStyles}
        ${className}
      `}
    >
      {children}
    </div>
  );
};

export default Card;

