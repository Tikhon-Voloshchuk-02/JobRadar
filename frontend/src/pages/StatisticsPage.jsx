import { useState, useRef, useEffect } from "react";
import Sidebar from "../components/applications/Sidebar";
import { getStatistics } from "../api/statistics";
import ActivityCard    from "../components/statistics/ActivityCard";
import TrendCard       from "../components/statistics/TrendCard";
import OverviewBar     from "../components/statistics/OverviewBar";
import PipelineCard    from "../components/statistics/PipelineCard";
import StageTimingsCard from "../components/statistics/StageTimingsCard";
import "./StatisticsPage.css";

export default function StatisticsPage() {
  const [sidebarOpen, setSidebarOpen]     = useState(true);
  const [hoveredStatus, setHoveredStatus] = useState(null);
  const [selectionApps, setSelectionApps] = useState(null);
  const activityRef = useRef(null);
  const [activityH, setActivityH] = useState(null);

  const [stats, setStats]     = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError]     = useState("");

  useEffect(() => {
    let active = true;
    getStatistics()
      .then(data  => { if (active) { setStats(data); setLoading(false); } })
      .catch(err  => { if (active) { setError(err.message || "Failed to load statistics"); setLoading(false); } });
    return () => { active = false; };
  }, []);

  useEffect(() => {
    if (!activityRef.current) return;
    const ro = new ResizeObserver(([e]) => setActivityH(Math.round(e.contentRect.height)));
    ro.observe(activityRef.current);
    return () => ro.disconnect();
  }, []);

  // ── Derived data ─────────────────────────────────────────────────────────────

  const today = new Date();
  today.setHours(0, 0, 0, 0);

  const sc    = stats?.statusCounts ?? {};
  const total = stats?.totalApplications ?? 0;

  const baseOverview = [
    { value: String(total),              label: "Total",     color: "#e6edf3" },
    { value: String(sc.APPLIED   ?? 0), label: "Applied",   color: "#58a6ff" },
    { value: String(sc.INTERVIEW ?? 0), label: "Interview", color: "#bc8cff" },
    { value: String(sc.WAITING   ?? 0), label: "Waiting",   color: "#e3b341" },
    { value: String(sc.OFFER     ?? 0), label: "Offers",    color: "#3fb950" },
    { value: String(sc.REJECTED  ?? 0), label: "Rejected",  color: "#f85149" },
  ];

  const overviewItems = selectionApps
    ? [
        { value: String(selectionApps.length),                                                  label: "Total",     color: "#e6edf3" },
        { value: String(selectionApps.filter(a => a.status === "Applied").length),   label: "Applied",   color: "#58a6ff" },
        { value: String(selectionApps.filter(a => a.status === "Interview").length), label: "Interview", color: "#bc8cff" },
        { value: String(selectionApps.filter(a => a.status === "Waiting").length),   label: "Waiting",   color: "#e3b341" },
        { value: String(selectionApps.filter(a => a.status === "Offer").length),     label: "Offers",    color: "#3fb950" },
        { value: String(selectionApps.filter(a => a.status === "Rejected").length),  label: "Rejected",  color: "#f85149" },
      ]
    : baseOverview;

  const pipelineItems = [
    { label: "Applied",   count: total,             pct: 100,                                                         color: "#58a6ff" },
    { label: "Interview", count: sc.INTERVIEW ?? 0, pct: total ? Math.round((sc.INTERVIEW ?? 0) / total * 100) : 0,  color: "#bc8cff" },
    { label: "Offer",     count: sc.OFFER     ?? 0, pct: total ? Math.round((sc.OFFER     ?? 0) / total * 100) : 0,  color: "#3fb950" },
    { label: "Rejected",  count: sc.REJECTED  ?? 0, pct: total ? Math.round((sc.REJECTED  ?? 0) / total * 100) : 0,  color: "#f85149" },
  ];

  const timings    = stats?.avgTimings;
  const timingRows = timings
    ? [
        { label: "Applied → Interview",  days: timings.appliedToInterviewDays  },
        { label: "Interview → Decision", days: timings.interviewToDecisionDays },
        { label: "Full cycle avg",       days: timings.fullCycleAvgDays        },
      ]
    : [];

  const responseRate = total > 0
    ? Math.round(((sc.INTERVIEW ?? 0) + (sc.OFFER ?? 0)) / total * 100)
    : 0;

  // ── Loading / error states ───────────────────────────────────────────────────

  const layout = (children) => (
    <div className={`app-layout ${sidebarOpen ? "sidebar-open" : "sidebar-closed"}`}>
      <Sidebar open={sidebarOpen} setOpen={setSidebarOpen} />
      <main className="statistics-page">{children}</main>
    </div>
  );

  if (loading) return layout(<p style={{ color: "#8b949e", padding: "2rem" }}>Loading statistics…</p>);
  if (error)   return layout(<p style={{ color: "#f85149", padding: "2rem" }}>{error}</p>);

  // ── Main render ──────────────────────────────────────────────────────────────

  return layout(
    <>
      <header className="stats-header">
        <h1>Statistics</h1>
        <p className="stats-header__sub">
          <strong>{total}</strong> applications total ·{" "}
          <strong>{responseRate}%</strong> response rate ·{" "}
          <span className="text-green">
            {sc.OFFER ?? 0} offer{(sc.OFFER ?? 0) !== 1 ? "s" : ""}
          </span> received
        </p>
      </header>

      <OverviewBar
        items={overviewItems}
        hoveredStatus={hoveredStatus}
        onHover={setHoveredStatus}
      />

      <div ref={activityRef}>
        <ActivityCard
          applicationsByDate={stats.applicationsByDate}
          today={today}
          hoveredStatus={hoveredStatus}
          onSelectionChange={setSelectionApps}
        />
      </div>

      <div style={activityH ? { height: activityH } : {}}>
        <TrendCard trendData={stats.weeklyTrend} />
      </div>

      <div className="stats-bottom">
        <PipelineCard items={pipelineItems} />
        <StageTimingsCard rows={timingRows} />
      </div>
    </>
  );
}
