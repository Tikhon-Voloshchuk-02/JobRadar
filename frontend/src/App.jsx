import { Routes, Route, Navigate } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import DashboardPage from "./pages/DashboardPage";
import ProtectedRoute from "./components/ProtectedRoute";
import VerifyEmailPage from "./pages/VerifyEmailPage";
import AiSuggestionsPage from "./pages/AiSuggestionsPage";
import UserPage from "./pages/UserPage";

function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />

      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/verify-email" element={<VerifyEmailPage />} />

      <Route
        path="/profile"
        element={
          <ProtectedRoute>
            <UserPage />
          </ProtectedRoute>
        }
      />

      <Route
        path="/user"
        element={
          <ProtectedRoute>
            <UserPage />
          </ProtectedRoute>
        }
      />

      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <DashboardPage />
          </ProtectedRoute>
        }
      />

      <Route
        path="/ai-suggestions"
        element={
          <ProtectedRoute>
            <AiSuggestionsPage />
          </ProtectedRoute>
        }
      />
    </Routes>
  );
}

export default App;
