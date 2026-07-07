'use client';

import { useMemo, useState } from 'react';
import { CrmRecord, SkippedRecord } from '@/lib/types';
import { CheckCircle2, XCircle } from 'lucide-react';
import clsx from 'clsx';

interface ResultTableProps {
  records: CrmRecord[];
  skipped: SkippedRecord[];
  totalRows: number;
  warnings: string[];
}

const STATUS_STYLES: Record<string, string> = {
  GOOD_LEAD_FOLLOW_UP: 'bg-blue-100 text-blue-700 dark:bg-blue-900/40 dark:text-blue-300',
  DID_NOT_CONNECT: 'bg-yellow-100 text-yellow-700 dark:bg-yellow-900/40 dark:text-yellow-300',
  BAD_LEAD: 'bg-red-100 text-red-700 dark:bg-red-900/40 dark:text-red-300',
  SALE_DONE: 'bg-green-100 text-green-700 dark:bg-green-900/40 dark:text-green-300',
};

const CRM_COLUMNS: { key: keyof CrmRecord; label: string }[] = [
  { key: 'name', label: 'Name' },
  { key: 'email', label: 'Email' },
  { key: 'countryCode', label: 'Code' },
  { key: 'mobileWithoutCountryCode', label: 'Mobile' },
  { key: 'company', label: 'Company' },
  { key: 'city', label: 'City' },
  { key: 'state', label: 'State' },
  { key: 'country', label: 'Country' },
  { key: 'leadOwner', label: 'Owner' },
  { key: 'crmStatus', label: 'Status' },
  { key: 'dataSource', label: 'Source' },
  { key: 'crmNote', label: 'Notes' },
  { key: 'createdAt', label: 'Created At' },
];

export default function ResultTable({ records, skipped, totalRows, warnings }: ResultTableProps) {
  const [tab, setTab] = useState<'imported' | 'skipped'>('imported');

  const summary = useMemo(
    () => [
      { label: 'Total Rows', value: totalRows, tone: 'text-gray-700 dark:text-gray-200' },
      { label: 'Imported', value: records.length, tone: 'text-green-600 dark:text-green-400' },
      { label: 'Skipped', value: skipped.length, tone: 'text-red-500 dark:text-red-400' },
    ],
    [records.length, skipped.length, totalRows]
  );

  return (
    <div>
      <div className="mb-5 grid grid-cols-3 gap-3">
        {summary.map((s) => (
          <div key={s.label} className="rounded-xl border border-gray-200 dark:border-gray-800 p-4 text-center">
            <div className={clsx('text-2xl font-bold', s.tone)}>{s.value}</div>
            <div className="text-xs text-gray-500 dark:text-gray-400 mt-1">{s.label}</div>
          </div>
        ))}
      </div>

      {warnings.length > 0 && (
        <div className="mb-4 rounded-lg bg-amber-50 dark:bg-amber-950/40 border border-amber-200 dark:border-amber-900 px-4 py-3 text-sm text-amber-800 dark:text-amber-300">
          {warnings.map((w, i) => (
            <div key={i}>{w}</div>
          ))}
        </div>
      )}

      <div className="mb-3 flex gap-2">
        <TabButton active={tab === 'imported'} onClick={() => setTab('imported')} icon={<CheckCircle2 size={15} />}>
          Imported ({records.length})
        </TabButton>
        <TabButton active={tab === 'skipped'} onClick={() => setTab('skipped')} icon={<XCircle size={15} />}>
          Skipped ({skipped.length})
        </TabButton>
      </div>

      {tab === 'imported' ? (
        <div className="sticky-table-wrapper" style={{ maxHeight: 480 }}>
          <table className="w-full min-w-max border-collapse text-sm">
            <thead>
              <tr>
                {CRM_COLUMNS.map((c) => (
                  <th
                    key={c.key}
                    className="whitespace-nowrap border-b border-gray-200 dark:border-gray-800 px-4 py-2.5 text-left font-semibold text-gray-700 dark:text-gray-200"
                  >
                    {c.label}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {records.map((r, i) => (
                <tr key={i} className="border-b border-gray-100 dark:border-gray-900 hover:bg-gray-50 dark:hover:bg-gray-900/60">
                  {CRM_COLUMNS.map((c) => (
                    <td key={c.key} className="whitespace-nowrap px-4 py-2 text-gray-700 dark:text-gray-300">
                      {c.key === 'crmStatus' && r.crmStatus ? (
                        <span className={clsx('rounded-full px-2 py-0.5 text-xs font-medium', STATUS_STYLES[r.crmStatus])}>
                          {r.crmStatus}
                        </span>
                      ) : (
                        r[c.key] || <span className="text-gray-300 dark:text-gray-700">—</span>
                      )}
                    </td>
                  ))}
                </tr>
              ))}
              {records.length === 0 && (
                <tr>
                  <td colSpan={CRM_COLUMNS.length} className="px-4 py-8 text-center text-gray-400">
                    No records were imported.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      ) : (
        <div className="sticky-table-wrapper" style={{ maxHeight: 480 }}>
          <table className="w-full min-w-max border-collapse text-sm">
            <thead>
              <tr>
                <th className="whitespace-nowrap border-b border-gray-200 dark:border-gray-800 px-4 py-2.5 text-left font-semibold">Row #</th>
                <th className="whitespace-nowrap border-b border-gray-200 dark:border-gray-800 px-4 py-2.5 text-left font-semibold">Reason Skipped</th>
                <th className="whitespace-nowrap border-b border-gray-200 dark:border-gray-800 px-4 py-2.5 text-left font-semibold">Original Row (raw)</th>
              </tr>
            </thead>
            <tbody>
              {skipped.map((s) => (
                <tr key={s.rowIndex} className="border-b border-gray-100 dark:border-gray-900 hover:bg-gray-50 dark:hover:bg-gray-900/60">
                  <td className="px-4 py-2 text-gray-700 dark:text-gray-300">{s.rowIndex + 1}</td>
                  <td className="px-4 py-2 text-red-600 dark:text-red-400">{s.reason}</td>
                  <td className="px-4 py-2 text-gray-500 dark:text-gray-400 max-w-md truncate">
                    {Object.entries(s.originalRow || {})
                      .map(([k, v]) => `${k}: ${v}`)
                      .join(', ')}
                  </td>
                </tr>
              ))}
              {skipped.length === 0 && (
                <tr>
                  <td colSpan={3} className="px-4 py-8 text-center text-gray-400">
                    Nothing was skipped — great data!
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

function TabButton({
  active,
  onClick,
  icon,
  children,
}: {
  active: boolean;
  onClick: () => void;
  icon: React.ReactNode;
  children: React.ReactNode;
}) {
  return (
    <button
      onClick={onClick}
      className={clsx(
        'inline-flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-sm font-medium transition-colors',
        active
          ? 'bg-brand-500 text-white'
          : 'bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-700'
      )}
    >
      {icon}
      {children}
    </button>
  );
}
