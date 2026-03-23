import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "HHSP Email — Transactional Email Infrastructure",
  description:
    "Developer-first transactional email API with inbound reply tracking and email verification.",
};

export default function RootLayout({
  children,
}: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
