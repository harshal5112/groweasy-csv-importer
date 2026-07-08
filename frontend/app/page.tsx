'use client';

import { useCallback, useState } from 'react';
import Papa from 'papaparse';
import { AlertTriangle, ArrowLeft, RefreshCcw } from 'lucide-react';
import FileDropzone from '@/components/FileDropzone';
import PreviewTable from '@/components/PreviewTable';
import ResultTable from '@/components/ResultTable';
import ProgressIndicator from '@/components/ProgressIndicator';
import ThemeToggle from '@/components/ThemeToggle';
import { importCsv, ApiError } from '@/lib/api';
import { AppStep, ImportResponse, ParsedCsv } from '@/lib/types';

export default function Home() {
  const [step, setStep] = useState<AppStep>('upload');
  const [file, setFile] = useState<File | null>(null);
  const [parsed, setParsed] = useState<ParsedCsv | null>(null);
  const [result, setResult] = useState<ImportResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  const handleFileSelected = useCallback((selectedFile: File) => {
    setError(null);
    setFile(selectedFile);

    // Client-side parse for preview ONLY - no AI processing happens here.
    Papa.parse(selectedFile, {
      header: true,
      skipEmptyLines: true,
      complete: (results) => {
        const headers = (results.meta.fields || []).filter((h) => h && h.trim().length > 0);
        if (headers.length === 0) {
          setError('Could not detect a header row in this CSV.');
          return;
        }
        setParsed({
          headers,
          rows: results.data as Record<string, string>[],
          fileName: selectedFile.name,
          fileSizeBytes: selectedFile.size,
        });
        setStep('preview');
      },
      error: (err) => {
        setError(`Failed to parse CSV: ${err.message}`);
      },
    });
  }, []);

  const handleConfirm = useCallback(async () => {
    if (!file) return;
    setStep('processing');
    setError(null);
    try {
      const response = await importCsv(file);
      setResult(response);
      setStep('result');
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Something went wrong during import.';
      setError(message);
      setStep('preview');
    }
  }, [file]);

  const handleReset = () => {
    setStep('upload');
    setFile(null);
    setParsed(null);
    setResult(null);
    setError(null);
  };

  return (
    <main className="mx-auto min-h-screen max-w-5xl px-4 py-10 sm:px-6 lg:px-8">
      <header className="mb-8 flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">GrowEasy CRM CSV Importer</h1>
          <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
            Upload any CSV export — Facebook, Google Ads, Excel, or a custom sheet — and let AI map it into GrowEasy's CRM format.
          </p>
        </div>
        <ThemeToggle />
      </header>

      <StepIndicator step={step} />

      <section className="mt-8 rounded-2xl border border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-900/40 p-6 sm:p-8 shadow-sm">
        {step === 'upload' && (
          <>
            <FileDropzone onFileSelected={handleFileSelected} error={error} />
            <div className="mt-4 text-center">
              <a
                href="/sample-crm-template.csv"
                download
                className="text-sm font-medium text-brand-600 dark:text-brand-400 hover:underline"
              >
                Download Sample CSV Template
              </a>
            </div>
          </>
        )}

        {step === 'preview' && parsed && (
          <div className="animate-fade-in">
            <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
              <div>
                <h2 className="font-semibold">Preview: {parsed.fileName}</h2>
                <p className="text-xs text-gray-500 dark:text-gray-400">
                  Review your data below. No AI processing has happened yet.
                </p>
              </div>
              <div className="flex gap-2">
                <button
                  onClick={handleReset}
                  className="inline-flex items-center gap-1.5 rounded-lg border border-gray-300 dark:border-gray-700 px-3 py-1.5 text-sm font-medium text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-800"
                >
                  <ArrowLeft size={15} /> Choose different file
                </button>
                <button
                  onClick={handleConfirm}
                  className="inline-flex items-center gap-1.5 rounded-lg bg-brand-500 px-4 py-1.5 text-sm font-semibold text-white hover:bg-brand-600 transition-colors"
                >
                  Confirm Import
                </button>
              </div>
            </div>

            {error && (
              <div className="mb-4 flex items-center gap-2 rounded-lg bg-red-50 dark:bg-red-950/40 px-4 py-2 text-sm text-red-700 dark:text-red-400">
                <AlertTriangle size={16} />
                {error}
              </div>
            )}

            <PreviewTable headers={parsed.headers} rows={parsed.rows} />
          </div>
        )}

        {step === 'processing' && <ProgressIndicator />}

        {step === 'result' && result && (
          <div className="animate-fade-in">
            <div className="mb-4 flex items-center justify-between">
              <h2 className="font-semibold">Import Complete</h2>
              <button
                onClick={handleReset}
                className="inline-flex items-center gap-1.5 rounded-lg border border-gray-300 dark:border-gray-700 px-3 py-1.5 text-sm font-medium text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-800"
              >
                <RefreshCcw size={15} /> Import another file
              </button>
            </div>
            <ResultTable
              records={result.records}
              skipped={result.skipped}
              totalRows={result.totalRows}
              warnings={result.warnings}
            />
          </div>
        )}
      </section>

      <footer className="mt-8 text-center text-xs text-gray-400 dark:text-gray-600">
        Built for the GrowEasy Software Developer assignment · Frontend: Next.js · Backend: Spring Boot · AI: Gemini
      </footer>
    </main>
  );
}

function StepIndicator({ step }: { step: AppStep }) {
  const steps: { key: AppStep; label: string }[] = [
    { key: 'upload', label: 'Upload' },
    { key: 'preview', label: 'Preview' },
    { key: 'processing', label: 'Processing' },
    { key: 'result', label: 'Result' },
  ];
  const currentIndex = steps.findIndex((s) => s.key === step);

  return (
    <div className="flex items-center gap-2">
      {steps.map((s, i) => (
        <div key={s.key} className="flex items-center gap-2 flex-1">
          <div
            className={`flex h-7 w-7 shrink-0 items-center justify-center rounded-full text-xs font-semibold transition-colors ${
              i <= currentIndex
                ? 'bg-brand-500 text-white'
                : 'bg-gray-200 dark:bg-gray-800 text-gray-400 dark:text-gray-500'
            }`}
          >
            {i + 1}
          </div>
          <span
            className={`hidden sm:inline text-sm font-medium ${
              i <= currentIndex ? 'text-gray-800 dark:text-gray-200' : 'text-gray-400 dark:text-gray-600'
            }`}
          >
            {s.label}
          </span>
          {i < steps.length - 1 && (
            <div className={`h-px flex-1 ${i < currentIndex ? 'bg-brand-400' : 'bg-gray-200 dark:bg-gray-800'}`} />
          )}
        </div>
      ))}
    </div>
  );
}
