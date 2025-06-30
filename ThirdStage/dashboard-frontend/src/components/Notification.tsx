import React, { useEffect, useState } from 'react';
import type { NotificationData } from '../types/notifications';

interface NotificationProps {
  notification: NotificationData;
  onClose: (id: string) => void;
}

const Notification: React.FC<NotificationProps> = ({ notification, onClose }) => {
  const [isVisible, setIsVisible] = useState(false);
  const [isExiting, setIsExiting] = useState(false);

  useEffect(() => {
    // Entrance animation
    const timer = setTimeout(() => setIsVisible(true), 100);
    
    // Auto-close timer
    const duration = notification.duration || 5000;
    const autoCloseTimer = setTimeout(() => {
      handleClose();
    }, duration);

    return () => {
      clearTimeout(timer);
      clearTimeout(autoCloseTimer);
    };
  }, [notification.duration]);

  const handleClose = () => {
    setIsExiting(true);
    setTimeout(() => {
      onClose(notification.id);
    }, 300); // Match exit animation duration
  };

  const getNotificationStyles = () => {
    const baseStyles = "relative p-4 rounded-lg shadow-lg border-l-4 max-w-md w-full";
    
    switch (notification.type) {
      case 'success':
        return `${baseStyles} bg-green-50 dark:bg-green-900/20 border-green-500 text-green-800 dark:text-green-200`;
      case 'error':
        return `${baseStyles} bg-red-50 dark:bg-red-900/20 border-red-500 text-red-800 dark:text-red-200`;
      case 'warning':
        return `${baseStyles} bg-yellow-50 dark:bg-yellow-900/20 border-yellow-500 text-yellow-800 dark:text-yellow-200`;
      case 'info':
        return `${baseStyles} bg-blue-50 dark:bg-blue-900/20 border-blue-500 text-blue-800 dark:text-blue-200`;
      default:
        return `${baseStyles} bg-gray-50 dark:bg-gray-900/20 border-gray-500 text-gray-800 dark:text-gray-200`;
    }
  };

  const getIcon = () => {
    switch (notification.type) {
      case 'success':
        return <i className="fas fa-check-circle text-green-500"></i>;
      case 'error':
        return <i className="fas fa-exclamation-circle text-red-500"></i>;
      case 'warning':
        return <i className="fas fa-exclamation-triangle text-yellow-500"></i>;
      case 'info':
        return <i className="fas fa-info-circle text-blue-500"></i>;
      default:
        return <i className="fas fa-bell text-gray-500"></i>;
    }
  };

  const animationClasses = isExiting 
    ? "transform translate-x-full opacity-0 transition-all duration-300 ease-in"
    : isVisible 
      ? "transform translate-x-0 opacity-100 transition-all duration-300 ease-out"
      : "transform translate-x-full opacity-0";

  return (
    <div className={`${getNotificationStyles()} ${animationClasses}`}>
      <div className="flex items-start">
        <div className="flex-shrink-0 mr-3 mt-0.5">
          {getIcon()}
        </div>
        <div className="flex-1 min-w-0">
          <h4 className="font-medium text-sm mb-1">{notification.title}</h4>
          <p className="text-sm opacity-90">{notification.message}</p>
        </div>
        <button
          onClick={handleClose}
          className="flex-shrink-0 ml-3 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 transition-colors"
        >
          <i className="fas fa-times text-sm"></i>
        </button>
      </div>
    </div>
  );
};

export default Notification;
