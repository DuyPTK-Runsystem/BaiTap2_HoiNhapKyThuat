package net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.service.testing;

import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Item;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.User;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.domain.table.Vocab;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.ItemRepository;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.UserRepository;
import net.runsystem.duyptk.BaiTap2_HoiNhapKyThuat_AI.repository.VocabRepository;

class VocabSourceResolverTests {
    private static final Long USER_ID = 1L;
    private static final String EMAIL = "learner@example.com";

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final ItemRepository itemRepository = Mockito.mock(ItemRepository.class);
    private final VocabRepository vocabRepository = Mockito.mock(VocabRepository.class);
    private final User currentUser = User.builder()
            .id(USER_ID)
            .email(EMAIL)
            .password("hashed")
            .build();
    private final VocabSourceResolver resolver = new VocabSourceResolver(
            userRepository,
            itemRepository,
            vocabRepository);

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldResolveRequestedNumberFromOwnedSources() {
        mockCurrentUser();
        Mockito.when(itemRepository.findByIdInAndUserId(List.of(10L), USER_ID))
                .thenReturn(List.of(item(10L)));
        Mockito.when(vocabRepository.findBySourceItemIdsAndUserId(List.of(10L), USER_ID))
                .thenReturn(List.of(vocab(1L), vocab(2L)));

        List<Vocab> result = resolver.resolve(List.of(10L), 2);

        Assertions.assertThat(result).extracting(Vocab::getId).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void shouldUseRandomWholeDatabaseWhenSourcesAreMissing() {
        mockCurrentUser();
        Mockito.when(vocabRepository.findRandom(Mockito.any())).thenReturn(List.of(vocab(1L), vocab(2L)));

        List<Vocab> result = resolver.resolve(null, 2);

        Assertions.assertThat(result).extracting(Vocab::getId).containsExactly(1L, 2L);
    }

    @Test
    void shouldRejectSourceNotOwnedByCurrentUser() {
        mockCurrentUser();
        Mockito.when(itemRepository.findByIdInAndUserId(List.of(10L), USER_ID)).thenReturn(List.of());

        Assertions.assertThatThrownBy(() -> resolver.resolve(List.of(10L), 1))
                .isInstanceOf(java.util.NoSuchElementException.class)
                .hasMessage("Nguồn bài test không tồn tại hoặc không thuộc người dùng hiện tại");
    }

    @Test
    void shouldRejectWhenSourceHasInsufficientVocabs() {
        mockCurrentUser();
        Mockito.when(itemRepository.findByIdInAndUserId(List.of(10L), USER_ID))
                .thenReturn(List.of(item(10L)));
        Mockito.when(vocabRepository.findBySourceItemIdsAndUserId(List.of(10L), USER_ID))
                .thenReturn(List.of(vocab(1L)));

        Assertions.assertThatThrownBy(() -> resolver.resolve(List.of(10L), 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nguồn không có đủ từ vựng để tạo bài test");
    }

    @Test
    void shouldRejectInvalidQuestionCount() {
        Assertions.assertThatThrownBy(() -> resolver.resolve(null, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Số lượng câu hỏi phải lớn hơn hoặc bằng 1");
    }

    @Test
    void shouldDeduplicateSourceIdsBeforeResolving() {
        mockCurrentUser();
        Mockito.when(itemRepository.findByIdInAndUserId(List.of(10L), USER_ID))
                .thenReturn(List.of(item(10L)));
        Mockito.when(vocabRepository.findBySourceItemIdsAndUserId(List.of(10L), USER_ID))
                .thenReturn(List.of(vocab(1L)));

        resolver.resolve(List.of(10L, 10L), 1);

        Mockito.verify(vocabRepository).findBySourceItemIdsAndUserId(List.of(10L), USER_ID);
    }

    private void mockCurrentUser() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(EMAIL, "n/a"));
        Mockito.when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(currentUser));
    }

    private Item item(Long id) {
        return Item.builder()
                .id(id)
                .user(currentUser)
                .build();
    }

    private Vocab vocab(Long id) {
        return Vocab.builder()
                .id(id)
                .word("word-" + id)
                .meaning("meaning-" + id)
                .build();
    }
}
