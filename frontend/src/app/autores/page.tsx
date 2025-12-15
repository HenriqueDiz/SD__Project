"use client";
import { useState } from 'react';
import ProfileCard from '@/components/ProfileCard/ProfileCard';
import Header from '@/components/Header/Header';
import StaggeredMenu from '@/components/StaggeredMenu/StaggeredMenu';

const autores = [
  {
    name: 'Henrique Diz',
    title: 'Gajo Fixe',
    handle: 'diz@student.dei.uc.pt',
    avatarUrl: '/Henrique.jpg',
    githubUrl: 'https://github.com/HenriqueDiz',
    linkedinUrl: 'https://www.linkedin.com/in/henrique-diz-1749a6309/',
    email: 'diz@student.dei.uc.pt',
  },
  {
    name: 'Rodrigo Manão',
    title: 'Prompt guy',
    handle: 'manao@student.dei.uc.pt',
    avatarUrl: '/Rodrigo.jpg',
    githubUrl: 'https://github.com/rodrigomanao',
    linkedinUrl: 'https://www.linkedin.com/in/rodrigo-man%C3%A3o-57b061330/',
    email: 'manao@student.dei.uc.pt',
  },
  {
    name: 'João Francisco',
    title: 'Um gah mesmo',
    handle: 'joaofrancisco@student.dei.uc.pt',
    avatarUrl: '/Joao.jpg',
    githubUrl: 'https://github.com/jonasfranciss',
    linkedinUrl: '',
    email: 'joaofrancisco@student.dei.uc.pt',
  }
];

const menuItems = [
  { label: 'Procurar palavra', ariaLabel: 'Procurar palavra no sistema', link: '/' },
  { label: 'Estatísticas', ariaLabel: 'Ver estatísticas do sistema', link: '/statistics' },
  { label: 'Ligações de url', ariaLabel: 'Consultar ligações de uma página', link: '/ligacoes' }
];

export default function AutoresPage() {
  const [isModalOpen, setIsModalOpen] = useState(false);

  const handleOpenReport = () => {
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
  };

  return (
    <>
      {/* Googol Logo - Top Left */}
      <Header />

      {/* Staggered Menu - Right */}
      <StaggeredMenu
        position="right"
        items={menuItems}
        displayItemNumbering={false}
        displaySocials={false}
        colors={["#9c43ff", "#4cb8e9", "rgba(15, 15, 20, 0.95)"]}
        accentColor="#9c43ff"
        menuButtonColor="rgba(255, 255, 255, 0.9)"
        openMenuButtonColor="rgba(255, 255, 255, 0.95)"
        isFixed={true}
      />

    <main style={{
      minHeight: '100vh',
      width: '100vw',
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'space-between',
      background: 'rgba(10,10,10,0.95)',
      padding: '2rem 1rem',
    }}>
      {/* Spacer */}
      <div style={{ flex: '0 0 80px' }}></div>

      {/* Authors Cards - Centered */}
      <div className="profile-cards-container" style={{ flex: '0 0 auto' }}>
        {autores.map((autor) => (
          <ProfileCard
            key={autor.name}
            name={autor.name}
            title={autor.title}
            handle={autor.handle}
            status="Online"
            avatarUrl={autor.avatarUrl}
            showUserInfo={true}
            enableTilt={true}
            enableMobileTilt={false}
            githubUrl={autor.githubUrl}
            linkedinUrl={autor.linkedinUrl}
            email={autor.email}
          />
        ))}
      </div>

      {/* Report Button - Bottom */}
      <div style={{
        flex: '0 0 auto',
        marginBottom: '3rem',
      }}>
        <button
          onClick={handleOpenReport}
          style={{
            position: 'relative',
            padding: '1rem 2.5rem',
            fontSize: '1rem',
            fontWeight: 600,
            color: '#ffffff',
            background: 'rgba(255, 255, 255, 0.05)',
            border: '1px solid rgba(156, 67, 255, 0.3)',
            borderRadius: '12px',
            cursor: 'pointer',
            transition: 'all 0.3s ease',
            backdropFilter: 'blur(10px)',
            overflow: 'hidden',
          }}
          onMouseEnter={(e) => {
            e.currentTarget.style.background = 'linear-gradient(135deg, rgba(156, 67, 255, 0.2), rgba(76, 184, 233, 0.2))';
            e.currentTarget.style.borderColor = 'rgba(156, 67, 255, 0.6)';
            e.currentTarget.style.transform = 'translateY(-2px)';
            e.currentTarget.style.boxShadow = '0 8px 25px rgba(156, 67, 255, 0.25)';
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.background = 'rgba(255, 255, 255, 0.05)';
            e.currentTarget.style.borderColor = 'rgba(156, 67, 255, 0.3)';
            e.currentTarget.style.transform = 'translateY(0)';
            e.currentTarget.style.boxShadow = 'none';
          }}
        >
          <span style={{ 
            display: 'flex', 
            alignItems: 'center', 
            gap: '0.5rem',
          }}>
            Ver Relatório
          </span>
        </button>
      </div>

      {/* macOS-style PDF Preview Modal */}
      {isModalOpen && (
        <div
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: 'rgba(0, 0, 0, 0.75)',
            backdropFilter: 'blur(8px)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 9999,
            padding: '2rem',
            animation: 'fadeIn 0.2s ease',
            cursor: 'none',
          }}
          onClick={handleCloseModal}
        >
          <div
            style={{
              position: 'relative',
              width: '90%',
              maxWidth: '1000px',
              height: '90vh',
              background: 'rgba(30, 30, 35, 0.98)',
              borderRadius: '12px',
              border: '1px solid rgba(255, 255, 255, 0.1)',
              boxShadow: '0 25px 50px rgba(0, 0, 0, 0.5)',
              display: 'flex',
              flexDirection: 'column',
              overflow: 'hidden',
              animation: 'slideUp 0.3s cubic-bezier(0.16, 1, 0.3, 1)',
            }}
            onClick={(e) => e.stopPropagation()}
          >
            {/* macOS-style Header */}
            <div style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              padding: '0.75rem 1rem',
              background: 'rgba(20, 20, 25, 0.95)',
              borderBottom: '1px solid rgba(255, 255, 255, 0.05)',
            }}>
              {/* Close X Button - Left */}
              <button
                onClick={handleCloseModal}
                style={{
                  width: '32px',
                  height: '32px',
                  borderRadius: '6px',
                  background: 'rgba(255, 255, 255, 0.05)',
                  border: '1px solid rgba(255, 255, 255, 0.1)',
                  color: 'rgba(255, 255, 255, 0.6)',
                  cursor: 'pointer',
                  transition: 'all 0.2s ease',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: '1.2rem',
                  fontWeight: 400,
                  lineHeight: 1,
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.background = 'rgba(255, 95, 87, 0.2)';
                  e.currentTarget.style.borderColor = 'rgba(255, 95, 87, 0.4)';
                  e.currentTarget.style.color = '#ff5f57';
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.background = 'rgba(255, 255, 255, 0.05)';
                  e.currentTarget.style.borderColor = 'rgba(255, 255, 255, 0.1)';
                  e.currentTarget.style.color = 'rgba(255, 255, 255, 0.6)';
                }}
              >
                ×
              </button>

              {/* Title */}
              <div style={{
                position: 'absolute',
                left: '50%',
                transform: 'translateX(-50%)',
                fontSize: '0.9rem',
                fontWeight: 600,
                color: 'rgba(255, 255, 255, 0.9)',
              }}>
                Relatório.pdf
              </div>

              {/* Download Button */}
              <a
                href="/files/Meta 2/Relatório.pdf"
                download
                style={{
                  padding: '0.4rem 1rem',
                  fontSize: '0.85rem',
                  fontWeight: 600,
                  color: '#ffffff',
                  background: 'rgba(156, 67, 255, 0.2)',
                  border: '1px solid rgba(156, 67, 255, 0.4)',
                  borderRadius: '6px',
                  cursor: 'pointer',
                  transition: 'all 0.2s ease',
                  textDecoration: 'none',
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.background = 'rgba(156, 67, 255, 0.3)';
                  e.currentTarget.style.borderColor = 'rgba(156, 67, 255, 0.6)';
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.background = 'rgba(156, 67, 255, 0.2)';
                  e.currentTarget.style.borderColor = 'rgba(156, 67, 255, 0.4)';
                }}
              >
                ↓ Download
              </a>
            </div>

            {/* PDF Viewer */}
            <div style={{
              flex: 1,
              overflow: 'auto',
              background: '#2a2a2e',
            }}>
              <iframe
                src="/files/Meta 2/Relatório.pdf"
                style={{
                  width: '100%',
                  height: '100%',
                  border: 'none',
                }}
                title="Relatório Meta 2"
              />
            </div>
          </div>

          <style jsx>{`
            @keyframes fadeIn {
              from {
                opacity: 0;
              }
              to {
                opacity: 1;
              }
            }

            @keyframes slideUp {
              from {
                opacity: 0;
                transform: translateY(20px) scale(0.98);
              }
              to {
                opacity: 1;
                transform: translateY(0) scale(1);
              }
            }
          `}</style>
        </div>
      )}
    </main>
    </>
  );
}
