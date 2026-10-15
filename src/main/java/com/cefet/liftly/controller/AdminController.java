package com.cefet.liftly.controller;
import com.cefet.liftly.repository.UsuarioRepository;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController @RequestMapping("/api/admin")
public class AdminController {
 private final UsuarioRepository usuarios;
 public AdminController(UsuarioRepository usuarios){this.usuarios=usuarios;}
 @GetMapping("/resumo") public Map<String,Object> resumo(){return Map.of("usuarios",usuarios.count(),"acesso","ADMIN");}
}
