# JobRadar

JobRadar is a web application for tracking job applications and automating parts of the job search process. It integrates with Gmail to analyze recruiter emails, update application statuses, and provide AI-generated suggestions.

## Features

* Job application management
* Application status history
* Dashboard and statistics
* CV and Cover Letter management
* Google OAuth2 authentication
* Gmail integration
* AI email analysis
* AI suggestions
* Automated Gmail scanning
* Multi-language support

## Tech Stack

### Backend

* Java 21
* Spring Boot
* Spring Security
* PostgreSQL

### Frontend

* React
* Vite

### AI

* Rule-Based Provider
* OpenAI Provider
* Provider abstraction layer

### Infrastructure

* Docker
* GitHub Actions

## Architecture

```text
React
    │
    ▼
Spring Boot
    ├── PostgreSQL
    ├── Gmail Integration
    ├── AI Provider Layer
    │      ├── Rule-Based Provider
    │      └── OpenAI Provider
    └── Automation Layer
```

## Project Structure

* **Authentication** — Google OAuth2 login
* **Applications** — Job application management
* **Dashboard** — Statistics and analytics
* **Documents** — CV and Cover Letter storage
* **Gmail Integration** — Recruiter email processing
* **AI Provider Layer** — Pluggable AI providers
* **Automation Layer** — Background email synchronization

## Current Version

**v0.3.3**

### Highlights

* Improved Gmail email analysis
* Enhanced email classification
* AI status transition validation
* Reduced false-positive email detection
* OpenAI integration
* Automated Gmail synchronization
* CI/CD with GitHub Actions

MIT Licens
