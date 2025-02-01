package xyz.mon0mon.schedulingtasks;

import org.awaitility.Durations;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

@SpringBootTest
class ScheduledTasksTest {
    @MockitoSpyBean
    ScheduledTasks tasks;

    @Test
    void reportCurrentTime() {
        await().atMost(Durations.TEN_SECONDS).untilAsserted(() -> {
            verify(tasks, atLeast(2)).reportCurrentTime();
        });
    }
}
