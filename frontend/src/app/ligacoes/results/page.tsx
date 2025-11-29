'use client';

import { useState, useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import AnimatedList, { AnimatedListItem } from '@/components/AnimatedList/AnimatedList';
import GradientText from '@/components/GradientText/GradientText';
import ApiStatus from '@/components/ApiStatus/ApiStatus';
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

  const menuItems = [
    { label: 'Procurar palavra', ariaLabel: 'Procurar palavra no sistema', link: '/' },
    { label: 'Indexar URL', ariaLabel: 'Adicionar novo URL ao sistema', link: '/indexar-url' },
    { label: 'Estatísticas', ariaLabel: 'Ver estatísticas do sistema', link: '#' },
  ];

  // Fetch connections using the real API
  const fetchConnections = async (url: string) => {
    setIsLoading(true);
    setError(null);
    setSearchedQuery(url);

    try {
      // Call the real API endpoint
      const response = await getConnections(url);
      
      // Convert SearchResult[] to AnimatedListItem[]
      const connectionItems: AnimatedListItem[] = response.connections.map(conn => ({
        url: conn.url,
        title: conn.title,
        description: conn.description,
      }));

      setResults(connectionItems);
      setTotalResults(response.totalConnections);
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
    if (url) {
      setQuery(url);
      fetchConnections(url);
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
    router.push(`/ligacoes/results?url=${encodeURIComponent(newQuery)}`);
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
      minHeight: '100vh', 
      overflow: 'auto',
      background: '#0a0a0a',
    }}>

      {/* Top Bar with Logo and Search */}
      <div style={{
        position: 'sticky',
        top: 0,
        zIndex: 10,
        background: 'rgba(10, 10, 10, 0.8)',
        backdropFilter: 'blur(20px)',
        borderBottom: '1px solid rgba(107, 255, 157, 0.2)',
        padding: '1rem 2rem',
        display: 'flex',
        alignItems: 'center',
        gap: '2rem',
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
        paddingTop: '2rem',
        paddingBottom: '1rem',
        paddingLeft: '2rem',
      }}>
        <h2 style={{
          color: 'rgba(255, 255, 255, 0.9)',
          fontSize: '1.2rem',
          fontWeight: 500,
          fontFamily: "'Space Grotesk', 'Inter', sans-serif",
          margin: 0
        }}>
          {isLoading ? (
            <>A carregar ligações...</>
          ) : error ? (
            <span style={{ color: '#6bff9d' }}>Erro: {error}</span>
          ) : searchedQuery ? (
            <>
              Encontradas <strong>{totalResults}</strong> ligações para{' '}
              <em style={{ fontStyle: 'italic', color: '#6bff9d' }}>"{searchedQuery}"</em>
            </>
          ) : (
            <>Ligações de URLs</>
          )}
        </h2>
      </div>

      {/* AnimatedList Container */}
      <div style={{ 
        position: 'relative',
        zIndex: 1,
        width: '95%',
        maxWidth: '1200px',
        margin: '0 auto',
        paddingBottom: '4rem'
      }}>
        {isLoading ? (
          <Loader />
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
                ⚠️ Erro ao carregar ligações
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
            textAlign: 'center',
            padding: '4rem 2rem',
            color: 'rgba(255, 255, 255, 0.6)',
            fontSize: '1rem'
          }}>
            <p style={{ fontSize: '3rem', margin: '0 0 1rem 0' }}>🔗</p>
            <p style={{ margin: '0', fontSize: '1.2rem' }}>
              Nenhuma ligação encontrada
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
            displayScrollbar={true}
          />
        )}
      </div>

      {/* API Status Indicator */}
      <ApiStatus showDetails={true} />

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
