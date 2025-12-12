'use client';

import { useState, useEffect, useRef } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import AnimatedList, { AnimatedListItem } from '@/components/AnimatedList/AnimatedList';
import GradientText from '@/components/GradientText/GradientText';

import Loader from '@/components/Loader/Loader';
import StaggeredMenu from '@/components/StaggeredMenu/StaggeredMenu';
import { searchQuery, SearchResult } from '@/services/api';

import { getContextAnalysis } from '@/services/api';

// Cache global para armazenar análises por query
const analysisCache = new Map<string, { analysis: string; timestamp: number }>();
const CACHE_DURATION = 30 * 60 * 1000; // 30 minutos em milissegundos

function ContextAnalysisButton({ query, results }: { query: string, results: any[] }) {
  const [show, setShow] = useState(false);
  const [loading, setLoading] = useState(false);
  const [analysis, setAnalysis] = useState('');
  const [error, setError] = useState('');
  const prevQueryRef = useRef<string>('');

  // Quando a query muda, verifica se existe no cache
  useEffect(() => {
    if (query !== prevQueryRef.current) {
      prevQueryRef.current = query;
      setError('');
      
      // Verifica se existe uma análise em cache para esta query
      const cached = analysisCache.get(query);
      if (cached) {
        const isExpired = Date.now() - cached.timestamp > CACHE_DURATION;
        if (!isExpired) {
          // Usa a análise do cache
          setAnalysis(cached.analysis);
          return;
        } else {
          // Remove do cache se expirou
          analysisCache.delete(query);
        }
      }
      
      // Se não há cache válido, limpa a análise
      setAnalysis('');
    }
  }, [query]);

  // Build citations string from results
  const citations = results && results.length > 0
    ? results.slice(0, 5).map(r => r.description || r.title || '').join(' | ')
    : '';

  const fetchAnalysis = async () => {
    if (!query) return;
    
    // Verifica novamente o cache antes de fazer request
    const cached = analysisCache.get(query);
    if (cached && Date.now() - cached.timestamp <= CACHE_DURATION) {
      setAnalysis(cached.analysis);
      return;
    }
    
    setLoading(true);
    setError('');
    try {
      const res = await getContextAnalysis(query, citations);
      setAnalysis(res);
      
      // Armazena no cache
      analysisCache.set(query, {
        analysis: res,
        timestamp: Date.now()
      });
    } catch (e: any) {
      setError(e.message || 'Erro ao obter análise');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ position: 'relative', display: 'inline-block' }}>
      <button
        aria-label="Análise AI contextualizada"
        className="ai-button"
        style={{
          background: 'linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%)',
          backgroundSize: '200% 200%',
          border: 'none',
          borderRadius: '20px',
          padding: '8px 16px',
          color: 'white',
          cursor: 'pointer',
          marginLeft: 8,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          gap: '6px',
          fontWeight: 600,
          fontSize: '13px',
          letterSpacing: '0.5px',
          position: 'relative',
          overflow: 'hidden',
          boxShadow: '0 4px 15px rgba(102, 126, 234, 0.4)',
          transition: 'all 0.3s ease',
          animation: 'gradientShift 3s ease infinite, pulse 2s ease-in-out infinite',
        }}
        onMouseEnter={(e) => { 
          setShow(true); 
          if (!analysis && !loading && query) fetchAnalysis();
          e.currentTarget.style.transform = 'translateY(-2px) scale(1.05)';
          e.currentTarget.style.boxShadow = '0 6px 25px rgba(102, 126, 234, 0.6)';
        }}
        onMouseLeave={(e) => {
          setShow(false);
          e.currentTarget.style.transform = 'translateY(0) scale(1)';
          e.currentTarget.style.boxShadow = '0 4px 15px rgba(102, 126, 234, 0.4)';
        }}
      >
        <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor" stroke="none">
          <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"/>
          <path d="M12 8L13.5 11.5L17 13L13.5 14.5L12 18L10.5 14.5L7 13L10.5 11.5L12 8Z" fill="rgba(255, 255, 255, 0.5)"/>
        </svg>
        <span>AI</span>
      </button>
      {show && (
        <div 
          className="liquid-glass-popup"
          style={{
            position: 'absolute',
            top: 48,
            right: 0,
            minWidth: 320,
            maxWidth: 450,
            background: 'rgba(0, 0, 0, 0.95)',
            backdropFilter: 'blur(30px) saturate(180%)',
            WebkitBackdropFilter: 'blur(30px) saturate(180%)',
            color: 'white',
            border: '1px solid rgba(102, 126, 234, 0.4)',
            borderRadius: 20,
            padding: '1.5rem',
            zIndex: 100,
            boxShadow: '0 8px 32px rgba(0, 0, 0, 0.6), 0 0 0 1px rgba(255, 255, 255, 0.05) inset, 0 2px 20px rgba(102, 126, 234, 0.3)',
            fontSize: 14,
            lineHeight: 1.6,
            textAlign: 'left',
            animation: 'slideIn 0.3s ease-out, glassShimmer 4s ease-in-out infinite',
            overflow: 'hidden',
          }}
        >
          {/* Header with icon */}
          <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: '10px',
            marginBottom: '12px',
            paddingBottom: '12px',
            borderBottom: '1px solid rgba(255, 255, 255, 0.15)',
            position: 'relative',
            zIndex: 2,
          }}>
            <div style={{
              width: 32,
              height: 32,
              borderRadius: '50%',
              background: 'linear-gradient(135deg, #667eea, #764ba2)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              boxShadow: '0 4px 12px rgba(102, 126, 234, 0.4)',
            }}>
              <svg width="18" height="18" viewBox="0 0 24 24" fill="white" stroke="none">
                <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"/>
                <path d="M12 8L13.5 11.5L17 13L13.5 14.5L12 18L10.5 14.5L7 13L10.5 11.5L12 8Z" fill="rgba(255, 255, 255, 0.4)"/>
              </svg>
            </div>
            <div>
              <div style={{ 
                fontSize: 15, 
                fontWeight: 700,
                background: 'linear-gradient(135deg, #667eea, #f093fb)',
                WebkitBackgroundClip: 'text',
                WebkitTextFillColor: 'transparent',
                backgroundClip: 'text',
              }}>
                Análise AI
              </div>
              <div style={{ 
                fontSize: 11, 
                color: 'rgba(255, 255, 255, 0.6)',
                marginTop: 2,
              }}>
                Contextual Intelligence
              </div>
            </div>
          </div>

          {/* Content */}
          <div style={{
            color: 'rgba(255, 255, 255, 0.95)',
            fontSize: 14,
            lineHeight: 1.7,
            textAlign: 'justify',
            position: 'relative',
            zIndex: 2,
          }}>
            {loading ? (
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px', justifyContent: 'center', padding: '1rem 0' }}>
                <div style={{
                  width: 20,
                  height: 20,
                  border: '3px solid rgba(255, 255, 255, 0.2)',
                  borderTop: '3px solid #667eea',
                  borderRadius: '50%',
                  animation: 'spin 1s linear infinite',
                }} />
                <span style={{ color: 'rgba(255, 255, 255, 0.7)' }}>A gerar análise inteligente...</span>
              </div>
            ) : error ? (
              <div style={{ 
                color: '#ff6b9d',
                background: 'rgba(255, 107, 157, 0.1)',
                padding: '12px',
                borderRadius: 12,
                border: '1px solid rgba(255, 107, 157, 0.3)',
              }}>
                <div style={{ fontWeight: 600, marginBottom: 4 }}>⚠️ Erro</div>
                {error}
              </div>
            ) : analysis ? (
              analysis
            ) : (
              <span style={{ color: 'rgba(255, 255, 255, 0.5)', fontStyle: 'italic' }}>
                Nenhuma análise disponível no momento.
              </span>
            )}
          </div>
        </div>
      )}

      <style jsx>{`
        @keyframes gradientShift {
          0%, 100% {
            background-position: 0% 50%;
          }
          50% {
            background-position: 100% 50%;
          }
        }

        @keyframes pulse {
          0%, 100% {
            opacity: 1;
          }
          50% {
            opacity: 0.85;
          }
        }

        @keyframes slideIn {
          from {
            opacity: 0;
            transform: translateY(-10px);
          }
          to {
            opacity: 1;
            transform: translateY(0);
          }
        }

        @keyframes glassShimmer {
          0%, 100% {
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.6), 0 0 0 1px rgba(255, 255, 255, 0.05) inset, 0 2px 20px rgba(102, 126, 234, 0.3);
          }
          50% {
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.6), 0 0 0 1px rgba(255, 255, 255, 0.1) inset, 0 2px 20px rgba(102, 126, 234, 0.6);
          }
        }

        @keyframes spin {
          to {
            transform: rotate(360deg);
          }
        }
      `}</style>
    </div>
  );
}

export default function DemoPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [query, setQuery] = useState(''); // What's typed in the search bar
  const [searchedQuery, setSearchedQuery] = useState(''); // What was actually searched
  const [results, setResults] = useState<AnimatedListItem[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [totalResults, setTotalResults] = useState(0);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  
  // Ref para rastrear a última pesquisa e evitar duplicatas
  const lastSearchRef = useRef<string>('');

  const menuItems = [
    { label: 'Procurar palavra', ariaLabel: 'Procurar palavra no sistema', link: '/' },
    { label: 'Indexar URL', ariaLabel: 'Adicionar novo URL ao sistema', link: '/indexar' },
    { label: 'Ligações de URLs', ariaLabel: 'Ver ligações de URLs', link: '/ligacoes' },
    { label: 'Estatísticas', ariaLabel: 'Ver estatísticas do sistema', link: '/statistics' },
    { label: 'Autores', ariaLabel: 'Ver autores do projeto', link: '/autores' }
  ];

  // Fetch results when query changes
  useEffect(() => {
    const q = searchParams.get('q');
    const p = searchParams.get('page');
    const pageNum = p ? parseInt(p) - 1 : 0; // Convert from 1-based to 0-based
    
    if (q) {
      // Criar identificador único para esta pesquisa
      const searchKey = `${q}-${pageNum}`;
      
      // Se for a mesma pesquisa que acabámos de fazer, ignorar (previne duplicatas do Strict Mode)
      if (lastSearchRef.current === searchKey) {
        return;
      }
      
      lastSearchRef.current = searchKey;
      setQuery(q);
      performSearch(q, pageNum);
    }
  }, [searchParams]);

  const performSearch = async (searchTerm: string, page: number = 0) => {
    if (!searchTerm.trim()) return;

    setIsLoading(true);
    setError(null);
    setSearchedQuery(searchTerm); // Update the searched query

    try {
      const response = await searchQuery(searchTerm.trim(), page);
      
      // Convert API results to AnimatedList format
      const formattedResults: AnimatedListItem[] = response.results.map((result: SearchResult) => ({
        url: result.url,
        title: result.title,
        description: result.description,
        references: result.references,
        hackerNews: result.hackerNews === true,
        // You can add links here if your backend provides them
        // links: result.links || []
      }));

      setResults(formattedResults);
      setTotalResults(response.totalResults);
      setCurrentPage(response.currentPage);
      setTotalPages(response.totalPages);
    } catch (err) {
      console.error('Search error:', err);
      setError(err instanceof Error ? err.message : 'Erro ao realizar pesquisa');
      setResults([]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleItemSelect = (item: AnimatedListItem, index: number) => {
    console.log('Selected item:', item, 'at index:', index);
    // Open URL in new tab
    if (item.url) {
      window.open(item.url, '_blank');
    }
  };

  const handleSearch = (newQuery: string) => {
    setQuery(newQuery);
    setCurrentPage(0); // Reset to first page on new search
    // Update URL and trigger new search (page=1 for user-facing URL)
    router.push(`/results?q=${encodeURIComponent(newQuery)}&page=1`);
  };

  const handlePageChange = (newPage: number) => {
    if (newPage >= 0 && newPage < totalPages) {
      // Update URL with new page parameter (1-based for user)
      router.push(`/results?q=${encodeURIComponent(query)}&page=${newPage + 1}`);
      // Scroll to top
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  };

  const handleClearSearch = () => {
    setQuery('');
  };

  const handleLogoClick = () => {
    router.push('/');
  };

  return (
    <main style={{ 
      position: 'relative', 
      width: '100%', 
      height: '100vh',
      overflow: 'hidden',
      background: '#0a0a0a',
      display: 'flex',
      flexDirection: 'column'
    }}>

      {/* Top Bar with Logo and Search */}
      <div style={{
        position: 'relative',
        zIndex: 10,
        background: 'rgba(10, 10, 10, 0.8)',
        backdropFilter: 'blur(20px)',
        borderBottom: '1px solid rgba(255, 255, 255, 0.1)',
        padding: '1rem 2rem',
        display: 'flex',
        alignItems: 'center',
        gap: '2rem',
        flexShrink: 0
      }}>
        {/* Googol Logo - Clickable */}
        <div 
          onClick={handleLogoClick}
          style={{
            transition: 'transform 0.2s ease',
          }}
          onMouseEnter={(e) => e.currentTarget.style.transform = 'scale(1.05)'}
          onMouseLeave={(e) => e.currentTarget.style.transform = 'scale(1)'}
        >
          <GradientText
            colors={['#9c43ff', '#4cb8e9', '#9c43ff', '#4cb8e9', '#9c43ff']}
            animationSpeed={3}
            showBorder={false}
          >
            <h1 style={{ 
              fontSize: '1.8rem', 
              margin: 0, 
              fontWeight: 700, 
              letterSpacing: '-0.02em',
              fontFamily: "'Space Grotesk', 'Inter', sans-serif"
            }}>Googol</h1>
          </GradientText>
        </div>

        {/* Search Bar Container */}
        <div style={{ 
          position: 'relative',
          maxWidth: '400px',
          width: '100%',
        }}>
          <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: '10px',
            width: '100%',
            padding: '10px 16px',
            background: 'rgba(255, 255, 255, 0.03)',
            backdropFilter: 'blur(10px)',
            border: '1px solid rgba(255, 255, 255, 0.1)',
            borderRadius: '50px',
            transition: 'all 0.3s ease',
          }}>
             <input
               type="text"
               value={query}
               onChange={(e) => setQuery(e.target.value)}
               onKeyDown={(e) => {
                 if (e.key === 'Enter' && query.trim()) {
                   e.preventDefault(); // Prevent form submission/redirect
                   handleSearch(query);
                 }
               }}
               placeholder="Search the web..."
               style={{
                 flex: 1,
                 background: 'transparent',
                 border: 'none',
                 outline: 'none',
                 color: 'rgba(255, 255, 255, 0.95)',
                 fontSize: '14px',
                 fontWeight: 400,
                 fontFamily: 'inherit',
                 padding: 0,
               }}
             />
            {query && (
              <button
                onClick={handleClearSearch}
                style={{
                  background: 'transparent',
                  border: 'none',
                  color: 'rgba(255, 255, 255, 0.5)',
                  cursor: 'pointer',
                  fontSize: '1rem',
                  padding: '4px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  transition: 'color 0.2s ease',
                  lineHeight: 1,
                }}
                onMouseEnter={(e) => e.currentTarget.style.color = 'rgba(255, 255, 255, 0.9)'}
                onMouseLeave={(e) => e.currentTarget.style.color = 'rgba(255, 255, 255, 0.5)'}
              >
                ✕
              </button>
            )}
            <button
              onClick={() => query.trim() && handleSearch(query)}
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                background: 'transparent',
                border: 'none',
                borderRadius: '50%',
                width: '36px',
                height: '36px',
                cursor: query.trim() ? 'pointer' : 'default',
                transition: 'all 0.3s ease',
                color: 'rgba(255, 255, 255, 0.5)',
                flexShrink: 0,
                position: 'relative',
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.color = 'rgba(255, 255, 255, 0.9)';
                e.currentTarget.style.transform = 'scale(1.1)';
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.color = 'rgba(255, 255, 255, 0.5)';
                e.currentTarget.style.transform = 'scale(1)';
              }}
            >
              <svg 
                width="18" 
                height="18" 
                viewBox="0 0 24 24" 
                fill="none" 
                stroke="currentColor" 
                strokeWidth="2" 
                strokeLinecap="round" 
                strokeLinejoin="round"
              >
                <circle cx="10" cy="10" r="7"></circle>
                <line x1="21" y1="21" x2="15" y2="15"></line>
              </svg>
            </button>
            {/* Contextual Analysis Button */}
            <ContextAnalysisButton query={searchedQuery} results={results} />
           </div>
          </div>
         </div>

      {/* Results Title */}
      <div style={{
        position: 'relative',
        zIndex: 1,
        paddingTop: '1.5rem',
        paddingBottom: '1rem',
        width: '95%',
        maxWidth: '1200px',
        margin: '0 auto',
        textAlign: 'center',
        flexShrink: 0
      }}>
        <h2 style={{
          color: 'rgba(255, 255, 255, 0.9)',
          fontSize: '1.2rem',
          fontWeight: 500,
          fontFamily: "'Space Grotesk', 'Inter', sans-serif",
          margin: 0
        }}>
           {isLoading ? (
             <></>
           ) : error ? (
             <span style={{ color: '#ff4444' }}>Erro: {error}</span>
           ) : searchedQuery && results.length > 0 ? (
             <>
               Encontrados <strong>{totalResults}</strong> resultados para{' '}
               <em style={{ fontStyle: 'italic', color: '#9c43ff' }}>"{searchedQuery}"</em>
             </>
            ) : null}
        </h2>
      </div>

      {/* AnimatedList Container */}
      <div style={{ 
        position: 'relative',
        zIndex: 1,
        width: '95%',
        maxWidth: '1200px',
        margin: '0 auto',
        flex: 1,
        display: 'flex',
        flexDirection: 'column',
        overflow: 'hidden',
        minHeight: 0,
        maxHeight: '100%',
        boxSizing: 'border-box'
      }}>
        {/* Scrollable Results Area */}
        <div style={{
          flex: 1,
          overflow: 'hidden',
          position: 'relative',
          boxSizing: 'border-box'
        }}>
        {isLoading ? (
          <Loader primaryColor="#9c43ff" secondaryColor="#4cb8e9" accentColor="#9c43ff" textColor="#4cb8e9" />
        ) : error ? (
          <div style={{
            textAlign: 'center',
            padding: '4rem 2rem',
            color: 'rgba(255, 255, 255, 0.8)',
            fontSize: '1rem'
          }}>
            <div style={{
              background: 'rgba(255, 68, 68, 0.1)',
              border: '1px solid rgba(255, 68, 68, 0.3)',
              borderRadius: '12px',
              padding: '2rem',
              maxWidth: '500px',
              margin: '0 auto'
            }}>
              <p style={{ margin: '0 0 1rem 0', fontSize: '1.2rem', color: '#ff4444' }}>
                ⚠️ Erro ao carregar resultados
              </p>
              <p style={{ margin: '0', fontSize: '0.9rem', color: 'rgba(255, 255, 255, 0.7)' }}>
                {error}
              </p>
              <button
                onClick={() => query && performSearch(query, currentPage)}
                style={{
                  marginTop: '1.5rem',
                  padding: '0.75rem 1.5rem',
                  background: 'linear-gradient(135deg, #9c43ff, #4cb8e9)',
                  border: 'none',
                  borderRadius: '8px',
                  color: 'white',
                  fontSize: '0.9rem',
                  cursor: 'pointer',
                  fontWeight: 500,
                  transition: 'transform 0.2s ease'
                }}
                onMouseEnter={(e) => e.currentTarget.style.transform = 'scale(1.05)'}
                onMouseLeave={(e) => e.currentTarget.style.transform = 'scale(1)'}
              >
                Tentar novamente
              </button>
            </div>
          </div>
        ) : results.length === 0 ? (
          <div style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            minHeight: '60vh',
            color: 'rgba(255, 255, 255, 0.6)',
            fontSize: '1rem',
            textAlign: 'center',
          }}>
            <p style={{ margin: '0', fontSize: '1.2rem' }}>
              Nenhum resultado encontrado para <em style={{ fontStyle: 'italic', color: '#9c43ff' }}>&quot;{searchedQuery}&quot;</em>
            </p>
            <p style={{ margin: '1rem 0 0 0', fontSize: '0.9rem', opacity: 0.7 }}>
              Tenta usar palavras diferentes ou verifica a ortografia
            </p>
          </div>
        ) : (
          <AnimatedList
            items={results}
            onItemSelect={handleItemSelect}
            showGradients={true}
            enableArrowNavigation={true}
          />
        )}
        </div>

        {/* Pagination Component */}
        {!isLoading && !error && results.length > 0 && totalPages > 1 && (
          <div style={{
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            gap: '0.5rem',
            padding: '1rem 0',
            flexShrink: 0
          }}>
            {/* Previous Button */}
            <button
              onClick={() => handlePageChange(currentPage - 1)}
              disabled={currentPage === 0}
              style={{
                padding: '0.6rem 1rem',
                background: currentPage === 0 
                  ? 'rgba(255, 255, 255, 0.05)' 
                  : 'rgba(156, 67, 255, 0.1)',
                border: '1px solid rgba(255, 255, 255, 0.1)',
                borderRadius: '8px',
                color: currentPage === 0 
                  ? 'rgba(255, 255, 255, 0.3)' 
                  : 'rgba(255, 255, 255, 0.9)',
                cursor: currentPage === 0 ? 'not-allowed' : 'pointer',
                fontSize: '0.9rem',
                fontWeight: 500,
                transition: 'all 0.2s ease',
              }}
              onMouseEnter={(e) => {
                if (currentPage !== 0) {
                  e.currentTarget.style.background = 'rgba(156, 67, 255, 0.2)';
                  e.currentTarget.style.borderColor = 'rgba(156, 67, 255, 0.5)';
                }
              }}
              onMouseLeave={(e) => {
                if (currentPage !== 0) {
                  e.currentTarget.style.background = 'rgba(156, 67, 255, 0.1)';
                  e.currentTarget.style.borderColor = 'rgba(255, 255, 255, 0.1)';
                }
              }}
            >
              ← Anterior
            </button>

            {/* Page Numbers */}
            <div style={{
              display: 'flex',
              gap: '0.3rem',
              alignItems: 'center',
            }}>
              {Array.from({ length: totalPages }, (_, i) => {
                const pageNum = i;
                const showPage =
                  pageNum < 3 ||
                  pageNum >= totalPages - 3 ||
                  Math.abs(pageNum - currentPage) <= 2;

                if (!showPage && (pageNum === 3 || pageNum === totalPages - 4)) {
                  return (
                    <span
                      key={`ellipsis-${pageNum}`}
                      style={{
                        color: 'rgba(255, 255, 255, 0.5)',
                        padding: '0 0.3rem',
                      }}
                    >
                      ...
                    </span>
                  );
                } else if (!showPage) {
                  return null;
                }

                return (
                  <button
                    key={pageNum}
                    onClick={() => handlePageChange(pageNum)}
                    style={{
                      minWidth: '40px',
                      height: '40px',
                      padding: '0.5rem',
                      background: currentPage === pageNum
                        ? 'linear-gradient(135deg, #9c43ff, #4cb8e9)'
                        : 'rgba(255, 255, 255, 0.05)',
                      border: currentPage === pageNum
                        ? '1px solid rgba(156, 67, 255, 0.5)'
                        : '1px solid rgba(255, 255, 255, 0.1)',
                      borderRadius: '8px',
                      color: 'rgba(255, 255, 255, 0.9)',
                      cursor: 'pointer',
                      fontSize: '0.9rem',
                      fontWeight: currentPage === pageNum ? 600 : 400,
                      transition: 'all 0.2s ease',
                    }}
                    onMouseEnter={(e) => {
                      if (currentPage !== pageNum) {
                        e.currentTarget.style.background = 'rgba(156, 67, 255, 0.15)';
                        e.currentTarget.style.borderColor = 'rgba(156, 67, 255, 0.3)';
                      }
                    }}
                    onMouseLeave={(e) => {
                      if (currentPage !== pageNum) {
                        e.currentTarget.style.background = 'rgba(255, 255, 255, 0.05)';
                        e.currentTarget.style.borderColor = 'rgba(255, 255, 255, 0.1)';
                      }
                    }}
                  >
                    {pageNum + 1}
                  </button>
                );
              })}
            </div>

            {/* Next Button */}
            <button
              onClick={() => handlePageChange(currentPage + 1)}
              disabled={currentPage >= totalPages - 1}
              style={{
                padding: '0.6rem 1rem',
                background: currentPage >= totalPages - 1
                  ? 'rgba(255, 255, 255, 0.05)' 
                  : 'rgba(156, 67, 255, 0.1)',
                border: '1px solid rgba(255, 255, 255, 0.1)',
                borderRadius: '8px',
                color: currentPage >= totalPages - 1
                  ? 'rgba(255, 255, 255, 0.3)' 
                  : 'rgba(255, 255, 255, 0.9)',
                cursor: currentPage >= totalPages - 1 ? 'not-allowed' : 'pointer',
                fontSize: '0.9rem',
                fontWeight: 500,
                transition: 'all 0.2s ease',
              }}
              onMouseEnter={(e) => {
                if (currentPage < totalPages - 1) {
                  e.currentTarget.style.background = 'rgba(156, 67, 255, 0.2)';
                  e.currentTarget.style.borderColor = 'rgba(156, 67, 255, 0.5)';
                }
              }}
              onMouseLeave={(e) => {
                if (currentPage < totalPages - 1) {
                  e.currentTarget.style.background = 'rgba(156, 67, 255, 0.1)';
                  e.currentTarget.style.borderColor = 'rgba(255, 255, 255, 0.1)';
                }
              }}
            >
              Próximo →
            </button>
          </div>
        )}
      </div>

      

      {/* Staggered Menu */}
      <StaggeredMenu
        position="right"
        items={menuItems}
        displayItemNumbering={false}
        displaySocials={false}
        colors={['#9c43ff', '#4cb8e9', 'rgba(15, 15, 20, 0.95)']}
        accentColor="#9c43ff"
        menuButtonColor="rgba(255, 255, 255, 0.9)"
        openMenuButtonColor="rgba(255, 255, 255, 0.95)"
        borderColor="rgba(156, 67, 255, 0.2)"
        scrollbarColor="rgba(156, 67, 255, 0.3)"
        scrollbarHoverColor="rgba(156, 67, 255, 0.5)"
        isFixed={true}
      />
    </main>
  );
}
