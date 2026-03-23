import { getServerSession } from "next-auth";
import { authOptions } from "@/lib/auth";
import { apiClient } from "@/lib/api";

interface EmailDetail {
  id: string;
  short_id: string;
  from: string;
  to: string;
  subject: string;
  status: string;
  created_at: string;
  sent_at?: string;
  opened_at?: string;
  clicked_at?: string;
  events: Array<{
    id: string;
    event_type: string;
    occurred_at: string;
    metadata: Record<string, string>;
  }>;
}

async function getEmail(token: string, id: string): Promise<EmailDetail | null> {
  try {
    const res = await apiClient.get<EmailDetail>(`/v1/email/${id}`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    return res.data;
  } catch {
    return null;
  }
}

export default async function EmailDetailPage({ params }: { params: { id: string } }) {
  const session = await getServerSession(authOptions);
  const email = session?.accessToken
    ? await getEmail(session.accessToken as string, params.id)
    : null;

  if (!email) {
    return (
      <div className="p-8">
        <p className="text-gray-500">Email not found.</p>
      </div>
    );
  }

  const fields = [
    ["ID",         email.id],
    ["Short ID",   email.short_id],
    ["From",       email.from],
    ["To",         email.to],
    ["Subject",    email.subject],
    ["Status",     email.status],
    ["Created",    new Date(email.created_at).toLocaleString()],
    email.opened_at ? ["Opened",  new Date(email.opened_at).toLocaleString()] : null,
    email.clicked_at ? ["Clicked", new Date(email.clicked_at).toLocaleString()] : null,
  ].filter(Boolean) as [string, string][];

  return (
    <div className="p-8 max-w-3xl">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Email Detail</h1>

      <div className="card mb-8">
        <dl className="divide-y divide-gray-50">
          {fields.map(([label, value]) => (
            <div key={label} className="flex py-3 gap-4">
              <dt className="text-sm font-medium text-gray-500 w-28 shrink-0">{label}</dt>
              <dd className="text-sm text-gray-900 font-mono break-all">{value}</dd>
            </div>
          ))}
        </dl>
      </div>

      <h2 className="text-lg font-semibold mb-4">Event Timeline</h2>
      <div className="space-y-3">
        {email.events?.map((evt) => (
          <div key={evt.id} className="flex gap-4 items-start">
            <span className="mt-0.5 w-2 h-2 rounded-full bg-brand-500 shrink-0" />
            <div>
              <p className="text-sm font-medium text-gray-900">{evt.event_type}</p>
              <p className="text-xs text-gray-400">{new Date(evt.occurred_at).toLocaleString()}</p>
              {Object.entries(evt.metadata ?? {}).map(([k, v]) => (
                <p key={k} className="text-xs text-gray-500">{k}: {v}</p>
              ))}
            </div>
          </div>
        ))}
        {!email.events?.length && (
          <p className="text-sm text-gray-400">No events yet.</p>
        )}
      </div>
    </div>
  );
}
