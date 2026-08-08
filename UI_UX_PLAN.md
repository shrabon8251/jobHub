# HIRVO UI/UX Redesign Plan

**Product:** HIRVO  
**Tagline:** Talent Meets Opportunity.  
**Stage:** 7A — planning only  
**Status:** No implementation changes are included in this stage.

## 1. Audit summary

The current interface is functional but not presentation-ready. The pages are mostly standalone, unstyled HTML documents with minimal hierarchy, little visual grouping, and no shared layout system.

Current template areas inspected:

- Public and authentication: `auth/*`, `publicsite/*`
- Job seeker: `seeker/*`
- Recruiter: `recruiter/*`
- Admin: `admin/*`
- Errors: `error/*`

There are currently no reusable Thymeleaf layouts or fragments, and no `static/css` or `static/js` assets. Several templates repeat navigation and form patterns independently. The redesign should introduce a shared shell and component vocabulary before individual pages are refined.

`PROJECT_SPEC.md` was not present in the repository during this audit. This plan therefore uses `ARCHITECTURE.md`, the existing routes/templates, and the stated HIRVO product direction as the working source of truth.

## 2. Product experience direction

HIRVO should feel like a calm, capable, editorial recruitment product—not an admin CRUD tool. The experience should communicate:

- Confidence: clear decisions, strong typography, predictable actions.
- Momentum: every page should make the next useful action obvious.
- Trust: transparent job details, visible status, honest empty/error states.
- Focus: generous whitespace around important content and controlled information density.
- Warm professionalism: premium surfaces and subtle color, without excessive gradients or decorative noise.

The design should be original to HIRVO. LinkedIn Jobs, Indeed, Wellfound, and modern SaaS products are quality references only; their branding, visual language, and layouts must not be copied.

## 3. Information architecture and shared shells

Create three reusable shells with common primitives:

### Public shell

- HIRVO wordmark and tagline treatment.
- Primary links: Find jobs, Companies or Recruiters where available, and a contextual CTA.
- Right side: Sign in and Create account for guests; avatar/profile menu for authenticated users.
- On smaller screens: wordmark, one primary action, menu button.
- Footer: brand promise, navigation groups, support/legal placeholders, and a concise copyright line.

### Seeker dashboard shell

- Desktop sidebar: Overview, Find jobs, Saved jobs, Applications, Interviews, Profile, Settings.
- Top bar: page title, optional search, notifications placeholder, profile menu.
- Mobile: compact top bar plus an off-canvas navigation drawer; drawer closes on selection and Escape.
- Persistent “Find jobs” action should remain easy to reach.

### Recruiter/admin dashboard shell

- Desktop sidebar with role-specific navigation and a clear active indicator.
- Top bar with page context, global search only where useful, help/notifications placeholders, and account menu.
- Sidebar should visually distinguish primary workflows from configuration links.
- Admin moderation actions should use destructive styling and explicit confirmation.

Every shell should use the same logo, focus treatment, page container, alerts, buttons, form fields, badges, and responsive behavior.

## 4. HIRVO design system

### Color system

Use semantic tokens so color meaning stays consistent across all roles and page types.

| Token | Suggested value | Use |
|---|---|---|
| Ink 900 | `#101828` | Main headings, navigation text, high-emphasis content |
| Ink 700 | `#344054` | Body text, labels, secondary headings |
| Ink 500 | `#667085` | Supporting text, metadata, placeholders |
| Canvas | `#F7F8FA` | Application background |
| Surface | `#FFFFFF` | Cards, forms, panels |
| Primary 600 | `#5146D8` | Main brand action and selected navigation |
| Primary 700 | `#4238B8` | Hover/pressed primary state |
| Primary tint | `#EEEDFF` | Soft selected backgrounds and informational accents |
| Teal 700 | `#0F766E` | Positive progress, selected outcomes, success |
| Teal tint | `#E7F7F4` | Success surfaces |
| Amber 700 | `#B45309` | Pending, attention, deadline warnings |
| Amber tint | `#FFF4D6` | Warning surfaces |
| Rose 700 | `#BE123C` | Destructive actions and rejected/removed states |
| Rose tint | `#FFF0F2` | Error/destructive surfaces |
| Border | `#E4E7EC` | Dividers, inputs, table structure |

Do not rely on color alone. Every status must include text, and critical alerts should include an icon or clear label plus accessible text.

### Typography

- Primary typeface: Inter, with a system sans-serif fallback for offline resilience.
- Body: 16px, line-height 1.55, regular weight.
- Small metadata: 13–14px, line-height 1.4.
- Page title: 32–40px desktop, 28–32px mobile, weight 650–700.
- Section title: 20–24px, weight 650.
- Card title/job title: 17–20px, weight 650.
- Labels/buttons: 14–15px, weight 600.
- Use sentence case for labels and actions. Avoid all-caps except compact status labels where legibility remains strong.

### Spacing and containers

Use a 4px base scale: 4, 8, 12, 16, 20, 24, 32, 40, 48, 64, 80.

- Public content max width: 1200px, with 24px horizontal padding on desktop.
- Dashboard content max width: 1320px; keep data-heavy areas from stretching indefinitely.
- Page sections: 48–80px vertical spacing on public pages.
- Dashboard sections: 24–40px vertical spacing.
- Form field groups: 16–20px vertical rhythm.
- Card internal padding: 20–24px desktop, 16–20px mobile.

### Shape and elevation

- Inputs and buttons: 10px radius.
- Standard cards and panels: 14px radius.
- Feature/hero panels and modals: 18px radius.
- Status badges: pill shape only for compact statuses.
- Use one restrained shadow: `0 8px 24px rgba(16, 24, 40, .06)`.
- Prefer borders and whitespace for separation. Avoid nested cards with heavy shadows.

### Buttons

Variants:

- Primary: indigo fill, white text; used for one main action per context.
- Secondary: white or surface fill with border; used for adjacent actions.
- Tertiary: text action with clear hover background.
- Destructive: rose fill or rose text/border, always paired with confirmation for irreversible actions.
- Icon-only: only for familiar actions, with visible tooltip and accessible label.

Rules:

- Minimum height 44px for touch controls.
- Preserve a visible `:focus-visible` ring.
- Button labels should describe the outcome: “Apply now”, “Save job”, “Suspend listing”, “Schedule interview”.
- Do not place two equally prominent primary buttons side by side.

### Inputs and forms

- Label above every field; helper/error text below it.
- Never use placeholder text as the only label.
- Input height: 44–48px; textarea minimum height 120px.
- Group related fields into visually named sections.
- Show validation errors near the field and a summary at the top for long forms.
- Preserve submitted values after validation errors.
- Use appropriate native types for email, date, time, URL, salary, and phone.
- Include CSRF-safe server-rendered forms; JavaScript may enhance but must not own validation or permissions.

### Cards

Cards should have a clear purpose and one visual focal point. Use a title, supporting metadata, and an explicit action area. Do not turn every line of content into a bordered card.

Standard card anatomy:

1. Eyebrow or status, when useful.
2. Title and primary identity.
3. Supporting metadata or short description.
4. Optional metric/progress line.
5. Action area aligned consistently at the bottom or top-right.

### Badges and statuses

Use a shared semantic status map:

| Domain state | Visual treatment |
|---|---|
| Active, selected, enabled | Teal tint + teal text |
| Draft, reviewing, pending | Neutral or amber tint |
| Interview, shortlisted | Indigo tint + indigo text |
| Paused, expired, closed | Neutral gray |
| Suspended, removed, rejected, disabled | Rose tint + rose text |

Status labels should be human-readable (“Active”, “Needs review”) rather than raw enum strings such as `JOB_SEEKER` or `SUSPENDED` in prominent UI.

### Tables

- Tables are for desktop scanning, not the default visual pattern for every page.
- Use sticky headers only when a long table benefits from them.
- Align text left and numeric metrics right.
- Keep row actions in a consistent last column; destructive actions should be visually separated.
- Provide a mobile card transformation rather than forcing horizontal scrolling whenever possible.
- Include empty, loading, and filtered-no-results states.

### Sidebar and navbar

- Sidebar background: deep ink with high-contrast active item, or a light surface with a strong indigo active rail. Choose one direction and use it consistently; recommended direction is deep ink for dashboard chrome and white content surfaces.
- Active item: indigo-tinted or teal-accented indicator, label, and icon; never only a color change.
- Group links by workflow: “Work”, “Manage”, and “Settings” where appropriate.
- Navbar remains visually quieter than the main content and never competes with the primary CTA.

### Modals, toasts, and feedback

- Use modals only for destructive confirmation, focused secondary tasks, or short decisions.
- Modal must have a title, clear consequence, cancel action, primary action, Escape support, focus trap, and focus restoration.
- Toasts confirm completed actions but are never the only place an error is explained.
- Inline/page-level feedback remains visible after redirects through server-rendered flash messages.
- Use `role="status"` for success/info and `role="alert"` for errors.

### Empty, error, and success states

- Empty state: short explanation, relevant icon/illustration, and one next action.
- Filtered empty state: acknowledge the filters and provide “Clear filters”.
- Error state: plain-language cause, recovery action, and no stack traces or database messages.
- Success state: confirm what changed and provide the next relevant destination.
- Avoid dead ends such as an empty table with no explanation.

## 5. Page-by-page redesign plan

### 5.1 Landing page

The current root experience is a minimal authenticated-home page. Redesign it as the public HIRVO entry point.

Structure:

- Public navbar with HIRVO wordmark, Find jobs, employer/recruiter entry, Sign in, and Create account.
- Hero section with “Talent Meets Opportunity.” as the supporting promise, a concise value proposition, and a two-field job search form for keyword and location.
- Featured/recent jobs preview with 3–6 high-quality job cards.
- Browse by category with compact category tiles and counts where available.
- Two-sided “How HIRVO works” section: discover opportunities for seekers and publish/manage talent for recruiters.
- Trust/metric band for platform activity when real metrics are available; do not fabricate numbers.
- Final CTA with distinct seeker and recruiter paths.
- Footer with accessible link groups.

Visual hierarchy: hero search first, job discovery second, explanatory content third. Keep the first viewport focused and avoid a crowded marketing page.

### 5.2 Job listing

The current page is a stacked form followed by unstructured articles. Redesign it as a search workspace.

- Page header with result count, current search summary, and “Clear all” when filters are active.
- Desktop: filter rail on the left and results column on the right.
- Mobile: one “Filters” button opens a bottom sheet/drawer; active filters become removable chips.
- Search controls: keyword, location, category, employment type, salary range, sort.
- Job card content priority: title, company, location/type, salary if available, deadline, category, and Save action.
- Use a visible selected/saved state without requiring a page reload for enhancement.
- Pagination is clear, keyboard accessible, and preserves query parameters.
- Filtered-empty state suggests widening the search or clearing filters.

### 5.3 Job details

- Breadcrumb/back link followed by a calm job hero: title, company, location, employment type, category, and deadline.
- Desktop two-column layout: main description/requirements/responsibilities and a sticky application panel.
- Application panel includes Apply now, Save job, deadline, and a compact “What to expect” note.
- Recruiter/company context appears in a separate trust panel with company name and profile link where available.
- Mobile keeps Apply now sticky near the bottom only when it does not obstruct content; provide an accessible close/expand behavior if implemented.
- Show inactive/expired/suspended states prominently and remove or disable application actions accordingly.
- Use readable long-form typography: 65–75ch line length, clear subheadings, lists for requirements.

### 5.4 Login

- Split or layered layout: quiet brand panel on larger screens, focused form surface on the other side.
- Keep the form short: email, password, show/hide password, Sign in.
- Display invalid credentials, disabled account, logout, and registration success as distinct alert treatments.
- Include “Create an account” as a secondary path.
- Preserve autofocus and autocomplete; do not add distracting social login placeholders unless supported.
- On mobile, collapse to one focused column with logo and tagline above the form.

### 5.5 Registration

- Present registration as a guided choice between Job seeker and Recruiter before showing role-specific supporting copy.
- Keep the form visually grouped: identity, credentials, and role profile name/company name.
- Explain what happens next after account creation.
- Password guidance should be visible before submission and errors should be specific.
- Use a progress cue only if registration becomes multi-step; avoid fake progress for a single page.
- Include sign-in link and privacy/terms acknowledgement only when the product actually supports those policies.

### 5.6 Job seeker dashboard

Primary goal: help a seeker resume a job search and understand application progress.

- Welcome header with profile-completeness indicator and one clear “Find jobs” CTA.
- Metric cards: applications, saved jobs, upcoming interviews, and active application count.
- “Continue your search” section with recommended/recent jobs.
- Application timeline or status pipeline showing the latest movement.
- Upcoming interview card with date/time, company, and Join/View details action.
- Recent applications list with status badges and next actions.
- Empty state for new seekers should prioritize completing profile and browsing jobs.

### 5.7 Recruiter dashboard

Primary goal: show recruiting momentum and surface the next candidate/job action.

- Welcome header with Create job as the primary CTA.
- Metrics: active jobs, total applicants, interviews, and selected candidates or selection rate where supported.
- Job performance list with status, applicant count, deadline, and quick View/manage action.
- Applicant pipeline visualization: Applied → Reviewing → Shortlisted → Interview → Selected/Rejected.
- Recent applicant activity and upcoming interviews.
- Profile/company completeness prompt if the company profile is incomplete.
- Avoid decorative charts unless the data is real and actionable.

### 5.8 Admin dashboard

Primary goal: monitor platform health and reach moderation actions quickly.

- KPI row: total users, job seekers, recruiters, active jobs, applications, interviews.
- Secondary health metrics: disabled users, draft jobs, suspended jobs, removed jobs, and recent activity.
- Moderation queue panel for suspended/flagged/problematic jobs when supported; otherwise show recent jobs with clear moderation entry points.
- User health panel with enabled vs disabled accounts.
- Quick actions: Manage users, Review jobs, Manage categories.
- Use compact, high-density cards and tables but preserve whitespace around major sections.
- Moderation actions must visibly distinguish Suspend, Remove, and Restore; normal active publishing must not be presented as requiring admin approval.

### 5.9 Profiles

#### Job seeker profile

- Header card: photo/avatar, full name, location, short bio, profile completeness, Edit profile.
- Sections: About, contact links, skills, experience, education, CV/resume actions.
- Use timeline styling for education/experience instead of a dense unordered list.
- Skills use compact tags with clear remove controls only in edit contexts.
- Protect sensitive contact/file actions with clear ownership and authorization expectations.

#### Recruiter/company profile

- Header: company name, website, location, industry/context when available, Edit profile.
- Brand surface should be restrained; do not use a large empty logo placeholder.
- Sections: company story, contact links, open jobs.
- Public view prioritizes trust and active opportunities; edit view prioritizes grouped fields and clear save/cancel actions.

### 5.10 Application pages

#### Seeker application form

- Show job summary at the top so the seeker knows what they are applying to.
- Use a focused cover-letter editor with character/help text and clear submit/cancel actions.
- Explain that the application cannot be duplicated when the system enforces one application per job.

#### Seeker application detail/list

- List uses job title/company, applied date, current status, and next action.
- Detail page uses a horizontal or vertical progress tracker for application lifecycle.
- Cover letter is visually secondary to status and next action.
- Rejected/selected/closed states should explain what the seeker can do next without implying unsupported actions.

#### Recruiter applicant list/detail

- Filters by job and status at the top.
- Candidate row/card: name, job, applied date, status, and quick review action.
- Detail page places candidate identity and application context first, then cover letter/profile, then status actions.
- Transition actions must be grouped into a clear pipeline area and should not expose invalid transitions.

### 5.11 Interview pages

- Interview detail should read like an event card: candidate/job, date, time, meeting link, notes, and status.
- Use a calendar/date icon treatment sparingly and keep date/time text explicit.
- Recruiter page: Reschedule and Cancel are secondary/destructive actions with confirmation.
- Seeker page: Join meeting is the primary action when a link exists; show timezone guidance if supported.
- Empty interview state should route to applications or applicant management.

### 5.12 Saved jobs

- Use the same job-card component as listings, with a saved indicator and Remove action.
- Include saved date only if useful.
- Empty state: “Save jobs while browsing to compare them here” plus Find jobs CTA.
- If a saved job becomes unavailable, show that status clearly and offer removal without breaking the list.

### 5.13 Job management

#### Recruiter jobs list

- Tab or segmented filter for All, Active, Draft, Paused, Closed, Expired.
- Each row/card: title, status, deadline, applicants, and last updated.
- Create job is the primary action; management actions are grouped in a menu or consistent action rail.

#### Job form

- Group fields into Basics, Description, Requirements, Compensation, Location/type, and Deadline.
- Use a right-side preview on desktop only if it can remain accurate without duplicating business rules.
- Clearly state that valid recruiter publishing makes a job active; do not add an admin approval step to the normal flow.
- Validation summary and inline messages should preserve the form content.

#### Job detail/manage

- Show job status and publication context in the hero.
- Keep Edit, Pause/Resume, Close, and Delete actions visually separated by risk.
- Make destructive actions confirmation-based and explain their effect on public visibility/applications.

### 5.14 Applicant management

- Start with a job selector or job context so the recruiter always knows which role they are reviewing.
- Use a pipeline board on wide screens only when candidate volume is manageable; otherwise use a filterable table/list.
- Candidate cards show only relevant preview data; full profile remains behind the detail view.
- Status transition controls should be keyboard accessible, provide success feedback, and update the visible status immediately after server confirmation.
- Empty states distinguish “no applicants yet” from “no applicants match this filter”.

### 5.15 Navigation and mobile navigation

- Every authenticated page has one consistent role shell; avoid page-specific ad hoc nav links.
- Desktop sidebar stays fixed within the application shell while content scrolls.
- Mobile navigation is an off-canvas drawer with:
  - menu button at a 44px target;
  - visible current section;
  - close button and Escape support;
  - focus management and body scroll lock;
  - no hidden critical actions.
- Public mobile header keeps Find jobs and account access visible without a crowded row.
- Breadcrumbs/back links are used for deep detail and edit flows.

### 5.16 Error pages

- 403/access denied: explain permission boundary, offer dashboard or public home based on authentication state.
- 404/not found: explain that the page or job is unavailable, offer job search and home.
- 500/server error: calm apology, retry action, home link, and support reference if available.
- Use a consistent illustration/icon treatment, not a blank browser-like error screen.
- Keep technical details out of the user-facing page while logging them server-side.

## 6. Responsive behavior

Use mobile-first CSS with these deliberate ranges:

| Width | Behavior |
|---|---|
| 375–575px | Single column; drawer navigation; full-width controls; job detail action stack; tables become cards |
| 576–767px | Wrapped search controls; two-column metadata where readable; cards remain single column |
| 768–991px | Tablet layout; temporary sidebar/drawer; two-column dashboard cards; filter drawer or narrow rail |
| 992–1199px | Stable desktop shell; persistent sidebar; job results filter rail; multi-column forms |
| 1200–1439px | Comfortable content width; two-column job details; dashboard supporting panels |
| 1440px+ | Centered max-width content; preserve readable line lengths; do not stretch tables across the full viewport |

Rules:

- No horizontal scrolling for primary workflows.
- Touch targets remain at least 44px.
- Preserve sticky application actions only when they do not cover content or keyboard focus.
- Reduce decorative spacing before reducing text size.
- Long job titles and company names must wrap gracefully without breaking cards.
- Dashboard cards should reorder by priority on mobile: primary action, status/metric, recent activity.

## 7. Accessibility requirements

- Semantic landmarks: header, nav, main, aside, section, footer.
- One logical `h1` per page and ordered heading hierarchy.
- Every input has an explicit label and error association.
- Keyboard navigation for all links, menus, modals, filters, tables, and status controls.
- Visible focus ring with sufficient contrast.
- Color contrast target: WCAG AA for normal text and controls.
- Status uses text plus visual treatment; do not communicate state through color alone.
- Respect `prefers-reduced-motion`.
- Provide meaningful link/button labels, including for icon-only controls.
- Use live regions for server-confirmed success/error feedback where appropriate.
- Ensure modal focus trap, Escape close, and focus restoration.
- Test at 200% zoom and with a narrow viewport.

## 8. Interaction and motion principles

- Use 150–220ms ease-out transitions for hover, focus, drawer, and toast entry.
- Use motion to clarify state, not to decorate every interaction.
- Keep server-rendered navigation and forms fully usable when JavaScript is unavailable.
- Filter drawers, password visibility, confirmation modals, and toasts may be progressive enhancements.
- Never use client-side state to authorize admin, recruiter, or seeker actions.

## 9. Recommended implementation structure for the next stage

Implementation should be staged in this order after approval of this plan:

1. Add design tokens and base CSS under `static/css`.
2. Add shared Thymeleaf fragments under `templates/fragments` for public header, dashboard shell, sidebar, alerts, buttons, pagination, badges, and form fields.
3. Add shell/layout composition under `templates/layouts` using the project’s existing Thymeleaf approach.
4. Redesign the public landing page, jobs listing, and job details.
5. Redesign login and registration.
6. Redesign seeker pages and shared application/interview components.
7. Redesign recruiter jobs, dashboard, profiles, and applicant flows.
8. Redesign admin dashboard, users, moderation, and categories.
9. Add mobile navigation and progressive-enhancement JavaScript.
10. Perform visual QA at the defined breakpoints and accessibility QA with keyboard-only navigation.

Do not change business rules, routes, authorization, lifecycle transitions, or persistence as part of the UI implementation unless a separate stage explicitly authorizes it.

## 10. Definition of done for the UI stage

- Every existing route has a consistent shell and responsive presentation.
- Public, seeker, recruiter, and admin experiences are visually related but role-appropriate.
- No standalone page remains with default browser styling.
- All forms have labels, validation presentation, CSRF-safe server paths, and clear success/error feedback.
- Job, application, interview, moderation, and account states have consistent badges and empty/error/success states.
- Desktop and mobile navigation are usable with keyboard and touch.
- The landing page communicates the product proposition within the first viewport.
- UI does not imply admin approval for normal recruiter job publishing.
- Visual QA covers 375px, 768px, 1024px, 1280px, and 1440px widths.
- Accessibility QA covers focus order, contrast, labels, keyboard operation, reduced motion, and zoom.

**Stage 7A boundary:** create this plan only. No templates, CSS, JavaScript, controllers, services, entities, or routes should be modified until the plan is approved for implementation.
