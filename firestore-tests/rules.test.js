// Run with: firebase emulators:exec --only firestore --project demo-shopp "npm test --prefix firestore-tests"
import { readFileSync } from 'node:fs';
import { before, after, beforeEach, describe, it } from 'node:test';
import { initializeTestEnvironment, assertFails, assertSucceeds } from '@firebase/rules-unit-testing';
import {
  doc, getDoc, setDoc, updateDoc, deleteDoc, writeBatch, serverTimestamp, arrayUnion,
} from 'firebase/firestore';

const PRODUCTS = 'data/stock/products';
let env;

before(async () => {
  env = await initializeTestEnvironment({
    projectId: 'demo-shopp',
    firestore: { rules: readFileSync(new URL('../firestore.rules', import.meta.url), 'utf8') },
  });
});
after(() => env.cleanup());
beforeEach(() => env.clearFirestore());

const db = (uid) => env.authenticatedContext(uid).firestore();
const seed = (fn) => env.withSecurityRulesDisabled((ctx) => fn(ctx.firestore()));

const validSeller = () => ({ shopName: 'Asha Traders', phone: '9876543210', status: 'approved', createdAt: serverTimestamp() });
const validProduct = (sellerId, extra = {}) => ({
  sellerId, title: 'Cotton Shirt', description: 'Soft', category: 'fashion', price: '999', actualPrice: '799.50',
  images: ['https://example.com/a.jpg'], stock: 5, active: true, otherDetails: {}, ...extra,
});
const makeSeller = (uid) => seed(async (d) => {
  await setDoc(doc(d, 'users', uid), { roles: ['customer', 'seller'] });
  await setDoc(doc(d, 'sellers', uid), { shopName: 'Shop', phone: '9876543210', status: 'approved' });
});
const order = (extra = {}) => ({
  userId: 'buyer', sellerIds: ['seller1'], status: 'ORDERED', items: { p1: 1 }, ...extra,
});

describe('users and roles', () => {
  it('owner can update their own cart but not other users', async () => {
    await assertSucceeds(setDoc(doc(db('u1'), 'users', 'u1'), { cartItems: { p1: 1 } }, { merge: true }));
    await assertFails(setDoc(doc(db('u2'), 'users', 'u1'), { cartItems: { p1: 1 } }, { merge: true }));
  });

  it('cannot grant yourself the seller role directly', async () => {
    await assertFails(setDoc(doc(db('u1'), 'users', 'u1'), { roles: ['customer', 'seller'] }, { merge: true }));
  });

  it('can gain the seller role together with a valid seller document', async () => {
    const d = db('u1');
    const batch = writeBatch(d);
    batch.set(doc(d, 'sellers', 'u1'), validSeller());
    batch.set(doc(d, 'users', 'u1'), { roles: arrayUnion('customer', 'seller') }, { merge: true });
    await assertSucceeds(batch.commit());
  });

  it('cannot gain the seller role with an invalid seller document', async () => {
    const d = db('u1');
    const batch = writeBatch(d);
    batch.set(doc(d, 'sellers', 'u1'), { ...validSeller(), phone: '123' });
    batch.set(doc(d, 'users', 'u1'), { roles: arrayUnion('customer', 'seller') }, { merge: true });
    await assertFails(batch.commit());
  });

  it('seller profiles are private to their owner', async () => {
    await seed((d) => setDoc(doc(d, 'sellers', 'u1'), validSeller()));
    await assertSucceeds(getDoc(doc(db('u1'), 'sellers', 'u1')));
    await assertFails(getDoc(doc(db('u2'), 'sellers', 'u1')));
  });
});

describe('addresses', () => {
  const address = { label: 'Home', recipient: 'Asha', phone: '9876543210', street: '12 Test Street', city: 'Bengaluru', pincode: '560001', primary: true };

  it('owner can write a valid address, others cannot read it', async () => {
    await assertSucceeds(setDoc(doc(db('u1'), 'users/u1/addresses/a1'), address));
    await assertFails(getDoc(doc(db('u2'), 'users/u1/addresses/a1')));
  });

  it('rejects malformed addresses', async () => {
    await assertFails(setDoc(doc(db('u1'), 'users/u1/addresses/a1'), { ...address, phone: '12345' }));
    await assertFails(setDoc(doc(db('u1'), 'users/u1/addresses/a1'), { ...address, pincode: '056001' }));
    await assertFails(setDoc(doc(db('u1'), 'users/u1/addresses/a1'), { ...address, extra: 'x' }));
  });
});

describe('products', () => {
  it('signed-in users can read the catalogue, signed-out cannot', async () => {
    await seed((d) => setDoc(doc(d, PRODUCTS, 'p1'), validProduct('seller1')));
    await assertSucceeds(getDoc(doc(db('anyone'), PRODUCTS, 'p1')));
    await assertFails(getDoc(doc(env.unauthenticatedContext().firestore(), PRODUCTS, 'p1')));
  });

  it('only sellers can create products, and only as themselves', async () => {
    await makeSeller('seller1');
    await assertSucceeds(setDoc(doc(db('seller1'), PRODUCTS, 'p1'), validProduct('seller1')));
    await assertFails(setDoc(doc(db('seller1'), PRODUCTS, 'p2'), validProduct('someone-else')));
    await assertFails(setDoc(doc(db('customer'), PRODUCTS, 'p3'), validProduct('customer')));
  });

  it('rejects malformed products', async () => {
    await makeSeller('seller1');
    await assertFails(setDoc(doc(db('seller1'), PRODUCTS, 'p1'), validProduct('seller1', { price: 'free' })));
    await assertFails(setDoc(doc(db('seller1'), PRODUCTS, 'p1'), validProduct('seller1', { stock: -1 })));
    await assertFails(setDoc(doc(db('seller1'), PRODUCTS, 'p1'), validProduct('seller1', { images: [] })));
  });

  it('sellers edit and delete only their own products', async () => {
    await makeSeller('seller1');
    await makeSeller('seller2');
    await seed((d) => setDoc(doc(d, PRODUCTS, 'p1'), validProduct('seller1')));
    await assertSucceeds(updateDoc(doc(db('seller1'), PRODUCTS, 'p1'), { active: false }));
    await assertFails(updateDoc(doc(db('seller2'), PRODUCTS, 'p1'), { active: false }));
    await assertFails(deleteDoc(doc(db('seller2'), PRODUCTS, 'p1')));
    await assertSucceeds(deleteDoc(doc(db('seller1'), PRODUCTS, 'p1')));
  });

  it('anyone can lower stock but nothing else, and never raise it', async () => {
    await seed((d) => setDoc(doc(d, PRODUCTS, 'p1'), validProduct('seller1', { stock: 5 })));
    await assertSucceeds(updateDoc(doc(db('buyer'), PRODUCTS, 'p1'), { stock: 3 }));
    await assertFails(updateDoc(doc(db('buyer'), PRODUCTS, 'p1'), { stock: 10 }));
    await assertFails(updateDoc(doc(db('buyer'), PRODUCTS, 'p1'), { stock: -1 }));
    await assertFails(updateDoc(doc(db('buyer'), PRODUCTS, 'p1'), { stock: 1, title: 'Hacked' }));
  });
});

describe('orders', () => {
  beforeEach(() => seed((d) => setDoc(doc(d, 'orders', 'o1'), order())));

  it('buyer and named seller can read; others cannot', async () => {
    await assertSucceeds(getDoc(doc(db('buyer'), 'orders', 'o1')));
    await assertSucceeds(getDoc(doc(db('seller1'), 'orders', 'o1')));
    await assertFails(getDoc(doc(db('stranger'), 'orders', 'o1')));
  });

  it('buyers can create only their own orders', async () => {
    await assertSucceeds(setDoc(doc(db('buyer'), 'orders', 'o2'), order()));
    await assertFails(setDoc(doc(db('buyer'), 'orders', 'o3'), order({ userId: 'someone-else' })));
  });

  it('buyer can cancel a fresh order, not edit anything else', async () => {
    await assertSucceeds(updateDoc(doc(db('buyer'), 'orders', 'o1'), { status: 'CANCELLED', cancelledAt: serverTimestamp() }));
    await assertFails(updateDoc(doc(db('buyer'), 'orders', 'o1'), { status: 'DELIVERED' }));
  });

  it('buyer cannot cancel once the order is packed', async () => {
    await seed((d) => setDoc(doc(d, 'orders', 'o1'), order({ status: 'PACKED' })));
    await assertFails(updateDoc(doc(db('buyer'), 'orders', 'o1'), { status: 'CANCELLED' }));
  });

  it('seller moves an order forward one valid step at a time', async () => {
    const ref = (uid) => doc(db(uid), 'orders', 'o1');
    await assertFails(updateDoc(ref('seller1'), { status: 'SHIPPED' })); // skips a step
    await assertSucceeds(updateDoc(ref('seller1'), { status: 'PACKED', statusUpdatedAt: serverTimestamp() }));
    await assertFails(updateDoc(ref('seller1'), { status: 'ORDERED' })); // no going back
    await assertSucceeds(updateDoc(ref('seller1'), { status: 'SHIPPED' }));
    await assertSucceeds(updateDoc(ref('seller1'), { status: 'DELIVERED' }));
  });

  it('only a seller named on the order may advance it, and only the status changes', async () => {
    await assertFails(updateDoc(doc(db('stranger'), 'orders', 'o1'), { status: 'PACKED' }));
    await assertFails(updateDoc(doc(db('seller1'), 'orders', 'o1'), { status: 'PACKED', userId: 'seller1' }));
  });
});

describe('reviews', () => {
  const review = (extra = {}) => ({
    userId: 'buyer', userName: 'Asha', orderId: 'o1', rating: 5, text: 'Great', updatedAt: serverTimestamp(), ...extra,
  });
  const path = `${PRODUCTS}/p1/reviews/buyer`;

  it('a buyer of a delivered order can review the product once, as themselves', async () => {
    await seed((d) => setDoc(doc(d, 'orders', 'o1'), order({ status: 'DELIVERED' })));
    await assertSucceeds(setDoc(doc(db('buyer'), path), review()));
    await assertFails(setDoc(doc(db('stranger'), path), review({ userId: 'stranger' })));
  });

  it('rejects reviews for orders that are not delivered or do not contain the product', async () => {
    await seed((d) => setDoc(doc(d, 'orders', 'o1'), order({ status: 'SHIPPED' })));
    await assertFails(setDoc(doc(db('buyer'), path), review()));
    await seed((d) => setDoc(doc(d, 'orders', 'o1'), order({ status: 'DELIVERED', items: { other: 1 } })));
    await assertFails(setDoc(doc(db('buyer'), path), review()));
  });

  it('rejects invalid ratings', async () => {
    await seed((d) => setDoc(doc(d, 'orders', 'o1'), order({ status: 'DELIVERED' })));
    await assertFails(setDoc(doc(db('buyer'), path), review({ rating: 6 })));
    await assertFails(setDoc(doc(db('buyer'), path), review({ rating: 0 })));
  });
});

describe('coupons', () => {
  it('are readable but never writable from the app', async () => {
    await seed((d) => setDoc(doc(d, 'coupons', 'WELCOME10'), { type: 'PERCENT', value: 10 }));
    await assertSucceeds(getDoc(doc(db('u1'), 'coupons', 'WELCOME10')));
    await assertFails(setDoc(doc(db('u1'), 'coupons', 'HACK'), { type: 'PERCENT', value: 100 }));
  });
});
