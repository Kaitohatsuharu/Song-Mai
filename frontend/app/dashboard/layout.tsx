import Link from "next/link";
import { redirect } from "next/navigation";
import { getServerSession } from "next-auth";
import { authOptions } from "@/lib/auth";

const navItems = [
  { href: "/dashboard",          label: "Overview",    icon: "📊" },
  { href: "/dashboard/emails",   label: "Emails",      icon: "✉️" },
  { href: "/dashboard/domains",  label: "Domains",     icon: "🌐" },
  { href: "/dashboard/api-keys", label: "API Keys",    icon: "🔑" },
  { href: "/dashboard/webhooks", label: "Webhooks",    icon: "🔗" },
  { href: "/dashboard/verify",   label: "Verify",      icon: "✅" },
  { href: "/dashboard/billing",  label: "Billing",     icon: "💳" },
  { href: "/dashboard/settings", label: "Settings",    icon: "⚙️" },
];

export default async function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const session = await getServerSession(authOptions);
  if (!session) redirect("/login");

  return (
    <div className="flex h-screen bg-gray-50">
      {/* Sidebar */}
      <aside className="w-56 bg-white border-r border-gray-100 flex flex-col">
        <div className="px-5 py-5 border-b border-gray-100">
          <Link href="/" className="font-bold text-lg text-brand-600">
            HHSP Email
          </Link>
        </div>

        <nav className="flex-1 px-3 py-4 space-y-0.5">
          {navItems.map((item) => (
            <Link
              key={item.href}
              href={item.href}
              className="flex items-center gap-2.5 px-3 py-2 rounded-lg text-sm text-gray-600 hover:bg-gray-50 hover:text-gray-900 transition"
            >
              <span>{item.icon}</span>
              <span>{item.label}</span>
            </Link>
          ))}
        </nav>

        <div className="px-4 py-4 border-t border-gray-100">
          <p className="text-xs text-gray-400 truncate">{session.user?.email}</p>
        </div>
      </aside>

      {/* Main content */}
      <main className="flex-1 overflow-y-auto">{children}</main>
    </div>
  );
}
