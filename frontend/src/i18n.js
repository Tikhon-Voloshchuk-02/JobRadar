import i18n from "i18next";
import { initReactI18next } from "react-i18next";

import en from "./i18n/en.json";
import de from "./i18n/de.json";
import ru from "./i18n/ru.json";

const resources = {
  en: { translation: en },
  de: { translation: de },
  ru: { translation: ru },
};

i18n.use(initReactI18next).init({
  resources,
  lng: localStorage.getItem("language") || "en",
  fallbackLng: "en",
  interpolation: {
    escapeValue: false,
  },
});

export default i18n;