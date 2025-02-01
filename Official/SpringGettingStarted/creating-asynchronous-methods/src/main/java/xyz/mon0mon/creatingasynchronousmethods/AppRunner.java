package xyz.mon0mon.creatingasynchronousmethods;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppRunner implements CommandLineRunner {
    private final GitHubLookupService gitHubLookupService;

    @Override
    public void run(String... args) throws Exception {
        //  Start the clock
        long start = System.currentTimeMillis();

        //  Kick of multiple, asynchronous lookups
        CompletableFuture<User> page1 = gitHubLookupService.findUser("PivotalSoftware");
        CompletableFuture<User> page2 = gitHubLookupService.findUser("CloudFoundry");
        CompletableFuture<User> page3 = gitHubLookupService.findUser("Spring-Projects");

//        //  Wait until they are all done
//        CompletableFuture.allOf(page1, page2, page3).join();
//
//        //  Print results, including elapsed time
//        log.info("Elapsed time: {}", (System.currentTimeMillis() - start));
//        log.info("--> {}", page1.get());
//        log.info("--> {}", page2.get());
//        log.info("--> {}", page3.get());

        //  Wait until they are all done
        CompletableFuture.allOf(page1, page2, page3).thenRun(() -> {
            //  Print results, including elapsed time
            log.info("Elapsed time: {}", (System.currentTimeMillis() - start));
        });
        page1.thenApply((res) -> {
            log.info("[page1] --> {}", res);
            return res;
        });
        page2.thenApply((res) -> {
            log.info("[page2] --> {}", res);
            return res;
        });
        page3.thenApply((res) -> {
            log.info("[page3] --> {}", res);
            return res;
        });

        do {
            log.info("[Status] {page1: {}, page2: {}, page3: {}}",
                    page1.isDone() ? "done" : "not yet",
                    page2.isDone() ? "done" : "not yet",
                    page3.isDone() ? "done" : "not yet");
            Thread.sleep(Duration.ofMillis(500));
        } while (!(page1.isDone() && page2.isDone() && page3.isDone()));
    }
}
