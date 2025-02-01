package xyz.mon0mon.schedulingtasks;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SchedulingTasksApplicationTests {

    @Autowired
    ScheduledTasks tasks;

    @Test
    void contextLoads() {
    }

}
