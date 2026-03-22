"use client";

import { useState, useEffect } from "react";
import axios from "axios";

interface WebhookEndpoint {
  id: string;
  url: string;
  events: string[];
  active: boolean;
  created_at: string;
}

const ALL_EVENTS = [
  "email.sent",
  "email.delivered",
  "email.opened",
  "email.clicked",
  "email.bounced",
  "email.complained",
  "email.replied",
];

export default function WebhooksPage() {
  const [webhooks, setWebhooks] = useState<WebhookEndpoint[]>([]);
  const [url, setUrl] = useState("");
  const [selectedEvents, setSelectedEvents] = useState<string[]>(ALL_EVENTS);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    axios.get<WebhookEndpoint[]>("/api/dashboard/webhooks").then((r) => setWebhooks(r.data));
  }, []);

  function toggleEvent(event: string) {
    setSelectedEvents((prev) =>
      prev.includes(event) ? prev.filter((e) => e !== event) : [...prev, event]
    );
  }

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await axios.post<WebhookEndpoint>("/api/dashboard/webhooks", {
        url,
        events: selectedEvents,
      });
      setWebhooks((prev) => [...prev, res.data]);
      setUrl("");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="p-8 max-w-3xl">
      <h1 className="text-2xl font-bold text-gray-900 mb-2">Webhooks</h1>
      <p className="text-gray-500 text-sm mb-8">
        Receive real-time notifications for email events.
      </p>

      <form onSubmit={handleCreate} className="card mb-8 space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Endpoint URL</label>
          <input
            type="url"
            value={url}
            onChange={(e) => setUrl(e.target.value)}
            required
            placeholder="https://yourapp.com/webhooks/hhsp"
            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Events</label>
          <div className="flex flex-wrap gap-2">
            {ALL_EVENTS.map((evt) => (
              <button
                key={evt}
                type="button"
                onClick={() => toggleEvent(evt)}
                className={`text-xs px-3 py-1 rounded-full border transition ${
                  selectedEvents.includes(evt)
                    ? "bg-brand-500 text-white border-brand-500"
                    : "bg-white text-gray-600 border-gray-200 hover:border-brand-300"
                }`}
              >
                {evt}
              </button>
            ))}
          </div>
        </div>

        <button
          type="submit"
          disabled={loading}
          className="bg-brand-500 text-white px-5 py-2 rounded-lg text-sm font-semibold hover:bg-brand-600 disabled:opacity-60"
        >
          {loading ? "Creating…" : "Create webhook"}
        </button>
      </form>

      <div className="space-y-4">
        {webhooks.map((wh) => (
          <div key={wh.id} className="card">
            <div className="flex items-start justify-between mb-2">
              <span className="font-mono text-sm text-gray-800 break-all">{wh.url}</span>
              <span className={wh.active ? "badge-green" : "badge-gray"}>
                {wh.active ? "Active" : "Inactive"}
              </span>
            </div>
            <div className="flex flex-wrap gap-1.5">
              {wh.events?.map((evt) => (
                <span key={evt} className="badge-gray">{evt}</span>
              ))}
            </div>
          </div>
        ))}
        {!webhooks.length && (
          <div className="card text-center py-10 text-gray-400">No webhooks configured yet.</div>
        )}
      </div>
    </div>
  );
}
