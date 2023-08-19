package jaeseok.study.reactorexample.example;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.BufferOverflowStrategy;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@Slf4j
@RestController
public class ExampleController {

    @GetMapping("/example/backpressure/1")
    public void example_backpressure_1() {
        Flux.range(1, 5)
                .doOnRequest(data -> log.info("# doOnRequest: {}", data))
                .subscribe(new BaseSubscriber<Integer>() {
                    @Override
                    protected void hookOnSubscribe(Subscription subscription) {
                        request(1);
                    }

                    @SneakyThrows
                    @Override
                    protected void hookOnNext(Integer value) {
                        Thread.sleep(2000L);
                        log.info("# hookOnNext: {}", value);
                        request(1);
                    }
                });
    }

    @GetMapping("/example/backpressure/2")
    public void example_backpressure_2() throws InterruptedException {
        Flux
                .interval(Duration.ofMillis(1L))                    // publisher는 1개의 데이터를 1ms마다 emit
                .onBackpressureError()                              // error 전략 사용
                .doOnNext(data -> log.info("# doOnNext: {}", data)) // 데이터 emit 시 처리동작
                .publishOn(Schedulers.parallel())                   // 별도의 스레드 사용
                .subscribe(data -> {
                    try {
                        Thread.sleep(5L);                     // subscriber는 1개의 데이터를 처리하는데에 5ms 소요
                        log.info("# onNext: {}", data);             // 데이터 subsribe 시 처리동작
                    } catch (InterruptedException e) {

                    }
                }, error -> {
                    log.info("# onError");
                });

        Thread.sleep(2000L);
    }

    @GetMapping("/example/backpressure/3")
    public void example_backpressure_3() throws InterruptedException {
        Flux
                .interval(Duration.ofMillis(1L))                    // publisher는 1개의 데이터를 1ms마다 emit
                .onBackpressureDrop(dropped -> {                    // drop 전략 사용
                    log.info("# dropped: {}", dropped);
                })
                .publishOn(Schedulers.parallel())                   // 별도의 스레드 사용
                .subscribe(data -> {
                    try {
                        Thread.sleep(5L);                     // subscriber는 1개의 데이터를 처리하는데에 5ms 소요
                        log.info("# onNext: {}", data);             // 데이터 subsribe 시 처리동작
                    } catch (InterruptedException e) {

                    }
                }, error -> {
                    log.info("# onError");
                });

        Thread.sleep(2000L);
    }

    @GetMapping("/example/backpressure/4")
    public void example_backpressure_4() throws InterruptedException {
        Flux
                .interval(Duration.ofMillis(1L))                    // publisher는 1개의 데이터를 1ms마다 emit
                .onBackpressureLatest()                             // LATEST 전략 사용
                .publishOn(Schedulers.parallel())                   // 별도의 스레드 사용
                .subscribe(data -> {
                    try {
                        Thread.sleep(5L);                     // subscriber는 1개의 데이터를 처리하는데에 5ms 소요
                        log.info("# onNext: {}", data);             // 데이터 subsribe 시 처리동작
                    } catch (InterruptedException e) {

                    }
                }, error -> {
                    log.info("# onError");
                });

        Thread.sleep(2000L);
    }

    @GetMapping("/example/backpressure/buffer/1")
    public void example_backpressure_buffer_1() throws InterruptedException {
        Flux
                .interval(Duration.ofMillis(300L))
                .doOnNext(data -> log.info("# emitted by original Flux: {}", data))     // 원본 데이터 -> Buffer
                .onBackpressureBuffer(2,                                        // BufferSize = 2
                        dropped -> log.info("** Overflow & Dropped: {} **", dropped),   // drop된 데이터 처리
                        BufferOverflowStrategy.DROP_LATEST)                             // DROP_LATEST 전략 사용
                .doOnNext(data -> log.info("[ # emitted by Buffer: {} ]", data))        // Buffer -> Downstream
                .publishOn(Schedulers.parallel(), false, 1)
                .subscribe(
                        data -> {
                            try {
                                Thread.sleep(1000L);
                                log.info("# onNext: {}", data);
                            } catch (InterruptedException e) {}
                        },
                        error -> log.error("# onError", error)
                );

        Thread.sleep(3000L);
    }

    @GetMapping("/example/backpressure/buffer/2")
    public void example_backpressure_buffer_2() throws InterruptedException {
        Flux
                .interval(Duration.ofMillis(300L))
                .doOnNext(data -> log.info("# emitted by original Flux: {}", data))     // 원본 데이터 -> Buffer
                .onBackpressureBuffer(2,                                        // BufferSize = 2
                        dropped -> log.info("** Overflow & Dropped: {} **", dropped),   // drop된 데이터 처리
                        BufferOverflowStrategy.DROP_OLDEST)                             // DROP_OLDEST 전략 사용
                .doOnNext(data -> log.info("[ # emitted by Buffer: {} ]", data))        // Buffer -> Downstream
                .publishOn(Schedulers.parallel(), false, 1)
                .subscribe(
                        data -> {
                            try {
                                Thread.sleep(1000L);
                                log.info("# onNext: {}", data);
                            } catch (InterruptedException e) {}
                        },
                        error -> log.error("# onError", error)
                );

        Thread.sleep(3000L);
    }
}
