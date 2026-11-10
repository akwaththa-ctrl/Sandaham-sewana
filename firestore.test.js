const { initializeTestEnvironment, assertFails, assertSucceeds } = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: "demo-no-project",
    firestore: {
      rules: fs.readFileSync("firestore.rules", "utf8"),
      host: "127.0.0.1",
      port: 8085
    }
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

test("unauthenticated user cannot read sermons", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("sermons").get());
});

test("authenticated user can read sermons", async () => {
  const aliceDb = testEnv.authenticatedContext("alice").firestore();
  await assertSucceeds(aliceDb.collection("sermons").get());
});

test("authenticated user can create sermon with valid schema", async () => {
  const aliceDb = testEnv.authenticatedContext("alice").firestore();
  await assertSucceeds(aliceDb.collection("sermons").add({
    userId: "alice",
    title: "කරණීය මෙත්ත සූත්‍රය",
    thero: "මහා සංඝරත්නය",
    category: "මහා පිරිත්",
    audioUrl: "https://example.com/audio.mp3",
    createdAt: new Date(),
    updatedAt: new Date()
  }));
});

test("user cannot create sermon with another user's ID", async () => {
  const aliceDb = testEnv.authenticatedContext("alice").firestore();
  await assertFails(aliceDb.collection("sermons").add({
    userId: "bob",
    title: "කරණීය මෙත්ත සූත්‍රය",
    thero: "මහා සංඝරත්නය",
    category: "මහා පිරිත්",
    audioUrl: "https://example.com/audio.mp3",
    createdAt: new Date(),
    updatedAt: new Date()
  }));
});

test("user can manage their own favorites", async () => {
  const aliceDb = testEnv.authenticatedContext("alice").firestore();
  await assertSucceeds(aliceDb.collection("users").doc("alice").collection("favorites").doc("sermon1").set({
    userId: "alice",
    sermonId: "sermon1",
    createdAt: new Date()
  }));

  const bobDb = testEnv.authenticatedContext("bob").firestore();
  await assertFails(bobDb.collection("users").doc("alice").collection("favorites").doc("sermon1").get());
});
