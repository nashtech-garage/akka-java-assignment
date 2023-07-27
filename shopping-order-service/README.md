## Running the sample code

1. DB Engine

The application needs a PostgreSQL database up and running. If you have not existing one, you could use docker by using docker-compose in the docker-compose.yml.

2. DB Scripts Execution

Execute DB Script to create the table located in src/main/resources/schema.sql

3. Start the application

    ```
    mvn compile exec:exec 
    
    ```

	`Or open IDE and run the main class in Main.java`
4. Try it

Open the Postman and import the script located in folder /scripts

