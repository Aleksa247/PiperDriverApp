PiperDriverApp

An Android application for Piper, a golf cart ride-sharing platform operating in the Scottsdale/Phoenix area. Built by a 5-person Agile team as a year-long industry capstone project at Arizona State University, presented at the ASU Ira A. Fulton Schools of Engineering Showcase (May 2026).


About the Project

Piper connects riders with drivers using golf carts for short-distance trips in the Scottsdale area. This repository contains the driver-side Android app, responsible for managing ride requests, driver authentication, and in-app support.

The project followed an Agile development workflow with regular sprints, code reviews, and team collaboration across both iOS and Android platforms.


My Contributions

I served as an Android Developer on this project, owning the following features end-to-end:


Login Screen — Implemented user authentication flow with form validation and session handling
Settings Screen — Built a full settings interface including notification preferences and account management
Zendesk Integration — Integrated Zendesk ticket creation so drivers can submit support requests directly from the app, including a custom Help screen UI backed by a ViewModel, Repository, and Retrofit network layer
Logout Functionality — Implemented secure session teardown and navigation back to the login flow



Tech Stack


Language: Kotlin
Architecture: MVVM + Clean Architecture (Repository pattern)
Dependency Injection: Hilt
Networking: Retrofit
Async: Kotlin Coroutines + ViewModelScope
Support: Zendesk SDK
Build System: Gradle (Kotlin DSL)



Team

This was a collaborative project built with a 5-person team. Contributors are listed on the repository's GitHub page.
