# DriverAppAndroid

This application uses scalable folder structure based on modern Android clean architecture, using Hilt, MVVM, Repository pattern, and modular separation
 
 
com.piperrideshare.driver/
│
├── api/                    // Retrofit interfaces, WebSocket, and DTO models
│   ├── models/
│   │   ├── request/
│   │   └── response/
│   ├── ApiService.kt
│   └── WebSocketHandler.kt
│
├── data/                   // Data-related implementations (local & remote)
│   ├── repository/         // Implements domain repository interfaces
│   └── local/              // (Optional) Room DB, DataStore, etc.
│
├── di/                     // Hilt modules (AppModule, NetworkModule, etc.)
│
├── domain/                 // Pure business logic layer
│   ├── model/              // Business models (clean versions)
│   ├── repository/         // Abstract interfaces used in the domain layer
│   └── usecase/            // Optional: encapsulate individual operations
│
├── services/               // SessionManager, ForegroundServices, etc.
│
├── ui/                     // Everything related to the UI
│   ├── components/         // Reusable UI components (composables, views)
│   ├── screens/            // One folder per screen (login, home, etc.)
│   │   └── login/
│   │       ├── LoginScreen.kt
│   │       └── LoginViewModel.kt
│   └── theme/              // Theme, typography, colors
│
├── utils/                  // Extension functions, constants, helpers
│
├── MainActivity.kt
└── PiperApp.kt             // Application class with @HiltAndroidApp