# 🛒 ShopApp - Modern E-Commerce App Built with Jetpack Compose

A modern Android E-Commerce application built using **Jetpack Compose**, **Kotlin**, **Firebase Authentication**, **Cloud Firestore**, and **Razorpay Payment Gateway**.

ShopApp delivers a complete shopping experience with real-time product updates, wishlist management, secure authentication, cart operations, and seamless online payments.

---

## 📱 Screenshots

> screenshots of application (Soon)

| Home Screen | Product Details | Cart       | Profile    |
| ----------- | --------------- | ---------- | ---------- |
| Screenshot  | Screenshot      | Screenshot | Screenshot |

---

## ✨ Features

### 🔐 Authentication System

* Firebase Email & Password Authentication
* Secure User Registration
* Login & Logout Functionality
* Persistent Authentication Sessions

### 👤 User Profile Management

* Dynamic User Profile
* Editable Delivery Address
* Real-Time Firestore Updates
* Cart Item Count Indicator

### 🛍️ Product Catalog

* Dynamic Promotional Banner Slider
* Category-Based Product Browsing
* Firestore-Powered Product Streaming
* Responsive Two-Column Product Grid
* Product Search & Discovery Experience

### 📦 Product Details

* Multiple Product Images
* Detailed Product Descriptions
* Product Specifications Engine
* Price & Discount Information

### 🛒 Shopping Cart System

* Add to Cart
* Remove from Cart
* Quantity Increment & Decrement
* Real-Time Firestore Synchronization
* Automatic Price Calculation

### ❤️ Wishlist System

* Local Wishlist Storage
* SharedPreferences Integration
* Fast Product Bookmarking
* Persistent Wishlist Data

### 💳 Checkout & Payments

* Dynamic Order Summary
* Tax Calculation
* Discount Processing
* Razorpay Payment Integration
* Secure Online Transactions

### 📄 Order Management

* Order Placement
* Order Storage in Firestore
* Order Tracking Architecture
* Purchase History Support

---

## 🏗️ Architecture

The application follows modern Android development principles:

### Architecture Pattern

* Clean Architecture
* Unidirectional Data Flow (UDF)
* State-Driven UI
* Reactive Data Handling

### Project Layers

```text
Presentation Layer
│
├── Screens
├── Components
├── Navigation
└── ViewModels

Domain Layer
│
├── Models
├── Use Cases
└── Business Logic

Data Layer
│
├── Firebase Firestore
├── Firebase Auth
├── SharedPreferences
└── Repositories
```

---

## 🛠️ Tech Stack

| Technology              | Usage                |
| ----------------------- | -------------------- |
| Kotlin                  | Programming Language |
| Jetpack Compose         | UI Toolkit           |
| Firebase Authentication | User Authentication  |
| Cloud Firestore         | Database             |
| Coil                    | Image Loading        |
| Razorpay SDK            | Payment Processing   |
| Compose Navigation      | App Navigation       |
| SharedPreferences       | Local Storage        |
| Material 3              | UI Design System     |

---

## 🔥 Firebase Integration

### Authentication

* Email/Password Sign-In
* User Session Management
* Account Registration

### Firestore

* Real-Time Data Streaming
* Product Catalog Storage
* User Information Storage
* Order Management

---

## 📂 Firestore Database Structure

```text
data
└── stock
    ├── categories
    │   └── category_id
    │       ├── id
    │       ├── name
    │       └── imageUrl
    │
    └── products
        └── product_id
            ├── id
            ├── title
            ├── description
            ├── price
            ├── actualPrice
            ├── category
            ├── images[]
            └── otherDetails{}

users
└── user_uid
    ├── uid
    ├── name
    ├── email
    ├── address
    └── cartItems{}

orders
└── order_id
    ├── id
    ├── userId
    ├── date
    ├── status
    ├── address
    └── items{}
```

---

## 💳 Razorpay Setup

Generate API keys from the Razorpay Dashboard and update:

```kotlin
fun getRazorpayApiKey(): String {
    return "RAZORPAY_TEST_KEY_ID"
}
```

---

## 🚀 Getting Started

### Prerequisites

* Android Studio Ladybug or newer
* Kotlin 2.0+
* Firebase Project
* Razorpay Account

### Installation

#### 1. Clone Repository

```bash
git clone https://github.com/Vaibhav-P1/Shopp---An-Ecom-App.git
```

#### 2. Open Project

```bash
Open in Android Studio
```

#### 3. Configure Firebase

* Create Firebase Project
* Register Android App
* Download `google-services.json`
* Place file inside:

```text
app/google-services.json
```

#### 4. Enable Firebase Services

* Authentication → Email/Password
* Cloud Firestore Database

#### 5. Configure Razorpay

Add your Razorpay Test Key.

#### 6. Build & Run

```bash
Shift + F10
```

or

```bash
Run ▶
```

---

## 📈 Future Enhancements

### Phase 2

* Product Search
* Order Tracking
* Push Notifications
* User Reviews & Ratings
* Product Filtering

### Phase 3

* Coupon System
* Inventory Dashboard
* Admin Panel
* AI Product Recommendations
* Multi-Vendor Support

### Phase 4

* Google Sign-In
* UPI Payment Support
* Dark Mode
* Offline Caching
* Analytics Dashboard

---

## 🎯 Learning Outcomes

This project demonstrates:

* Modern Android Development
* Jetpack Compose UI Design
* Firebase Integration
* State Management
* Real-Time Database Operations
* Payment Gateway Integration
* Clean Architecture Principles
* Local Data Persistence

---

## 🤝 Contributing

Contributions are welcome.

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to your branch
5. Open a Pull Request

---

## 📄 License

Licensed under the MIT License.

---

## 👨‍💻 Author

**Vaibhav Pandey**

* GitHub: https://github.com/Vaibhav-P1
* LinkedIn: https://www.linkedin.com/in/vaibhav-pandey-351308329

If you found this project useful, don't forget to ⭐ the repository.
