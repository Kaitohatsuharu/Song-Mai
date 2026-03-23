"use client";

import { useState } from "react";
import axios from "axios";

interface VerifyResult {
  email: string;
  valid: boolean;
  disposable: boolean;
  mx_found: boolean;
  smtp_result: string;
  score: number;
}

export default function VerifyPage() {
  const [email, setEmail] = useState("");
  const [result, setResult] = useState<VerifyResult | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function handleVerify(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    setResult(null);
    setLoading(true);
    try {
      const res = await axios.post<VerifyResult>("/api/v1/verify", { email });
      setResult(res.data);
    } catch {
      setError("Verification failed. Check your API key and try again.");
    } finally {
      setLoading(false);
    }
  }

  const scoreColor = result
    ? result.score >= 80 ? "text-green-600" : result.score >= 50 ? "text-yellow-600" : "text-red-600"
    : "";

  return (
    <div className="p-8 max-w-2xl">
      <h1 className="text-2xl font-bold text-gray-900 mb-2">Email Verification Playground</h1>
      <p className="text-gray-500 text-sm mb-8">
        Check if an email address exists without sending a real email.
      </p>

      <form onSubmit={handleVerify} className="card flex gap-3 mb-6 p-4">
        <input
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
          placeholder="someone@example.com"
          className="flex-1 border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
        />
        <button
          type="submit"
          disabled={loading}
          className="bg-brand-500 text-white px-5 py-2 rounded-lg text-sm font-semibold hover:bg-brand-600 disabled:opacity-60"
        >
          {loading ? "Checking…" : "Verify"}
        </button>
      </form>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 text-sm px-4 py-3 rounded-lg mb-4">
          {error}
        </div>
      )}

      {result && (
        <div className="card">
          <div className="flex items-center justify-between mb-6">
            <h2 className="font-semibold text-gray-900">{result.email}</h2>
            <div className={`text-3xl font-extrabold ${scoreColor}`}>{result.score}</div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <ResultRow label="Valid" value={result.valid} type="bool" />
            <ResultRow label="Disposable" value={result.disposable} type="bool" invert />
            <ResultRow label="MX Found" value={result.mx_found} type="bool" />
            <ResultRow label="SMTP Result" value={result.smtp_result} type="text" />
          </div>

          {/* Score bar */}
          <div className="mt-6">
            <div className="flex justify-between text-xs text-gray-500 mb-1">
              <span>Score</span>
              <span>{result.score} / 100</span>
            </div>
            <div className="h-2 bg-gray-100 rounded-full overflow-hidden">
              <div
                className={`h-full rounded-full ${
                  result.score >= 80 ? "bg-green-500" : result.score >= 50 ? "bg-yellow-500" : "bg-red-500"
                }`}
                style={{ width: `${result.score}%` }}
              />
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

function ResultRow({
  label,
  value,
  type,
  invert = false,
}: {
  label: string;
  value: boolean | string;
  type: "bool" | "text";
  invert?: boolean;
}) {
  let display: React.ReactNode = String(value);
  if (type === "bool") {
    const positive = invert ? !value : value;
    display = (
      <span className={positive ? "badge-green" : "badge-red"}>
        {String(value)}
      </span>
    );
  }
  return (
    <div>
      <p className="text-xs text-gray-500 mb-0.5">{label}</p>
      <p className="text-sm font-medium">{display}</p>
    </div>
  );
}
