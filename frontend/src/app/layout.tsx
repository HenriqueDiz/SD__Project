import Noise from '@/components/Noise/Noise';
import Cursor from '@/components/Cursor/Cursor';

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="pt" style={{ overflow: 'hidden', height: '100%' }}>
      <head>
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link rel="preconnect" href="https://fonts.gstatic.com" crossOrigin="anonymous" />
        <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&family=Space+Grotesk:wght@700&display=swap" rel="stylesheet" />
      </head>
      <body style={{ margin: 0, padding: 0, backgroundColor: '#000', fontFamily: 'Inter, system-ui, -apple-system, sans-serif', overflow: 'hidden', height: '100vh', width: '100vw' }}>
        <Cursor />
        <Noise 
          patternSize={250}
          patternScaleX={1}
          patternScaleY={1}
          patternRefreshInterval={2}
          patternAlpha={15}
        />
        {children}
      </body>
    </html>
  )
}
