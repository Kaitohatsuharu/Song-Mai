import { getServerSession } from "next-auth";
import { authOptions } from "@/lib/auth";
import { apiClient } from "@/lib/api";

interface Overview {
  plan: string;
  quota_monthly: number;
  quota_used: number;
  total_messages: number;
}

async function getOverview(token: string): Promise<Overview | null> {
  try {
    const res = await apiClient.get<Overview>("/api/dashboard/overview", {
      headers: { Authorization: `Bearer ${token}` },
    });
    return res.data;
  } catch {
    return null;
  }
}

export default async function DashboardPage() {
  const session = await getServerSession(authOptions);
  const overview = session?.accessToken
    ? await getOverview(session.accessToken as string)
    : null;

  const usedPct = overview
    ? Math.round((overview.quota_used / overview.quota_monthly) * 100)
    : 0;

  return (
    <div className="p-8">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Overview</h1>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <StatCard
          label="Emails sent this month"
          value={overview?.quota_used?.toLocaleString() ?? "—"}
          sub={`of ${overview?.quota_monthly?.toLocaleString() ?? "—"} quota`}
        />
        <StatCard
          label="Total messages"
          value={overview?.total_messages?.toLocaleString() ?? "—"}
          sub="all time"
        />
        <StatCard
          label="Current plan"
          value={capitalize(overview?.plan ?? "—")}
          sub={<a href="/dashboard/billing" className="text-brand-500 text-xs hover:underline">Upgrade</a>}
        />
      </div>

      {/* Quota progress */}
      {overview && (
        <div className="card">
          <div className="flex justify-between mb-2">
            <span className="text-sm font-medium text-gray-700">Monthly email quota</span>
            <span className="text-sm text-gray-500">{usedPct}% used</span>
          </div>
          <div className="h-2 bg-gray-100 rounded-full overflow-hidden">
            <div
              className={`h-full rounded-full transition-all ${
                usedPct > 90 ? "bg-red-500" : usedPct > 70 ? "bg-yellow-500" : "bg-brand-500"
              }`}
              style={{ width: `${Math.min(usedPct, 100)}%` }}
            />
          </div>
        </div>
      )}
    </div>
  );
}

function StatCard({
  label,
  value,
  sub,
}: {
  label: string;
  value: string;
  sub: React.ReactNode;
}) {
  return (
    <div className="card">
      <p className="text-sm text-gray-500 mb-1">{label}</p>
      <p className="text-3xl font-bold text-gray-900">{value}</p>
      <p className="text-xs text-gray-400 mt-1">{sub}</p>
    </div>
  );
}

function capitalize(s: string) {
  return s.charAt(0).toUpperCase() + s.slice(1);
}
