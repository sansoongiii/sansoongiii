package server.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class GameTimerService implements AutoCloseable {

    private final ScheduledExecutorService scheduler =Executors.newSingleThreadScheduledExecutor();
    private final ConcurrentHashMap<String, ScheduledFuture<?>> timers =new ConcurrentHashMap<>();

    // 지정한 시간 후 작업 실행
    public void startTimer(String timerId,int seconds,Runnable onTimeout) {
        if (timerId == null || onTimeout == null) {
            throw new IllegalArgumentException("타이머 ID와 실행 작업은 필수입니다.");
        }
        if (seconds <= 0) {
            throw new IllegalArgumentException("시간은 1초 이상이어야 합니다.");
        }

        // 같은 ID의 기존 타이머 취소
        cancelTimer(timerId);

        ScheduledFuture<?> future = scheduler.schedule(() -> {
                try {
                    onTimeout.run();
                } finally {
                    timers.remove(timerId);
                }},seconds,TimeUnit.SECONDS);
        timers.put(timerId, future);
    }

    // 타이머 취소
    public void cancelTimer(String timerId) {
        ScheduledFuture<?> future = timers.remove(timerId);
        if (future != null) {
            future.cancel(false);
        }
    }

    // 모든 타이머 종료
    @Override
    public void close() {
        for (ScheduledFuture<?> future : timers.values()) {
            future.cancel(false);
        }

        timers.clear();
        scheduler.shutdownNow();
    }
}