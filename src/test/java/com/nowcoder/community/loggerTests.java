package com.nowcoder.community;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class loggerTests {

    private static final Logger logger = LoggerFactory.getLogger(loggerTests.class);

    @Test
    public void testLogger() {
        logger.debug("It works!");
    }

}
