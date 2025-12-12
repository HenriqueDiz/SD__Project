'use client';

import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import './AIDropdown.css';

interface AIDropdownProps {
  query: string;
  results: any[];
  onAnalysisFetch: (query: string, citations: string) => Promise<string>;
  onExpandChange?: (isExpanded: boolean) => void;
}

const AIDropdown: React.FC<AIDropdownProps> = ({ query, results, onAnalysisFetch, onExpandChange }) => {
  const [isExpanded, setIsExpanded] = useState(false);
  const [loading, setLoading] = useState(false);
  const [analysis, setAnalysis] = useState('');
  const [error, setError] = useState('');

  // Build citations string from results
  const citations = results && results.length > 0
    ? results.slice(0, 5).map(r => r.description || r.title || '').join(' | ')
    : '';

  const fetchAnalysis = async () => {
    if (!query || analysis) return;
    
    setLoading(true);
    setError('');
    try {
      const res = await onAnalysisFetch(query, citations);
      setAnalysis(res);
    } catch (e: any) {
      setError(e.message || 'Erro ao obter análise');
    } finally {
      setLoading(false);
    }
  };

  const handleToggle = () => {
    if (!isExpanded && !analysis && !loading) {
      fetchAnalysis();
    }
    const newExpandedState = !isExpanded;
    setIsExpanded(newExpandedState);
    
    // Notify parent component about expansion change
    if (onExpandChange) {
      onExpandChange(newExpandedState);
    }
  };

  // Reset when query changes
  useEffect(() => {
    setAnalysis('');
    setError('');
    setIsExpanded(false);
  }, [query]);

  // Format analysis text into paragraphs
  const formatAnalysis = (text: string) => {
    if (!text) return null;
    
    // Split by double line breaks or single line breaks followed by empty line
    const paragraphs = text.split(/\n\n+|\n(?=\s*\n)/);
    
    return paragraphs
      .filter(p => p.trim())
      .map((paragraph, index) => (
        <p key={index}>{paragraph.trim()}</p>
      ));
  };

  return (
    <div className={`ai-dropdown-container ${isExpanded ? 'expanded' : ''}`}>
      {/* Barra do topo - sempre visível */}
      <motion.button
        className="ai-dropdown-bar"
        onClick={handleToggle}
        whileTap={{ scale: 0.98 }}
        style={{ borderRadius: '14px 14px 0 0' }}
      >
        <div className="ai-dropdown-bar-content">
          <motion.div 
            className="ai-dropdown-chevron-group"
            animate={{ 
              rotate: isExpanded ? 180 : 0,
            }}
            transition={{ 
              rotate: { duration: 0.3 },
            }}
            style={{
              animation: isExpanded ? 'none' : undefined
            }}
          >
            <svg width="16" height="20" viewBox="0 0 24 30" fill="none" stroke="currentColor" strokeWidth="2.5">
              <polyline points="6 6 12 12 18 6"></polyline>
              <polyline points="6 13 12 19 18 13"></polyline>
              <polyline points="6 20 12 26 18 20"></polyline>
            </svg>
          </motion.div>
          <span className="ai-dropdown-bar-text">Análise AI</span>
          <motion.div 
            className="ai-dropdown-chevron-group"
            animate={{ 
              rotate: isExpanded ? 180 : 0,
            }}
            transition={{ 
              rotate: { duration: 0.3 },
            }}
            style={{
              animation: isExpanded ? 'none' : undefined
            }}
          >
            <svg width="16" height="20" viewBox="0 0 24 30" fill="none" stroke="currentColor" strokeWidth="2.5">
              <polyline points="6 6 12 12 18 6"></polyline>
              <polyline points="6 13 12 19 18 13"></polyline>
              <polyline points="6 20 12 26 18 20"></polyline>
            </svg>
          </motion.div>
        </div>
      </motion.button>

      {/* Conteúdo expansível */}
      <AnimatePresence>
        {isExpanded && (
          <motion.div
            className="ai-dropdown-content"
            initial={{ height: 0, opacity: 0 }}
            animate={{ height: 'auto', opacity: 1 }}
            exit={{ height: 0, opacity: 0 }}
            transition={{ duration: 0.3, ease: 'easeInOut' }}
          >
            <div className="ai-dropdown-inner">
              {loading ? (
                <div className="ai-dropdown-loading">
                  <div className="ai-spinner" />
                  <span>A gerar análise inteligente...</span>
                </div>
              ) : error ? (
                <div className="ai-dropdown-error">
                  <div className="ai-error-icon">⚠️</div>
                  <div className="ai-error-text">
                    <div className="ai-error-title">Erro</div>
                    <div>{error}</div>
                  </div>
                </div>
              ) : analysis ? (
                <div className="ai-dropdown-analysis">
                  {formatAnalysis(analysis)}
                </div>
              ) : (
                <div className="ai-dropdown-empty">
                  Nenhuma análise disponível no momento.
                </div>
              )}
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};

export default AIDropdown;
