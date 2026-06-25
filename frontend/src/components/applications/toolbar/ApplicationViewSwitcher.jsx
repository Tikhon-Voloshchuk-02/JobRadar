import "./ApplicationViewSwitcher.css";

export default function ApplicationViewSwitcher({ viewMode, onChange }) {
  return (
    <div className="application-view-switcher">
      <button
        type="button"
        className={viewMode === "table" ? "active" : ""}
        onClick={() => onChange("table")}
      >
        Table
      </button>

      <button
        type="button"
        className={viewMode === "kanban" ? "active" : ""}
        onClick={() => onChange("kanban")}
      >
        Kanban
      </button>
    </div>
  );
}