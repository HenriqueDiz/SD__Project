'use client';

import React, { useRef, useState, useEffect, UIEvent } from 'react';
import { motion } from 'motion/react';
import { Accordion, AccordionItem } from '@heroui/react';
import './AnimatedList.css';

export interface AnimatedListItem {
  url: string;
  title?: string;
  description?: string;
  links?: string[];
}

interface AnimatedListProps {
  items: AnimatedListItem[];
  onItemSelect?: (item: AnimatedListItem, index: number) => void;
  showGradients?: boolean;
  enableArrowNavigation?: boolean;
  className?: string;
  itemClassName?: string;
  displayScrollbar?: boolean;
  initialSelectedIndex?: number;
}

const AnimatedList: React.FC<AnimatedListProps> = ({
  items,
  onItemSelect,
  showGradients = true,
  enableArrowNavigation = true,
  className = '',
  itemClassName = '',
  displayScrollbar = true,
  initialSelectedIndex = -1
}) => {
  const listRef = useRef<HTMLDivElement>(null);
  const [selectedIndex, setSelectedIndex] = useState<number>(initialSelectedIndex);
  const [keyboardNav, setKeyboardNav] = useState<boolean>(false);
  const [topGradientOpacity, setTopGradientOpacity] = useState<number>(0);
  const [bottomGradientOpacity, setBottomGradientOpacity] = useState<number>(1);

  const handleScroll = (e: UIEvent<HTMLDivElement>) => {
    const target = e.target as HTMLDivElement;
    const { scrollTop, scrollHeight, clientHeight } = target;

    setTopGradientOpacity(Math.min(scrollTop / 50, 1));
    const bottomDistance = scrollHeight - (scrollTop + clientHeight);
    setBottomGradientOpacity(scrollHeight <= clientHeight ? 0 : Math.min(bottomDistance / 50, 1));
  };

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
          if (onItemSelect) {
            onItemSelect(items[selectedIndex], selectedIndex);
          }
        }
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [items, selectedIndex, onItemSelect, enableArrowNavigation]);

  useEffect(() => {
    if (!keyboardNav || selectedIndex < 0 || !listRef.current) return;

    const container = listRef.current;
    const accordionItems = container.querySelectorAll('[data-index]');
    const selectedItem = accordionItems[selectedIndex] as HTMLElement | null;

    if (selectedItem) {
      const extraMargin = 50;
      const containerScrollTop = container.scrollTop;
      const containerHeight = container.clientHeight;
      
      // Get the accordion item button element
      const accordionButton = selectedItem.closest('button');
      if (!accordionButton) return;
      
      const itemTop = accordionButton.offsetTop;
      const itemBottom = itemTop + accordionButton.offsetHeight;

      if (itemTop < containerScrollTop + extraMargin) {
        container.scrollTo({ top: itemTop - extraMargin, behavior: 'smooth' });
      } else if (itemBottom > containerScrollTop + containerHeight - extraMargin) {
        container.scrollTo({
          top: itemBottom - containerHeight + extraMargin,
          behavior: 'smooth'
        });
      }
    }

    setKeyboardNav(false);
  }, [selectedIndex, keyboardNav]);

  return (
    <div className={`scroll-list-container ${className}`}>
      <div 
        ref={listRef} 
        className={`scroll-list ${!displayScrollbar ? 'no-scrollbar' : ''}`} 
        onScroll={handleScroll}
      >
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
        >
          <Accordion 
            variant="splitted"
            className="animated-list-main-accordion"
          >
            {items.map((item, index) => {
              const isActive = selectedIndex === index;

              return (
                <AccordionItem
                  key={`item-${index}`}
                  aria-label={item.title || item.url}
                  title={
                    <div
                      data-index={index}
                      onMouseEnter={() => setSelectedIndex(index)}
                    >
                      <h3 style={{
                        margin: 0,
                        fontFamily: "'Space Grotesk', 'Inter', sans-serif",
                        fontSize: '1.05rem',
                        letterSpacing: '-0.5px',
                        color: '#ffffff',
                        fontWeight: 600,
                        marginBottom: '4px'
                      }}>
                        {item.title || item.url}
                      </h3>
                      <p style={{
                        margin: 0,
                        fontSize: '0.85rem',
                        color: 'rgba(255, 255, 255, 0.5)',
                        wordBreak: 'break-all'
                      }}>
                        {item.url}
                      </p>
                    </div>
                  }
                  className={`main-accordion-item ${isActive ? 'selected' : ''} ${itemClassName}`}
                >
                  <div style={{ padding: '8px 0' }}>
                    {/* Description */}
                    {item.description && (
                      <div style={{ marginBottom: '16px' }}>
                        <h4 style={{
                          margin: '0 0 8px 0',
                          fontSize: '0.9rem',
                          fontWeight: 600,
                          color: 'rgba(255, 255, 255, 0.8)'
                        }}>
                          Description
                        </h4>
                        <p style={{ 
                          margin: 0, 
                          color: 'rgba(255, 255, 255, 0.7)', 
                          fontSize: '0.9rem',
                          lineHeight: '1.5'
                        }}>
                          {item.description}
                        </p>
                      </div>
                    )}

                    {/* Related Links */}
                    {item.links && item.links.length > 0 && (
                      <div>
                        <h4 style={{
                          margin: '0 0 8px 0',
                          fontSize: '0.9rem',
                          fontWeight: 600,
                          color: 'rgba(255, 255, 255, 0.8)'
                        }}>
                          Related Links ({item.links.length})
                        </h4>
                        <ul style={{ 
                          margin: 0, 
                          paddingLeft: '20px', 
                          color: 'rgba(255, 255, 255, 0.7)' 
                        }}>
                          {item.links.map((link, idx) => (
                            <li key={idx} style={{ marginBottom: '8px' }}>
                              <a
                                href={link}
                                target="_blank"
                                rel="noopener noreferrer"
                                style={{
                                  color: '#60a5fa',
                                  textDecoration: 'none',
                                  fontSize: '0.85rem',
                                  wordBreak: 'break-all',
                                  transition: 'color 0.2s ease'
                                }}
                                className="related-link"
                                onClick={(e) => {
                                  e.stopPropagation();
                                }}
                              >
                                {link}
                              </a>
                            </li>
                          ))}
                        </ul>
                      </div>
                    )}

                    {/* Link to main URL */}
                    <div style={{ marginTop: '16px', paddingTop: '12px', borderTop: '1px solid rgba(255, 255, 255, 0.1)' }}>
                      <a
                        href={item.url}
                        target="_blank"
                        rel="noopener noreferrer"
                        style={{
                          color: '#60a5fa',
                          textDecoration: 'none',
                          fontSize: '0.9rem',
                          fontWeight: 500,
                          transition: 'color 0.2s ease'
                        }}
                        className="related-link"
                        onClick={(e) => {
                          e.stopPropagation();
                          if (onItemSelect) {
                            onItemSelect(item, index);
                          }
                        }}
                      >
                        Visit Website →
                      </a>
                    </div>
                  </div>
                </AccordionItem>
              );
            })}
          </Accordion>
        </motion.div>
      </div>

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
