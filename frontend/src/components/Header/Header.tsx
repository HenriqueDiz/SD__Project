'use client';

import { useRouter, usePathname } from 'next/navigation';
import GradientText from '@/components/GradientText/GradientText';

export default function Header() {
  const router = useRouter();
  const pathname = usePathname();

  // Escolhe as cores baseadas na rota
  const getColors = () => {
    if (pathname === '/statistics') {
      return ['#ff6666', '#cc0000', '#ff6666', '#cc0000', '#ff6666'];
    }
    if (pathname === '/ligacoes' || pathname === '/ligacoes/results') {
      return ['#00ff88', '#88ff00', '#00ff88', '#88ff00', '#00ff88'];
    }
    if (pathname === '/indexar-url') {
      return ['#ff6b9d', '#ff8c42', '#ff6b9d', '#ff8c42', '#ff6b9d'];
    }
    return ['#9c43ff', '#4cb8e9','#9c43ff', '#4cb8e9', '#9c43ff'];
  };

  return (
    <div style={{ 
      position: 'absolute', 
      top: '2rem', 
      left: '2rem', 
      zIndex: 100,
      overflow: 'visible',
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
          colors={getColors()}
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
          }}
          >
            Googol
          </h1>
        </GradientText>
      </div>
    </div>
  );
}
