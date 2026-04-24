import i18n from "i18next";
import "./LanguageSwitcher.css";

import gbFlag from "../assets/flags/gb.svg";
import deFlag from "../assets/flags/de.svg";
import ruFlag from "../assets/flags/ru.svg";

export default function LanguageSwitcher() {
  const currentLang = i18n.language;

  function changeLanguage(lang) {
    localStorage.setItem("language", lang);
    i18n.changeLanguage(lang);
  }

  return (
    <div className="language-switcher">
      <button
        className={currentLang === "en" ? "active" : ""}
        onClick={() => changeLanguage("en")}
      >
        <img src={gbFlag} alt="English" />
        EN
      </button>

      <button
        className={currentLang === "de" ? "active" : ""}
        onClick={() => changeLanguage("de")}
      >
        <img src={deFlag} alt="Deutsch" />
        DE
      </button>

      <button
        className={currentLang === "ru" ? "active" : ""}
        onClick={() => changeLanguage("ru")}
      >
        <img src={ruFlag} alt="Русский" />
        RU
      </button>
    </div>
  );
}