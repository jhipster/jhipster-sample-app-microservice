package gatling.simulations;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.header;
import static io.gatling.javaapi.http.HttpDsl.headerRegex;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import ch.qos.logback.classic.LoggerContext;
import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import org.slf4j.LoggerFactory;

/**
 * Performance test for the BankAccount entity.
 */
public class BankAccountGatlingTest extends Simulation {

    LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

    {
        // Log all HTTP requests
        //context.getLogger("io.gatling.http").setLevel(Level.valueOf("TRACE"));
        // Log failed HTTP requests
        //context.getLogger("io.gatling.http").setLevel(Level.valueOf("DEBUG"));
    }

    String baseURL = Optional.ofNullable(System.getProperty("baseURL")).orElse("http://localhost:8081");

    HttpProtocolBuilder httpConf = http
        .baseUrl(baseURL)
        .inferHtmlResources()
        .acceptHeader("*/*")
        .acceptEncodingHeader("gzip, deflate")
        .acceptLanguageHeader("fr,fr-fr;q=0.8,en-us;q=0.5,en;q=0.3")
        .connectionHeader("keep-alive")
        .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:33.0) Gecko/20100101 Firefox/33.0")
        .silentResources(); // Silence all resources like css or css so they don't clutter the results

    Map<String, String> headers_http = Map.of("Accept", "application/json");

    Map<String, String> headers_http_authentication = Map.of("Content-Type", "application/json", "Accept", "application/json");

    Map<String, String> headers_http_authenticated = Map.of("Accept", "application/json", "Authorization", "${access_token}");

    ChainBuilder scn = exec(http("First unauthenticated request").get("/api/account").headers(headers_http).check(status().is(401)))
        .exitHereIfFailed()
        .pause(10)
        .exec(
            http("Authentication")
                .post("/api/authenticate")
                .headers(headers_http_authentication)
                .body(StringBody("{\"username\":\"admin\", \"password\":\"admin\"}"))
                .asJson()
                .check(header("Authorization").saveAs("access_token"))
        )
        .exitHereIfFailed()
        .pause(2)
        .exec(http("Authenticated request").get("/api/account").headers(headers_http_authenticated).check(status().is(200)))
        .pause(10)
        .repeat(2)
        .on(
            exec(
                http("Get all bankAccounts")
                    .get("/services/jhipstersamplemicroservice/api/bank-accounts")
                    .headers(headers_http_authenticated)
                    .check(status().is(200))
            )
                .pause(Duration.ofSeconds(10), Duration.ofSeconds(20))
                .exec(
                    http("Create new bankAccount")
                        .post("/services/jhipstersamplemicroservice/api/bank-accounts")
                        .headers(headers_http_authenticated)
                        .body(StringBody("{" + "\"name\": \"SAMPLE_TEXT\"" + ", \"balance\": 0" + "}"))
                        .asJson()
                        .check(status().is(201))
                        .check(headerRegex("Location", "(.*)").saveAs("new_bankAccount_url"))
                )
                .exitHereIfFailed()
                .pause(10)
                .repeat(5)
                .on(
                    exec(
                        http("Get created bankAccount")
                            .get("/services/jhipstersamplemicroservice${new_bankAccount_url}")
                            .headers(headers_http_authenticated)
                    ).pause(10)
                )
                .exec(
                    http("Delete created bankAccount")
                        .delete("/services/jhipstersamplemicroservice${new_bankAccount_url}")
                        .headers(headers_http_authenticated)
                )
                .pause(10)
        );

    ScenarioBuilder users = scenario("Test the BankAccount entity").exec(scn);

    {
        setUp(
            users.injectOpen(rampUsers(Integer.getInteger("users", 100)).during(Duration.ofMinutes(Integer.getInteger("ramp", 1))))
        ).protocols(httpConf);
    }
}
