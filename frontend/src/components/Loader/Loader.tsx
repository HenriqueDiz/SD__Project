import React from 'react';
import './Loader.css';

const Loader = () => {
  return (
    <div className="loader">
      <div className="load-inner load-one" />
      <div className="load-inner load-two" />
      <div className="load-inner load-three" />
      <span className="text">Loading...</span>
    </div>
  );
};

export default Loader;
