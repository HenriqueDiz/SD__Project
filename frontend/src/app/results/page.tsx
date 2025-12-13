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

  // Função para buscar análise AI com cache
  const fetchAIAnalysis = async (searchQuery: string, citations: string): Promise<string> => {
    // Verifica cache primeiro
    const cached = analysisCache.get(searchQuery);
    if (cached && Date.now() - cached.timestamp <= CACHE_DURATION) {
      return cached.analysis;
    }

    // Faz request se não houver cache
    const analysis = await getContextAnalysis(searchQuery, citations);
    
    // Armazena no cache
    analysisCache.set(searchQuery, {
      analysis,
      timestamp: Date.now()
    });

    return analysis;
  };

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
    <>
      <style jsx>{`
        .filled::before {
          content: '';
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background: linear-gradient(135deg, #9c43ff, #4cb8e9);
          border-radius: 50%;
          opacity: 1;
          transition: opacity 0.4s ease;
          z-index: -1;
        }

        button:not(.filled)::before {
          content: '';
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background: linear-gradient(135deg, #9c43ff, #4cb8e9);
          border-radius: 50%;
          opacity: 0;
          transition: opacity 0.4s ease;
          z-index: -1;
        }
      `}</style>
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
              className={query.trim() ? 'filled' : ''}
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
                color: query.trim() ? 'white' : 'rgba(255, 255, 255, 0.5)',
                flexShrink: 0,
                position: 'relative',
                overflow: 'hidden',
              }}
              onMouseEnter={(e) => {
                if (!query.trim()) {
                  e.currentTarget.style.color = 'rgba(255, 255, 255, 0.9)';
                }
                e.currentTarget.style.transform = 'scale(1.1)';
              }}
              onMouseLeave={(e) => {
                if (!query.trim()) {
                  e.currentTarget.style.color = 'rgba(255, 255, 255, 0.5)';
                }
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
            showAIDropdown={true}
            query={searchedQuery}
            onAnalysisFetch={fetchAIAnalysis}
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
    </>
  );
}
