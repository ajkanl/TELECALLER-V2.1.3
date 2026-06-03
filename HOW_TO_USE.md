# Student Debt Recovery Platform — User Manual

Welcome to the **Student Debt Recovery Platform**. This application is an offline-first, highly secure mobile CRM designed specifically to assist educational institutions and telecalling recovery agencies. The platform features an advanced predictive call dialer queue, industry-standard compliance and data protection, and a highly optimized real-time automatic Firestore sync.

This document serves as an exhaustive operational guide for both **Administrators** and **Telecaller Agents**.

---

## 🗺️ Architectural Ecosystem & Sync Mechanism

To ensure maximum resilience under unstable networks, the app uses an **Offline-First, Real-Time Sync architecture**:
*   **Local Level (SQLite/Room):** Every record, transaction, call status, and payment commitment is saved immediately to the local device database.
*   **Automatic Cloud Synchronization:** The application actively monitors localized state transitions. Any change made to Debtors, Call Logs, Promises To Pay, or Agent Login States is automatically debounced by **3 seconds** (to avoid write-contention) and pushed silently to your **Cloud Firestore** instance.
*   **Firestore Collection Instantiation:** On launch or first sync, the system writes native welcome placeholder records (`_welcome_placeholder_`) to prevent Firestore from showing empty collections, immediately populating documents for:
    1.  `debtors`: Individual debtor tracking, overdue balances, and segments.
    2.  `call_logs`: Call events, agent notes, call outcomes, and timestamping.
    3.  `promises_to_pay`: Payment commitment deadlines, amounts, and active grace tracking.
    4.  `telecallers`: Profile matrix values, call statistics, and live online/offline presence tracking.

---

## 👤 Part 1: Telecaller Agent Guide

The Telecaller role focuses on dialing, structured call disposition recording, and logging payment commitments.

### 1. Daily Authentication
1.  Launch the application.
2.  Input your allocated username and password. 
3.  Once authenticated, your state dynamically transitions to **ONLINE** in the database. This presence status is immediately pushed to the Administrator Dashboard in real-time.

### 2. Utilizing the Smart Queue Optimizer
The **Smart Queue** automates workflow efficiency by automatically classifying customer files into list queues:
*   **General Queue:** Contains student profiles that require contact.
*   **Follow-Up Queue:** Contains students who have outstanding current PTP promises or active grace windows.
*   **Advanced Cooldown (24-Hour Lock):** A student profile is automatically filtered out of the general queue for 24 hours matching the last contact date to prevent spamming.
*   **Grace Timeline Window:** When a payment commitment is broken, the student profile transitions to the Follow-up queue for a **15-day grace period** before reverting to the General dialing pool.

### 3. Log Call Dispositions
When dialing a student:
1.  Tap **Call** on the active item in the queue.
2.  The interactive Call Workflow Overlay will open.
3.  Choose the outcome category:
    *   `ANSWERED`
    *   `RINGING_NO_ANSWER`
    *   `BUSY / DISCONNECTED`
4.  Enter your detail-oriented notes in the text input (such as reasons for fee delays, partial recovery claims, or scheduling).

### 4. Create Promise-To-Pay (PTP) Commitments
If a student commits to clear their arrears on a future date:
1.  Within the active dialing workflow card, check the **Create Promise to Pay (PTP)** checkbox.
2.  Select the **Promised Date** using the date selector.
3.  Specify the **Commitment Amount** (matching or under the total outstanding balance).
4.  Tap **Save Call Log**. The student's database status will update instantly to `PTP`.

---

## 🛡️ Part 2: Administrator Guide

The Administrator role focuses on dynamic configuration, performance monitoring, security policies, and backup control.

### 1. Real-Time Telecaller Monitor
*   Navigate to the **Admin Analytics Dashboard**.
*   Review the active **Telecaller Matrix Table**.
*   View live **ONLINE** or **OFFLINE** status indicators as agents log in or log out.
*   Track key performance indicators (KPIs) per agent including:
    *   Total dials made.
    *   Aggregate talk-time minutes recorded.
    *   Total count of successful PTP commitments backed up.

### 2. Multi-Category Portfolio Monitoring
*   View real-time high-level charts showing:
    *   Total Overdue Portfolio vs Recovered Capital.
    *   Portfolio Distribution by DPD Bucket (Days Past Due: e.g., 1-30, 31-60, 61-90, 90+ days).
    *   Segmented fee breakdowns (e.g. Regular Tuition, Hostel Outstanding, Lab Equipment, Library Arrears).
*   Review detail tables containing every call outcome logged in the field.

### 3. Compliance and Security Policy Control
Under **Security Settings**, toggle physical compliance parameters:
*   **Mask Student Contact Numbers:** Hides student phone numbers (`+91******789`) from agents to protect personal contact information.
*   **Hardware Token Binding:** Restricts application access securely to recognized IMEI/Device footprints.
*   **Block Screenshots:** Programmatically disables the device's screen-capture capability when viewing student data pages to prevent physical data extraction.

### 4. Cloud Integration Settings
Navigate to the **Sync Settings Console** to manage manual and automatic connections:
*   **Network Constraint (Wi-Fi Only):** Ensures large data archives only upload when connected to local unmetered broadband.
*   **Data Sync Console Logging:** Monitor real-time, color-coded diagnostic logs from the terminal showing step-by-step backup outcomes for debtors, call records, commitments, and system state settings.
*   **Force Manual Sync:** Tap `SYNC NOW` to immediately push current local tables to Cloud Firestore.
*   **Deep Purge & Reset (`RESET DB`):** Purges all local databases to a clean, empty state and mirrors that clean state to your Firestore console—excellent for loading new batches of test cases or resetting between academic semesters.

---

## 🛠️ Developer Integration Notes

### Firestore Rule Deployment
To prevent unauthenticated reading/writing, deploy the following file content to your Google Cloud Console for Firestore Security Rules:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    function isAuthenticated() {
      return request.auth != null;
    }
    match /{document=**} {
      allow read, write: if isAuthenticated();
    }
  }
}
```
*Locate Firestore Rules in Firebase Console under **Build > Firestore Database > Rules**.*
