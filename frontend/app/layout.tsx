import type { Metadata } from 'next';
import './globals.css';

export const metadata: Metadata = {
  title: 'GrowEasy CRM CSV Importer',
  description: 'AI-powered CSV importer that maps any CSV format into GrowEasy CRM records.',
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en" suppressHydrationWarning>
      <head>
        {/* Prevent flash of wrong theme before hydration */}
        <script
          dangerouslySetInnerHTML={{
            __html: `
              try {
                const stored = localStorage.getItem('theme');
                const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
                if (stored === 'dark' || (!stored && prefersDark)) {
                  document.documentElement.classList.add('dark');
                }
              } catch (e) {}
            `,
          }}
        />
      </head>
      <body>{children}</body>
    </html>
  );
}
