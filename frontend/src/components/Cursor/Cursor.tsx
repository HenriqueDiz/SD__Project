'use client';

import { useEffect, useRef } from 'react';
import './Cursor.css';

interface Circle extends HTMLDivElement {
  x?: number;
  y?: number;
}

const Cursor = () => {
  const coordsRef = useRef({ x: 0, y: 0 });
  const circlesRef = useRef<Circle[]>([]);

  // Cores do gradiente do site (roxo/azul)
  const colors = [
    "#9c43ff",
    "#9548ff",
    "#8e4dff",
    "#8752ff",
    "#8057ff",
    "#795cff",
    "#7261ff",
    "#6b66ff",
    "#646bff",
    "#5d70ff",
    "#5675ff",
    "#4f7aff",
    "#487fff",
    "#4184ff",
    "#3a89ff",
    "#338eff",
    "#2c93ff",
    "#2598ff",
    "#1e9dff",
    "#17a2ff",
    "#10a7ff",
    "#09acff",
    "#02b1ff",
    "#00b6ff",
    "#00bbff",
    "#00c0ff",
    "#00c5ff",
    "#00caff",
    "#00cfff",
    "#4cb8e9"
  ];

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
