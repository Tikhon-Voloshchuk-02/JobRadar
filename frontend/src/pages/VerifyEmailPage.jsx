import { useEffect, useState } from "react";
import { useSearchParams, useNavigate } from "react-router-dom";
import "./VerifyEmailPage.css";

function VerifyEmailPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = searchParams.get("token");

  const [status, setStatus] = useState(token ? "loading" : "error");
  // loading | success | error

  useEffect(() => {
    if (!token) {
      return;
    }

    async function verify() {
      try {
        const response = await fetch(
          `/api/auth/verify-email?token=${encodeURIComponent(token)}`
        );

        if (!response.ok) {
          throw new Error();
        }

        setStatus("success");

        // через 2 сек → login
        setTimeout(() => {
          navigate("/login");
        }, 2000);

      } catch {
        setStatus("error");
      }
    }

    verify();
  }, [token, navigate]);

  return (
    <div className="verify-page">
      <div className="verify-card">
        {status === "loading" && <h2>Verifying your email...</h2>}

        {status === "success" && (
          <>
            <h2 className="verify-success">Email verified successfully ✅</h2>
            <p>Redirecting to login...</p>
          </>
        )}

        {status === "error" && (
          <>
            <h2 className="verify-error">Verification failed ❌</h2>
            <p>Invalid or expired link</p>
          </>
        )}
      </div>
    </div>
  );
}

export default VerifyEmailPage;
