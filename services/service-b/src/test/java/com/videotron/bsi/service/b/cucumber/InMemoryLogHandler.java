package com.videotron.bsi.service.b.cucumber;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class InMemoryLogHandler extends Handler {
    private final List<LogRecord> records = new ArrayList<>();
    private final Predicate<LogRecord> filter;

    public InMemoryLogHandler(Predicate<LogRecord> filter) {
        this.filter = filter;
    }

    @Override
    public void publish(LogRecord record) {
        if (filter.test(record)) {
            records.add(record);
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
        records.clear();
    }

    public List<LogRecord> getRecords() {
        return new ArrayList<>(records);
    }

    public boolean hasLogContaining(String text) {
        return getRecords().stream()
            .map(LogRecord::getMessage)
            .anyMatch(message -> message != null && message.contains(text));
    }
}
