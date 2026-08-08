# HIRVO — Architecture & UI/UX Blueprint

**Product:** HIRVO  
**Tagline:** *Talent Meets Opportunity.*  
**Technical project:** JobHub  
**Stage:** 0 — blueprint only; no application implementation is included in this document.

## 1. Architectural direction

HIRVO is a server-rendered Spring MVC application. The primary request path is:

`Browser → Controller → Service → Repository → MySQL`

Thymeleaf renders pages on the server. JavaScript provides progressive enhancement only (for example, menus, dialogs, and interactive filter controls); it does not own authentication, authorization, validation, job publication, or any other business rule.

Use constructor injection throughout. Controllers coordinate HTTP concerns, services contain transactional business rules, repositories provide persistence access, and entities model the database domain. DTO/form classes keep web input separate from persistent entities where a form is non-trivial.

## 2. Final package structure

Base package: `com.example.jobhub` (rename only if the existing application package establishes a different package convention).

```text
com.example.jobhub
├── JobHubApplication
├── config/                 # MVC, Thymeleaf, application configuration
├── controller/
│   ├── publicsite/         # home, jobs, public recruiter/company pages
│   ├── auth/               # login, registration, logout landing
│   ├── seeker/             # /seeker/**
│   ├── recruiter/          # /recruiter/**
│   └── admin/              # /admin/**
├── service/
│   ├── impl/
│   └── authorization/      # ownership checks and access policies
├── repository/
├── entity/
│   ├── enums/              # Role, JobStatus, ApplicationStatus, EmploymentType
│   └── embeddable/         # Address
├── dto/
│   ├── form/               # registration, profile, job, application forms
│   └── view/               # focused dashboard/list projections when useful
├── mapper/                 # explicit entity/form/view mapping where needed
├── security/               # SecurityFilterChain, UserDetailsService, handlers
├── validation/             # custom constraints and validators
├── exception/              # domain exceptions and MVC exception handling
└── util/                   # small, stateless shared helpers only

src/main/resources
├── templates/
│   ├── layouts/            # base public, auth, and dashboard shells
│   ├── fragments/          # navbar, sidebar, alerts, pagination, form fields
│   ├── publicsite/
│   ├── auth/
│   ├── seeker/
│   ├── recruiter/
│   ├── admin/
│   └── error/
├── static/
│   ├── css/                # tokens, base, components, layouts, responsive
│   ├── js/                 # small page/feature modules
│   └── images/             # brand and application-owned static imagery
└── application.properties
```

No duplicate “manager”, “helper”, or parallel MVC package hierarchies should be introduced. Uploaded files should be stored outside the static classpath and exposed through an authorized file-serving strategy in a later stage.

## 3. Entity and database relationship design

### Main entities

| Entity | Important relationships / constraints |
|---|---|
| `User` | One role (`JOB_SEEKER`, `RECRUITER`, `ADMIN`); unique email; enabled flag; owns at most one role-specific profile. |
| `RecruiterProfile` | `1:1` with its recruiter `User`; `1:N` jobs; contains embedded `Address`. |
| `JobSeekerProfile` | `1:1` with its job-seeker `User`; `1:N` educations, experiences, applications, saved jobs; `N:M` skills; contains embedded `Address`. |
| `Job` | `N:1` recruiter profile and `N:1` category; `1:N` applications; current lifecycle status. |
| `Category` | `1:N` jobs; name is unique. |
| `Application` | `N:1` job and `N:1` job seeker profile; one application per seeker/job; `1:1` interview. |
| `Education` | `N:1` job seeker profile. |
| `Experience` | `N:1` job seeker profile. |
| `Skill` | `N:M` job seeker profiles; normalized unique name. |
| `SavedJob` | `N:1` job and `N:1` job seeker profile; one saved record per seeker/job. |
| `Interview` | `1:1` application; created only by the owning job’s recruiter. |
| `Address` | `@Embeddable`, stored in profile table columns; no independent table/id. |

### Relationship map

```text
User (RECRUITER) 1 ── 1 RecruiterProfile 1 ── * Job * ── 1 Category
User (JOB_SEEKER) 1 ── 1 JobSeekerProfile 1 ── * Education
                                           1 ── * Experience
                                           * ── * Skill
                                           1 ── * SavedJob * ── 1 Job
                                           1 ── * Application * ── 1 Job
Application 1 ── 1 Interview
```

### Database integrity

- `users.email` has a unique database constraint.
- `applications(job_id, job_seeker_id)` has a unique composite constraint.
- `saved_jobs(job_id, job_seeker_id)` has a unique composite constraint.
- `skills.name` and `categories.name` have unique constraints (case-normalization policy belongs in the service layer).
- Foreign keys are non-null where the relationship is mandatory. Use cascading only for true owned profile children (education/experience); do not cascade delete users, jobs, applications, or categories casually.
- Use `createdAt` timestamps on entities specified by the product, and define indexes for public job discovery (`status`, `deadline`, `category_id`, `recruiter_id`) and recruiter applicant lookups (`job_id`, `status`).

## 4. Authentication, authorization, and ownership

### Authentication flow

1. Visitor submits the Spring Security login form with email and password.
2. `UserDetailsService` loads `User` by unique email.
3. `PasswordEncoder` compares the submitted password with the stored secure hash.
4. A disabled account fails authentication; it never receives an authenticated session.
5. On success, Spring Security creates a session and redirects by role to `/seeker/dashboard`, `/recruiter/dashboard`, or `/admin/dashboard` (or the previously requested permitted URL).
6. Logout invalidates the session and clears the security context. CSRF protection applies to logout and all state-changing form requests.

Registration creates a role-specific user only for `JOB_SEEKER` or `RECRUITER`. Admin accounts are provisioned through a controlled operational path, never a public registration form.

### Authorization flow

`SecurityFilterChain` provides route-level protection:

| Route | Required authority |
|---|---|
| public home, job discovery, job detail | anonymous or authenticated |
| `/seeker/**` | `ROLE_JOB_SEEKER` |
| `/recruiter/**` | `ROLE_RECRUITER` |
| `/admin/**` | `ROLE_ADMIN` |
| state-changing requests | authenticated role + valid CSRF token |

Unauthenticated requests go to login. Authenticated users without the required role receive a deliberate access-denied response/page. Controllers and service methods must never rely only on hidden navigation links.

### Ownership authorization flow

Role checks answer “what area may this user enter?” Ownership checks answer “which record may they act on?” Every private mutation and sensitive read follows this service-level sequence:

```text
Authenticated principal → resolve current User/Profile → load target record
→ verify relationship to current profile/user → perform action or deny
```

- Recruiter job actions require `job.recruiter.user.id == currentUser.id`.
- Recruiter applicant/interview/status actions additionally require that the application belongs to a job owned by that recruiter.
- Job seeker profile, saved job, application, education, and experience actions require the current seeker profile to own the record.
- Admin actions are role-authorized and auditable; they are not constrained to one recruiter’s records.
- Sensitive public profile fields/files are selectively rendered and file access is checked server-side.

## 5. Lifecycle rules

### Job lifecycle

```text
DRAFT ──publish after server validation──> ACTIVE ──deadline passes──> EXPIRED
  │                                         │  ├─ recruiter pause ─> PAUSED ──resume──> ACTIVE
  └─ recruiter edits/publishes ─────────────└─ recruiter close ────> CLOSED
                                            └─ admin moderation ──> SUSPENDED or REMOVED
```

- A valid recruiter submission is immediately `ACTIVE`; admin approval is never on the normal publishing path.
- Jobs in `ACTIVE` state appear publicly and alone may accept applications while `deadline` is still future.
- Expiration is evaluated at query/action time and updated by a scheduled lifecycle process later; the action-time rule is the safety net.
- `PAUSED`, `CLOSED`, `SUSPENDED`, `REMOVED`, and `EXPIRED` jobs are unavailable for applications. Removed jobs are hidden from public discovery.
- Admin suspension/removal overrides recruiter publication control. A moderator decision needs a recorded reason in the eventual supporting moderation/audit design if added.

### Application lifecycle

```text
APPLIED → REVIEWING → SHORTLISTED → INTERVIEW → SELECTED
   └──────────────────────────────────────────────→ REJECTED
REVIEWING / SHORTLISTED / INTERVIEW ───────────────→ REJECTED
```

- Only an active, unexpired job may receive a new application, and the seeker may apply once only.
- Status changes are made by the owning job’s recruiter; the service validates allowed transitions.
- `Interview` may be scheduled when an application is `SHORTLISTED` or moved into `INTERVIEW` as the scheduling action; the final stage will adopt one consistent transactional rule.
- `SELECTED` and `REJECTED` are terminal in the normal flow. Invalid backwards or terminal transitions are rejected.

## 6. Core user flows

### Job Seeker

Discover public jobs → register/login → complete profile → search/filter/sort → view job → save or apply → view application tracker → attend scheduled interview → observe selected/rejected outcome.

Guest users can browse and inspect jobs. An attempt to save or apply redirects to login and returns to the job context after authentication.

### Recruiter

Register/login → complete company profile → create a validated job → job becomes active → monitor job performance/applicants → review candidates → update validated application status → schedule interview → select or reject candidate → manage own job lifecycle.

### Admin moderation

Login → dashboard statistics → inspect users/jobs/applications → investigate suspicious or reported jobs → suspend or remove only violating jobs → enable/disable users → manage categories → return to monitoring. Normal recruiter jobs remain publishable while an admin is offline.

## 7. Page map and dashboard structure

### Public pages

| Page | Route direction | Purpose |
|---|---|---|
| Home | `/` | Brand promise, search, featured/recent jobs, category entry points. |
| Jobs | `/jobs` | Search, filters, sort, pagination, job-card results. |
| Job detail | `/jobs/{id-or-slug}` | Full job information, company summary, save/apply CTA. |
| Company/recruiter | `/companies/{id}` | Public company profile and its active jobs. |
| Login / register | `/login`, `/register` | Focused, low-distraction authentication screens. |

### Job seeker dashboard

Sidebar: Overview, Profile, Applications, Saved Jobs, Interviews, Settings.  
Overview: welcome/profile-completeness card, application-status counts, upcoming interview, recommended jobs, recent activity.  
Profile: identity, photo, contact/social links, bio, address, CV/resume, education, experience, skills.  
Applications: searchable status tracker with contextual next action.  

### Recruiter dashboard

Sidebar: Overview, Company Profile, Jobs, Create Job, Applicants, Interviews, Settings.  
Overview: active jobs, total applicants, interviews, selection rate/recent activity.  
Jobs: status tabs and per-job performance summary.  
Applicants: job-aware candidate list, filters, status pipeline, protected profile review.  

### Admin dashboard

Sidebar: Overview, Users, Recruiters, Job Seekers, Jobs, Categories, Applications, Moderation.  
Overview: user/job/application trends, active vs. moderated jobs, recent platform events, action queue.  
Moderation: searchable risk/reported job list, job context, recruiter context, suspend/remove action with reason and confirmation.

## 8. UI/UX design system

### Brand and visual voice

Use `HIRVO` prominently in a precise, confident wordmark treatment, with the tagline “Talent Meets Opportunity.” in welcome/marketing contexts. The aesthetic is calm, editorial, and premium: deep ink navigation, warm off-white surfaces, blue-violet primary action, and a restrained teal success accent. Avoid template-like gradient overload, dense bordered boxes, and generic Bootstrap visual defaults.

### Foundations

| Token | Direction |
|---|---|
| Typography | `Inter` or a comparable modern sans-serif; 16px base; 1.5–1.6 body line-height; headings with tight but readable tracking and clear size steps. |
| Colors | Ink/navy for text and navigation; off-white page canvas; white elevated surfaces; indigo primary; teal success; amber warning; rose/destructive; accessible neutral grays. |
| Spacing | 4px base scale: 4, 8, 12, 16, 24, 32, 48, 64, 80. Use generous vertical rhythm on public pages and compact, scannable rhythm in dashboards. |
| Radius | 8px controls, 12px standard cards, 16px feature panels/modals; avoid excessive pill shapes. |
| Shadows | One soft low-elevation shadow for cards; stronger but diffuse shadow for floating menus/modals. Borders carry most separation. |
| Motion | 150–220ms ease-out for menus, toasts, hover/focus feedback; respect `prefers-reduced-motion`. |

### Components

- **Buttons:** Clear primary (solid indigo), secondary (subtle neutral), tertiary/text, destructive, and icon-only variants. Each has hover, focus-visible, disabled, and loading states. Minimum 44px pointer target where practical.
- **Inputs:** Label above input, clear help/error text below, visible focus ring, never placeholder-only labels. Use consistent heights and optional leading icons only when they clarify.
- **Cards:** Job cards prioritize title, employer, location/type, salary where provided, deadline, and one decisive CTA. Use whitespace and hierarchy instead of heavy outlines.
- **Badges:** Compact semantic status labels. Job and application states use consistent color meanings plus text, never color alone.
- **Tables:** Desktop-only dense data tool for recruiter/admin. Sticky header where helpful; row actions behind an accessible overflow menu; mobile becomes stacked record cards.
- **Navbar:** Public navbar: brand, jobs, optional company discovery, login/register or profile menu. Dashboard top bar: page context, global search only when useful, notifications later, profile menu.
- **Sidebar:** Fixed/collapsible desktop navigation with role-specific sections and active-state indicator. On tablet/mobile it becomes an off-canvas drawer with focus management.
- **Toasts:** Brief success/info/error confirmations in a predictable corner; announcements use appropriate live regions. Never use toasts as the sole error explanation for a form.
- **Modals:** Reserved for destructive confirmation, concise decisions, and focused secondary tasks. Trap focus, support Escape, restore focus on close, and retain a non-JS-safe confirmation path for critical actions.
- **Empty states:** Explain the absence, show a relevant illustration/icon treatment, and give one clear next step (for example, “Create your first job” or “Explore opportunities”).
- **Error states:** Inline field messages for validation; page-level summary for multiple errors; specific access/not-found pages with a recovery route.
- **Success states:** Calm confirmation panel/toast, concise next step, and status visibility in the relevant list/detail view.

## 9. Responsive layout strategy

Build mobile-first, then enhance deliberately at these target widths:

| Viewport | Layout behavior |
|---|---|
| 375–575px | Single-column pages; compact public header; full-width controls; filter drawer; dashboard sidebar off-canvas; tables become cards. |
| 576–767px | Two-column metadata/groups where useful; job search controls begin wrapping intelligently. |
| 768–991px | Tablet layouts; wider filter panel; dashboard content with temporary/collapsible navigation; 2-column cards. |
| 992–1199px | Stable desktop dashboard with sidebar; jobs results plus filter rail; multi-column profile forms. |
| 1200–1439px | Comfortable content widths, persistent filter rail, 3-column supporting cards where meaningful. |
| 1440px+ | Max-width centered public content; dashboards use whitespace rather than stretching data rows excessively. |

Critical job actions remain visible and usable at every width. Touch targets, readable type, keyboard navigation, contrast, semantic HTML, labels, and focus order are required accessibility baselines.

## 10. JavaScript responsibilities

JavaScript may enhance, but cannot replace, server-rendered workflows:

- mobile navigation and dashboard sidebar;
- filter drawer, active filter chips, and optional URL-synchronized controls;
- password visibility, character counters, image/file-selection previews;
- accessible modal/confirmation and toast behavior;
- small dashboard chart rendering from server-provided data;
- profile-completeness display and theme preference if introduced.

Forms, links, confirmations, and navigation must retain a usable server-rendered path. Never put permissions, lifecycle transitions, form validation authority, or security decisions solely in client code.

## 11. Security architecture

- Spring Security uses `SecurityFilterChain`, `UserDetailsService`, `PasswordEncoder`, session authentication, CSRF protection, and explicit authentication/access-denied handlers.
- Passwords are BCrypt-encoded (or an equally strong Spring Security-supported adaptive encoder); plaintext is never logged, rendered, or persisted.
- Roles are mapped to `ROLE_ADMIN`, `ROLE_RECRUITER`, and `ROLE_JOB_SEEKER` authorities.
- Session fixation protection, secure cookie configuration in production, HTTPS, and suitable security headers are configured in the deployment stage.
- Server-side authorization is enforced both at route and service/ownership levels. Form fields and path variables are untrusted input.
- Uploads are validated by type/size, given server-generated names, kept outside executable/static paths, and served only through controlled access rules.

## 12. Validation strategy

Apply Bean Validation to input form DTOs and repeat critical invariant checks in services before persistence.

- Required names/text, valid email, password policy, URL/phone formats, sensible lengths.
- Unique email, duplicate save/application prevention, and category/skill normalization are service/database enforced.
- Salary minimum/maximum are non-negative and `min ≤ max`; deadline must be future when a job is created/published.
- Job application validation rechecks job status and deadline transactionally; UI state is not trusted.
- Date ranges are coherent (`end >= start`); experience may intentionally allow an open-ended current role.
- File uploads receive server-side extension/content-type/size checks and errors are reported safely.
- Status changes use explicit transition policy methods rather than accepting arbitrary enum values from a request.

## 13. Exception handling strategy

Domain-specific exceptions describe failures such as record not found, duplicate application/save, invalid status transition, inactive job, forbidden ownership, and disabled account. A central `@ControllerAdvice` maps expected exceptions to clear redirect/flash messages for normal form flows or dedicated 403/404/500 pages where appropriate.

- Do not expose stack traces, database messages, or security internals to users.
- Preserve safe submitted form fields and field-level validation feedback after correctable errors.
- Log unexpected exceptions with request/context identifiers while excluding passwords, tokens, cover-letter private content, and sensitive profile data.
- Security failures use the configured access-denied/authentication handlers, not generic unhandled errors.

## 14. Stage boundary

This document deliberately defines no controllers, services, repositories, entities, security configuration, templates, CSS, or JavaScript. Implementation begins only when a later stage explicitly requests it.
