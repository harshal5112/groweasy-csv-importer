'use client';

import { useCallback, useRef, useState } from 'react';
import { UploadCloud, FileWarning } from 'lucide-react';
import clsx from 'clsx';

interface FileDropzoneProps {
  onFileSelected: (file: File) => void;
  error?: string | null;
}

const MAX_SIZE_BYTES = 5 * 1024 * 1024; // 5MB, matches backend limit

export default function FileDropzone({ onFileSelected, error }: FileDropzoneProps) {
  const [isDragging, setIsDragging] = useState(false);
  const [localError, setLocalError] = useState<string | null>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  const validateAndEmit = useCallback(
    (file: File | undefined) => {
      if (!file) return;
      if (!file.name.toLowerCase().endsWith('.csv')) {
        setLocalError('Only .csv files are supported.');
        return;
      }
      if (file.size > MAX_SIZE_BYTES) {
        setLocalError('File is too large. Maximum size is 5MB.');
        return;
      }
      setLocalError(null);
      onFileSelected(file);
    },
    [onFileSelected]
  );

  const handleDrop = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(false);
    validateAndEmit(e.dataTransfer.files?.[0]);
  };

  const displayedError = error ?? localError;

  return (
    <div className="w-full">
      <div
        onDragOver={(e) => {
          e.preventDefault();
          setIsDragging(true);
        }}
        onDragLeave={() => setIsDragging(false)}
        onDrop={handleDrop}
        onClick={() => inputRef.current?.click()}
        role="button"
        tabIndex={0}
        onKeyDown={(e) => {
          if (e.key === 'Enter' || e.key === ' ') inputRef.current?.click();
        }}
        className={clsx(
          'flex flex-col items-center justify-center gap-3 rounded-2xl border-2 border-dashed p-12 text-center cursor-pointer transition-all duration-200',
          isDragging
            ? 'border-brand-500 bg-brand-50 dark:bg-brand-900/20 scale-[1.01]'
            : 'border-gray-300 dark:border-gray-700 hover:border-brand-400 hover:bg-gray-50 dark:hover:bg-gray-900'
        )}
      >
        <div className={clsx('rounded-full p-4 transition-colors', isDragging ? 'bg-brand-100 dark:bg-brand-900/40' : 'bg-gray-100 dark:bg-gray-800')}>
          <UploadCloud className={clsx('h-8 w-8', isDragging ? 'text-brand-600' : 'text-gray-500 dark:text-gray-400')} />
        </div>
        <div>
          <p className="text-base font-medium">Drop your CSV file here</p>
          <p className="text-sm text-gray-500 dark:text-gray-400">or click to browse files</p>
        </div>
        <p className="text-xs text-gray-400 dark:text-gray-500">Supported file: .csv (max 5MB)</p>
        <input
          ref={inputRef}
          type="file"
          accept=".csv"
          className="hidden"
          onChange={(e) => validateAndEmit(e.target.files?.[0])}
        />
      </div>

      {displayedError && (
        <div className="mt-3 flex items-center gap-2 rounded-lg bg-red-50 dark:bg-red-950/40 px-4 py-2 text-sm text-red-700 dark:text-red-400 animate-fade-in">
          <FileWarning size={16} />
          {displayedError}
        </div>
      )}
    </div>
  );
}
