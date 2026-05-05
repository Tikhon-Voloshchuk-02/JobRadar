import { NavLink } from "react-router-dom";
import "./Sidebar.css";

function Sidebar({ open, setOpen }) {
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
              AI Suggestions
            </NavLink>

            <NavLink to="/profile" className="sidebar-link">
              Profile
            </NavLink>
          </nav>
        </>
      )}
    </aside>
  );
}

export default Sidebar;