import { useState, useRef } from "react";
import CustomSelect from "./CustomSelect";
import {
  buildYearsData,
  buildCellData,
  buildMonthLabels,
  cellDateOf,
  DAY_LABELS,
} from "./heatmapUtils";

const STATUS_MAP = {
  Applied: "Applied", Interview: "Interview",
  Waiting: "Waiting", Offers: "Offer", Rejected: "Rejected",
};
const HIGHLIGHT_MAP = {
  Applied: "applied", Interview: "interview",
  Waiting: "waiting", Offers: "offer", Rejected: "rejected",
};

export default function ActivityCard({ applicationsByDate, today, hoveredStatus, onSelectionChange }) {
  const { yearsData, dateMap, availableYears } = buildYearsData(applicationsByDate);

  const [year, setYear]                           = useState(availableYears[0]);
  const [tooltip, setTooltip]                     = useState(null);
  const [selectedCell, setSelectedCell]           = useState(null);
  const [selectedCompanies, setSelectedCompanies] = useState(new Set());
  const hideTimer = useRef(null);

  const { weeks, base, total, meta } = yearsData[year] ?? { weeks: [], base: new Date(), total: 0, meta: "" };
  const numWeeks     = weeks.length;
  const cellData     = buildCellData(weeks, base, dateMap);
  const monthLabels  = buildMonthLabels(numWeeks, base);
  const cols         = `repeat(${numWeeks}, 1fr)`;
  const hasSelection = selectedCompanies.size > 0;

  function isFuture(wi, di) { return cellDateOf(base, wi, di) > today; }

  function scheduleHide() { hideTimer.current = setTimeout(() => setTooltip(null), 120); }
  function cancelHide()   { if (hideTimer.current) { clearTimeout(hideTimer.current); hideTimer.current = null; } }

  function onCellEnter(e, key) {
    cancelHide();
    const data = cellData[key];
    if (!data) return;
    const rect = e.currentTarget.getBoundingClientRect();
    setTooltip({ x: rect.left + rect.width / 2, y: rect.top, ...data });
  }

  function getMatchedApps(companies) {
    const apps = [];
    for (const data of Object.values(cellData)) {
      for (const app of data.apps) {
        if (companies.has(app.company)) apps.push(app);
      }
    }
    return apps;
  }

  function onCompanyClick(e, company) {
    e.stopPropagation();
    setTooltip(null);
    if (selectedCell === null && selectedCompanies.size === 1 && selectedCompanies.has(company)) {
      setSelectedCompanies(new Set());
      onSelectionChange?.(null);
      return;
    }
    setSelectedCell(null);
    const companies = new Set([company]);
    setSelectedCompanies(companies);
    onSelectionChange?.(getMatchedApps(companies));
  }

  function onCellClick(key, future) {
    if (future || !cellData[key] || key === selectedCell) {
      setSelectedCell(null);
      setSelectedCompanies(new Set());
      onSelectionChange?.(null);
      return;
    }
    setSelectedCell(key);
    const companies = new Set(cellData[key].apps.map(a => a.company));
    setSelectedCompanies(companies);
    onSelectionChange?.(getMatchedApps(companies));
  }

  function getHighlight(key) {
    if (!hasSelection) return null;
    const data = cellData[key];
    if (!data) return null;
    const matched  = data.apps.filter(a => selectedCompanies.has(a.company));
    if (matched.length === 0) return null;
    const statuses = matched.map(a => a.status);
    if (statuses.includes("Offer"))            return "offer";
    if (statuses.includes("Interview"))        return "interview";
    if (statuses.every(s => s === "Rejected")) return "rejected";
    return "applied";
  }

  function getStatusHighlight(key) {
    const data = cellData[key];
    if (!data) return null;
    if (hoveredStatus === "Total") {
      const s = data.apps.map(a => a.status);
      if (s.includes("Offer"))            return "offer";
      if (s.includes("Interview"))        return "interview";
      if (s.every(x => x === "Rejected")) return "rejected";
      if (s.includes("Waiting"))          return "waiting";
      return "applied";
    }
    const target = STATUS_MAP[hoveredStatus];
    if (!target || !data.apps.some(a => a.status === target)) return null;
    return HIGHLIGHT_MAP[hoveredStatus];
  }

  const effectiveHasSelection = Boolean(hoveredStatus) || hasSelection;
  function computeHighlight(key) { return hoveredStatus ? getStatusHighlight(key) : getHighlight(key); }

  function changeYear(y) {
    setYear(y);
    setSelectedCell(null);
    setSelectedCompanies(new Set());
    onSelectionChange?.(null);
  }

  return (
    <>
      <div className="gh-card">
        <div className="gh-card-header">
          <span className="gh-card-title">Activity</span>
          <span className="gh-card-meta">{total} applications {meta}</span>
          {availableYears.length > 1 && (
            <CustomSelect
              className="year-select"
              value={year}
              onChange={y => changeYear(Number(y))}
              options={availableYears.map(y => ({ value: y, label: String(y) }))}
            />
          )}
        </div>

        <div className="heatmap-container">
          <div className="heatmap-months-grid" style={{ gridTemplateColumns: cols }}>
            {monthLabels.map((label, wi) => (
              <div key={wi} className="heatmap-month-col">{label}</div>
            ))}
          </div>

          <div className="heatmap-body">
            <div className="heatmap-days">
              {DAY_LABELS.map((d, i) => <span key={i} className="heatmap-day">{d}</span>)}
            </div>

            <div
              className={`heatmap-grid${effectiveHasSelection ? " has-selection" : ""}`}
              style={{ gridTemplateColumns: cols }}
            >
              {weeks.flatMap((week, wi) =>
                week.map((level, di) => {
                  const key        = `${wi}-${di}`;
                  const future     = isFuture(wi, di);
                  const lvl        = future ? 0 : level;
                  const highlight  = computeHighlight(key);
                  const isSelected = key === selectedCell;
                  return (
                    <div
                      key={key}
                      className={`heatmap-cell${isSelected ? " heatmap-selected" : ""}`}
                      data-level={lvl}
                      data-future={future ? "true" : undefined}
                      data-highlight={highlight ?? undefined}
                      onClick={() => onCellClick(key, future)}
                      onMouseEnter={!future && lvl > 0 ? e => onCellEnter(e, key) : undefined}
                      onMouseLeave={!future && lvl > 0 ? scheduleHide : undefined}
                    />
                  );
                })
              )}
            </div>
          </div>

          <div className="heatmap-legend">
            <span>Less</span>
            {[0, 1, 2, 3].map(l => <div key={l} className="heatmap-cell" data-level={l} />)}
            <span>More</span>
          </div>
        </div>
      </div>

      {tooltip && (
        <div
          className="heatmap-tooltip"
          onMouseEnter={cancelHide}
          onMouseLeave={() => setTooltip(null)}
          style={{
            left: tooltip.x,
            top:  tooltip.y,
            transform: tooltip.y < 160
              ? "translate(-50%, 18px)"
              : "translate(-50%, calc(-100% - 8px))",
          }}
        >
          <div className="heatmap-tooltip__date">{tooltip.date}</div>
          {tooltip.apps.map((app, i) => (
            <div key={i} className="heatmap-tooltip__row">
              <span
                className="heatmap-tooltip__company heatmap-tooltip__company--link"
                onClick={e => onCompanyClick(e, app.company)}
              >
                {app.company}
              </span>
              <span
                className={`heatmap-tooltip__status tt-${app.status.toLowerCase()}${app.gmailMessageId ? " heatmap-tooltip__status--link" : ""}`}
                title={app.gmailMessageId ? "Open source email in Gmail" : undefined}
                onClick={app.gmailMessageId ? e => {
                  e.stopPropagation();
                  window.open(
                    `https://mail.google.com/mail/u/0/#all/${app.gmailMessageId}`,
                    "_blank",
                    "noopener,noreferrer"
                  );
                } : undefined}
              >
                {app.status}{app.gmailMessageId ? " ↗" : ""}
              </span>
            </div>
          ))}
        </div>
      )}
    </>
  );
}
