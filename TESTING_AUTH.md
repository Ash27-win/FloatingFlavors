# 🧪 Manual Testing Guide: Auth & Remember Me

Use this checklist to verify that your Authentication and "Remember Me" features are working exactly as intended.

---

## 1. The "Remember Me" Test (Happy Path) ✅
**Goal:** Verify that the app remembers you when the checkbox is selected.

1.  **Start State:** Ensure you are logged out (on the Login Screen).
2.  **Action:**
    *   Enter valid Email & Password.
    *   **Select Role:** User (or Delivery).
    *   **☑ CHECK** the "Remember Me" box.
    *   Click **Login**.
3.  **Observation:** App should navigate to the **Home Screen**.
4.  **The Test:**
    *   **Minimize the app** and swipe it away (Force Close / Kill App). 💀
    *   Re-open the app.
5.  **Expected Result:**
    *   You see the Splash Screen with the delay (1.5s).
    *   The app **automatically navigates to the Home Screen**.
    *   You are **NOT** asked to login again.
    *   *Result:* Pass 🟢 / Fail 🔴

---

## 2. The "Don't Remember Me" Test (Public Device Mode) 🛡️
**Goal:** Verify that the app forces a login if the user opted out.

1.  **Start State:** Log out if you are logged in.
2.  **Action:**
    *   Enter valid Email & Password.
    *   **Select Role:** User.
    *   **☐ UNCHECK** the "Remember Me" box.
    *   Click **Login**.
3.  **Observation:** App navigates to Home Screen.
4.  **The Test:**
    *   **Force Close / Kill App**. 💀
    *   Re-open the app.
5.  **Expected Result:**
    *   Splash Screen appears.
    *   The app **navigates to the LOGIN Screen**.
    *   *Result:* Pass 🟢 / Fail 🔴

---

## 3. The Logout Security Test 🔒
**Goal:** Verifying that logging out completely wipes the session.

1.  **Start State:** Logged in.
2.  **Action:**
    *   Go to **Settings** (or Profile).
    *   Click **Log Out**.
3.  **Observation:** App returns to Login Screen.
4.  **The Test:**
    *   **Force Close / Kill App**.
    *   Re-open the app.
5.  **Expected Result:**
    *   Splash Screen -> **Login Screen**.
    *   (The app must not "remember" the previous session because you explicitly logged out).
    *   *Result:* Pass 🟢 / Fail 🔴

---

## 4. Multi-Role Session Test 👥
**Goal:** Verify that Delivery and User data don't mix.

1.  **Action:** Login as **USER** (ID: 1). Check data (e.g., Profile Name).
2.  **Action:** Logout.
3.  **Action:** Login as **DELIVERY** (ID: 4 or similar).
    *   Go to **Profile**.
4.  **Expected Result:**
    *   Profile should show **Delivery Partner Details** (not User details).
    *   Bottom Navigation should be the **Delivery Dashboard**.
    *   *Result:* Pass 🟢 / Fail 🔴

---

## 🔍 How to Debug if it Fails?
If any test fails, check the **Logcat** in Android Studio:
- Search tag: `LOGIN` -> See if userId is printed.
- Search tag: `Splash` -> See if it says "Auto-login success" or "Navigating to Login".
