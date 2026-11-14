# Firestore security review notes

## Data paths found in the Android source

- `users/{uid}`: profile document written and read by `AccountStore`. Fields: id, firstName, lastName, age, license, login, imageUri, gender, location, role, companyName, and approvalStatus.
- `users/{uid}/completed_orders/{orderId}`: completed delivery history written by `DeliveryManager` and read ordered by timestamp descending. Fields: id, customerName, price, distanceKm, pickup, delivery, completedAt, timestamp.
- `orders/{orderId}`: shared delivery requests created by clients or companies, listened to by the owner and by drivers for requested/assigned work. Fields: ownerUid, ownerRole, customerName, pickup, delivery, recipientName, recipientPhone, packageDescription, status, driverUid, price, distanceKm, createdAt, and optional updatedAt.
- Order status transitions are restricted to requested -> cancelled by its owner, requested -> accepted by a driver, accepted -> picked_up by the assigned driver, and picked_up -> delivered by the assigned driver.

## Access assumptions

- Email/password Firebase Auth is the only account provider used by the app.
- A profile and its completed-order history are private to the owning UID.
- Clients and companies can read only requests whose ownerUid is their authenticated UID. Drivers can read only requested jobs and jobs assigned to their UID.
- The role values are client, driver, or company. They authorize only the order actions described by the rules; they do not grant company-wide access to other user accounts or administrative access.
- A user may choose an account type at signup, but cannot change it afterward. Client profiles are approved on creation; driver and company profiles are created as pending.
- Only an administrator using the Firebase Console can change a driver or company profile's approvalStatus to approved. Client rules prevent users from changing their own approval status.
- Approved status is required for both driver and company order actions. Pending accounts can authenticate but cannot use delivery features.
- Delivery history is writable only by its owning, approved driver account.

## Manual adversarial review

- Unauthenticated list/read/write: denied by the owner check.
- Reading or writing another user's profile/history: denied because the path UID must equal the authenticated UID.
- Changing an existing profile role: denied because update requires the stored role to remain unchanged; legacy profiles without a role are treated as drivers.
- Creating a profile under another UID: denied because both the path UID and profile id must equal request.auth.uid.
- Reading another client's/company's order or listing unrelated orders: denied by owner UID and driver status/assignment checks; query filters must satisfy those rules.
- Creating an order for another UID or with a mismatched role: denied by owner UID and stored profile-role checks.
- Changing order addresses, recipient details, pricing, distance, or ownership after creation: denied by immutable-field checks.
- Skipping or forging a driver status transition, or updating another driver's job: denied by assigned UID and transition checks.
- Oversized strings or unexpected fields: denied by length limits and hasOnly checks.
- Wrong field types or invalid driver/company fields: denied by validUser.
- Creating or modifying completed-order history as a client/company: denied by the driver-role check.
- Self-selecting driver or company at signup no longer grants order permissions: the profile is pending and `roleIs` requires approvalStatus == approved. The remaining risk is operational: an administrator must verify applicants before changing that field. Company account approval is manual and does not verify legal business identity.

## Validation status

Rules and client code were updated locally; rules still need to be published to the named `fast-delivery-app` database in Firebase Console. No Emulator Suite or adversarial rules test has been run, so treat them as unverified until tested.
