
package com.example.campkit.external;

public interface ExternalProductClient {

    DummyJsonPage list(int limit, int skip);

    DummyJsonPage search(String query, int limit, int skip);

    DummyJsonProduct findById(long id);
}
