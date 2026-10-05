const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// Test 1: Anyone can read services
test("Unauthenticated user: can read services directory", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(unauthDb.collection("services").get());
});

// Test 2: Unauthenticated user cannot create service
test("Unauthenticated user: cannot create service", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("services").doc("p1").set({
      name: "Garage Test",
      category: "MECANICIEN",
      city: "Bamako",
      phone: "+223 70 00 00 00"
    })
  );
});

// Test 3: Authenticated user can create valid service
test("Authenticated user: can create valid service", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("services").doc("p1").set({
      name: "Garage Alice",
      category: "MECANICIEN",
      city: "Bamako",
      phone: "+223 70 11 22 33",
      ownerId: ALICE_UID
    })
  );
});

// Test 4: Authenticated user cannot access Bob's bookings
test("Authenticated user: cannot read another user's bookings", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("bookings").doc("b1").set({
      id: "b1",
      userId: BOB_UID,
      providerId: "p1",
      serviceType: "Vidange",
      clientName: "Bob",
      date: "2026-10-10",
      status: "CONFIRME"
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("bookings").doc("b1").get());
});

// Test 5: Authenticated user can create their own booking
test("Authenticated user: can create their own booking", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("bookings").doc("b2").set({
      id: "b2",
      userId: ALICE_UID,
      providerId: "p1",
      serviceType: "Diagnostic",
      clientName: "Alice",
      date: "2026-10-12",
      status: "EN_ATTENTE"
    })
  );
});
