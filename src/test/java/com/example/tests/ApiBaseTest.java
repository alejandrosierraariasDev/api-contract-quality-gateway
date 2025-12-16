package com.example.tests;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import io.restassured.RestAssured;
import io.github.cdimascio.dotenv.Dotenv;


public class ApiBaseTest {
    protected static RequestSpecification requestSpec;

    @BeforeAll
    public static void setup() {
        Dotenv dotenv = Dotenv.load();
        String baseUri = dotenv.get( "BASE_URL");

        requestSpec = new RequestSpecBuilder()
                .setBaseUri(baseUri)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build();
        
        // Make sure RestAssured is using our spec
        RestAssured.requestSpecification = requestSpec;
    }
}