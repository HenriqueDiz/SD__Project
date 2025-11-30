'use client';

import { useState } from 'react'
import Orb from '@/components/Orb/Orb'
import Header from '@/components/Header/Header'
import SearchBar from '@/components/SearchBar/SearchBar'
import StaggeredMenu from '@/components/StaggeredMenu/StaggeredMenu'


export default function Home() {
  const [isSearchBarHovered, setIsSearchBarHovered] = useState(false);

  const handleSearch = (query: string) => {
    console.log('Searching for:', query);
    // Temporarily redirect to /demo on search
  };

  const menuItems = [
    { label: 'Indexar URL', ariaLabel: 'Adicionar URL para indexação', link: '/indexar' },
    { label: 'Estatísticas', ariaLabel: 'Ver estatísticas do sistema', link: '/statistics' },
    { label: 'Ligações de url', ariaLabel: 'Consultar ligações de uma página', link: '/ligacoes' },
    { label: 'Autores', ariaLabel: 'Ver autores do projeto', link: '/autores' }
   ];

  return (
    <>
      <main style={{ position: 'fixed', top: 0, left: 0, width: '100vw', height: '100vh', overflow: 'hidden' }}>
        {/* Orb Background */}
        <div style={{ width: '100%', height: '100%', position: 'absolute', top: 0, left: 0, zIndex: 0 }}>
          <Orb
            hoverIntensity={0.5}
            rotateOnHover={true}
            hue={0}
            forceHoverState={isSearchBarHovered}
          />
        </div>

        {/* Googol Logo - Top Left */}
        <Header />

        {/* Search Bar - Center */}
        <div style={{ 
          position: 'absolute', 
          top: '50%', 
          left: '50%', 
          transform: 'translate(-50%, -50%)',
          zIndex: 1,
          width: '90%',
          maxWidth: '480px',
          display: 'flex',
          justifyContent: 'center'
        }}>
          <SearchBar 
            onSearch={handleSearch} 
            placeholder="Search the web..."
            redirectOnSubmit={true}
            redirectPath="/results"
            onHoverChange={setIsSearchBarHovered}
          />
        </div>
      </main>

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
        isFixed={true}
      />
    </>
  )
}
