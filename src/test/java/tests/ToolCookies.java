package tests;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;


public class ToolCookies {

    public static final String email = "qaz@ya.ru";
    public static final String password = "123456";
    public static final String info = "/login";


    public String getCookies() {
        return given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("Email", email)
                .formParam("Password", password)
                .formParam("RememberMe", "false")
                .when()
                .post(baseURI + "/login")
                .then()
                .log().all()
                .statusCode(302)
                .extract().cookie("NOPCOMMERCE.AUTH");
    }
}