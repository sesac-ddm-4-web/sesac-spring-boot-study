package com.example.wishlist.management.domain.external;

import java.util.List;

public interface ExternalProductClient {
    List<ExternalProduct> list(int limit, int skip);
    List<ExternalProduct> search(String q, int limit, int skip);
    ExternalProduct get(long id);
}
