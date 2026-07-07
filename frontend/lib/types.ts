export interface CrmRecord {
  createdAt: string;
  name: string;
  email: string;
  countryCode: string;
  mobileWithoutCountryCode: string;
  company: string;
  city: string;
  state: string;
  country: string;
  leadOwner: string;
  crmStatus: string;
  crmNote: string;
  dataSource: string;
  possessionTime: string;
  description: string;
}

export interface SkippedRecord {
  rowIndex: number;
  reason: string;
  originalRow: Record<string, string>;
}

export interface ImportResponse {
  totalRows: number;
  totalImported: number;
  totalSkipped: number;
  records: CrmRecord[];
  skipped: SkippedRecord[];
  warnings: string[];
}

export type AppStep = 'upload' | 'preview' | 'processing' | 'result';

export interface ParsedCsv {
  headers: string[];
  rows: Record<string, string>[];
  fileName: string;
  fileSizeBytes: number;
}
