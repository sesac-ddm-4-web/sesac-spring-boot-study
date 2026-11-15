package com.example.shorturl.support;

import com.example.shorturl.link.domain.ShortUrlKeyGenerator;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class SequenceKeyGenerator implements ShortUrlKeyGenerator {

    private final Deque<String> keys = new ArrayDeque<>();
    private int generatedCount;

    public SequenceKeyGenerator(String... keys) {
        this.keys.addAll(List.of(keys));
    }

    @Override
    public String generate() {
        generatedCount++;
        return keys.size() > 1 ? keys.poll() : keys.peek();
    }

    public int getGeneratedCount() {
        return generatedCount;
    }
}
