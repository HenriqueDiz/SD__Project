'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import './SearchBar.css';

interface SearchBarProps {
  onSearch?: (query: string) => void;
  placeholder?: string;
  redirectOnSubmit?: boolean;
  redirectPath?: string;
}

export default function SearchBar({ 
  onSearch, 
  placeholder = 'Search the web...',
  redirectOnSubmit = false,
  redirectPath = '/demo'
}: SearchBarProps) {
  const [query, setQuery] = useState('');
  const router = useRouter();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (query.trim()) {
      if (onSearch) {
        onSearch(query.trim());
      }
      if (redirectOnSubmit) {
        router.push(`${redirectPath}?q=${encodeURIComponent(query.trim())}`);
      }
    }
  };

  const handleButtonClick = () => {
    if (query.trim()) {
      if (onSearch) {
        onSearch(query.trim());
      }
      if (redirectOnSubmit) {
        router.push(`${redirectPath}?q=${encodeURIComponent(query.trim())}`);
      }
    }
  };

  return (
    <form className="search-bar" onSubmit={handleSubmit}>
      <input
        type="text"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        placeholder={placeholder}
        className="search-input"
      />
      <button type="button" onClick={handleButtonClick} className={`search-button ${query.trim() ? 'filled' : ''}`}>
        <svg 
          width="20" 
          height="20" 
          viewBox="0 0 24 24" 
          fill="none" 
          stroke="currentColor" 
          strokeWidth="2" 
          strokeLinecap="round" 
          strokeLinejoin="round"
        >
          <circle cx="11" cy="11" r="8"></circle>
          <path d="m21 21-4.35-4.35"></path>
        </svg>
      </button>
    </form>
  );
}
