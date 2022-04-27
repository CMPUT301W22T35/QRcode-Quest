# QRcode-Quest

### What is QRCodeQuest?
QRCodeQuest is an assignment project of UofA written by us (see [this page](../../wiki) for a list of the members). Our team's goal is to develop a project that is testable, maintainable and appropriately documented under the time constraint of one course term. QRCodeQuest is an Android Studio java project with Javadoc documentation available in the codebase, and you may learn more about its status and design by looking at the [wiki](../../wiki) page of this repository.

A video demo of the project is available [here](https://youtu.be/Tvy3v0vWhqk).

### Build Dependencies
Our project depends on the following components (see the app level gradle file for more details.):
- Android Navigation
- OpenStreetMap
- Firebase Firestore and Storage
- ZXing
- JUnit
- Espresso
- Mockito

In order to build our application, you need to add the google-services.json file from a project you created on Google's Firestore console to the project directory. After that is done, open it with Android Studio and build the source code.

### Game Description
In QRCodeQuest, the main objective is capturing QR codes to score points to a player's account. 

Our game features convenient account management. Whether you are a player or a product owner, login to the app can be done via multiple ways: The first time you open the app, you can create a new account by simply choosing a username. For the second time and onwards, You will automatically login to the home. If you want to transfer an account to another device, you may use the QR code sharing feature in the app. For the product owner, you may delete a player's account or a particular QR code by searching it up in the leaderboard and delete it using the delete button.

After login, you can look at the local map to determine the nearby QR codes that have been captured by other players. You can then navigates to a QR code on the map and capture it - the code will be recorded to your account to help you to get a higher score on the leaderboard. The game has a variety of stats recorded for a player: highest points QR code, number of codes captured, total scores and etc. You can see them in a leaderboard - and don't forget to make a comment under an interesting QR code you capture/discover on the leaderboard.

### What did we learn?
In this project, we started with little knowledge of Android development. In the development process, we learnt to keeping track of user stories and maintaining the UI mockups and UML of the project. We applied agile practices and experimented with one week's standup meetings. by working with various libraries of Android Studio, we learnt how to work with permissions, view models, Android life cycles and database asynchronous calls. We learnt to write unit tests to specify requirements and check if the said requirements are met for any particular commit. We learnt to work with multiple Git branches and setup Git Action to automate unit tests.

### Acknowledgement
We'd like to thank everybody in the team for completing this assignment, in particular Jayden who put a lot of efforts into this project, as well as our TA and professor Ildar Akhmetov for giving us advice on this project.

The QR capture feature of our app is modified from the ZXing library by the zxing authors, as noted in the source code.
