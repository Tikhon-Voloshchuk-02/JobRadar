import { NavLink } from "react-router-dom";
import { useEffect, useState } from "react";
import "./Sidebar.css";

function Sidebar({ open, setOpen }) {

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
              Dashboard
            </NavLink>

            <NavLink to="/ai-suggestions" className="sidebar-link">
              <span>AI Suggestions</span>

              {pendingCount > 0 && (
                <span className="sidebar-badge">
                  {pendingCount}
                </span>
              )}
            </NavLink>

            <NavLink to="/user" className="sidebar-link">
              Profile
            </NavLink>
          </nav>
        </>
      )}
    </aside>
  );
}

export default Sidebar;