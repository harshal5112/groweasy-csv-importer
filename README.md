# GrowEasy CRM CSV Importer

AI-powered CSV importer built for the GrowEasy Software Developer assignment.
Upload a CSV in **any layout** (Facebook Lead Ads, Google Ads, Excel exports,
other CRMs, manual sheets...) and the app uses **Gemini AI** to intelligently
map whatever columns exist into GrowEasy's fixed CRM schema.

> **Tech stack note:** The assignment's suggested backend stack was Node.js +
> Express. This implementation uses **Spring Boot (Java)** instead, since
> that's the candidate's strongest stack — everything else (functional
> requirements, AI mapping rules, API contract) follows the assignment
> exactly.

---

## Architecture

```
┌─────────────────┐        multipart/form-data        ┌──────────────────────┐        REST + JSON        ┌─────────────┐
│   Next.js SPA    │ ───────────────────────────────▶ │  Spring Boot API     │ ─────────────────────────▶ │  Gemini AI   │
│  (Vercel, free)  │ ◀─────────────────────────────── │  (Render, free)      │ ◀───────────────────────── │ (free tier)  │
└─────────────────┘        structured JSON result       └──────────────────────┘                            └─────────────┘
```

- **Frontend (Next.js + TypeScript + Tailwind):** drag-and-drop upload,
  client-side CSV preview (PapaParse) with a virtualized, sticky-header
  table, a confirm step, and a results view with import/skip breakdown.
  Includes dark mode and an animated progress indicator.
- **Backend (Spring Boot):** stateless REST API. Parses the CSV with
  Apache Commons CSV (no fixed column assumption), batches rows, calls
  Gemini with a strict JSON response schema, validates/sanitizes the AI's
  output against the CRM enum rules, and retries failed batches with
  exponential backoff.
- **No database** — everything is processed in memory per request, exactly
  like a stateless import pipeline should be for this use case.

---

## Project structure

```
groweasy-csv-importer/
├── backend/     Spring Boot API (Java 17, Maven)
└── frontend/    Next.js app (TypeScript)
```

---

## 1. Get a free Gemini API key (no credit card needed)

1. Go to https://aistudio.google.com/app/apikey
2. Sign in with a Google account.
3. Click **Create API key** → copy it. It looks like `AIza...`.
4. Keep it secret — you'll set it as an environment variable, never commit it.

The backend uses `gemini-2.5-flash`, which has a generous free tier.

---

## 2. Run locally

### Backend

```bash
cd backend
export GEMINI_API_KEY=your_key_here
mvn spring-boot:run
```
Requires Maven + JDK 17 installed locally. Runs on `http://localhost:8080`.
Health check: `GET /api/csv/health`.

Run tests:
```bash
mvn test
```

### Frontend

```bash
cd frontend
cp .env.local.example .env.local   # NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
npm install
npm run dev
```
Runs on `http://localhost:3000`.

---

## 3. Deploy for free

### Step A — Push to GitHub
Push this whole folder as one public repo (or two, your choice — one repo
with `backend/` and `frontend/` subfolders is simplest).

### Step B — Deploy the backend on Render (free)
1. Go to https://render.com → sign up (free, no card required for this).
2. **New +** → **Web Service** → connect your GitHub repo.
3. Root Directory: `backend`
4. Runtime: **Docker** (Render will detect the `Dockerfile` automatically).
5. Instance type: **Free**.
6. Add environment variables:
   - `GEMINI_API_KEY` = your key from Step 1
   - `ALLOWED_ORIGINS` = `http://localhost:3000` (update this after Step C)
7. Click **Create Web Service**. First build takes a few minutes.
8. Copy the resulting URL, e.g. `https://groweasy-csv-importer.onrender.com`.

> Free-tier note: Render's free web services spin down after inactivity and
> take ~30–60s to wake up on the next request. This is normal and fine for
> an assignment submission — just mention it if asked, or "wake" the
> backend once before a live demo.

### Step C — Deploy the frontend on Vercel (free)
1. Go to https://vercel.com → sign up with GitHub.
2. **Add New** → **Project** → import the same repo.
3. Root Directory: `frontend`.
4. Framework preset: Next.js (auto-detected).
5. Add environment variable:
   - `NEXT_PUBLIC_API_BASE_URL` = your Render URL from Step B (no trailing slash)
6. Deploy. Copy the resulting URL, e.g. `https://groweasy-csv-importer.vercel.app`.

### Step D — Close the loop on CORS
Go back to Render → your backend service → Environment → update
`ALLOWED_ORIGINS` to your real Vercel URL (comma-separate multiple origins
if needed) → save, which triggers a redeploy.

You're done — both are live, both are free.

---

## 4. API contract

`POST /api/csv/import` — `multipart/form-data`, field name `file`.

Response:
```json
{
  "totalRows": 42,
  "totalImported": 39,
  "totalSkipped": 3,
  "records": [ { "createdAt": "...", "name": "...", "email": "...", "...": "..." } ],
  "skipped": [ { "rowIndex": 7, "reason": "...", "originalRow": { } } ],
  "warnings": []
}
```

---

## 5. What's implemented (mapped to the assignment's evaluation criteria)

- ✅ Drag & drop **and** file picker upload
- ✅ Client-side preview with horizontal/vertical scroll, sticky headers,
  responsive layout, **virtualized rows** (TanStack Virtual) for large files
- ✅ No AI call until "Confirm Import" is clicked
- ✅ Batch processing to Gemini with a strict JSON `responseSchema`
  (prevents malformed AI output rather than hoping for it)
- ✅ Deterministic server-side re-validation of `crm_status` / `data_source`
  enums, as a safety net on top of AI instructions
- ✅ Multi-email / multi-phone handling per the spec (first value used,
  rest appended to `crm_note`)
- ✅ Skip rule enforced both in the AI prompt and again in code
- ✅ Retry with exponential backoff for failed AI batches
- ✅ Progress indicator during AI processing
- ✅ Dark mode
- ✅ Unit tests (CSV parsing + CRM validation logic)
- ✅ Docker setup for the backend
- ✅ Deployment on free platforms (Render + Vercel)
- ✅ Global error handling with meaningful HTTP status codes

---

## 6. Submission checklist

- [ ] Hosted app URL (Vercel)
- [ ] Public GitHub repo URL
- [ ] This README
- [ ] Position applied for (Intern / Full-Time)
- [ ] Email to varun@groweasy.ai
