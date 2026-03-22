"use client";

import { useState, useEffect } from "react";
import axios from "axios";

interface ApiKey {
  id: string;
  name: string;
  key_prefix: string;
  last_used_at?: string;
  created_at: string;
}

export default function ApiKeysPage() {
  const [keys, setKeys] = useState<ApiKey[]>([]);
  const [name, setName] = useState("");
  const [newKey, setNewKey] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    axios.get<ApiKey[]>("/api/dashboard/api-keys").then((r) => setKeys(r.data));
  }, []);

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await axios.post<{ key: string; id: string; name: string; key_prefix: string; created_at: string }>(
        "/api/dashboard/api-keys",
        { name }
      );
      setNewKey(res.data.key);
      setKeys((prev) => [...prev, res.data]);
      setName("");
    } finally {
      setLoading(false);
    }
  }

  async function handleDelete(id: string) {
    await axios.delete(`/api/dashboard/api-keys/${id}`);
    setKeys((prev) => prev.filter((k) => k.id !== id));
  }

  return (
    <div className="p-8 max-w-3xl">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">API Keys</h1>

      {newKey && (
        <div className="bg-green-50 border border-green-200 rounded-xl p-4 mb-6">
          <p className="text-sm font-semibold text-green-800 mb-1">
            Copy your API key — it won&apos;t be shown again!
          </p>
          <code className="text-sm font-mono bg-white border border-green-100 rounded px-3 py-1.5 block break-all">
            {newKey}
          </code>
          <button
            onClick={() => { navigator.clipboard.writeText(newKey); }}
            className="mt-2 text-xs text-green-700 hover:underline"
          >
            Copy to clipboard
          </button>
        </div>
      )}

      <form onSubmit={handleCreate} className="card flex gap-3 mb-8 p-4">
        <input
          type="text"
          value={name}
          onChange={(e) => setName(e.target.value)}
          required
          placeholder="Key name (e.g. Production)"
          className="flex-1 border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
        />
        <button
          type="submit"
          disabled={loading}
          className="bg-brand-500 text-white px-4 py-2 rounded-lg text-sm font-semibold hover:bg-brand-600 disabled:opacity-60"
        >
          {loading ? "Creating…" : "Create key"}
        </button>
      </form>

      <div className="card p-0 overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 border-b border-gray-100">
            <tr>
              {["Name", "Prefix", "Last used", "Created", ""].map((h) => (
                <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wide">
                  {h}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-50">
            {keys.map((key) => (
              <tr key={key.id}>
                <td className="px-4 py-3 font-medium">{key.name}</td>
                <td className="px-4 py-3 font-mono text-gray-500">{key.key_prefix}…</td>
                <td className="px-4 py-3 text-gray-400 text-xs">
                  {key.last_used_at ? new Date(key.last_used_at).toLocaleString() : "Never"}
                </td>
                <td className="px-4 py-3 text-gray-400 text-xs">
                  {new Date(key.created_at).toLocaleDateString()}
                </td>
                <td className="px-4 py-3">
                  <button
                    onClick={() => handleDelete(key.id)}
                    className="text-red-500 hover:underline text-xs"
                  >
                    Revoke
                  </button>
                </td>
              </tr>
            ))}
            {!keys.length && (
              <tr>
                <td colSpan={5} className="px-4 py-10 text-center text-gray-400">
                  No API keys yet.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
