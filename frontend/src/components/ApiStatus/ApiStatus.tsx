'use client';

import { useState, useEffect } from 'react';
import { checkHealth, isApiAvailable } from '@/services/api';

interface ApiStatusProps {
  showDetails?: boolean;
}

export default function ApiStatus({ showDetails = false }: ApiStatusProps) {
  const [isOnline, setIsOnline] = useState<boolean | null>(null);
  const [gatewayStatus, setGatewayStatus] = useState<string>('CHECKING');

  useEffect(() => {
    checkApiStatus();
    const interval = setInterval(checkApiStatus, 10000); // Check every 10 seconds
    return () => clearInterval(interval);
  }, []);

  const checkApiStatus = async () => {
    try {
      const health = await checkHealth();
      setIsOnline(health.status === 'UP');
      setGatewayStatus(health.gateway);
    } catch {
      setIsOnline(false);
      setGatewayStatus('DISCONNECTED');
    }
  };

  if (isOnline === null) {
    return null; // Don't show anything while checking
  }

  if (isOnline && !showDetails) {
    return null; // Don't show anything if online and not showing details
  }

  return (
    <div
      style={{
        position: 'fixed',
        bottom: '1rem',
        right: '1rem',
        zIndex: 1000,
        padding: '0.75rem 1rem',
        background: isOnline
          ? 'rgba(46, 213, 115, 0.15)'
          : 'rgba(255, 68, 68, 0.15)',
        backdropFilter: 'blur(10px)',
        border: `1px solid ${isOnline ? 'rgba(46, 213, 115, 0.3)' : 'rgba(255, 68, 68, 0.3)'}`,
        borderRadius: '12px',
        color: isOnline ? '#2ed573' : '#ff4444',
        fontSize: '0.85rem',
        fontWeight: 500,
        display: 'flex',
        alignItems: 'center',
        gap: '0.5rem',
        transition: 'all 0.3s ease',
      }}
    >
      <div
        style={{
          width: '8px',
          height: '8px',
          borderRadius: '50%',
          background: isOnline ? '#2ed573' : '#ff4444',
          animation: isOnline ? 'pulse 2s ease-in-out infinite' : 'none',
        }}
      />
      <span>
        {isOnline ? 'API Online' : 'API Offline'}
        {showDetails && gatewayStatus && ` • Gateway: ${gatewayStatus}`}
      </span>
      <style jsx>{`
        @keyframes pulse {
          0%, 100% {
            opacity: 1;
          }
          50% {
            opacity: 0.5;
          }
        }
      `}</style>
    </div>
  );
}
