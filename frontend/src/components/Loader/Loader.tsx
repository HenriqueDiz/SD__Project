import React from 'react';
import './Loader.css';

interface LoaderProps {
  primaryColor?: string;
  secondaryColor?: string;
  accentColor?: string;
  textColor?: string;
}

const Loader: React.FC<LoaderProps> = ({
  primaryColor = '#6bff9d',
  secondaryColor = '#00ff88',
  accentColor = '#c5ff42',
  textColor = '#c5ff42'
}) => {
  return (
    <div
      className="loader"
      style={{
        ['--loader-color-one' as any]: primaryColor,
        ['--loader-color-two' as any]: secondaryColor,
        ['--loader-color-three' as any]: accentColor,
        ['--loader-text-color' as any]: textColor,
      }}
    >
      <div className="load-inner load-one" />
      <div className="load-inner load-two" />
      <div className="load-inner load-three" />
      <span className="text">Loading...</span>
    </div>
  );
};

export default Loader;
