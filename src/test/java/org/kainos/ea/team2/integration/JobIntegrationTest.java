package org.kainos.ea.team2.integration;

import org.kainos.ea.team2.MovesLikeSwaggerApplication;
import org.kainos.ea.team2.MovesLikeSwaggerConfiguration;
import org.kainos.ea.team2.cli.BasicCredentials;
import org.kainos.ea.team2.cli.Job;

import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * integration testing, verifies that the job controller, job service and job dao communicate properly.
 */
@ExtendWith(DropwizardExtensionsSupport.class)
public class JobIntegrationTest {

    static final DropwizardAppExtension<MovesLikeSwaggerConfiguration> APP = new DropwizardAppExtension<>(
            MovesLikeSwaggerApplication.class, null,
            new ResourceConfigurationSourceProvider()
    );

    private static final String VALID_USER_NAME = System.getenv("TEST_VALID_USERNAME");
    private static final String VALID_USER_PASSWORD = System.getenv("TEST_VALID_PASSWORD");

    private String getJWT() {
        if(VALID_USER_NAME == null || VALID_USER_PASSWORD == null){
            throw new IllegalArgumentException("Test credential environment variables not set!");
        }
        BasicCredentials credentials = new BasicCredentials(VALID_USER_NAME,VALID_USER_PASSWORD);
        Response response = APP.client().target("http://localhost:8080/api/login").request().post(Entity.json(credentials));

        return response.readEntity(String.class);
    }


    /**
     * Verify that the getJobs method returns a list of jobs from the database.
     */
    @Test
    void getJobs_shouldReturnListOfJobs() {
        String jwt = getJWT();

        // list of employees, add each employee returned from the db
        List<Job> response = APP.client().target("http://localhost:8080/api/job-roles")
                .request().header("Authorization", "Bearer " + jwt).get(List.class);

        // check that the list of jobs is non-empty
        Assertions.assertTrue(response.size() > 0);
     }

    /**
     * Checks that status 400 bad request returned when user visits url when not logged in.
     */
    @Test
     void getJobs_shouldReturn400BadRequestWhenNotLoggedIn(){
         // call url to get jobs, no passed in jwt
         Response response = APP.client().target("http://localhost:8080/api/job-roles")
                 .request()
                 .get();

         // check status code 400 bad request returned
         Assertions.assertEquals(400,response.getStatus());
     }

    /**
     * Verify that the getJobSpec method returns a JobSpecificationResponse.
     */
    @Test
    void getJobSpec_shouldReturnJobSpec() {

        // call url to get job spec where job id = 1
        Response response = APP.client().target("http://localhost:8080/api/job-specification/1")
                .request()
                .header("Authorization", "Bearer " + getJWT())
                .get();

        // check status code 200 returned
        Assertions.assertEquals(200,response.getStatus());

        // check response entity is not null
        Assertions.assertNotNull(response.getEntity());

    }

    /**
     * Verify that the getJobSpec method returns status code
     * 404 not found and correct error message in entity
     * when dao returns null.
     */
    @Test
    void getJobSpec_shouldReturnStatusCode404WhenDaoReturnsNull() {

        // call url to get job spec with job id that doesn't exist
        Response response = APP.client().target("http://localhost:8080/api/job-specification/-1")
                .request()
                .header("Authorization", "Bearer " + getJWT())
                .get();

        // check status code 404 returned
        Assertions.assertEquals(404,response.getStatus());

        // check response entity is not null
        Assertions.assertEquals("Job does not exist.", response.readEntity(String.class));

    }

    /**
     * Checks that status 400 bad request returned when user visits url when not logged in.
     */
    @Test
    void getJobSpec_shouldReturn400BadRequestWhenNotLoggedIn(){
        // call url to get jobs, no passed in jwt
        Response response = APP.client().target("http://localhost:8080/api/job-specification/1")
                .request()
                .get();

        // check status code 400 bad request returned
        Assertions.assertEquals(400,response.getStatus());
    }
}
