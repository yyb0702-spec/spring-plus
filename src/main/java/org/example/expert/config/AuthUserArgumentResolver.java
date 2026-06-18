package org.example.expert.config;

import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.annotation.Auth;
import org.example.expert.domain.common.dto.AuthUser;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

//@AuthenticationPrincipal 역활을 직접구현
public class AuthUserArgumentResolver implements HandlerMethodArgumentResolver {

    // @Auth가 붙은 파라미터를 처리
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasAuthAnnotation = parameter.getParameterAnnotation(Auth.class) != null;
        boolean isAuthUserType = parameter.getParameterType().equals(AuthUser.class);

        if (hasAuthAnnotation != isAuthUserType) {
            throw new AuthException("@Auth와 AuthUser 타입은 함께 사용되어야 합니다.");
        }

        return hasAuthAnnotation;
    }

    //SecurityContext에서 꺼내서 넣음 @AuthenticationPrincipal 역활을 대체
    @Override
    public Object resolveArgument(
            @Nullable MethodParameter parameter,//현재 처리 중인 컨트롤러 파라미터 정보
            @Nullable ModelAndViewContainer mavContainer,//MVC의 Model 객체에 접근할 때 사용
            NativeWebRequest webRequest,//HTTP 요청/응답 래퍼
            @Nullable WebDataBinderFactory binderFactory //요청 파라미터를 Java 객체로 바인딩할 때 사용
            /*
            인터페이스가 이러한 시그니처로 구성되어있어서 구현시 선언을 해야하지만
            securityContext는 어디서든 접근가능한 전역저장소라 파라미터가 필요하지않음
             */

    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUser)) {
            throw new AuthException("인증 정보를 찾을 수 없습니다.");
        }

        return (AuthUser) authentication.getPrincipal();
    }
}
