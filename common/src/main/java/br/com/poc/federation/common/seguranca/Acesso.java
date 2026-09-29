package br.com.poc.federation.common.seguranca;

import br.com.poc.federation.common.erro.AcessoNegadoException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class Acesso {

    public static final String BACKOFFICE = "SCOPE_backoffice";

    public String clienteLogado() {
        return autenticacao().getName();
    }

    public boolean backoffice() {
        return autenticacao().getAuthorities().stream().anyMatch(a -> BACKOFFICE.equals(a.getAuthority()));
    }

    public boolean podeVer(String clienteId) {
        return backoffice() || clienteLogado().equals(clienteId);
    }

    public void exigirAcessoAoCliente(String clienteId) {
        if (!podeVer(clienteId)) {
            throw new AcessoNegadoException("Sem permissão para acessar dados do cliente " + clienteId);
        }
    }

    private Authentication autenticacao() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            throw new AuthenticationCredentialsNotFoundException("Token JWT ausente");
        }
        return auth;
    }
}
