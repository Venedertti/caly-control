package com.calycontrol.otica.shared.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("handleBusinessException deve adicionar flash 'erro' e redirecionar para /")
    void handleBusinessException_deveAdicionarErroERedirectParaRaiz() {
        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();
        BusinessException ex = new BusinessException("Saldo insuficiente");

        String view = handler.handleBusinessException(ex, attrs);

        assertThat(view).isEqualTo("redirect:/");
        assertThat(attrs.getFlashAttributes().get("erro")).isEqualTo("Saldo insuficiente");
    }

    @Test
    @DisplayName("handleNoResource deve redirecionar para / sem lançar exceção")
    void handleNoResource_deveRedirectParaRaiz() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/static/missing.js");
        NoResourceFoundException ex = mock(NoResourceFoundException.class);

        String view = handler.handleNoResource(ex, request);

        assertThat(view).isEqualTo("redirect:/");
    }

    @Test
    @DisplayName("handleGenericException deve adicionar mensagem genérica e redirecionar para /")
    void handleGenericException_deveAdicionarMensagemGenericaERedirect() {
        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();
        Exception ex = new RuntimeException("Erro inesperado interno");

        String view = handler.handleGenericException(ex, attrs);

        assertThat(view).isEqualTo("redirect:/");
        assertThat(attrs.getFlashAttributes().get("erro").toString())
                .contains("erro inesperado");
    }
}
