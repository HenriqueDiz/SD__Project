'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Orb from '@/components/Orb/Orb';
import GradientText from '@/components/GradientText/GradientText';
import StaggeredMenu from '@/components/StaggeredMenu/StaggeredMenu';

export default function LigacoesPage() {
  const router = useRouter();
  const [url, setUrl] = useState('');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);
  const [isInputFocused, setIsInputFocused] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!url.trim()) {
      setMessage({ type: 'error', text: 'Por favor, insira um URL válido' });
      return;
    }

    // Validate URL format
    try {
      new URL(url);
    } catch {
      setMessage({ type: 'error', text: 'URL inválido. Use o formato: https://exemplo.com' });
      return;
    }

    setLoading(true);
    setMessage(null);

    // Redirect to results page with URL parameter and page 1
    router.push(`/ligacoes/results?url=${encodeURIComponent(url)}&page=1`);
  };

  const menuItems = [
    { label: 'Procurar palavra', ariaLabel: 'Procurar palavra no sistema', link: '/' },
    { label: 'Indexar URL', ariaLabel: 'Adicionar novo URL ao sistema', link: '/indexar' },
    { label: 'Estatísticas', ariaLabel: 'Ver estatísticas do sistema', link: '/statistics' },
    { label: 'Autores', ariaLabel: 'Ver autores do projeto', link: '/autores' }
  ];

  return (
    <main style={{ position: 'fixed', top: 0, left: 0, width: '100vw', height: '100vh', overflow: 'hidden' }}>
      {/* Orb Background */}
      <div style={{ width: '100%', height: '100%', position: 'fixed', top: 0, left: 0, zIndex: 0 }}>
        <Orb
          hoverIntensity={0.5}
          rotateOnHover={true}
          hue={117}
          forceHoverState={isInputFocused}
        />
      </div>

      {/* Header with Logo */}
      <div style={{ 
        position: 'absolute', 
        top: '2rem', 
        left: '2rem', 
        zIndex: 100,
      }}
      onClick={() => router.push('/')}
      >
        <div style={{
          transition: 'transform 0.2s ease',
          display: 'inline-block',
          overflow: 'visible',
          willChange: 'transform',
          margin: '-10px',
          padding: '10px',
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
              fontSize: '2.5rem', 
              margin: 0, 
              fontWeight: 700, 
              letterSpacing: '-0.02em',
              fontFamily: "'Space Grotesk', 'Inter', sans-serif",
              overflow: 'visible',
              display: 'inline-block',
            }}>
              Googol
            </h1>
          </GradientText>
        </div>
      </div>

      {/* Main Content */}
      <div style={{ 
        position: 'absolute',
        top: 0,
        left: 0,
        width: '100%',
        height: '100%',
        zIndex: 1,
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        overflow: 'hidden',
      }}>
        <div style={{
          maxWidth: '600px',
          width: '100%',
          background: 'rgba(15, 15, 20, 0.8)',
          backdropFilter: 'blur(20px)',
          borderRadius: '24px',
          padding: '3rem',
          border: '1px solid rgba(107, 255, 157, 0.2)',
          boxShadow: '0 20px 60px rgba(107, 255, 157, 0.2)',
        }}>
          <h2 style={{
            fontSize: '2rem',
            fontWeight: 700,
            marginBottom: '0.5rem',
            background: 'linear-gradient(135deg, #6bff9d, #c5ff42)',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
            textAlign: 'center',
          }}>
            Ligações de URL
          </h2>
          
          <p style={{
            color: 'rgba(255, 255, 255, 0.6)',
            textAlign: 'center',
            marginBottom: '2rem',
            fontSize: '0.95rem',
          }}>
            Descubra quais páginas fazem referência a um URL específico
          </p>

          <form onSubmit={handleSubmit}>
            <div style={{ marginBottom: '1.5rem' }}>
              <label 
                htmlFor="url-input"
                style={{
                  display: 'block',
                  marginBottom: '0.5rem',
                  color: 'rgba(255, 255, 255, 0.8)',
                  fontSize: '0.9rem',
                  fontWeight: 500,
                  cursor: "none",
                }}
              >
                URL
              </label>
              <input
                id="url-input"
                type="text"
                value={url}
                onChange={(e) => setUrl(e.target.value)}
                onFocus={() => setIsInputFocused(true)}
                onBlur={() => setIsInputFocused(false)}
                placeholder="https://exemplo.com"
                disabled={loading}
                style={{
                  width: '100%',
                  padding: '1rem 1.25rem',
                  fontSize: '1rem',
                background: 'rgba(107, 255, 157, 0.05)',
                border: '1px solid rgba(107, 255, 157, 0.2)',
                  borderRadius: '12px',
                  color: '#fff',
                  outline: 'none',
                  transition: 'all 0.3s ease',
                  boxSizing: 'border-box',
                }}
                onMouseEnter={(e) => {
                  if (!loading) {
                    e.currentTarget.style.borderColor = 'rgba(107, 255, 157, 0.5)';
                    e.currentTarget.style.background = 'rgba(107, 255, 157, 0.08)';
                  }
                }}
                onMouseLeave={(e) => {
                  if (!isInputFocused) {
                    e.currentTarget.style.borderColor = 'rgba(107, 255, 157, 0.2)';
                    e.currentTarget.style.background = 'rgba(107, 255, 157, 0.05)';
                  }
                }}
              />
            </div>

            {message && (
              <div style={{
                padding: '1rem',
                marginBottom: '1.5rem',
                borderRadius: '12px',
                background: message.type === 'success' 
                  ? 'rgba(197, 255, 66, 0.1)' 
                  : 'rgba(107, 255, 157, 0.1)',
                border: `1px solid ${message.type === 'success' 
                  ? 'rgba(197, 255, 66, 0.3)' 
                  : 'rgba(107, 255, 157, 0.3)'}`,
                color: message.type === 'success' ? '#c5ff42' : '#6bff9d',
                fontSize: '0.9rem',
              }}>
                {message.text}
              </div>
            )}

            <button
              type="submit"
              disabled={loading}
              style={{
                width: '100%',
                padding: '1rem',
                fontSize: '1rem',
                fontWeight: 600,
                background: loading 
                  ? 'rgba(107, 255, 157, 0.3)' 
                  : 'linear-gradient(135deg, #6bff9d, #c5ff42)',
                border: 'none',
                borderRadius: '12px',
                color: '#1a1a1aff',
                cursor: loading ? 'not-allowed' : 'pointer',
                transition: 'all 0.3s ease',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '0.5rem',
              }}
              onMouseEnter={(e) => {
                if (!loading) {
                  e.currentTarget.style.transform = 'translateY(-2px)';
                  e.currentTarget.style.boxShadow = '0 10px 30px rgba(107, 255, 157, 0.4)';
                }
              }}
              onMouseLeave={(e) => {
                if (!loading) {
                  e.currentTarget.style.transform = 'translateY(0)';
                  e.currentTarget.style.boxShadow = 'none';
                }
              }}
            >
              {loading ? (
                <>
                  <div style={{
                    width: '20px',
                    height: '20px',
                    border: '2px solid rgba(255, 255, 255, 0.3)',
                    borderTop: '2px solid #fff',
                    borderRadius: '50%',
                    animation: 'spin 0.8s linear infinite',
                  }} />
                  <span>A procurar...</span>
                  <style jsx>{`
                    @keyframes spin {
                      0% { transform: rotate(0deg); }
                      100% { transform: rotate(360deg); }
                    }
                  `}</style>
                </>
              ) : (
                'Ver Ligações'
              )}
            </button>
          </form>
        </div>
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
