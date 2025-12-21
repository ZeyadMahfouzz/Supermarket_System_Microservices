import React from 'react';
import { X, CheckCircle, AlertCircle, Info } from 'lucide-react';

const Toast = ({ message, type = 'info', onClose }) => {
  const types = {
    success: {
      bg: 'bg-green-50',
      border: 'border-green-500',
      text: 'text-green-800',
      icon: CheckCircle,
    },
    error: {
      bg: 'bg-red-50',
      border: 'border-red-500',
      text: 'text-red-800',
      icon: AlertCircle,
    },
    info: {
      bg: 'bg-blue-50',
      border: 'border-blue-500',
      text: 'text-blue-800',
      icon: Info,
    },
  };

  const config = types[type];
  const Icon = config.icon;

  return (
    <div className={`${config.bg} ${config.text} border-l-4 ${config.border} p-4 rounded-lg shadow-lg animate-slide-down flex items-center gap-3`}>
      <Icon className="h-5 w-5 flex-shrink-0" />
      <p className="flex-1">{message}</p>
      <button
        onClick={onClose}
        className="flex-shrink-0 hover:opacity-70 transition-opacity"
      >
        <X className="h-5 w-5" />
      </button>
    </div>
  );
};

export default Toast;

