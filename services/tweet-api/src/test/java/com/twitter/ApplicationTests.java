package com.twitter;

import com.twitter.testconfig.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class ApplicationTests extends BaseIntegrationTest {

    @Test
    void contextLoads() {
    }

}
