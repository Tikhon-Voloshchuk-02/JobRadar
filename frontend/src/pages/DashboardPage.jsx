import { useTranslation } from "react-i18next";
import { useState } from "react";
import { useNavigate } from "react-router-dom";

import { logout } from "../auth/auth";

import DashboardSummary from "../components/DashboardSummary";
import LanguageSwitcher from "../components/LanguageSwitcher";
import Sidebar from "../components/Sidebar";

import { useDashboard } from "../hooks/useDashboard";

import "./DashboardPage.css";

export default function DashboardPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();

  const [sidebarOpen, setSidebarOpen] = useState(true);

  const { summary } = useDashboard(t);

  function handleLogout() {
    logout();
    window.location.href = "/login";
  }

  return (
    <div className={`app-layout ${sidebarOpen ? "sidebar-open" : "sidebar-closed"}`}>
      <Sidebar open={sidebarOpen} setOpen={setSidebarOpen} />

      <main className="dashboard-page">
        <header className="dashboard-header">
          <div>
            <h1>{t("dashboard.title")}</h1>
            <p>{t("dashboard.subtitle")}</p>
          </div>

          <div className="dashboard-header__actions">
            <LanguageSwitcher />

            <button onClick={() => navigate("/applications")}>
              Applications
            </button>

            <button onClick={() => navigate("/user")}>
              {t("nav.profile")}
            </button>

            <button onClick={handleLogout}>
              {t("logout")}
            </button>
          </div>
        </header>

        <DashboardSummary summary={summary} />

        <section className="dashboard-overview-panel">
          <div>
            <h2>Job search overview</h2>
            <p>
              Use Applications to manage your job search in table or kanban view.
            </p>
          </div>

          <button onClick={() => navigate("/applications")}>
            View applications
          </button>
        </section>
      </main>
    </div>
  );
}