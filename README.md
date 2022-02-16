# ExampleAPI
This repository contains the ExampleAPI code for the Example project.

## Code structure
The main components of the source code can be found in `ExampleAPI/src/main/kotlin/`.

* `ExampleAPI/src/main/kotlin/endpoints/`
  
  * Contains the `RESTful` endpoints that will handle user requests.
* `ExampleAPI/src/main/kotlin/services/`
  
  * Contains the main business logic for the components
* `ExampleAPI/src/main/kotlin/repositories/`
  
  * Contains the `DAO` (Data Access Objects) that handle the database access.
  * Contains the `Mappers` that map the database result sets to `Java` objects
  
* Database Schema can be found in: `ExampleAPI/src/main/resources/schema_mysql.sql`

## Requirements 
Make sure you have `Java JRE`, `Java SDK` and `Maven` installed. At the moment we are using Java version 11

 - Install Java Runtime Environment
    ```
    sudo apt install openjdk-11-jre
    ```

- Install Java Development Kit
    ```
    sudo apt install openjdk-11-jdk
    ```

- Install Maven
    ```
    sudo apt install maven
    ```

### Additional tools
- Install XAMPP (https://www.apachefriends.org/faq_linux.html). XAMPP provides a local MySQL database, which is needed to run the API locally.
  
  - Start XAMPP
    ```
    sudo /opt/lampp/lampp start
    ```

  - Stop XAMPP
    ```
    sudo /opt/lampp/lampp stop
    ```

- Create test database
    * Create a local database on XAMPP called `example`
    * Execute the SQL queries found in `ExampleAPI/src/main/resources/schema_mysql.sql`

## IntelliJ IDEA configuration
* Make sure that the used Project SDK is 11
  
    File/Project Structure/Project Settings/Project SDK/11

* Add New SDK 11 (https://www.jetbrains.com/help/idea/sdk.html)
  
    File/Project Structure/Platform Settings/SDKs/11

## How to run backend
Run the main Kotlin application in `ExampleAPI/src/main/kotlin/ExampleApiApplication.kt`

## Additional Documentation
More documentation can be found in the `ExampleAPI/Documentation/` folder.