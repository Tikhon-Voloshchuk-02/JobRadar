import { NavLink } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useEffect, useState } from "react";
import "./Sidebar.css";

function Sidebar({ open, setOpen }) {

  const { t } = useTranslation();
  const [pendingCount, setPendingCount] = useState(0);

  useEffect(() => {
    const fetchPendingCount = async () => {
      try {
        const token = localStorage.getItem("token");

        const response = await fetch(
          "http://localhost:8080/api/ai-suggestions/pending/count",
          {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }
        );

        if (!response.ok) {
          return;
        }

        const data = await response.json();
        setPendingCount(data.count);

      } catch (error) {
        console.error("Failed to fetch AI suggestions count", error);
      }
    };

    fetchPendingCount();
  }, []);

  return (
    <aside className={`sidebar ${open ? "open" : "closed"}`}>

      <button className="sidebar-toggle" onClick={() => setOpen(!open)}>
        {open ? "←" : "→"}
      </button>

      {open && (
        <>
          <div className="sidebar-logo">
            <span className="sidebar-logo-icon">◎</span>
            <span className="sidebar-logo-text">JobRadar</span>
          </div>

          <nav className="sidebar-nav">
            <NavLink to="/dashboard" className="sidebar-link">
              {t("nav.dashboard")}
            </NavLink>

            <NavLink to="/ai-suggestions" className="sidebar-link">
              <span>{t("nav.ai_suggestions")}</span>

              {pendingCount > 0 && (
                <span className="sidebar-badge">
                  {pendingCount}
                </span>
              )}
            </NavLink>

            <NavLink to="/user" className="sidebar-link">
              {t("nav.profile")}
            </NavLink>
          </nav>
        </>
      )}
    </aside>
  );
}

export default Sidebar;