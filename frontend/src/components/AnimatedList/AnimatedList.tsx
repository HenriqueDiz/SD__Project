'use client';

import React, { useRef, useState, useEffect, UIEvent } from 'react';
import { motion } from 'motion/react';
import './AnimatedList.css';
import AIDropdown from '@/components/AIDropdown/AIDropdown';

export interface AnimatedListItem {
  url: string;
  title?: string;
  description?: string;
  links?: string[];
  references?: number;
  hackerNews?: boolean;
}

interface AnimatedListProps {
  items: AnimatedListItem[];
  onItemSelect?: (item: AnimatedListItem, index: number) => void;
  showGradients?: boolean;
  enableArrowNavigation?: boolean;
  className?: string;
  itemClassName?: string;
  initialSelectedIndex?: number;
  
  // AI Dropdown props
  showAIDropdown?: boolean;
  query?: string;
  onAnalysisFetch?: (query: string, citations: string) => Promise<string>;
}

const AnimatedList: React.FC<AnimatedListProps> = ({
  items,
  onItemSelect,
  showGradients = true,
  enableArrowNavigation = true,
  className = '',
  itemClassName = '',
  initialSelectedIndex = -1,
  
  // AI Dropdown
  showAIDropdown = false,
  query = '',
  onAnalysisFetch,
}) => {
  const listRef = useRef<HTMLDivElement>(null);
  const [selectedIndex, setSelectedIndex] = useState<number>(initialSelectedIndex);
  const [openIndex, setOpenIndex] = useState<number>(-1);
  const [keyboardNav, setKeyboardNav] = useState<boolean>(false);
  const [topGradientOpacity, setTopGradientOpacity] = useState<number>(0);
  const [bottomGradientOpacity, setBottomGradientOpacity] = useState<number>(0);
  const [isAIExpanded, setIsAIExpanded] = useState<boolean>(false);

  // Handler for AI dropdown expansion
  const handleAIDropdownExpand = (isExpanded: boolean) => {
    setIsAIExpanded(isExpanded);
    if (isExpanded && listRef.current) {
      // Scroll to top when AI dropdown expands
      listRef.current.scrollTo({ top: 0, behavior: 'smooth' });
    }
  };

  const handleScroll = (e: UIEvent<HTMLDivElement>) => {
    const target = e.target as HTMLDivElement;
    const { scrollTop, scrollHeight, clientHeight } = target;

    setTopGradientOpacity(Math.min(scrollTop / 50, 1));
    const bottomDistance = scrollHeight - (scrollTop + clientHeight);
    setBottomGradientOpacity(scrollHeight <= clientHeight ? 0 : Math.min(bottomDistance / 50, 1));
  };

  // Calcula a opacidade inicial do gradiente de baixo
  useEffect(() => {
    if (listRef.current) {
      const { scrollHeight, clientHeight } = listRef.current;
      setBottomGradientOpacity(scrollHeight > clientHeight ? 1 : 0);
    }
  }, [items]);

  useEffect(() => {
    if (!enableArrowNavigation) return;

    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'ArrowDown' || (e.key === 'Tab' && !e.shiftKey)) {
        e.preventDefault();
        setKeyboardNav(true);
        setSelectedIndex(prev => Math.min(prev + 1, items.length - 1));
      } else if (e.key === 'ArrowUp' || (e.key === 'Tab' && e.shiftKey)) {
        e.preventDefault();
        setKeyboardNav(true);
        setSelectedIndex(prev => Math.max(prev - 1, 0));
      } else if (e.key === 'Enter') {
        if (selectedIndex >= 0 && selectedIndex < items.length) {
          e.preventDefault();
          // Toggle open state on Enter (não abre o site)
          setOpenIndex(prev => prev === selectedIndex ? -1 : selectedIndex);
        }
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [items, selectedIndex, onItemSelect, enableArrowNavigation]);

  useEffect(() => {
    if (!keyboardNav || selectedIndex < 0 || !listRef.current) return;

    const container = listRef.current;
    const itemElements = container.querySelectorAll('.animated-list-item-header');
    const el = itemElements[selectedIndex] as HTMLElement | null;

    if (el) {
      const extraMargin = 50;
      const containerScrollTop = container.scrollTop;
      const containerHeight = container.clientHeight;
      const itemTop = el.offsetTop;
      const itemBottom = itemTop + el.offsetHeight;
      if (itemTop < containerScrollTop + extraMargin) {
        container.scrollTo({ top: itemTop - extraMargin, behavior: 'smooth' });
      } else if (itemBottom > containerScrollTop + containerHeight - extraMargin) {
        container.scrollTo({ top: itemBottom - containerHeight + extraMargin, behavior: 'smooth' });
      }
    }

    setKeyboardNav(false);
  }, [selectedIndex, keyboardNav]);

  return (
    <div className={`scroll-list-container ${className}`}>
      <div 
        ref={listRef} 
        className={`scroll-list no-scrollbar`} 
        onScroll={handleScroll}
      >
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          className="animated-list"
        >
          {items.map((item, index) => {
            const isSelected = selectedIndex === index;
            const isOpen = openIndex === index;
            const showBadge = item.hackerNews === true;
            return (
              <div
                key={`item-${index}`}
                className={`animated-list-item ${isSelected ? 'selected' : ''} ${itemClassName}`}
              >
                <button
                  className="animated-list-item-header"
                  data-index={index}
                  onMouseEnter={() => setSelectedIndex(index)}
                  onClick={() => setOpenIndex(prev => prev === index ? -1 : index)}
                  aria-expanded={isOpen}
                >
                  <div className="animated-list-item-texts">
                    <h3 className="animated-list-item-title">
                      {item.title || item.url}
                    </h3>
                    <p className="animated-list-item-url">{item.url}</p>
                  </div>
                    <div className="animated-list-item-right">
                      {item.references !== undefined && (
                        <div className="animated-list-item-references">
                          {item.references}
                        </div>
                      )}
                      {showBadge && (
                        <div className="hackernews-badge-container">
                          <span className="hackernews-badge">
                            <svg className="hackernews-icon" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                              <circle cx="12" cy="12" r="10" fill="#9d00ffff" />
                               <path d="M8 7 L12 12 L12 17" stroke="#ffffff" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" fill="none" />
                               <path d="M16 7 L12 12" stroke="#ffffff" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" fill="none" />
                            </svg>
                          </span>
                        </div>
                      )}
                    </div>
                </button>
                <motion.div
                  className="animated-list-item-content"
                  initial={false}
                  animate={{ height: isOpen ? 'auto' : 0, opacity: isOpen ? 1 : 0 }}
                  style={{ overflow: 'hidden' }}
                >
                  {isOpen && (
                    <div className="animated-list-item-inner">
                      {item.description && (
                        <div className="animated-list-section">
                          <h4 className="animated-list-section-title">Description</h4>
                          <p className="animated-list-section-text">{item.description}</p>
                        </div>
                      )}
                      {item.links && item.links.length > 0 && (
                        <div className="animated-list-section">
                          <h4 className="animated-list-section-title">Related Links ({item.links.length})</h4>
                          <ul className="animated-list-links">
                            {item.links.map((link, idx) => (
                              <li key={idx} className="animated-list-link-item">
                                <a
                                  href={link}
                                  target="_blank"
                                  rel="noopener noreferrer"
                                  className="related-link"
                                  onClick={e => e.stopPropagation()}
                                >
                                  {link}
                                </a>
                              </li>
                            ))}
                          </ul>
                        </div>
                      )}
                      <div className="animated-list-visit">
                        <a
                          href={item.url}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="related-link visit-main"
                          onClick={(e) => { e.stopPropagation(); if (onItemSelect) onItemSelect(item, index); }}
                        >Visit Website →
                        </a>
                      </div>
                    </div>
                  )}
                </motion.div>
              </div>
            );
          })}
        </motion.div>
      </div>

      {/* AI Dropdown - overlay absoluto no topo */}
      {showAIDropdown && onAnalysisFetch && (
        <div className={`ai-dropdown-overlay ${isAIExpanded ? 'visible' : ''}`}>
          <AIDropdown
            query={query}
            results={items}
            onAnalysisFetch={onAnalysisFetch}
            onExpandChange={handleAIDropdownExpand}
          />
        </div>
      )}

      {showGradients && (
        <>
          <div className="top-gradient" style={{ opacity: topGradientOpacity }}></div>
          <div className="bottom-gradient" style={{ opacity: bottomGradientOpacity }}></div>
        </>
      )}
    </div>
  );
};

export default AnimatedList;
