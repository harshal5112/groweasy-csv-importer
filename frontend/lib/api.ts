import { ImportResponse } from './types';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';

export class ApiError extends Error {
  status?: number;
  constructor(message: string, status?: number) {
    super(message);
    this.status = status;
  }
}

/**
 * Sends the raw CSV file to the backend for AI-powered CRM extraction.
 * The backend re-parses the file itself (never trusts the client alone),
 * batches rows, and calls the AI model to map arbitrary columns.
 */
export async function importCsv(file: File): Promise<ImportResponse> {
  const formData = new FormData();
  formData.append('file', file);

  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}/api/csv/import`, {
      method: 'POST',
      body: formData,
    });
  } catch (err) {
    throw new ApiError(
      'Could not reach the import server. Please check your connection and that the backend is running.'
    );
  }

  if (!response.ok) {
    let message = `Import failed with status ${response.status}.`;
    try {
      const body = await response.json();
      if (body?.message) message = body.message;
    } catch {
      // ignore JSON parse errors on error body
    }
    throw new ApiError(message, response.status);
  }

  return (await response.json()) as ImportResponse;
}
