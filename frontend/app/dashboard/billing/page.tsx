export default function BillingPage() {
  const plans = [
    {
      name: "Starter",
      price: "$9/mo",
      emails: "10,000",
      verifications: "1,000",
      current: true,
    },
    {
      name: "Growth",
      price: "$29/mo",
      emails: "100,000",
      verifications: "10,000",
      current: false,
    },
    {
      name: "Pro",
      price: "$79/mo",
      emails: "500,000",
      verifications: "Unlimited",
      current: false,
    },
  ];

  return (
    <div className="p-8 max-w-4xl">
      <h1 className="text-2xl font-bold text-gray-900 mb-2">Billing</h1>
      <p className="text-gray-500 text-sm mb-8">Manage your subscription and usage.</p>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        {plans.map((plan) => (
          <div
            key={plan.name}
            className={`card ${plan.current ? "ring-2 ring-brand-500" : ""}`}
          >
            {plan.current && (
              <span className="text-xs font-semibold text-brand-600 uppercase mb-1 block">
                Current plan
              </span>
            )}
            <h3 className="text-lg font-bold mb-1">{plan.name}</h3>
            <p className="text-2xl font-extrabold text-gray-900 mb-4">{plan.price}</p>
            <ul className="text-sm text-gray-600 space-y-2 mb-6">
              <li>📧 {plan.emails} emails/month</li>
              <li>✅ {plan.verifications} verifications</li>
              <li>↩️ Inbound reply tracking</li>
            </ul>
            {!plan.current && (
              <button className="w-full py-2 bg-brand-500 text-white rounded-lg text-sm font-semibold hover:bg-brand-600">
                Upgrade
              </button>
            )}
          </div>
        ))}
      </div>

      <div className="card">
        <h2 className="font-semibold text-gray-900 mb-4">Billing Portal</h2>
        <p className="text-sm text-gray-500 mb-4">
          Manage your payment method, download invoices, or cancel your subscription via Stripe.
        </p>
        <button className="border border-gray-200 text-gray-700 px-4 py-2 rounded-lg text-sm hover:bg-gray-50 transition">
          Open Stripe portal →
        </button>
      </div>
    </div>
  );
}
