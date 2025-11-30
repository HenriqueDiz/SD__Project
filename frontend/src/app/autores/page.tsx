"use client";
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
  },
  {
    name: 'Rodrigo Manão',
    title: '',
    handle: '',
    avatarUrl: '',
    githubUrl: 'https://github.com/rodrigomanao',
  },
  {
    name: 'João',
    title: '',
    handle: '',
    avatarUrl: '',
    githubUrl: '',
  }
];

const menuItems = [
  { label: 'Procurar palavra', ariaLabel: 'Procurar palavra no sistema', link: '/' },
  { label: 'Estatísticas', ariaLabel: 'Ver estatísticas do sistema', link: '/statistics' },
  { label: 'Ligações de url', ariaLabel: 'Consultar ligações de uma página', link: '/ligacoes' }
];

export default function AutoresPage() {
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
      justifyContent: 'center',
      background: 'rgba(10,10,10,0.95)',
      padding: '80px 0 0 0',
    }}>
      <div className="profile-cards-container">
        {autores.map((autor) => (
          <ProfileCard
            key={autor.name}
            name={autor.name}
            title={autor.title}
            handle={autor.handle}
            status="Online"
            contactText="Github"
            avatarUrl={autor.avatarUrl}
            showUserInfo={true}
            enableTilt={true}
            enableMobileTilt={false}
            onContactClick={() => window.open(autor.githubUrl, '_blank')}
          />
        ))}
      </div>
    </main>
    </>
  );
}
