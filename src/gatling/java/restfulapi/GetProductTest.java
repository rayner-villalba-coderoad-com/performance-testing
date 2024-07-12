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

public class GetProductTest extends Simulation {
    String baseUrl = System.getProperty("baseUrl", "https://api.restful-api.dev");
    String concurrentUsers = System.getProperty("concurrentUsers", "5");

    ObjectMapper objectMapper = new ObjectMapper();
    //Define test data
    FeederBuilder.FileBased<Object> feeder = jsonFile("data/products.json").circular();


    private HttpProtocolBuilder httpProtocol = http.baseUrl(baseUrl);

    ScenarioBuilder scn = scenario("Create a new product")
            .feed(feeder)
            .exec(http("Get Objects")
                    .get("/objects/#{id}")
                    .check(bodyString().saveAs("BODY"))
                    .check(status().is(200))
                    .check(bodyString().transform(is -> {
                        try {
                            return JsonValidator.builder()
                                            .withJsonNode(is)
                                            .withJsonSchema(objectMapper.readTree(GetProductTest.class.getResourceAsStream("schemas/product-schema.json")))
                                            .build()
                                            .validate().size()<=0;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return false;
                    }).in(true))
            )
            .exec(
                    session -> {
                        System.out.println("Product: " + session.getString("BODY"));
                        return session;
                    }
            );

    {
        setUp(
                scn.injectClosed(
                        constantConcurrentUsers(Integer.parseInt(concurrentUsers)).during(Duration.ofSeconds(10))
                )
        ).protocols(httpProtocol);
    }

}
