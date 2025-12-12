'use client';

import { useState, useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import AnimatedList, { AnimatedListItem } from '@/components/AnimatedList/AnimatedList';
import GradientText from '@/components/GradientText/GradientText';

import Loader from '@/components/Loader/Loader';
import StaggeredMenu from '@/components/StaggeredMenu/StaggeredMenu';
import { getConnections, ApiError } from '@/services/api';

export default function LigacoesResultsPage() {
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

  const menuItems = [
    { label: 'Procurar palavra', ariaLabel: 'Procurar palavra no sistema', link: '/' },
    { label: 'Indexar URL', ariaLabel: 'Adicionar novo URL ao sistema', link: '/indexar' },
    { label: 'Estatísticas', ariaLabel: 'Ver estatísticas do sistema', link: '/statistics' },
    { label: 'Autores', ariaLabel: 'Ver autores do projeto', link: '/autores' }
  ];

  // Fetch connections using the real API
  const fetchConnections = async (url: string, page: number = 0) => {
    setIsLoading(true);
    setError(null);
    setSearchedQuery(url);

    try {
      // Call the real API endpoint with page parameter
      const response = await getConnections(url, page);
      
      // Convert SearchResult[] to AnimatedListItem[]
      const connectionItems: AnimatedListItem[] = response.connections.map(conn => ({
        url: conn.url,
        title: conn.title,
        description: conn.description,
      }));

      setResults(connectionItems);
      setTotalResults(response.totalConnections);
      setCurrentPage(response.currentPage);
      setTotalPages(response.totalPages);
    } catch (err) {
      console.error('Connections fetch error:', err);
      if (err instanceof ApiError) {
        setError(err.message);
      } else {
        setError(err instanceof Error ? err.message : 'Erro ao carregar ligações');
      }
      setResults([]);
      setTotalResults(0);
    } finally {
      setIsLoading(false);
    }
  };

  // Fetch connections when URL query changes
  useEffect(() => {
    const url = searchParams.get('url');
    const p = searchParams.get('page');
    const pageNum = p ? parseInt(p) - 1 : 0; // Convert from 1-based to 0-based
    
    if (url) {
      setQuery(url);
      fetchConnections(url, pageNum);
    }
  }, [searchParams]);

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
    router.push(`/ligacoes/results?url=${encodeURIComponent(newQuery)}&page=1`);
  };

  const handlePageChange = (newPage: number) => {
    if (newPage >= 0 && newPage < totalPages) {
      // Update URL with new page parameter (1-based for user)
      router.push(`/ligacoes/results?url=${encodeURIComponent(query)}&page=${newPage + 1}`);
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
        borderBottom: '1px solid rgba(107, 255, 157, 0.2)',
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
            colors={['#00ff88', '#88ff00', '#00ff88', '#88ff00', '#00ff88']}
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
          maxWidth: '600px',
          width: '100%',
        }}>
          <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: '10px',
            width: '100%',
            padding: '10px 16px',
            background: 'rgba(107, 255, 157, 0.05)',
            backdropFilter: 'blur(10px)',
            border: '1px solid rgba(107, 255, 157, 0.2)',
            borderRadius: '50px',
            transition: 'all 0.3s ease',
          }}>
            <input
              type="text"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter' && query.trim()) {
                  e.preventDefault();
                  handleSearch(query);
                }
              }}
              placeholder="Digite uma URL para ver suas ligações..."
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
                background: query.trim() ? 'linear-gradient(135deg, #6bff9d, #c5ff42)' : 'transparent',
                border: 'none',
                borderRadius: '50%',
                width: '32px',
                height: '32px',
                cursor: 'pointer',
                transition: 'all 0.4s ease',
                color: query.trim() ? 'white' : 'rgba(255, 255, 255, 0.7)',
                flexShrink: 0,
                position: 'relative',
                overflow: 'hidden',
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
                <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"></path>
                <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"></path>
              </svg>
            </button>
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
        {(isLoading || error || (searchedQuery && results.length > 0)) && (
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
              <span style={{ color: '#6bff9d' }}>Erro: {error}</span>
            ) : (searchedQuery && results.length > 0) ? (
              <>
                Encontradas <strong>{totalResults}</strong> ligações para{' '}
                <em style={{ fontStyle: 'italic', color: '#6bff9d' }}>&quot;{searchedQuery}&quot;</em>
              </>
            ) : null}
          </h2>
        )}
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
          <Loader primaryColor="#6bff9d" secondaryColor="#00ff88" accentColor="#c5ff42" textColor="#c5ff42" />
        ) : error ? (
          <div style={{
            textAlign: 'center',
            padding: '4rem 2rem',
            color: 'rgba(255, 255, 255, 0.8)',
            fontSize: '1rem'
          }}>
            <div style={{
              background: 'rgba(107, 255, 157, 0.1)',
              border: '1px solid rgba(107, 255, 157, 0.3)',
              borderRadius: '12px',
              padding: '2rem',
              maxWidth: '500px',
              margin: '0 auto'
            }}>
              <p style={{ margin: '0 0 1rem 0', fontSize: '1.2rem', color: '#6bff9d' }}>
                Erro ao carregar ligações
              </p>
              <p style={{ margin: '0', fontSize: '0.9rem', color: 'rgba(255, 255, 255, 0.7)' }}>
                {error}
              </p>
              <button
                onClick={() => query && fetchConnections(query)}
                style={{
                  marginTop: '1.5rem',
                  padding: '0.75rem 1.5rem',
                  background: 'linear-gradient(135deg, #6bff9d, #c5ff42)',
                  border: 'none',
                  borderRadius: '8px',
                  color: 'white',
                  fontSize: '0.9rem',
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
            minHeight: '300px',
            textAlign: 'center',
            padding: '4rem 2rem',
            color: 'rgba(255, 255, 255, 0.6)',
            fontSize: '1rem'
          }}>
            <p style={{ margin: '0', fontSize: '1.2rem' }}>
              Nenhuma ligação encontrada para <em style={{ fontStyle: 'italic', color: '#6bff9d' }}>&quot;{searchedQuery}&quot;</em>
            </p>
            <p style={{ margin: '1rem 0 0 0', fontSize: '0.9rem', opacity: 0.7 }}>
              Não foram encontradas páginas que referenciam este URL
            </p>
          </div>
        ) : (
          <AnimatedList
            items={results}
            onItemSelect={handleItemSelect}
            showGradients={true}
            enableArrowNavigation={true}
            compactMode={true}
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
                  : 'rgba(107, 255, 157, 0.1)',
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
                  e.currentTarget.style.background = 'rgba(107, 255, 157, 0.2)';
                  e.currentTarget.style.borderColor = 'rgba(107, 255, 157, 0.5)';
                }
              }}
              onMouseLeave={(e) => {
                if (currentPage !== 0) {
                  e.currentTarget.style.background = 'rgba(107, 255, 157, 0.1)';
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
                // Show first 3, current page +/- 2, and last 3
                const showPage = 
                  pageNum < 3 || // First 3 pages
                  pageNum >= totalPages - 3 || // Last 3 pages
                  Math.abs(pageNum - currentPage) <= 2; // Current +/- 2

                // Show ellipsis before or after the visible range
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
                        ? 'linear-gradient(135deg, #6bff9d, #c5ff42)'
                        : 'rgba(255, 255, 255, 0.05)',
                      border: currentPage === pageNum 
                        ? '1px solid rgba(107, 255, 157, 0.5)'
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
                        e.currentTarget.style.background = 'rgba(107, 255, 157, 0.15)';
                        e.currentTarget.style.borderColor = 'rgba(107, 255, 157, 0.3)';
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
                  : 'rgba(107, 255, 157, 0.1)',
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
                  e.currentTarget.style.background = 'rgba(107, 255, 157, 0.2)';
                  e.currentTarget.style.borderColor = 'rgba(107, 255, 157, 0.5)';
                }
              }}
              onMouseLeave={(e) => {
                if (currentPage < totalPages - 1) {
                  e.currentTarget.style.background = 'rgba(107, 255, 157, 0.1)';
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
        colors={['#6bff9d', '#c5ff42', 'rgba(15, 15, 20, 0.95)']}
        accentColor="#6bff9d"
        menuButtonColor="rgba(255, 255, 255, 0.9)"
        openMenuButtonColor="rgba(255, 255, 255, 0.95)"
        borderColor="rgba(0, 255, 136, 0.2)"
        scrollbarColor="rgba(0, 255, 136, 0.3)"
        scrollbarHoverColor="rgba(0, 255, 136, 0.5)"
        isFixed={true}
      />
    </main>
  );
}
