"use client";

import { useState } from "react";

export default function SettingsPage() {
  const [name, setName] = useState("");
  const [saved, setSaved] = useState(false);

  async function handleSave(e: React.FormEvent) {
    e.preventDefault();
    // TODO: POST to /api/dashboard/account
    setSaved(true);
    setTimeout(() => setSaved(false), 3000);
  }

  return (
    <div className="p-8 max-w-xl">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Account Settings</h1>

      <form onSubmit={handleSave} className="card space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Display name</label>
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
            placeholder="Your name"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Email address</label>
          <input
            type="email"
            disabled
            className="w-full border border-gray-200 bg-gray-50 rounded-lg px-3 py-2 text-sm text-gray-400 cursor-not-allowed"
            placeholder="your@email.com"
          />
          <p className="text-xs text-gray-400 mt-1">Email cannot be changed.</p>
        </div>

        <div className="flex items-center gap-3">
          <button
            type="submit"
            className="bg-brand-500 text-white px-5 py-2 rounded-lg text-sm font-semibold hover:bg-brand-600"
          >
            Save changes
          </button>
          {saved && (
            <span className="text-sm text-green-600">Saved!</span>
          )}
        </div>
      </form>

      <div className="mt-8 card border-red-200">
        <h2 className="font-semibold text-red-700 mb-2">Danger Zone</h2>
        <p className="text-sm text-gray-500 mb-4">
          Permanently delete your account and all data. This cannot be undone.
        </p>
        <button className="border border-red-200 text-red-600 px-4 py-2 rounded-lg text-sm hover:bg-red-50 transition">
          Delete account
        </button>
      </div>
    </div>
  );
}
