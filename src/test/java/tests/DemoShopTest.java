package tests;

import io.qameta.allure.Link;
import io.qameta.allure.Owner;
import io.restassured.http.Cookies;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static listeners.CustomAllureListener.withCustomTemplates;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static tests.ToolCookies.email;

public class DemoShopTest {

    public Cookies cookies;
    ToolCookies app = new ToolCookies();

    @Owner(" *** QA.GURU ***")
    @BeforeAll
    static void beforeAll() {
        baseURI = "http://demowebshop.tricentis.com";
        filters(withCustomTemplates());
    }

    @Test
    @DisplayName("Регистрация на сайте ")
    void checkLoginWithCookies() {
        String cookie = app.getCookies();

        String str = given()
                .cookie("NOPCOMMERCE.AUTH", cookie)
                .when()
                .get("/info")
                .then()
                .extract().asString();

        assertThat(str.contains(email)).isTrue();
    }

    @Test
    @DisplayName(" Попытка регистрации на сайте без кук")
    void checkLoginWithoutCookies() {
        String cookie = app.getCookies();
        String str = given()
//                .cookie("NOPCOMMERCE.AUTH", cookie)
                .when()
                .get("/info")
                .then()
                .extract().asString();

        assertThat(str.contains(email)).isFalse();
    }

    @Test
    @DisplayName("Проверка подписки на новости")
    @Link(value = "Testing URL", url = "http://demowebshop.tricentis.com")
    void subscribeNewsLetterTest() {
        given()
                .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                .body("email=123%40123.ru")
                .when()
                .post("/subscribenewsletter")
                .then()
                .statusCode(200)
                .body("Success", is(true))
                .body("Result", is("Thank you for signing up! A verification email has been sent." +
                        " We appreciate your interest."));
    }

    /*
      Добавление книг в корзину согласно внутренней спецификации
      Наименование книг  - Computing and Internet, Fiction, Health Book
      В целях добавления товаров в корзину используется детализация кук во внутреннем формате
     */
    @Test
    @DisplayName("Проверка корректного добавления 3-х по наименованию книг в корзину")
    void addBooksToCartTest() {
        Cookies cookies =
                given()
                        .contentType("application/x-www-form-urlencoded; charset=UTF-8")
//                .cookie(String.valueOf(cookies))
                        .body("addtocart_13.EnteredQuantity=1")
                        .when()
                        .post("/addproducttocart/catalog/13/1/1")
                        .then()
                        .statusCode(200)
                        .body("success", is(true))
                        .extract().response().getDetailedCookies();

        given()
                .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                .cookie(String.valueOf(cookies))
                .body("addtocart_45.EnteredQuantity=1")
                .when()
                .post("/addproducttocart/details/45/1")
                .then()
                .statusCode(200)
                .body("success", is(true));

        given()
                .contentType("application/x-www-form-urlencoded; charset=UTF-8")
                .cookie(String.valueOf(cookies))
                .body("addtocart_22.EnteredQuantity=1")
                .when()
                .post("/addproducttocart/details/22/1")
                .then()
                .statusCode(200)
                .body("success", is(true));

        String checkCartHTML =
                given()
                        .cookie(String.valueOf(cookies))
                        .when()
                        .get("/cart")
                        .then()
                        .statusCode(200)
                        .extract().response().asString();
        
        // Распарсим корзину в формат Документа
        Document doc = Jsoup.parse(checkCartHTML);
        int numberBooksByName = doc.select("div.name").size();            // число книг по наименованию в корзине

        String nameBook1 = doc.select("div.name").get(0).text();
        String nameBook2 = doc.select("div.name").get(1).text();
        String nameBook3 = doc.select("div.name").get(2).text();

        assertThat(numberBooksByName).isEqualTo(3);
        assertThat(nameBook3).isEqualTo("Computing and Internet");
        assertThat(nameBook2).isEqualTo("Fiction");
        assertThat(nameBook1).isEqualTo("Health Book");
    }
}