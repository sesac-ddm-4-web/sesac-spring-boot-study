package com.example.personal_project.application;

import com.example.personal_project.domain.Item;
import com.example.personal_project.domain.ItemRepository;
import com.example.personal_project.domain.SourceType;
import com.example.personal_project.infrastructure.DummyJsonClient;
import com.example.personal_project.infrastructure.DummyJsonProduct;
import com.example.personal_project.presentation.ItemDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Service
public class ItemService {
    private final ItemRepository itemRepository;
    private final ValidationService validationService;
    private final DummyJsonClient client;

    @Autowired
    public ItemService(ItemRepository itemRepository,
                       ValidationService validationService,
                       DummyJsonClient client) {
        this.itemRepository = itemRepository;
        this.validationService = validationService;
        this.client = client;
    }

    public ItemDto add(ItemDto itemDto) {
        String name = validationService.validateName(itemDto.name());
        int price = validationService.validatePrice(itemDto.price());

        Item item = new Item(null, name, price);
        Item savedItem = itemRepository.add(item);
        return ItemDto.from(savedItem);
    }

    public ItemDto findById(Long id) {
        Item item = itemRepository.findById(id);
        return ItemDto.from(item);
    }

    public List<ItemDto> findAll() {
        List<Item> items = itemRepository.findAll();
        List<ItemDto> itemDtos = items.stream()
                                .map(item -> ItemDto.from(item))
                                .toList();
        return itemDtos;
    }

    public List<ItemDto> recommend(int budget) {
        int validBudget = validationService.validateBudget(budget);
        List<Item> items = itemRepository.findAll();
        return items.stream()
                .filter(item -> item.getPrice() <= validBudget)
                .sorted(Comparator.comparing(item -> item.getPrice()))
                .limit(3)
                .map(item -> ItemDto.from(item))
                .toList();
    }

    public List<ItemDto> findByNameContaining(String name) {
        String validName = validationService.validateName(name);
        List<Item> items = itemRepository.findByNameContaining(validName);
        List<ItemDto> itemDtos = items.stream()
                                .map(item -> ItemDto.from(item))
                                .toList();
        return itemDtos;
    }

    public ItemDto importExternal(long externalId) {
        // 중복 확인
        if (itemRepository.existsExternal(SourceType.DUMMY_JSON, externalId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, // 409
                    "이미 가져온 상품입니다."
            );
        }

        DummyJsonProduct product = client.findById(externalId);
        // 가격 환산
        BigDecimal convertedPrice = product.price().multiply(BigDecimal.valueOf(1000));
        // 반올림
        convertedPrice = convertedPrice.setScale(0, RoundingMode.HALF_UP);

        int price = convertedPrice.intValueExact();

        Item item = new Item(
                null,
                product.title(),
                price,
                SourceType.DUMMY_JSON,
                product.id(),
                product.stock()
        );
        Item savedItem = itemRepository.add(item);

        return ItemDto.from(savedItem);
    }

    public ItemDto update(Long id, ItemDto itemDto) {
        Item existingItem = itemRepository.findById(id);

        if (existingItem.getSourceType() != SourceType.MANUAL) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, // 409
                    ("외부 상품의 이름과 가격은 수정할 수 없습니다.")
            );
        }
        String name = validationService.validateName(itemDto.name());
        int price = validationService.validatePrice(itemDto.price());

        Item updatedItem = new Item(existingItem.getId(), name, price);
        Item savedItem = itemRepository.update(updatedItem);

        return ItemDto.from(savedItem);
    }
}
