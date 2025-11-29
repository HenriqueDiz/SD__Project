'use client';

import { useRouter } from 'next/navigation';
import GradientText from '@/components/GradientText/GradientText';

export default function Header() {
  const router = useRouter();

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
          colors={['#9c43ff', '#4cb8e9','#9c43ff', '#4cb8e9', '#9c43ff']}
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
