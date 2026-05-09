package io.nickreuter.retroapi.retro;

import io.nickreuter.retroapi.retro.thought.ThoughtService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
public class RequireActiveRetroAspect {
    private final RetroActiveGuard retroActiveGuard;
    private final ThoughtService thoughtService;

    public RequireActiveRetroAspect(RetroActiveGuard retroActiveGuard, ThoughtService thoughtService) {
        this.retroActiveGuard = retroActiveGuard;
        this.thoughtService = thoughtService;
    }

    @Before("@annotation(requireActiveRetro)")
    public void checkRetroIsActive(JoinPoint joinPoint, RequireActiveRetro requireActiveRetro) throws RetroNotActiveException, RetroNotFoundException {
        UUID retroId = resolveRetroId(joinPoint, requireActiveRetro);
        if (!retroActiveGuard.isValid(retroId)) {
            throw new RetroNotActiveException();
        }
    }

    private UUID resolveRetroId(JoinPoint joinPoint, RequireActiveRetro annotation) {
        if (!annotation.thoughtIdParam().isEmpty()) {
            UUID thoughtId = extractParam(joinPoint, annotation.thoughtIdParam());
            return thoughtService.getThought(thoughtId).orElseThrow().getRetroId();
        }
        return extractParam(joinPoint, annotation.retroIdParam());
    }

    private UUID extractParam(JoinPoint joinPoint, String paramName) {
        var signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < paramNames.length; i++) {
            if (paramNames[i].equals(paramName)) {
                return (UUID) args[i];
            }
        }
        throw new IllegalArgumentException("Parameter '%s' not found on method '%s'".formatted(paramName, signature.getName()));
    }
}
