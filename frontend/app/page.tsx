import Link from "next/link";

const plans = [
  {
    name: "Starter",
    price: "$9",
    emails: "10,000",
    verifications: "1,000",
    inbound: true,
    cta: "Get started",
    highlighted: false,
  },
  {
    name: "Growth",
    price: "$29",
    emails: "100,000",
    verifications: "10,000",
    inbound: true,
    cta: "Get started",
    highlighted: true,
  },
  {
    name: "Pro",
    price: "$79",
    emails: "500,000",
    verifications: "Unlimited",
    inbound: true,
    cta: "Contact us",
    highlighted: false,
  },
];

export default function LandingPage() {
  return (
    <main className="min-h-screen bg-white">
      {/* Nav */}
      <nav className="border-b border-gray-100 px-6 py-4 flex items-center justify-between max-w-6xl mx-auto">
        <span className="font-bold text-xl text-brand-600">HHSP Email</span>
        <div className="flex gap-4 items-center">
          <Link href="/login" className="text-sm text-gray-600 hover:text-gray-900">
            Sign in
          </Link>
          <Link
            href="/register"
            className="text-sm bg-brand-500 text-white px-4 py-2 rounded-lg hover:bg-brand-600 transition"
          >
            Start free
          </Link>
        </div>
      </nav>

      {/* Hero */}
      <section className="max-w-4xl mx-auto px-6 py-24 text-center">
        <h1 className="text-5xl font-extrabold text-gray-900 leading-tight mb-6">
          Transactional email infrastructure
          <br />
          <span className="text-brand-500">built for developers</span>
        </h1>
        <p className="text-xl text-gray-500 mb-10 max-w-2xl mx-auto">
          Send transactional emails, track replies, and verify email addresses —
          all through a simple REST API. Self-hosted on your own infrastructure.
        </p>
        <div className="flex gap-4 justify-center">
          <Link
            href="/register"
            className="bg-brand-500 text-white px-6 py-3 rounded-xl font-semibold hover:bg-brand-600 transition"
          >
            Start for free
          </Link>
          <a
            href="#pricing"
            className="border border-gray-200 text-gray-700 px-6 py-3 rounded-xl font-semibold hover:bg-gray-50 transition"
          >
            See pricing
          </a>
        </div>
      </section>

      {/* Features */}
      <section className="max-w-5xl mx-auto px-6 py-16 grid grid-cols-1 md:grid-cols-3 gap-8">
        {[
          {
            icon: "✉️",
            title: "Transactional API",
            desc: "Send emails with a single POST request. Track opens, clicks, and delivery status in real-time.",
          },
          {
            icon: "↩️",
            title: "Inbound Reply Tracking",
            desc: "Automatically capture replies to any email via catch-all routing on @customer.hhsp.dev.",
          },
          {
            icon: "✅",
            title: "Email Verification",
            desc: "Verify email addresses without sending — MX lookup, disposable detection, and SMTP handshake.",
          },
        ].map((f) => (
          <div key={f.title} className="card">
            <div className="text-3xl mb-3">{f.icon}</div>
            <h3 className="font-semibold text-lg mb-2">{f.title}</h3>
            <p className="text-gray-500 text-sm">{f.desc}</p>
          </div>
        ))}
      </section>

      {/* Code snippet */}
      <section className="max-w-3xl mx-auto px-6 py-12">
        <h2 className="text-2xl font-bold text-center mb-8">Simple API, powerful results</h2>
        <pre className="bg-gray-900 text-green-400 rounded-xl p-6 text-sm overflow-x-auto">
{`curl -X POST https://api.hhsp.dev/v1/email/send \\
  -H "X-API-Key: sk_live_xxxxxxxxxxxx" \\
  -H "Content-Type: application/json" \\
  -d '{
    "from": "hello@yourdomain.com",
    "to": "customer@example.com",
    "subject": "Welcome!",
    "html": "<h1>Welcome aboard!</h1>"
  }'`}
        </pre>
      </section>

      {/* Pricing */}
      <section id="pricing" className="max-w-5xl mx-auto px-6 py-20">
        <h2 className="text-3xl font-bold text-center mb-12">Simple, transparent pricing</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {plans.map((plan) => (
            <div
              key={plan.name}
              className={`rounded-2xl border p-8 flex flex-col ${
                plan.highlighted
                  ? "border-brand-500 shadow-lg shadow-brand-100 bg-brand-50"
                  : "border-gray-200 bg-white"
              }`}
            >
              {plan.highlighted && (
                <span className="text-xs font-semibold text-brand-600 uppercase tracking-wide mb-2">
                  Most popular
                </span>
              )}
              <h3 className="text-xl font-bold mb-1">{plan.name}</h3>
              <div className="text-4xl font-extrabold mb-6">
                {plan.price}
                <span className="text-base font-normal text-gray-500">/mo</span>
              </div>
              <ul className="space-y-3 text-sm text-gray-600 flex-1 mb-8">
                <li>📧 {plan.emails} emails/month</li>
                <li>✅ {plan.verifications} verifications</li>
                <li>↩️ Inbound reply tracking</li>
                <li>🔗 Webhooks</li>
              </ul>
              <Link
                href="/register"
                className={`text-center py-3 rounded-xl font-semibold transition ${
                  plan.highlighted
                    ? "bg-brand-500 text-white hover:bg-brand-600"
                    : "border border-gray-200 hover:bg-gray-50"
                }`}
              >
                {plan.cta}
              </Link>
            </div>
          ))}
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-gray-100 py-8 text-center text-sm text-gray-400">
        © {new Date().getFullYear()} HHSP Email. Built with ❤️ on Hetzner + Mac Mini.
      </footer>
    </main>
  );
}
