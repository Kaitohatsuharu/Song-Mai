import { getServerSession } from "next-auth";
import { authOptions } from "@/lib/auth";
import { apiClient } from "@/lib/api";
import Link from "next/link";

interface Message {
  id: string;
  short_id: string;
  from_address: string;
  to_address: string;
  subject: string;
  status: string;
  created_at: string;
}

interface PagedMessages {
  content: Message[];
  totalElements: number;
  totalPages: number;
  number: number;
}

async function getEmails(token: string, page = 0): Promise<PagedMessages | null> {
  try {
    const res = await apiClient.get<PagedMessages>(
      `/api/dashboard/emails?page=${page}&size=20`,
      { headers: { Authorization: `Bearer ${token}` } }
    );
    return res.data;
  } catch {
    return null;
  }
}

const statusBadge: Record<string, string> = {
  queued:    "badge-gray",
  sent:      "badge-yellow",
  delivered: "badge-green",
  bounced:   "badge-red",
  failed:    "badge-red",
  opened:    "badge-green",
};

export default async function EmailsPage() {
  const session = await getServerSession(authOptions);
  const data = session?.accessToken
    ? await getEmails(session.accessToken as string)
    : null;

  return (
    <div className="p-8">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Email Logs</h1>

      <div className="card overflow-hidden p-0">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 border-b border-gray-100">
            <tr>
              {["To", "Subject", "Status", "Sent At", ""].map((h) => (
                <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wide">
                  {h}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-50">
            {data?.content?.map((msg) => (
              <tr key={msg.id} className="hover:bg-gray-50 transition">
                <td className="px-4 py-3 text-gray-700">{msg.to_address}</td>
                <td className="px-4 py-3 text-gray-900 max-w-xs truncate">{msg.subject}</td>
                <td className="px-4 py-3">
                  <span className={statusBadge[msg.status] ?? "badge-gray"}>{msg.status}</span>
                </td>
                <td className="px-4 py-3 text-gray-400 text-xs">
                  {new Date(msg.created_at).toLocaleString()}
                </td>
                <td className="px-4 py-3">
                  <Link href={`/dashboard/emails/${msg.id}`} className="text-brand-500 hover:underline text-xs">
                    View
                  </Link>
                </td>
              </tr>
            ))}
            {!data?.content?.length && (
              <tr>
                <td colSpan={5} className="px-4 py-12 text-center text-gray-400">
                  No emails yet. Send your first email via the API.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
