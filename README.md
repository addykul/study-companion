# Study Companion

Study Companion is a full-stack learning app for asking questions about course notes, practicing with generated quizzes, and seeing which topics need more review. It combines a React + TypeScript frontend with a Java 21 / Spring Boot API and PostgreSQL with pgvector.

This project was created as a hands-on way to understand **Retrieval-Augmented Generation (RAG)** and other AI-related concepts while building a practical application. It is also a study project for clean backend boundaries, REST APIs, data modeling, vector search, and full-stack integration.

## What it does

- Create courses and upload PDF or plain-text notes.
- Extract and chunk text, create embeddings, and store them with course and page metadata.
- Ask questions grounded in one course’s notes and inspect the source excerpts used for answers.
- Generate five-question multiple-choice quizzes from course materials.
- Score quiz submissions in Java and summarize topic accuracy.
- Process document ingestion asynchronously and show its status in the UI.

## What this project helps you learn

- **RAG:** why retrieval happens before generation, and how retrieved passages ground an answer.
- **Embeddings and vector search:** converting text to vectors and using pgvector cosine similarity to find related chunks.
- **Chunking and citations:** choosing passage sizes, retaining page references, and mapping retrieved chunks back to source documents.
- **Structured model output:** asking a model for quiz data, validating it, and keeping answer scoring deterministic in the backend.
- **AI application boundaries:** isolating model calls behind a provider interface so the implementation can change without rewriting study workflows.
- **Full-stack engineering:** connecting a typed React UI to a Java REST API and PostgreSQL.
- **Production concepts:** async work, database migrations, configuration, error handling, and a path toward deployment.

## How the RAG flow works

1. A learner uploads a PDF or text file to a course.
2. The API extracts text, preserves PDF page numbers, and splits text into overlapping chunks.
3. Spring AI requests an embedding for each chunk; PostgreSQL stores the vectors through pgvector.
4. For a question, the API embeds the question and searches only chunks belonging to the selected course.
5. The API sends the best matching excerpts to the configured chat model and returns the answer with source references.

The app asks the model to stay grounded in the retrieved excerpts. If retrieval finds no relevant material, it responds that it could not find support in the course instead of generating an answer from unrelated knowledge.

## AI approach and scope

The current AI feature is a RAG workflow with structured quiz generation. **It is not an autonomous agent yet**: Java code controls the workflow, while the model answers questions or drafts quiz content at defined steps. Understanding RAG and structured model interaction is the foundation for adding an explicit agent workflow later.

The backend uses Spring AI and a project-owned `StudyAiProvider` interface. OpenRouter is the current adapter for chat and embeddings. The code does not currently use LangChain or LangGraph. A future provider adapter, such as Amazon Bedrock, can implement the same interface. The configured embedding model must return 1,536 dimensions unless the database vector column is migrated and existing documents are re-embedded.

## Technology stack

| Area | Technology |
| --- | --- |
| Frontend | React, TypeScript, Vite, CSS |
| Backend | Java 21, Spring Boot, Spring Web, Spring JDBC |
| AI | Spring AI, OpenRouter-compatible chat and embedding APIs |
| Data | PostgreSQL, pgvector, Flyway |
| Document processing | Apache PDFBox, asynchronous Spring task executor |
| Local services | Docker Compose |
| Frontend checks | Vitest, Testing Library |
| Backend checks | JUnit, Mockito |

## Architecture

The backend follows a small ports-and-adapters structure. Controllers handle HTTP and DTO mapping; application services coordinate use cases; domain models and repository interfaces describe core concepts; infrastructure adapters implement JDBC persistence, local file storage, document extraction, and model-provider calls.

```text
study-companion/
├── backend/
│   └── src/main/java/dev/studycompanion/
│       ├── api/                 # Controllers, DTOs, API mapper, error handling
│       ├── application/         # Services, ports, commands, app exceptions
│       ├── config/              # Typed settings, CORS, async executor
│       ├── domain/              # Domain records and repository interfaces
│       └── infrastructure/      # JDBC, OpenRouter, local storage, PDFBox
├── frontend/                    # React application
├── docs/                        # Detailed developer and deployment guide
├── output/pdf/                  # PDF version of the developer guide
├── docker-compose.yml           # Local PostgreSQL + pgvector
└── .env.example                 # Configuration template (no credentials)
```

Spring Boot auto-configures the `DataSource` and `JdbcTemplate` from `spring.datasource.*` settings. The app uses JDBC repositories rather than JPA, so domain records are not ORM entities. Flyway migrations define the schema.

## Run locally

### Requirements

- Java 21
- Maven 3.6.3 or newer
- Node.js 20.19+ or 22.12+
- pnpm 10+
- Docker with Compose
- An OpenRouter API key for live chat, embeddings, and quiz generation

### Configure

From the repository root:

```sh
cp .env.example .env
```

Add your key to the local `.env` file:

```dotenv
OPENROUTER_API_KEY=your-key-here
```

`.env` is ignored by Git. Spring Boot reads environment variables, not the `.env` file directly, so export its values in the terminal used to start the backend:

```sh
set -a
source .env
set +a
```

### Start the database and backend

```sh
docker compose up -d postgres
cd backend
mvn spring-boot:run
```

The API listens on `http://localhost:8080`. The health endpoint is `http://localhost:8080/api/health`.

### Start the frontend

In a second terminal from the repository root:

```sh
cd frontend
pnpm install
pnpm dev
```

Open the Vite URL shown in the terminal, normally `http://localhost:5173`.

## Configuration

See [`.env.example`](.env.example) for available settings. Important values include:

- `OPENROUTER_API_KEY`: local model-provider key.
- `OPENROUTER_CHAT_MODEL`: chat model; choose one that supports structured output.
- `OPENROUTER_EMBEDDING_MODEL`: embedding model. The default is `openai/text-embedding-3-small` with 1,536 dimensions.
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`: local database connection settings.
- `FRONTEND_ORIGIN`: exact browser origin allowed by backend CORS.
- `UPLOAD_DIR`: local file storage path.
- `VITE_API_BASE_URL`: backend URL included at frontend build time.

Do not commit `.env`, API keys, database credentials, or uploaded study files. The project is single-user and does not include authentication. Retrieved excerpts are sent to the configured model provider, so do not upload sensitive or confidential material.

## API overview

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/api/health` | API health |
| `GET`, `POST` | `/api/courses` | List and create courses |
| `GET`, `POST` | `/api/courses/{courseId}/documents` | List and upload course documents |
| `GET` | `/api/documents/{documentId}` | Read document processing status |
| `POST` | `/api/courses/{courseId}/chat` | Ask a cited question |
| `POST` | `/api/courses/{courseId}/quizzes` | Generate a quiz |
| `POST` | `/api/quizzes/{quizId}/submissions` | Submit answers and get results |
| `GET` | `/api/courses/{courseId}/progress` | Read topic progress |

## Run checks

```sh
cd backend && mvn test
cd ../frontend && pnpm test && pnpm build
```

The backend tests use mocks for model services and do not require OpenRouter credentials. The end-to-end manual demo does require a live provider key.

## Current limitations and next learning steps

- The app is a local, single-user learning project, not a production service. It has no authentication or user-level data isolation.
- Uploaded files are stored on local disk; cloud object storage and AWS deployment are not implemented.
- Chat history is persisted, but the UI does not yet reload prior conversation history after a page refresh.
- There is no agent planner, tool-selection loop, or LangGraph workflow yet.

For a deeper explanation of the business flows, frontend, backend layering, RAG behavior, provider portability, CORS, GitHub, and an AWS learning path, see the [developer guide](docs/STUDY_COMPANION_GUIDE.md) or its [PDF version](output/pdf/STUDY_COMPANION_GUIDE.pdf).
