'use client';

import { useEffect, useRef } from 'react';
import { usePathname } from 'next/navigation';
import './Cursor.css';

interface Circle extends HTMLDivElement {
  x?: number;
  y?: number;
}

const Cursor = () => {
  const pathname = usePathname();
  const coordsRef = useRef({ x: 0, y: 0 });
  const circlesRef = useRef<Circle[]>([]);

  // Cores baseadas na rota
  const getColors = () => {
    // Página de estatísticas usa vermelho
    if (pathname === '/statistics') {
      return [
        "#ff6666",
        "#ff6060",
        "#ff5a5a",
        "#ff5454",
        "#ff4e4e",
        "#ff4848",
        "#ff4242",
        "#ff3c3c",
        "#ff3636",
        "#ff3030",
        "#ff2a2a",
        "#ff2424",
        "#ff1e1e",
        "#ff1818",
        "#ff1212",
        "#ff0c0c",
        "#ff0606",
        "#ff0000",
        "#f50000",
        "#eb0000",
        "#e10000",
        "#d70000",
        "#cd0000",
        "#c30000",
        "#b90000",
        "#af0000",
        "#a50000",
        "#9b0000",
        "#910000",
        "#cc0000"
      ];
    }
    
    // Página de ligações usa verde/amarelo
    if (pathname === '/ligacoes' || pathname === '/ligacoes/results') {
      return [
        "#00ff88",
        "#0bff87",
        "#16ff86",
        "#21ff85",
        "#2cff84",
        "#37ff83",
        "#42ff82",
        "#4dff81",
        "#58ff80",
        "#63ff7f",
        "#6eff7e",
        "#79ff7d",
        "#84ff7c",
        "#8fff7b",
        "#9aff7a",
        "#a5ff79",
        "#b0ff78",
        "#bbff77",
        "#c6ff76",
        "#d1ff75",
        "#dcff74",
        "#e7ff73",
        "#f2ff72",
        "#fdff71",
        "#fffc70",
        "#fff16f",
        "#ffe66e",
        "#ffdb6d",
        "#ffd06c",
        "#88ff00"
      ];
    }
    
    // Página de indexar URL usa rosa/laranja
    if (pathname === '/indexar') {
      return [
        "#ff6b9d",
        "#ff6d9a",
        "#ff6f97",
        "#ff7194",
        "#ff7391",
        "#ff758e",
        "#ff778b",
        "#ff7988",
        "#ff7b85",
        "#ff7d82",
        "#ff7f7f",
        "#ff817c",
        "#ff8379",
        "#ff8576",
        "#ff8773",
        "#ff8970",
        "#ff8b6d",
        "#ff8d6a",
        "#ff8f67",
        "#ff9164",
        "#ff9361",
        "#ff955e",
        "#ff975b",
        "#ff9958",
        "#ff9b55",
        "#ff9d52",
        "#ff9f4f",
        "#ffa14c",
        "#ffa349",
        "#ff8c42"
      ];
    }
    
    // Página principal usa roxo/azul
    return [
      "#9c43ff",
      "#9a46ff",
      "#9849ff",
      "#964cff",
      "#944fff",
      "#9252ff",
      "#9055ff",
      "#8e58ff",
      "#8c5bff",
      "#8a5eff",
      "#8861ff",
      "#8664ff",
      "#8467ff",
      "#826aff",
      "#806dff",
      "#7e70ff",
      "#7c73ff",
      "#7a76ff",
      "#7879ff",
      "#767cff",
      "#747fff",
      "#7282ff",
      "#7085ff",
      "#6e88ff",
      "#6c8bff",
      "#6a8eff",
      "#6891ff",
      "#6694ff",
      "#64b0f4",
      "#4cb8e9"
    ];
  };

  const colors = getColors();

  useEffect(() => {
    // Inicializar posições dos círculos
    circlesRef.current.forEach((circle) => {
      if (circle) {
        circle.x = 0;
        circle.y = 0;
      }
    });

    const handleMouseMove = (e: MouseEvent) => {
      coordsRef.current.x = e.clientX;
      coordsRef.current.y = e.clientY;
    };

    const animateCircles = () => {
      let x = coordsRef.current.x;
      let y = coordsRef.current.y;

      circlesRef.current.forEach((circle, index) => {
        if (!circle) return;

        circle.style.left = x - 12 + "px";
        circle.style.top = y - 12 + "px";
        circle.style.transform = `scale(${(circlesRef.current.length - index) / circlesRef.current.length})`;

        circle.x = x;
        circle.y = y;

        const nextCircle = circlesRef.current[index + 1] || circlesRef.current[0];
        if (nextCircle && nextCircle.x !== undefined && nextCircle.y !== undefined) {
          x += (nextCircle.x - x) * 0.3;
          y += (nextCircle.y - y) * 0.3;
        }
      });

      requestAnimationFrame(animateCircles);
    };

    window.addEventListener("mousemove", handleMouseMove);
    animateCircles();

    return () => {
      window.removeEventListener("mousemove", handleMouseMove);
    };
  }, []);

  return (
    <>
      {colors.map((color, index) => (
        <div
          key={index}
          className="circle"
          ref={(el) => {
            if (el) circlesRef.current[index] = el as Circle;
          }}
          style={{ backgroundColor: color }}
        />
      ))}
    </>
  );
};

export default Cursor;
