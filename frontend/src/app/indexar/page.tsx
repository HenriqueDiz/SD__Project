'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Orb from '@/components/Orb/Orb';
import GradientText from '@/components/GradientText/GradientText';
import Modal from '@/components/Modal/Modal';
import StaggeredMenu from '@/components/StaggeredMenu/StaggeredMenu';
import Cursor from '@/components/Cursor/Cursor';
import { addUrl, AddUrlResponse } from '@/services/api';

export default function IndexarURL() {
  const router = useRouter();
  const [url, setUrl] = useState('');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);
  const [isInputFocused, setIsInputFocused] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [pendingUrl, setPendingUrl] = useState('');

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

    try {
      const response: AddUrlResponse = await addUrl(url);
      
      if (response.success) {
        // Check if URL is already indexed
        if (response.alreadyIndexed) {
          setPendingUrl(url);
          setShowModal(true);
          setLoading(false);
          return;
        }
        
        setMessage({ type: 'success', text: response.message || 'URL adicionado com sucesso!' });
        setUrl('');
        
        // Clear success message after 3 seconds
        setTimeout(() => setMessage(null), 3000);
      } else {
        setMessage({ type: 'error', text: response.message || 'Erro ao adicionar URL' });
      }
    } catch (error: any) {
      setMessage({ 
        type: 'error', 
        text: error.message || 'Erro ao conectar com o servidor. Verifique se a API está ativa.' 
      });
    } finally {
      setLoading(false);
    }
  };

  const handleReindex = async () => {
    setShowModal(false);
    setLoading(true);
    setMessage(null);

    try {
      // Re-index with indexAnyway=true to force re-indexing
      const response: AddUrlResponse = await addUrl(pendingUrl, true);
      
      if (response.success) {
        setMessage({ type: 'success', text: 'URL reindexado com sucesso!' });
        setUrl('');
        setPendingUrl('');
        
        // Clear success message after 3 seconds
        setTimeout(() => setMessage(null), 3000);
      } else {
        setMessage({ type: 'error', text: response.message || 'Erro ao reindexar URL' });
      }
    } catch (error: any) {
      setMessage({ 
        type: 'error', 
        text: error.message || 'Erro ao reindexar URL' 
      });
    } finally {
      setLoading(false);
    }
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setPendingUrl('');
  };

  const menuItems = [
    { label: 'Procurar palavra', ariaLabel: 'Procurar palavra no sistema', link: '/' },
    { label: 'Estatísticas', ariaLabel: 'Ver estatísticas do sistema', link: '/statistics' },
    { label: 'Ligações de url', ariaLabel: 'Consultar ligações de uma página', link: '/ligacoes' },
    { label: 'Autores', ariaLabel: 'Ver autores do projeto', link: '/autores' }
  ];

  return (
    <main style={{ position: 'fixed', top: 0, left: 0, width: '100vw', height: '100vh', overflow: 'hidden' }}>
      <Cursor />
      {/* Orb Background */}
      <div style={{ width: '100%', height: '100%', position: 'fixed', top: 0, left: 0, zIndex: 0 }}>
        <Orb
          hoverIntensity={0.5}
          rotateOnHover={true}
          hue={270}
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
        <GradientText
          colors={['#ff6b9d', '#ff8c42', '#ff6b9d', '#ff8c42', '#ff6b9d']}
          animationSpeed={3}
          showBorder={false}
        >
          <h1 style={{ 
            fontSize: '2.5rem', 
            margin: 0, 
            fontWeight: 700, 
            letterSpacing: '-0.02em',
            fontFamily: "'Space Grotesk', 'Inter', sans-serif",
            transition: 'transform 0.2s ease',
          }}
          onMouseEnter={(e) => e.currentTarget.style.transform = 'scale(1.05)'}
          onMouseLeave={(e) => e.currentTarget.style.transform = 'scale(1)'}
          >
            Googol
          </h1>
        </GradientText>
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
          border: '1px solid rgba(255, 255, 255, 0.1)',
          boxShadow: '0 20px 60px rgba(0, 0, 0, 0.3)',
        }}>
          <h2 style={{
            fontSize: '2rem',
            fontWeight: 700,
            marginBottom: '0.5rem',
            background: 'linear-gradient(135deg, #ff6b9d, #ff8c42)',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
            textAlign: 'center',
          }}>
            Indexar URL
          </h2>
          
          <p style={{
            color: 'rgba(255, 255, 255, 0.6)',
            textAlign: 'center',
            marginBottom: '2rem',
            fontSize: '0.95rem',
          }}>
            Adicione um URL para ser indexado pelo sistema Googol
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
                  background: 'rgba(255, 255, 255, 0.05)',
                  border: '1px solid rgba(255, 255, 255, 0.1)',
                  borderRadius: '12px',
                  color: '#fff',
                  outline: 'none',
                  transition: 'all 0.3s ease',
                  boxSizing: 'border-box',
                }}
                onMouseEnter={(e) => {
                  if (!loading) {
                    e.currentTarget.style.borderColor = 'rgba(255, 107, 157, 0.5)';
                    e.currentTarget.style.background = 'rgba(255, 255, 255, 0.08)';
                  }
                }}
                onMouseLeave={(e) => {
                  if (!isInputFocused) {
                    e.currentTarget.style.borderColor = 'rgba(255, 255, 255, 0.1)';
                    e.currentTarget.style.background = 'rgba(255, 255, 255, 0.05)';
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
                  ? 'rgba(255, 140, 66, 0.1)' 
                  : 'rgba(255, 67, 54, 0.1)',
                border: `1px solid ${message.type === 'success' 
                  ? 'rgba(255, 140, 66, 0.3)' 
                  : 'rgba(255, 67, 54, 0.3)'}`,
                color: message.type === 'success' ? '#ff8c42' : '#ff4336',
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
                  ? 'rgba(255, 107, 157, 0.3)' 
                  : 'linear-gradient(135deg, #ff6b9d, #ff8c42)',
                border: 'none',
                borderRadius: '12px',
                color: '#fff',
                transition: 'all 0.3s ease',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '0.5rem',
              }}
              onMouseEnter={(e) => {
                if (!loading) {
                  e.currentTarget.style.transform = 'translateY(-2px)';
                  e.currentTarget.style.boxShadow = '0 10px 30px rgba(255, 107, 157, 0.4)';
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
                  <span>Adicionando...</span>
                  <style jsx>{`
                    @keyframes spin {
                      0% { transform: rotate(0deg); }
                      100% { transform: rotate(360deg); }
                    }
                  `}</style>
                </>
              ) : (
                'Adicionar URL'
              )}
            </button>
          </form>
        </div>
      </div>

      {/* Reindex Confirmation Modal */}
      <Modal
        isOpen={showModal}
        onClose={handleCloseModal}
        onConfirm={handleReindex}
        title="URL já indexado"
        message={`O URL "${pendingUrl}" já foi indexado anteriormente. Deseja reindexá-lo?`}
        confirmText="Reindexar"
        cancelText="Cancelar"
        confirmColor="#ff6b9d"
      />

      {/* Staggered Menu */}
      <StaggeredMenu
        position="right"
        items={menuItems}
        displayItemNumbering={false}
        displaySocials={false}
        colors={['#ff6b9d', '#ff8c42', 'rgba(15, 15, 20, 0.95)']}
        accentColor="#ff6b9d"
        menuButtonColor="rgba(255, 255, 255, 0.9)"
        openMenuButtonColor="rgba(255, 255, 255, 0.95)"
        isFixed={true}
      />
    </main>
  );
}
