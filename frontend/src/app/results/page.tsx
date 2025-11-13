'use client';

import AnimatedList, { AnimatedListItem } from '@/components/AnimatedList/AnimatedList';
import Orb from '@/components/Orb/Orb';
import GradientText from '@/components/GradientText/GradientText';

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
];

export default function DemoPage() {
  const handleItemSelect = (item: AnimatedListItem, index: number) => {
    console.log('Selected item:', item, 'at index:', index);
  };

  return (
    <main style={{ position: 'relative', width: '100%', minHeight: '100vh', overflow: 'auto' }}>
      {/* Orb Background */}
      <div style={{ 
        width: '100%', 
        height: '100vh', 
        position: 'fixed', 
        top: 0, 
        left: 0, 
        zIndex: 0 
      }}>
        <Orb
          hoverIntensity={0.5}
          rotateOnHover={true}
          hue={0}
          forceHoverState={false}
        />
      </div>

      {/* Googol Logo - Top Left */}
      <div style={{ 
        position: 'absolute', 
        top: '2rem', 
        left: '2rem', 
        zIndex: 2
      }}>
        <GradientText
          colors={['#9c43ff', '#4cb8e9', '#0f14ff', '#4cb8e9', '#9c43ff']}
          animationSpeed={3}
          showBorder={false}
        >
          <h1 style={{ 
            fontSize: '2.5rem', 
            margin: 0, 
            fontWeight: 700, 
            letterSpacing: '-0.02em',
            fontFamily: "'Space Grotesk', 'Inter', sans-serif"
          }}>Googol</h1>
        </GradientText>
      </div>

      {/* Demo Title */}
      <div style={{
        position: 'relative',
        zIndex: 1,
        paddingTop: '6rem',
        paddingBottom: '2rem',
        textAlign: 'center'
      }}>
        <h2 style={{
          color: 'white',
          fontSize: '1.5rem',
          fontWeight: 600,
          fontFamily: "'Space Grotesk', 'Inter', sans-serif",
          margin: 0
        }}>
          Resultados da pesquisa para <em style={{ fontStyle: 'italic' }}>"bouas"</em>
        </h2>
      </div>

      {/* AnimatedList Container */}
      <div style={{ 
        position: 'relative',
        zIndex: 1,
        width: '90%',
        maxWidth: '800px',
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
