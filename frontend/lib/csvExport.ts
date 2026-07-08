import { CrmRecord } from './types';

const CRM_HEADERS: { key: keyof CrmRecord; header: string }[] = [
  { key: 'createdAt', header: 'created_at' },
  { key: 'name', header: 'name' },
  { key: 'email', header: 'email' },
  { key: 'countryCode', header: 'country_code' },
  { key: 'mobileWithoutCountryCode', header: 'mobile_without_country_code' },
  { key: 'company', header: 'company' },
  { key: 'city', header: 'city' },
  { key: 'state', header: 'state' },
  { key: 'country', header: 'country' },
  { key: 'leadOwner', header: 'lead_owner' },
  { key: 'crmStatus', header: 'crm_status' },
  { key: 'crmNote', header: 'crm_note' },
  { key: 'dataSource', header: 'data_source' },
  { key: 'possessionTime', header: 'possession_time' },
  { key: 'description', header: 'description' },
];

/** Escapes a single CSV field per RFC 4180 - quotes it if it contains a comma, quote, or newline. */
function escapeCsvField(value: string | undefined | null): string {
  const v = value ?? '';
  if (v.includes(',') || v.includes('"') || v.includes('\n') || v.includes('\r')) {
    return `"${v.replace(/"/g, '""')}"`;
  }
  return v;
}

export function recordsToCsv(records: CrmRecord[]): string {
  const headerLine = CRM_HEADERS.map((h) => h.header).join(',');
  const lines = records.map((r) =>
    CRM_HEADERS.map((h) => escapeCsvField(r[h.key] as string)).join(',')
  );
  return [headerLine, ...lines].join('\r\n');
}

export function downloadCrmCsv(records: CrmRecord[], fileName = 'groweasy_crm_import.csv') {
  const csv = recordsToCsv(records);
  const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
}
