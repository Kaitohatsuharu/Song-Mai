"use client";

import { useState, useEffect } from "react";
import axios from "axios";

interface Domain {
  id: string;
  domain: string;
  spf_verified: boolean;
  dkim_verified: boolean;
  dmarc_verified: boolean;
  created_at: string;
}

export default function DomainsPage() {
  const [domains, setDomains] = useState<Domain[]>([]);
  const [domain, setDomain] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    axios.get<Domain[]>("/api/dashboard/domains").then((r) => setDomains(r.data));
  }, []);

  async function handleAdd(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const res = await axios.post<Domain>("/api/dashboard/domains", { domain });
      setDomains((prev) => [...prev, res.data]);
      setDomain("");
    } catch {
      setError("Failed to add domain. It may already exist.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="p-8 max-w-3xl">
      <h1 className="text-2xl font-bold text-gray-900 mb-2">Sending Domains</h1>
      <p className="text-gray-500 text-sm mb-8">
        Add a domain and configure SPF, DKIM, and DMARC records for best deliverability.
      </p>

      <form onSubmit={handleAdd} className="card flex gap-3 mb-8 p-4">
        <input
          type="text"
          value={domain}
          onChange={(e) => setDomain(e.target.value)}
          required
          placeholder="yourdomain.com"
          className="flex-1 border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
        />
        <button
          type="submit"
          disabled={loading}
          className="bg-brand-500 text-white px-4 py-2 rounded-lg text-sm font-semibold hover:bg-brand-600 disabled:opacity-60"
        >
          {loading ? "Adding…" : "Add domain"}
        </button>
      </form>
      {error && <p className="text-red-600 text-sm mb-4">{error}</p>}

      <div className="space-y-4">
        {domains.map((d) => (
          <div key={d.id} className="card">
            <div className="flex items-center justify-between mb-4">
              <h3 className="font-semibold text-gray-900">{d.domain}</h3>
              <span className="text-xs text-gray-400">
                Added {new Date(d.created_at).toLocaleDateString()}
              </span>
            </div>
            <div className="grid grid-cols-3 gap-4">
              <DnsRecord label="SPF" verified={d.spf_verified} />
              <DnsRecord label="DKIM" verified={d.dkim_verified} />
              <DnsRecord label="DMARC" verified={d.dmarc_verified} />
            </div>
          </div>
        ))}
        {!domains.length && (
          <div className="card text-center py-10 text-gray-400">
            No sending domains added yet.
          </div>
        )}
      </div>
    </div>
  );
}

function DnsRecord({ label, verified }: { label: string; verified: boolean }) {
  return (
    <div className="flex items-center gap-2">
      <span className={`w-2 h-2 rounded-full ${verified ? "bg-green-500" : "bg-gray-300"}`} />
      <span className="text-sm font-mono">{label}</span>
      <span className={`text-xs ${verified ? "text-green-600" : "text-gray-400"}`}>
        {verified ? "Verified" : "Pending"}
      </span>
    </div>
  );
}
