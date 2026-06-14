import { Link } from "react-router-dom";
import "./LandingPage.css";
import { FiMail, FiGithub } from "react-icons/fi";
import { FaTelegramPlane, FaLinkedin } from "react-icons/fa";

function LandingPage() {
  return (
    <div className="landing-page">
      <header className="landing-header">
        <div className="landing-brand">
          <div className="landing-logo">JR</div>
          <span>JobRadar</span>
        </div>

        <nav className="landing-nav">
          <a href="#features">Features</a>
          <a href="#about">About</a>
          <a href="#contact">Contact</a>

          <Link to="/login" className="nav-login">
            Login
          </Link>

          <Link to="/register" className="nav-register">
            Get Started
          </Link>
        </nav>
      </header>

      <main className="landing-main">
        <section className="hero">
          <div className="hero-badge">
            AI-assisted Job Search Platform
          </div>

          <h1>
            Track your job search
            <br />
            smarter with JobRadar
          </h1>

          <p>
            Manage applications, analyze recruitment emails
            and match your CV to vacancies in one place.
          </p>

          <div className="hero-actions">
            <Link to="/register" className="hero-primary">
              Get Started
            </Link>

            <Link to="/login" className="hero-secondary">
              Sign In
            </Link>
          </div>
        </section>

        <section id="features" className="features">
          <div className="feature-card">
            <h3>Application Tracking</h3>
            <p>
              Manage applications, interviews,
              offers and rejections.
            </p>
          </div>

          <div className="feature-card">
            <h3>Email Analysis</h3>
            <p>
              Automatically process recruitment
              emails using AI.
            </p>
          </div>

          <div className="feature-card">
            <h3>CV Matching</h3>
            <p>
              Compare your CV with vacancy
              requirements and discover gaps.
            </p>
          </div>
        </section>

        <section id="about" className="about-section">
          <h2>About JobRadar</h2>

          <p>
            JobRadar helps job seekers organize applications,
            automate email tracking and improve CV quality.
          </p>
        </section>
      </main>

      <footer id="contact" className="landing-footer">
        <h3>Contact</h3>

        <div className="contact-links">
          <a href="mailto:voloshchuk.tikhon02@gmail.com">
            <FiMail />
            <span>Email</span>
          </a>

          <a
            href="https://t.me/voloshchuk02"
            target="_blank"
            rel="noreferrer"
          >
            <FaTelegramPlane />
            <span>Telegram</span>
          </a>

          <a
            href="https://github.com/Tikhon-Voloshchuk-02"
            target="_blank"
            rel="noreferrer"
          >
            <FiGithub />
            <span>GitHub</span>
          </a>

          <a
            href="https://www.linkedin.com/in/tikhon-voloshchuk/"
            target="_blank"
            rel="noreferrer"
          >
            <FaLinkedin />
            <span>LinkedIn</span>
          </a>
        </div>

        <p>Built by Tikhon Voloshchuk</p>
      </footer>
    </div>
  );
}

export default LandingPage;