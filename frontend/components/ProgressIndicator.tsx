'use client';

import { useEffect, useState } from 'react';
import { Sparkles } from 'lucide-react';

const MESSAGES = [
  'Uploading your CSV…',
  'Detecting column structure…',
  'Mapping fields with AI…',
  'Validating CRM status & source values…',
  'Wrapping up your import…',
];

export default function ProgressIndicator() {
  const [messageIndex, setMessageIndex] = useState(0);
  const [progress, setProgress] = useState(6);

  useEffect(() => {
    const messageTimer = setInterval(() => {
      setMessageIndex((i) => Math.min(i + 1, MESSAGES.length - 1));
    }, 1800);

    // Progress bar creeps forward but never claims 100% until the real response arrives
    const progressTimer = setInterval(() => {
      setProgress((p) => (p < 90 ? p + (90 - p) * 0.08 + 0.5 : p));
    }, 250);

    return () => {
      clearInterval(messageTimer);
      clearInterval(progressTimer);
    };
  }, []);

  return (
    <div className="flex flex-col items-center justify-center gap-5 py-16">
      <div className="rounded-full bg-brand-100 dark:bg-brand-900/40 p-4 animate-pulse">
        <Sparkles className="h-8 w-8 text-brand-600 dark:text-brand-400" />
      </div>
      <div className="w-full max-w-sm">
        <div className="h-2 w-full overflow-hidden rounded-full bg-gray-200 dark:bg-gray-800">
          <div
            className="h-full rounded-full bg-brand-500 transition-all duration-300 ease-out"
            style={{ width: `${Math.min(progress, 95)}%` }}
          />
        </div>
      </div>
      <p className="text-sm font-medium text-gray-600 dark:text-gray-300">{MESSAGES[messageIndex]}</p>
      <p className="text-xs text-gray-400 dark:text-gray-500">This can take a few seconds for larger files.</p>
    </div>
  );
}
