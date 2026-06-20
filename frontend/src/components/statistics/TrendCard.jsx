import { useState, useRef, useEffect } from "react";
import CustomSelect from "./CustomSelect";

const METRIC_CONFIG = {
  apps:      { label: "Applications", color: "#58a6ff", unit: "" },
  interview: { label: "Interview",    color: "#bc8cff", unit: "" },
  offer:     { label: "Offer",        color: "#3fb950", unit: "" },
  rejected:  { label: "Rejected",     color: "#f85149", unit: "" },
};

function buildLinePath(pts) {
  if (!pts.length) return "";
  let d = `M ${pts[0][0].toFixed(1)},${pts[0][1].toFixed(1)}`;
  for (let i = 1; i < pts.length; i++) {
    const [x0, y0] = pts[i - 1];
    const [x1, y1] = pts[i];
    const cx = ((x0 + x1) / 2).toFixed(1);
    d += ` C ${cx},${y0.toFixed(1)} ${cx},${y1.toFixed(1)} ${x1.toFixed(1)},${y1.toFixed(1)}`;
  }
  return d;
}

function TrendChart({ metric, trendData }) {
  const containerRef             = useRef(null);
  const [chartW, setChartW]      = useState(600);
  const [chartH, setChartH]      = useState(150);
  const [hoveredIdx, setHovered] = useState(null);

  useEffect(() => {
    if (!containerRef.current) return;
    const ro = new ResizeObserver(([e]) => {
      setChartW(Math.floor(e.contentRect.width));
      setChartH(Math.floor(e.contentRect.height));
    });
    ro.observe(containerRef.current);
    return () => ro.disconnect();
  }, []);

  const cfg = METRIC_CONFIG[metric];

  if (!trendData || trendData.length < 2) {
    return (
      <div ref={containerRef} style={{ width: "100%", height: "100%", display: "flex", alignItems: "center", justifyContent: "center" }}>
        <span style={{ color: "#8b949e", fontSize: 13 }}>Not enough data</span>
      </div>
    );
  }

  const VH  = chartH || 150;
  const pad = { top: 16, right: 8, bottom: 28, left: 28 };
  const cW  = chartW - pad.left - pad.right;
  const cH  = VH - pad.top - pad.bottom;

  const rawMax = Math.max(...trendData.map(d => d[metric]));
  const maxVal = Math.max(Math.ceil(rawMax / 5) * 5, 5);
  const ticks  = [0, Math.round(maxVal / 2), maxVal];

  const toX   = i => pad.left + (i / (trendData.length - 1)) * cW;
  const toY   = v => pad.top + cH - (v / maxVal) * cH;
  const baseY = pad.top + cH;

  const pts      = trendData.map((d, i) => [toX(i), toY(d[metric])]);
  const linePath = buildLinePath(pts);
  const areaPath = `${linePath} L ${pts.at(-1)[0].toFixed(1)},${baseY} L ${pts[0][0].toFixed(1)},${baseY} Z`;
  const gradId   = `grad-${metric}`;

  const TW  = 155;
  const TH  = 22 + Object.keys(METRIC_CONFIG).length * 22 + 10;
  const hovD = hoveredIdx !== null ? trendData[hoveredIdx] : null;
  const hovX = hoveredIdx !== null ? toX(hoveredIdx) : 0;
  const hovY = hoveredIdx !== null ? toY(hovD[metric]) : 0;
  const tx   = hovX + 16 + TW > chartW ? hovX - 16 - TW : hovX + 16;
  const ty   = Math.max(pad.top, Math.min(hovY - TH / 2, baseY - TH));

  return (
    <div ref={containerRef} style={{ width: "100%", height: "100%" }}>
      <svg width="100%" height={VH} style={{ display: "block" }} onMouseLeave={() => setHovered(null)}>
        <defs>
          <linearGradient id={gradId} x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%"   stopColor={cfg.color} stopOpacity="0.22" />
            <stop offset="100%" stopColor={cfg.color} stopOpacity="0"    />
          </linearGradient>
        </defs>

        {ticks.map(t => {
          const y = toY(t);
          return (
            <g key={t}>
              <line x1={pad.left} y1={y} x2={chartW} y2={y} stroke="#21262d" strokeWidth="1" />
              <text x={4} y={y - 4} fontSize="9" fill="#8b949e">{t}</text>
            </g>
          );
        })}

        {hovD && <>
          <line x1={hovX} y1={hovY} x2={hovX} y2={baseY} stroke={cfg.color} strokeWidth="1" strokeDasharray="4 3" opacity="0.55" />
          <line x1={0}    y1={hovY} x2={hovX} y2={hovY}  stroke={cfg.color} strokeWidth="1" strokeDasharray="4 3" opacity="0.55" />
        </>}

        <path d={areaPath} fill={`url(#${gradId})`} />
        <path d={linePath} fill="none" stroke={cfg.color} strokeWidth="2" strokeLinejoin="round" strokeLinecap="round" />

        {pts.map(([x, y], i) => (
          <circle key={i} cx={x} cy={y}
                  r={hoveredIdx === i ? 5 : 3}
                  fill={cfg.color} stroke="#161b22"
                  strokeWidth={hoveredIdx === i ? 2 : 1.5}
                  pointerEvents="none" />
        ))}

        {trendData.map((d, i) => (
          <text key={i} x={toX(i)} y={VH - 4} textAnchor="middle" fontSize="9"
                fill={hoveredIdx === i ? "#e6edf3" : "#8b949e"}>
            {d.week}
          </text>
        ))}

        {hovD && (
          <g pointerEvents="none">
            <rect x={tx} y={ty} width={TW} height={TH} fill="#1c2128" stroke="#444c56" strokeWidth="1" rx="6" />
            <text x={tx + 10} y={ty + 15} fontSize="11" fontWeight="600" fill="#8b949e">{hovD.week}</text>
            {Object.entries(METRIC_CONFIG).map(([key, c], i) => (
              <g key={key}>
                <circle cx={tx + 14} cy={ty + 28 + i * 22} r="3.5" fill={c.color} />
                <text x={tx + 24}      y={ty + 33 + i * 22} fontSize="10" fill="#e6edf3">{c.label}</text>
                <text x={tx + TW - 10} y={ty + 33 + i * 22} fontSize="10" fontWeight="600" fill={c.color} textAnchor="end">
                  {hovD[key]}{c.unit}
                </text>
              </g>
            ))}
          </g>
        )}

        {trendData.map((_, i) => {
          const x    = toX(i);
          const prev = i > 0 ? (x + toX(i - 1)) / 2 : pad.left;
          const next = i < trendData.length - 1 ? (x + toX(i + 1)) / 2 : chartW;
          return (
            <rect key={i} x={prev} y={pad.top} width={next - prev} height={cH}
                  fill="transparent" style={{ cursor: "crosshair" }}
                  onMouseEnter={() => setHovered(i)} />
          );
        })}
      </svg>
    </div>
  );
}

export default function TrendCard({ trendData }) {
  const [metric, setMetric] = useState("apps");
  const cfg     = METRIC_CONFIG[metric];
  const current = trendData?.at(-1)?.[metric] ?? 0;
  const prev    = trendData?.at(-2)?.[metric] ?? 0;
  const delta   = current - prev;

  return (
    <div className="gh-card trend-card">
      <div className="gh-card-header">
        <span className="gh-card-title">Tendency</span>

        <CustomSelect
          value={metric}
          onChange={setMetric}
          options={Object.entries(METRIC_CONFIG).map(([key, c]) => ({ value: key, label: c.label }))}
        />

        <div className="trend-kpi">
          <span className="trend-kpi__val" style={{ color: cfg.color }}>
            {current}{cfg.unit}
          </span>
          {delta !== 0 && (
            <span className={`trend-kpi__delta ${delta > 0 ? "trend-up" : "trend-down"}`}>
              {delta > 0 ? "▲" : "▼"} {Math.abs(delta)}{cfg.unit}
            </span>
          )}
          <span className="trend-kpi__sub">last week</span>
        </div>
      </div>

      <div style={{ flex: 1, minHeight: 0 }}>
        <TrendChart metric={metric} trendData={trendData ?? []} />
      </div>
    </div>
  );
}
