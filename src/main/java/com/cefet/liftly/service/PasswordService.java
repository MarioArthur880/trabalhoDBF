package com.cefet.liftly.service;

import org.springframework.stereotype.Service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class PasswordService {
    private static final String PREFIXO = "pbkdf2";
    private static final int ITERACOES = 120_000;
    private static final int TAMANHO_CHAVE = 256;
    private final SecureRandom secureRandom = new SecureRandom();

    public String hash(String senha) {
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        byte[] hash = derivar(senha.toCharArray(), salt, ITERACOES);
        return PREFIXO + "$" + ITERACOES + "$" + Base64.getEncoder().encodeToString(salt)
                + "$" + Base64.getEncoder().encodeToString(hash);
    }

    public boolean confere(String senhaInformada, String armazenada) {
        if (senhaInformada == null || armazenada == null) return false;
        if (!armazenada.startsWith(PREFIXO + "$")) {
            // Compatibilidade com contas antigas que ainda estavam em texto puro.
            return armazenada.equals(senhaInformada);
        }
        try {
            String[] partes = armazenada.split("\\$");
            int iteracoes = Integer.parseInt(partes[1]);
            byte[] salt = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            byte[] atual = derivar(senhaInformada.toCharArray(), salt, iteracoes);
            return MessageDigest.isEqual(esperado, atual);
        } catch (RuntimeException e) {
            return false;
        }
    }

    public boolean precisaMigrar(String armazenada) {
        return armazenada != null && !armazenada.startsWith(PREFIXO + "$");
    }

    private byte[] derivar(char[] senha, byte[] salt, int iteracoes) {
        PBEKeySpec spec = new PBEKeySpec(senha, salt, iteracoes, TAMANHO_CHAVE);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível proteger a senha.", e);
        } finally {
            spec.clearPassword();
        }
    }
}
