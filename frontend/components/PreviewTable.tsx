'use client';

import { useRef } from 'react';
import { useVirtualizer } from '@tanstack/react-virtual';

interface PreviewTableProps {
  headers: string[];
  rows: Record<string, string>[];
}

const ROW_HEIGHT = 40;

export default function PreviewTable({ headers, rows }: PreviewTableProps) {
  const parentRef = useRef<HTMLDivElement>(null);

  const rowVirtualizer = useVirtualizer({
    count: rows.length,
    getScrollElement: () => parentRef.current,
    estimateSize: () => ROW_HEIGHT,
    overscan: 10,
  });

  return (
    <div>
      <div className="mb-2 flex items-center justify-between text-sm text-gray-500 dark:text-gray-400">
        <span>{rows.length} row{rows.length !== 1 ? 's' : ''} detected</span>
        <span>{headers.length} column{headers.length !== 1 ? 's' : ''}</span>
      </div>
      <div ref={parentRef} className="sticky-table-wrapper" style={{ maxHeight: 420 }}>
        <table className="w-full min-w-max border-collapse text-sm">
          <thead>
            <tr>
              {headers.map((h) => (
                <th
                  key={h}
                  className="whitespace-nowrap border-b border-gray-200 dark:border-gray-800 px-4 py-2.5 text-left font-semibold text-gray-700 dark:text-gray-200"
                >
                  {h || <span className="italic text-gray-400">(blank)</span>}
                </th>
              ))}
            </tr>
          </thead>
          <tbody style={{ height: rowVirtualizer.getTotalSize(), position: 'relative', display: 'block' }}>
            {rowVirtualizer.getVirtualItems().map((virtualRow) => {
              const row = rows[virtualRow.index];
              return (
                <tr
                  key={virtualRow.key}
                  style={{
                    position: 'absolute',
                    top: 0,
                    left: 0,
                    width: '100%',
                    height: ROW_HEIGHT,
                    transform: `translateY(${virtualRow.start}px)`,
                    display: 'flex',
                  }}
                  className="border-b border-gray-100 dark:border-gray-900 hover:bg-gray-50 dark:hover:bg-gray-900/60"
                >
                  {headers.map((h) => (
                    <td
                      key={h}
                      className="flex-1 whitespace-nowrap px-4 py-2 text-gray-700 dark:text-gray-300 flex items-center"
                      style={{ minWidth: 160 }}
                    >
                      {row[h] || <span className="text-gray-300 dark:text-gray-700">—</span>}
                    </td>
                  ))}
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}
