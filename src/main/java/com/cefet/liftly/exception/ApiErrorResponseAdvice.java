package com.cefet.liftly.exception;

import java.util.Map;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/** Gives existing controller error responses the same safe JSON contract. */
@RestControllerAdvice(basePackages = "com.cefet.liftly.controller")
public class ApiErrorResponseAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter parameter, Class<? extends HttpMessageConverter<?>> converter) { return true; }
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter parameter, MediaType type,
            Class<? extends HttpMessageConverter<?>> converter, ServerHttpRequest request, ServerHttpResponse response) {
        if (body != null || !(response instanceof ServletServerHttpResponse servlet)) return body;
        int status = servlet.getServletResponse().getStatus();
        if (status < 400) return body;
        String path = request.getURI().getPath();
        String code = "REQUEST_ERROR";
        String message = switch (status) {
            case 400 -> "Confira os dados informados e tente novamente.";
            case 401 -> "Sua sessão expirou. Entre novamente para continuar.";
            case 403 -> "Você não tem permissão para realizar esta ação.";
            case 404 -> "O registro solicitado não foi encontrado. Atualize a página.";
            case 409 -> "Esta operação entra em conflito com os dados atuais. Atualize a página.";
            default -> "Não foi possível concluir a operação. Tente novamente mais tarde.";
        };
        if (status == 401 && path.endsWith("/login")) message = "E-mail ou senha incorretos.";
        if (status == 401 && path.endsWith("/senha")) message = "A senha atual está incorreta.";
        if (status == 409 && path.endsWith("/cadastro")) { code = "EMAIL_ALREADY_EXISTS"; message = "Este e-mail já está cadastrado."; }
        if (status == 409 && path.endsWith("/convidar")) message = "Este usuário já faz parte do grupo ou possui um convite pendente.";
        if (status == 404 && path.contains("/entrar")) message = "Código de grupo inválido.";
        if (status == 404 && path.endsWith("/convidar")) message = "Não encontramos o grupo ou um usuário Liftly com esse e-mail.";
        return new ApiExceptionHandler.ApiError(status, code, message, Map.of());
    }
}
