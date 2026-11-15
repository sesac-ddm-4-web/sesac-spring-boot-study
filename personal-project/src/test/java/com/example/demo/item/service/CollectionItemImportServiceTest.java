package com.example.demo.item.service;

import com.example.demo.global.exception.ConflictException;
import com.example.demo.global.exception.EntityNotFoundException;
import com.example.demo.global.exception.ExternalApiException;
import com.example.demo.item.domain.SourceType;
import com.example.demo.item.dto.req.CollectionItemReq;
import com.example.demo.item.dto.req.ExternalImportReq;
import com.example.demo.item.dto.res.CollectionItemRes;
import com.example.demo.item.repository.ListCollectionItemRepository;
import com.example.demo.support.FakeExternalProductClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.example.demo.support.FakeExternalProductClient.product;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CollectionItemImportServiceTest {

    private ListCollectionItemRepository repository;
    private FakeExternalProductClient client;
    private CollectionItemImportService importService;
    private SimpleCollectionItemService itemService;

    @BeforeEach
    void setUp() {
        repository = new ListCollectionItemRepository();
        client = new FakeExternalProductClient();
        client.add(product(1L, "Essence Mascara", "9.99", 99));
        client.add(product(117L, "Sportbike Motorcycle", "7499.99", 0));
        ValidationService validationService = new ValidationService();
        importService = new CollectionItemImportService(repository, client, validationService);
        itemService = new SimpleCollectionItemService(repository, validationService);
    }

    @Test
    void 외부_상품을_가져오면_서버가_조회한_이름과_환산한_가격으로_저장된다() {
        CollectionItemRes saved = importService.importExternal(new ExternalImportReq(1L));

        assertEquals("Essence Mascara", saved.name());
        assertEquals(9990, saved.price());
        assertEquals(SourceType.EXTERNAL, saved.sourceType());
        assertEquals(1L, saved.externalId());
        assertEquals(99, saved.stock());
        assertEquals(1, itemService.findAll().size());
    }

    @Test
    void 내_위시리스트_id와_외부_id는_따로_관리된다() {
        itemService.add(new CollectionItemReq("코트", 120000));
        itemService.add(new CollectionItemReq("청바지", 50000));

        CollectionItemRes saved = importService.importExternal(new ExternalImportReq(1L));

        assertEquals(3L, saved.id());
        assertEquals(1L, saved.externalId());
        assertNotEquals(saved.id(), saved.externalId());
    }

    @Test
    void 품절된_외부_상품도_위시리스트에는_저장할_수_있다() {
        CollectionItemRes saved = importService.importExternal(new ExternalImportReq(117L));

        assertEquals(0, saved.stock());
        assertEquals(7499990, saved.price());
    }

    @Test
    void 같은_외부_상품을_다시_가져오면_409_예외가_발생하고_외부_API를_다시_부르지_않는다() {
        importService.importExternal(new ExternalImportReq(1L));

        assertThrows(ConflictException.class, () -> importService.importExternal(new ExternalImportReq(1L)));
        assertEquals(1, itemService.findAll().size());
        assertEquals(1, client.findByIdCount());
    }

    @Test
    void 외부에_없는_상품을_가져오면_404_예외가_발생하고_아무것도_저장되지_않는다() {
        assertThrows(EntityNotFoundException.class, () -> importService.importExternal(new ExternalImportReq(99999L)));
        assertTrue(itemService.findAll().isEmpty());
    }

    @Test
    void 외부_API가_실패하면_502_예외가_발생하고_기존_목록은_그대로다() {
        itemService.add(new CollectionItemReq("코트", 120000));
        client.fail();

        assertThrows(ExternalApiException.class, () -> importService.importExternal(new ExternalImportReq(1L)));
        assertEquals(1, itemService.findAll().size());
    }

    @Test
    void 가져온_외부_상품은_이름과_가격을_수정할_수_없고_값이_유지된다() {
        CollectionItemRes saved = importService.importExternal(new ExternalImportReq(1L));

        assertThrows(ConflictException.class,
                () -> itemService.update(saved.id(), new CollectionItemReq("변경", 1)));

        CollectionItemRes found = itemService.findById(saved.id());
        assertEquals("Essence Mascara", found.name());
        assertEquals(9990, found.price());
        assertEquals(SourceType.EXTERNAL, found.sourceType());
    }

    @Test
    void 수동_상품은_가져오기_기능이_추가된_뒤에도_수정할_수_있다() {
        CollectionItemRes saved = itemService.add(new CollectionItemReq("코트", 120000));

        itemService.update(saved.id(), new CollectionItemReq("롱코트", 150000));

        CollectionItemRes found = itemService.findById(saved.id());
        assertEquals("롱코트", found.name());
        assertEquals(SourceType.MANUAL, found.sourceType());
    }
}
