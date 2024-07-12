package restfulapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import utils.JsonValidator;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class ProductTest extends Simulation {
    String baseUrl = System.getProperty("baseUrl", "https://api.restful-api.dev");
    String concurrentUsers = System.getProperty("concurrentUsers", "10");

    //Preconditions
    //Test insertion of a new product where after inserting the endpoint should return the id
    //Then use the id to get the new inserted product
    //Verify that the product name should match the object product name after request the product by id

    ObjectMapper objectMapper = new ObjectMapper();
    FeederBuilder.FileBased<Object> feeder = jsonFile("data/newProducts.json").circular();

    private HttpProtocolBuilder httpProtocol = http.baseUrl(baseUrl)
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    //Insert a new Product Test
    ScenarioBuilder scn = scenario("Post Product Test")
            .feed(feeder)
            .exec(http("Post a new Product")
                    .post("/objects")
                    .header("Content-type", "application/json")
                    .body(StringBody("{ \"name\": \"#{name}\", \"data\": {\"year\": \"#{data.year}\", \"price\": \"#{data.price}\" }}"))
                    .check(jsonPath("$.id").saveAs("productId"))
                    .check(bodyString().transform(is -> {
                        try {
                            return JsonValidator.builder()
                                    .withJsonNode(is)
                                    .withJsonSchema(objectMapper.readTree(ProductTest.class.getResourceAsStream("/schemas/product-schema.json")))
                                    .build()
                                    .validate().size()<=0;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return false;
                    }).in(true))
                    .check(bodyString().saveAs("BODY"))
                    .check(status().is(200))
            )
            .exec(
                    session -> {
                        System.out.println("New Product: " + session.getString("BODY"));
                        return session;
                    }
            )
            .pause(1) // Pause for 1 second between requests
            .exec(http("Update a new Product")
                    .put("/objects/#{productId}")
                    .header("Content-type", "application/json")
                    .body(RawFileBody("data/updatedProduct.json"))
                    .check(jsonPath("$.name").saveAs("newName"))
                    .check(jsonPath("$.data.price").saveAs("newPrice"))
                    .check(bodyString().saveAs("UpdatedProduct"))
                    .check(status().is(200))
            )
            .pause(1) // Pause for 1 second between requests
            .exec(http("Get Request by ID")
                    .get("/objects/#{productId}")
                    .check(jmesPath("name").isEL("#{newName}"))
                    .check(jmesPath("data.price").isEL("#{newPrice}"))
                    .check(bodyString().saveAs("UpdatedProduct"))
                    .check(status().is(200))
            ).exec(
                    session -> {
                        System.out.println("Updated Product: " + session.getString("UpdatedProduct"));
                        return session;
                    }
            );
    {
        setUp(
                scn.injectClosed(
                        constantConcurrentUsers(Integer.parseInt(concurrentUsers)).during(Duration.ofSeconds(1))
                )
        ).protocols(httpProtocol);
    }
}
