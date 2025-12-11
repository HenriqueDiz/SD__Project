'use client';
 
import { useEffect, useMemo, useState, type CSSProperties } from 'react';
import Header from '@/components/Header/Header';
import Cursor from '@/components/Cursor/Cursor';
import StaggeredMenu from '@/components/StaggeredMenu/StaggeredMenu';
import { getStatistics, StatisticsResponse, getActiveBarrels, getRegisteredBarrels, subscribeStatistics } from '@/services/api';
 
type ParsedBarrel = {
  name: string;
  port?: string;
  host?: string;
  indexSize?: string;
  ativo?: boolean;
};
 
function parseBarrelInfo(info: string): ParsedBarrel {
  // Accepts formats: name, name:port, name:port:host, name:port:host:indexSize
  const parts = String(info).split(':');
  const [name, port, host, indexSize] = parts;
  return {
    name: name || info,
    port,
    host,
    indexSize,
  };
}
 
const panelStyle: CSSProperties = {
  background: 'rgba(255, 255, 255, 0.03)',
  backdropFilter: 'blur(10px)',
  WebkitBackdropFilter: 'blur(10px)',
  borderRadius: 16,
  padding: 0,
  width: '100%',
  overflow: 'hidden',
  boxShadow: '0 4px 20px rgba(0, 0, 0, 0.05)',
  border: '1px solid rgba(255, 255, 255, 0.1)',
  display: 'flex',
  flexDirection: 'column',
  alignSelf: 'flex-start',
  transition: 'all 0.3s ease'
};
 
const panelHeaderStyle: CSSProperties = {
  padding: '16px 24px',
  borderBottom: '1px solid rgba(255, 255, 255, 0.1)',
  background: 'rgba(255, 255, 255, 0.02)'
};
 
const panelBodyStyle: CSSProperties = {
  padding: 20,
  overflow: 'auto'
};
 
const titleStyle: CSSProperties = {
  marginTop: 0,
  marginBottom: 0,
  fontWeight: 700,
  letterSpacing: 0.5,
  backgroundImage: 'linear-gradient(135deg, #ff3333, #990000)',
  WebkitBackgroundClip: 'text',
  WebkitTextFillColor: 'transparent',
  backgroundClip: 'text',
  fontSize: '1.2rem'
};
 
const textMuted: CSSProperties = { opacity: 0.6, color: 'rgba(255, 255, 255, 0.7)' };
 
const getBarrelCardStyle = (count: number): CSSProperties => ({
  background: 'rgba(255, 255, 255, 0)',
  backdropFilter: 'blur(10px)',
  WebkitBackdropFilter: 'blur(10px)',
  borderRadius: 12,
  padding: count <= 3 ? '16px 20px' : count <= 6 ? '12px 16px' : '10px 14px',
  marginBottom: count <= 3 ? 16 : count <= 6 ? 12 : 8,
  border: '1px solid rgba(255, 255, 255, 0.1)',
  transition: 'all 0.3s ease'
});
 
export default function StatisticsPage() {
  const [data, setData] = useState<StatisticsResponse>({ topSearches: {}, averageResponseTime: {} });
  const [error, setError] = useState<string | null>(null);
  const [activeBarrels, setActiveBarrels] = useState<string[]>([]);
  const [registeredBarrels, setRegisteredBarrels] = useState<string[]>([]);
  const [hoveredCard, setHoveredCard] = useState<string | null>(null);
 
  const menuItems = [
    { label: 'Procurar palavra', ariaLabel: 'Procurar palavra no sistema', link: '/' },
    { label: 'Indexar URL', ariaLabel: 'Adicionar novo URL ao sistema', link: '/indexar' },
    { label: 'Ligações de url', ariaLabel: 'Consultar ligações de uma página', link: '/ligacoes' },
    { label: 'Autores', ariaLabel: 'Ver autores do projeto', link: '/autores' }
  ];
 
  useEffect(() => {
    let mounted = true;
    // Estado inicial via REST (fallback)
    const fetchInitial = async () => {
      try {
        const [stats, active, registered] = await Promise.all([
          getStatistics(),
          getActiveBarrels(),
          getRegisteredBarrels(),
        ]);
        if (mounted) {
          setData(stats);
          setActiveBarrels(active);
          setRegisteredBarrels(registered);
        }
      } catch (e: any) {
        if (mounted) setError(e?.message || 'Erro ao carregar estatísticas');
      }
    };
    fetchInitial();
 
    // Subscrever SSE para updates em tempo real
    const unsubscribe = subscribeStatistics(
      (payload) => { if (mounted) setData(payload); },
      (active) => { if (mounted) setActiveBarrels(active); },
      (registered) => { if (mounted) setRegisteredBarrels(registered); },
    );
 
    return () => { mounted = false; unsubscribe(); };
  }, []);
 
  const topList = useMemo(() => {
    return Object.entries(data.topSearches)
      .sort((a, b) => b[1] - a[1])
      .slice(0, 10);
  }, [data.topSearches]);
 
  const avgList = useMemo(() => {
    return Object.entries(data.averageResponseTime)
      .sort((a, b) => Number(b[1]) - Number(a[1]));
  }, [data.averageResponseTime]);
 
  const parsedActive = useMemo(() => {
    return activeBarrels.map(parseBarrelInfo)
      .sort((a, b) => (a.name > b.name ? 1 : -1));
  }, [activeBarrels]);
 
  const activeByName = useMemo(() => {
    const map = new Map<string, ParsedBarrel>();
    parsedActive.forEach(b => map.set(b.name, b));
    return map;
  }, [parsedActive]);
 
  // Lista única de barrels, indicando se está ativo
  const unifiedBarrels = useMemo(() => {
    return registeredBarrels.map(parseBarrelInfo)
      .map(rb => {
        const ab = activeByName.get(rb.name);
        return {
          name: rb.name,
          host: rb.host || ab?.host,
          port: rb.port || ab?.port,
          indexSize: rb.indexSize || ab?.indexSize,
          ativo: !!ab,
        } as ParsedBarrel;
      })
      .sort((a, b) => (a.name > b.name ? 1 : -1));
  }, [registeredBarrels, activeByName]);
 
  return (
    <main style={{ position: 'fixed', top: 0, left: 0, width: '100vw', height: '100vh', overflow: 'hidden', color: '#fff', background: '#0a0a0a' }}>
      <Cursor />
      <Header />
       <div style={{
         paddingTop: 140,
         display: 'grid',
         gridTemplateColumns: '1fr 1.2fr',
         gridTemplateRows: 'auto auto',
         gridTemplateAreas: `
           'top10 barrels'
           'tempo barrels'
         `,
         gap: 24,
         justifyContent: 'center',
         alignItems: 'stretch',
         boxSizing: 'border-box',
         padding: '140px 32px 32px 32px',
         maxWidth: 1400,
         margin: '0 auto',
         overflow: 'auto',
         maxHeight: '100vh'
       }}>
        {/* Layout atualizado: esquerda (top10, tempo), direita (barrels em toda a altura) */}
        {/* Top 10 pesquisas */}
        <section 
          style={{
            ...panelStyle,
            gridArea: 'top10',
            border: hoveredCard === 'top10' ? '1px solid rgba(255, 51, 51, 0.4)' : panelStyle.border,
            boxShadow: hoveredCard === 'top10' ? '0 0 20px rgba(255, 51, 51, 0.2)' : panelStyle.boxShadow
          }}
          onMouseEnter={() => setHoveredCard('top10')}
          onMouseLeave={() => setHoveredCard(null)}
        >
          <div style={panelHeaderStyle}>
            <h2 style={titleStyle}>Top 10 Pesquisas</h2>
          </div>
          <div style={panelBodyStyle}>
            {topList.length === 0 ? (
              <p style={{ opacity: 0.7 }}>Ainda não há pesquisas registadas.</p>
            ) : (
              <ol style={{ margin: 0, paddingLeft: 20 }}>
                {topList.map(([word, count], idx) => (
                  <li key={word + idx} style={{ marginBottom: 10, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ fontWeight: 600, color: '#ffffff', fontSize: '0.95rem' }}>{word}</span>
                    <span style={textMuted}>{count} {count > 1 ? 'pesquisas' : 'pesquisa'}</span>
                  </li>
                ))}
              </ol>
            )}
          </div>
        </section>
 
        {/* Barrels - painel único */}
        <section 
          style={{
            ...panelStyle,
            gridArea: 'barrels',
            height: '100%',
            display: 'flex',
            flexDirection: 'column',
            border: hoveredCard === 'barrels' ? '1px solid rgba(255, 51, 51, 0.4)' : panelStyle.border,
            boxShadow: hoveredCard === 'barrels' ? '0 0 20px rgba(255, 51, 51, 0.2)' : panelStyle.boxShadow
          }}
          onMouseEnter={() => setHoveredCard('barrels')}
          onMouseLeave={() => setHoveredCard(null)}
        >
          <div style={panelHeaderStyle}>
            <h2 style={titleStyle}>Barrels</h2>
          </div>
           <div style={{ ...panelBodyStyle, flex: 1, minHeight: 0, overflow: 'auto' }}>
            {unifiedBarrels.length === 0 ? (
              <p style={{ opacity: 0.7 }}>Nenhum barrel registado.</p>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', flex: 1, minHeight: 0, overflow: 'auto' }}>
                {unifiedBarrels.map(({ name, host, port, indexSize, ativo }, idx) => {
                  const cardStyle = getBarrelCardStyle(unifiedBarrels.length);
                  const titleFontSize = unifiedBarrels.length <= 3 ? '1.1em' : unifiedBarrels.length <= 6 ? '0.95em' : '0.85em';
                  const contentFontSize = unifiedBarrels.length <= 3 ? '0.95em' : unifiedBarrels.length <= 6 ? '0.85em' : '0.75em';
                  return (
                    <div key={name + (host || '') + (port || '') + idx} style={cardStyle}>
                      <h3 style={{ margin: '0 0 8px 0', fontSize: titleFontSize, fontWeight: 700, backgroundImage: 'linear-gradient(135deg, #ff3333, #990000)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent', backgroundClip: 'text' }}>{name}</h3>
                      <div style={{ display: 'grid', gridTemplateColumns: 'auto auto auto auto', gap: '20px', fontSize: contentFontSize, justifyContent: 'start', alignItems: 'center' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                          <span style={{ ...textMuted, fontWeight: 600 }}>Host:</span>
                          <span style={{ color: '#ffffff' }}>{host || '—'}</span>
                        </div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                          <span style={{ ...textMuted, fontWeight: 600 }}>Porta:</span>
                          <span style={{ color: '#ffffff' }}>{port || '—'}</span>
                        </div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                          <span style={{ ...textMuted, fontWeight: 600 }}>Index Size:</span>
                          <span style={{ color: '#ffffff' }}>{indexSize ?? '—'}</span>
                        </div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                          <span style={{ ...textMuted, fontWeight: 600 }}>Ativo:</span>
                          <span style={{ color: ativo ? '#00ff99' : '#ff3333', fontWeight: 700 }}>{ativo ? 'Sim' : 'Não'}</span>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </section>
 
        {/* Tempo médio por Barrel */}
        <section 
          style={{
            ...panelStyle,
            gridArea: 'tempo',
            border: hoveredCard === 'tempo' ? '1px solid rgba(255, 51, 51, 0.4)' : panelStyle.border,
            boxShadow: hoveredCard === 'tempo' ? '0 0 20px rgba(255, 51, 51, 0.2)' : panelStyle.boxShadow
          }}
          onMouseEnter={() => setHoveredCard('tempo')}
          onMouseLeave={() => setHoveredCard(null)}
        >
          <div style={panelHeaderStyle}>
            <h2 style={titleStyle}>Tempo médio por Barrel</h2>
          </div>
          <div style={panelBodyStyle}>
            {avgList.length === 0 ? (
              <p style={{ opacity: 0.7 }}>Sem tempos registados.</p>
            ) : (
              <ul style={{ margin: 0, paddingLeft: 20 }}>
                {avgList.map(([barrel, nanos]) => {
                  const ms = Number(nanos) / 1_000_000;
                  const barrelName = barrel.split(':')[0]; // Só o nome antes dos ':'
                  return (
                    <li key={barrel} style={{ marginBottom: 10, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <span style={{ fontWeight: 700, color: '#ff3333', fontSize: '0.95rem' }}>{barrelName}</span>
                      <span style={textMuted}>{ms.toFixed(2)} ms</span>
                    </li>
                  );
                })}
              </ul>
            )}
          </div>
        </section>
 
 
      </div>
      {error && (
        <div style={{ position: 'absolute', bottom: 20, left: '50%', transform: 'translateX(-50%)', background: 'rgba(255,0,0,0.15)', border: '1px solid rgba(255,0,0,0.35)', color: '#ffb3b3', padding: '8px 14px', borderRadius: 8 }}>
          {error}
        </div>
      )}
      <StaggeredMenu 
        position="right"
        items={menuItems}
        displayItemNumbering={false}
        displaySocials={false}
        colors={['#ff3333', '#cc0000', 'rgba(15, 15, 20, 0.95)']}
        accentColor="#ff3333"
        borderColor="rgba(255, 51, 51, 0.3)"
        scrollbarColor="rgba(255, 51, 51, 0.3)"
        scrollbarHoverColor="rgba(255, 51, 51, 0.5)"
        menuButtonColor="rgba(255, 255, 255, 0.9)"
        openMenuButtonColor="rgba(255, 255, 255, 0.95)"
        isFixed={true}
      />
    </main>
  );
}

