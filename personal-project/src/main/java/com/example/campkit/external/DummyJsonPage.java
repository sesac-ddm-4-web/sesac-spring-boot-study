
package com.example.campkit.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DummyJsonPage(
        List<DummyJsonProduct> products,
        Integer total,
        Integer skip,
        Integer limit
) {}
