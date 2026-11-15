package com.work.shortenurl.fake;

import com.work.shortenurl.keygen.KeyGenerator;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class FixedKeyGenerator implements KeyGenerator {

    private final Queue<String> keys;

    public FixedKeyGenerator(String... keys) {
        this.keys = new LinkedList<>(List.of(keys));
    }

    @Override
    public String generate() {
        return keys.poll();
    }
}