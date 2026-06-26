package io.nickreuter.retroapi.retro;

import io.nickreuter.retroapi.retro.thought.ThoughtEntity;
import io.nickreuter.retroapi.retro.thought.ThoughtService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class RequireActiveRetroAspectTest {
    private final RetroActiveGuard mockRetroActiveGuard = mock(RetroActiveGuard.class);
    private final ThoughtService mockThoughtService = mock(ThoughtService.class);
    private final RequireActiveRetroAspect subject = new RequireActiveRetroAspect(mockRetroActiveGuard, mockThoughtService);

    @Test
    void checkRetroIsActive_WithRetroIdParam_WhenRetroIsActive_DoesNotThrow() throws Exception {
        var retroId = UUID.randomUUID();
        var joinPoint = createJoinPoint(new String[]{"retroId"}, new Object[]{retroId});
        var annotation = createAnnotation("retroId", "");
        when(mockRetroActiveGuard.isValid(retroId)).thenReturn(true);

        subject.checkRetroIsActive(joinPoint, annotation);

        verify(mockRetroActiveGuard).isValid(retroId);
    }

    @Test
    void checkRetroIsActive_WithRetroIdParam_WhenRetroIsNotActive_ThrowsRetroNotActiveException() throws Exception {
        var retroId = UUID.randomUUID();
        var joinPoint = createJoinPoint(new String[]{"retroId"}, new Object[]{retroId});
        var annotation = createAnnotation("retroId", "");
        when(mockRetroActiveGuard.isValid(retroId)).thenReturn(false);

        assertThatThrownBy(() -> subject.checkRetroIsActive(joinPoint, annotation))
                .isInstanceOf(RetroNotActiveException.class);
    }

    @Test
    void checkRetroIsActive_WithThoughtIdParam_WhenRetroIsActive_DoesNotThrow() throws Exception {
        var thoughtId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        var thought = new ThoughtEntity(thoughtId, "message", 0, false, "category", retroId, null);
        var joinPoint = createJoinPoint(new String[]{"thoughtId"}, new Object[]{thoughtId});
        var annotation = createAnnotation("", "thoughtId");
        when(mockThoughtService.getThought(thoughtId)).thenReturn(Optional.of(thought));
        when(mockRetroActiveGuard.isValid(retroId)).thenReturn(true);

        subject.checkRetroIsActive(joinPoint, annotation);

        verify(mockRetroActiveGuard).isValid(retroId);
    }

    @Test
    void checkRetroIsActive_WithThoughtIdParam_WhenRetroIsNotActive_ThrowsRetroNotActiveException() throws Exception {
        var thoughtId = UUID.randomUUID();
        var retroId = UUID.randomUUID();
        var thought = new ThoughtEntity(thoughtId, "message", 0, false, "category", retroId, null);
        var joinPoint = createJoinPoint(new String[]{"thoughtId"}, new Object[]{thoughtId});
        var annotation = createAnnotation("", "thoughtId");
        when(mockThoughtService.getThought(thoughtId)).thenReturn(Optional.of(thought));
        when(mockRetroActiveGuard.isValid(retroId)).thenReturn(false);

        assertThatThrownBy(() -> subject.checkRetroIsActive(joinPoint, annotation))
                .isInstanceOf(RetroNotActiveException.class);
    }

    @Test
    void checkRetroIsActive_WhenRetroNotFound_ThrowsRetroNotFoundException() throws Exception {
        var retroId = UUID.randomUUID();
        var joinPoint = createJoinPoint(new String[]{"retroId"}, new Object[]{retroId});
        var annotation = createAnnotation("retroId", "");
        when(mockRetroActiveGuard.isValid(retroId)).thenThrow(new RetroNotFoundException());

        assertThatThrownBy(() -> subject.checkRetroIsActive(joinPoint, annotation))
                .isInstanceOf(RetroNotFoundException.class);
    }

    private JoinPoint createJoinPoint(String[] paramNames, Object[] args) {
        var joinPoint = mock(JoinPoint.class);
        var signature = mock(MethodSignature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getParameterNames()).thenReturn(paramNames);
        when(joinPoint.getArgs()).thenReturn(args);
        when(signature.getName()).thenReturn("testMethod");
        return joinPoint;
    }

    private RequireActiveRetro createAnnotation(String retroIdParam, String thoughtIdParam) {
        var annotation = mock(RequireActiveRetro.class);
        when(annotation.retroIdParam()).thenReturn(retroIdParam);
        when(annotation.thoughtIdParam()).thenReturn(thoughtIdParam);
        return annotation;
    }
}
