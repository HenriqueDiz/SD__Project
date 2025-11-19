'use client';

import { useState, useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import AnimatedList, { AnimatedListItem } from '@/components/AnimatedList/AnimatedList';
import GradientText from '@/components/GradientText/GradientText';
import SearchBar from '@/components/SearchBar/SearchBar';

const mockSearchResults: AnimatedListItem[] = [
  {
    url: 'https://en.wikipedia.org/wiki/Googol',
    title: 'Googol - Wikipedia',
    description: 'A googol is the large number 10^100. In decimal notation, it is written as the digit 1 followed by one hundred zeroes.',
    links: [
      'https://en.wikipedia.org/wiki/Googolplex',
      'https://en.wikipedia.org/wiki/Large_numbers',
      'https://en.wikipedia.org/wiki/Names_of_large_numbers'
    ]
  },
  {
    url: 'https://www.mathsisfun.com/googol.html',
    title: 'What is a Googol? - Math is Fun',
    description: 'Learn about googol and googolplex, two of the largest numbers with names. Find out how many zeros they have and how to write them.',
    links: [
      'https://www.mathsisfun.com/numbers/index.html',
      'https://www.mathsisfun.com/large-numbers.html'
    ]
  },
  {
    url: 'https://www.britannica.com/science/googol',
    title: 'Googol | Definition & Facts | Britannica',
    description: 'Googol, in mathematics, a large number introduced in 1938 by American mathematician Edward Kasner.',
  },
  {
    url: 'https://www.google.com/search?q=origin+of+google+name',
    title: 'Origin of Google Name',
    description: 'The name Google is a misspelling of Googol, which was chosen to reflect the mission to organize the immense amount of information on the web.',
    links: [
      'https://about.google/intl/en/',
      'https://www.google.com/about/our-story/'
    ]
  },
  {
    url: 'https://mathworld.wolfram.com/Googol.html',
    title: 'Googol -- from Wolfram MathWorld',
    description: 'The number 10^100, also known as ten duotrigintillion, ten thousand sexdecillion, or ten sexdecilliard.',
  },
  {
    url: 'https://www.numberphile.com/videos/googol-and-googolplex',
    title: 'Googol and Googolplex - Numberphile',
    description: 'An interesting video explaining the concept of googol and googolplex numbers.',
    links: [
      'https://www.youtube.com/watch?v=8GEebx72-qs'
    ]
  },
  {
    url: 'https://www.scientificamerican.com/article/what-is-a-googol/',
    title: 'What Is a Googol? - Scientific American',
    description: 'A googol is 1 followed by 100 zeros. The term was coined by 9-year-old Milton Sirotta, nephew of mathematician Edward Kasner.',
  },
  {
    url: 'https://www.khanacademy.org/math/algebra/exponents',
    title: 'Understanding Exponents and Large Numbers - Khan Academy',
    description: 'Learn about exponents, powers of ten, and how they help us represent extremely large numbers like a googol.',
    links: [
      'https://www.khanacademy.org/math/algebra',
      'https://www.khanacademy.org/math/pre-algebra/exponents-radicals'
    ]
  },
  {
    url: 'https://www.history.com/topics/inventions/history-of-google',
    title: 'History of Google - How it Got Its Name',
    description: 'Explore the history of Google and discover why the company chose a misspelling of googol as its name.',
    links: [
      'https://www.history.com/topics/inventions',
      'https://www.google.com/about/our-story/'
    ]
  },
  {
    url: 'https://brilliant.org/wiki/large-numbers/',
    title: 'Large Numbers and Notation - Brilliant Math',
    description: 'An in-depth look at large numbers including googol, googolplex, and other enormous quantities.',
    links: [
      'https://brilliant.org/wiki/scientific-notation/',
      'https://brilliant.org/courses/mathematics/'
    ]
  },
  {
    url: 'https://www.amazon.com/books/googol-mathematics',
    title: 'Books About Googol and Mathematics',
    description: 'Discover books that explore the concept of googol, large numbers, and fascinating mathematical ideas.',
    links: [
      'https://www.amazon.com/Mathematics-Magic-Mystery/dp/0486270882'
    ]
  },
  {
    url: 'https://www.reddit.com/r/math/comments/googol_discussion',
    title: 'Discussion: Googol and Its Significance - r/math',
    description: 'Community discussion about googol, its practical applications, and why mathematicians find it interesting.',
  },
  {
    url: 'https://mathoverflow.net/questions/googol-applications',
    title: 'Are There Any Real Applications of Googol? - MathOverflow',
    description: 'Mathematics Q&A discussing whether numbers as large as googol have any practical applications in real-world scenarios.',
    links: [
      'https://mathoverflow.net/questions/tagged/large-numbers'
    ]
  },
  {
    url: 'https://www.wolframalpha.com/input/?i=googol',
    title: 'Googol Computation - Wolfram Alpha',
    description: 'Use Wolfram Alpha to explore googol and perform computations with this enormous number.',
    links: [
      'https://www.wolframalpha.com/examples/mathematics'
    ]
  },
  {
    url: 'https://ed.ted.com/lessons/how-big-is-a-googol',
    title: 'How Big is a Googol? - TED-Ed',
    description: 'An educational video lesson explaining the magnitude of a googol and comparing it to quantities in the universe.',
    links: [
      'https://ed.ted.com/lessons',
      'https://www.youtube.com/watch?v=example'
    ]
  },
];

export default function DemoPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [query, setQuery] = useState('');

  useEffect(() => {
    const q = searchParams.get('q');
    if (q) {
      setQuery(q);
    }
  }, [searchParams]);

  const handleItemSelect = (item: AnimatedListItem, index: number) => {
    console.log('Selected item:', item, 'at index:', index);
  };

  const handleSearch = (newQuery: string) => {
    setQuery(newQuery);
    console.log('New search:', newQuery);
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
        borderBottom: '1px solid rgba(255, 255, 255, 0.1)',
        padding: '1rem 2rem',
        display: 'flex',
        alignItems: 'center',
        gap: '2rem',
      }}>
        {/* Googol Logo - Clickable */}
        <div 
          onClick={handleLogoClick}
          style={{ 
            cursor: 'pointer',
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
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                background: query.trim() ? 'linear-gradient(135deg, #9c43ff, #4cb8e9)' : 'transparent',
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
                <circle cx="11" cy="11" r="8"></circle>
                <path d="m21 21-4.35-4.35"></path>
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
          {query ? (
            <>Resultados da pesquisa para <em style={{ fontStyle: 'italic', color: '#9c43ff' }}>"{query}"</em></>
          ) : (
            <>Resultados da pesquisa</>
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
        <AnimatedList
          items={mockSearchResults}
          onItemSelect={handleItemSelect}
          showGradients={true}
          enableArrowNavigation={true}
          displayScrollbar={true}
        />
      </div>
    </main>
  );
}
