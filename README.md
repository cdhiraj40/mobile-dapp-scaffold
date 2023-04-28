# Solana Mobile dApp Scaffold
The Solana Mobile dApp scaffold is a repository template providing a fast and efficient way for developers to build decentralized mobile applications on the Solana blockchain. With the scaffold, developers can quickly set up and get started with building their Solana mobile dApp. The scaffold includes pre-built components integrated with features, such as wallet connection, airdrop SOL(DEVNET/TESTNET), sign messages and send transactions.
Scaffold follows clean architecture and MVVM principles, enabling efficient and robust mobile app development.

[Believe me, There is no better time to build consumer mobile dApps.](https://twitter.com/intent/tweet?text="Believe%20me%2C%20there%20is%20no%20better%20time%20to%20build%20consumer%20mobile%20dApps.%22%20-%20%40cdhiraj40%0A%0AStart%20building%20on%20%40solanamobile%20today!&url=https%3A%2F%2Fgithub.com%2Fcdhiraj40%2Fmobile-dapp-scaffold%2F%20)

## Screenshots

<table>
  <thead>
    <tr>
      <th><h3>Home</h3></th>
      <th><h3>Dashboard</h3></th>
    </tr>
  </thead>
  <tbody>
  <tr>
    <td><img src="https://user-images.githubusercontent.com/75211982/232579436-c54da8e8-ee31-465c-a921-9227bedc34c0.png" alt="Image 1" width="400"></td>
    <td><img src="https://user-images.githubusercontent.com/75211982/232579459-c5858817-52bb-4382-9d77-4cd989cdc860.png" alt="Image 2" width="400"></td>
  </tr>
    </tbody>
</table>

## Getting Started 

To start using scaffold, follow the below steps:
1. Click on "Use this template" dropdown.
2. Select "Create a new repository".

## Structure

By adhering to best practices for architecture and design patterns, the scaffold ensures that developers can create applications that are easy to maintain and extend, with a separation of concerns between the data , domain and presentation layer.<br/>Below is the structure for Mobile dApp Scaffold considering from [app/src/main](https://github.com/cdhiraj40/mobile-dapp-scaffold/tree/main/app/src/main).
```
├── java/com/example/solanamobiledappscaffold : App's core logic
│   ├── Common : common classes used through of the project
│   ├── data : folder containing Data layer classes
│   │   ├── di : should house the dependency injection components
│   │   ├── remote : contains classes and interfaces related to handling network requests and responses, as well as remote data sources such as APIs etc
│   │   ├── repository : should contain abstract data sources behind a single interface for the domain layer's use
│   ├── domain : folder containing Domain layer classes
│   │   ├── model : should contain data models and and business logic that define the data
│   │   ├── repository : contains interfaces that the data layer implementation must conform to, providing a separation between the domain and data layer
│   │   ├── use_case : contains specific business logic or operations that the application needs to perform
│   │   ├── utils : reusable utility classes or helper functions
│   ├── presentation : folder containing Presentation layer classes, typically activities, fragments, viewModels etc.
├── res : folder contains application resources such as layouts, strings, and drawable assets, that are used by the UI. 
```


## 👨‍💻 Contributing

- Any contributions you make to this project is **greatly appreciated**.

## Bug / Feature Request

If you find a bug in the app, kindly open an issue [here](https://github.com/cdhiraj40/mobile-dapp-scaffold/issues/new?assignees=&labels=bug&template=bug_report.md&title=%5BBug%5D%3A+) to report it by
including a proper description of the bug and the expected result. https://github.com/cdhiraj40/mobile-dapp-scaffold/issues/new?assignees=&labels=enhancement&template=feature_request.md&title=%5BFeature+Request%5D%3A+ are appreciated too. If you feel like a certain feature is missing, feel free to create an issue to discuss with the maintainers.
